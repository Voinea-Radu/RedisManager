package dev.lightdream.redismanager.manager;

import dev.lightdream.filemanager.GsonSettings;
import dev.lightdream.lambda.ScheduleManager;
import dev.lightdream.logger.Debugger;
import dev.lightdream.logger.Logger;
import dev.lightdream.redismanager.dto.RedisConfig;
import dev.lightdream.redismanager.dto.RedisResponse;
import dev.lightdream.redismanager.event.RedisEvent;
import dev.lightdream.redismanager.event.impl.ResponseEvent;
import dev.lightdream.redismanager.type_adapter.RedisEventTypeAdapter;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Getter
@Accessors(chain = true, fluent = true)
public class RedisManager {

    private static @Getter RedisManager instance;

    private final Queue<RedisResponse<?>> awaitingResponses;
    private final RedisEventManager redisEventManager;
    private final RedisDebugger debugger;
    private final GsonSettings gsonSettings;
    private final RedisConfig redisConfig;
    private final Reflections reflections;
    private final boolean debug;
    private final boolean localOnly;
    private JedisPool jedisPool;
    private Thread redisTread;
    private JedisPubSub subscriberJedisPubSub;
    private long id;

    @lombok.Builder(builderClassName = "Builder")
    public RedisManager(GsonSettings gsonSettings, RedisConfig redisConfig, Reflections reflections, boolean debug,
                        boolean localOnly) {
        instance = this;

        this.gsonSettings = gsonSettings;
        this.redisConfig = redisConfig;
        this.reflections = reflections;
        this.debug = debug;
        this.localOnly = localOnly;

        debugger = new RedisDebugger(debug());
        debugger.creatingListener(redisConfig().getChannel());
        redisEventManager = new RedisEventManager();
        awaitingResponses = new ConcurrentLinkedQueue<>();

        new RedisEventTypeAdapter().register(gsonSettings);

        if (!localOnly()) {
            connectJedis();
            subscribe();
        }
    }

    public static Builder builder() {
        return new Builder()
                .gsonSettings(new GsonSettings() )
                .redisConfig(new RedisConfig())
                .reflections(new Reflections())
                .debug(false)
                .localOnly(false);
    }

    public <T> RedisResponse<T> send(RedisEvent<T> event) {
        event.setOriginator(redisConfig().getChannel());

        if (event instanceof ResponseEvent) {
            if (event.getRedisTarget().equals(event.getOriginator())) {
                debugger.sendResponse("LOCAL", event.serialize());
                redisEventManager.fire(event);

                ResponseEvent responseEvent = (ResponseEvent) event;

                RedisResponse<?> response = getResponse(responseEvent);
                if (response == null) {
                    return null;
                }
                response.respond(responseEvent);

                return null;
            }

            debugger.sendResponse(event.getRedisTarget(), event.serialize());

            try (Jedis jedis = jedisPool.getResource()) {
                jedis.publish(event.getRedisTarget(), event.serialize());
            } catch (Exception e) {
                if (debugger.isEnabled()) {
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                }
            }

            return null;
        }

        id++;
        event.setId(id);

        RedisResponse<T> redisResponse = new RedisResponse<>(event.getId());
        awaitingResponses.add(redisResponse);

        if (event.getRedisTarget().equals(event.getOriginator())) {
            debugger.send("LOCAL", event.serialize());
            redisEventManager.fire(event);

            return redisResponse;
        }

        debugger.send(event.getRedisTarget(), event.serialize());

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(event.getRedisTarget(), event.serialize());
        } catch (JedisConnectionException e) {
            throw new RuntimeException("Unable to publish channel message", e);
        }

        return redisResponse;
    }

    private void connectJedis() {
        if (jedisPool != null) {
            jedisPool.destroy();
        }

        JedisPoolConfig jedisConfig = new JedisPoolConfig();
        jedisConfig.setMaxTotal(16);

        jedisPool = new JedisPool(
                jedisConfig,
                redisConfig().getHost(),
                redisConfig().getPort(),
                0,
                redisConfig().getPassword()
        );
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    private RedisResponse<?> getResponse(ResponseEvent command) {
        //Remove streams, these are slow when called a lot
        for (RedisResponse response : awaitingResponses) {
            if (response.getId() == command.getId()) {
                return response;
            }
        }

        return null;
    }

    private void subscribe() {
        subscriberJedisPubSub = new JedisPubSub() {

            public void onMessage(String channel, final String command) {
                try {
                    onMessageReceive(channel, command);
                } catch (Throwable throwable) {
                    if (Debugger.isEnabled()) {
                        //noinspection CallToPrintStackTrace
                        throwable.printStackTrace();
                    }

                    Logger.error("There was an error while receiving a message from Redis.");
                }
            }

            public void onMessageReceive(String channel, final String event) {
                if (event.trim().isEmpty()) {
                    return;
                }

                RedisEvent<?> redisEvent = RedisEvent.deserialize(event);

                if (redisEvent == null) {
                    Logger.error("An error occurred while creating the class instance of the RedisEvent. " +
                            "Please refer to the error above if there is any.");
                    return;
                }

                if (redisEvent.getClass().equals(ResponseEvent.class)) {
                    ResponseEvent responseEvent = (ResponseEvent) redisEvent;

                    debugger.receiveResponse(channel, event);
                    RedisResponse<?> response = getResponse(responseEvent);
                    if (response == null) {
                        return;
                    }
                    response.respond(responseEvent);

                    return;
                }

                ScheduleManager.runTaskAsync(() -> {
                    debugger.receive(channel, event);
                    redisEvent.fireEvent();
                });
            }

            @Override
            public void onSubscribe(String channel, int subscribedChannels) {
                debugger.subscribed(channel);
            }

            @Override
            public void onUnsubscribe(String channel, int subscribedChannels) {
                debugger.unsubscribed(channel);
            }
        };


        startRedisThread();
    }

    private void startRedisThread() {
        if (redisTread != null) {
            redisTread.interrupt();
        }

        redisTread = new Thread(() -> {
            try (Jedis subscriberJedis = jedisPool.getResource()) {
                subscriberJedis.subscribe(subscriberJedisPubSub, redisConfig().getChannel(),
                        redisConfig().getChannelBase() + "#*");
            } catch (Exception e) {
                Logger.error("Lost connection to redis server. Retrying in 3 seconds...");
                if (debugger.isEnabled()) {
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {
                }

                Logger.good("Reconnecting to redis server.");
                startRedisThread();
            }
        });
        redisTread.start();
    }
}