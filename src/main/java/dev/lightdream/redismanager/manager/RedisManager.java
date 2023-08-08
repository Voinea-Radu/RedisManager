package dev.lightdream.redismanager.manager;

import dev.lightdream.logger.Debugger;
import dev.lightdream.logger.Logger;
import dev.lightdream.redismanager.RedisMain;
import dev.lightdream.redismanager.Statics;
import dev.lightdream.redismanager.dto.RedisConfig;
import dev.lightdream.redismanager.dto.RedisResponse;
import dev.lightdream.redismanager.event.RedisEvent;
import dev.lightdream.redismanager.event.impl.ResponseEvent;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@SuppressWarnings("LombokGetterMayBeUsed")
public class RedisManager {

    private final @Getter Queue<RedisResponse<?>> awaitingResponses = new ConcurrentLinkedQueue<>();
    private final @Getter RedisDebugger debugger;

    public RedisEventManager redisEventManager;

    private JedisPool jedisPool;
    private Thread redisTread = null;
    private JedisPubSub subscriberJedisPubSub;
    private int id = 0;

    @SuppressWarnings("unused")
    public RedisManager() {
        this(false);
    }

    public RedisManager(boolean debug) {
        debugger = new RedisDebugger(debug);

        redisEventManager = new RedisEventManager(this);
        debugger.creatingListener(getConfig().getChannel());

        connectJedis();
        subscribe();
    }

    private void connectJedis() {
        if (jedisPool != null) {
            jedisPool.destroy();
        }

        JedisPoolConfig jedisConfig = new JedisPoolConfig();
        jedisConfig.setMaxTotal(16);

        this.jedisPool = new JedisPool(
                jedisConfig,
                getConfig().getHost(),
                getConfig().getPort(),
                0,
                getConfig().getPassword());
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    private RedisResponse<?> getResponse(ResponseEvent command) {
        //Remove streams, these are slow when called a lot
        for (RedisResponse response : awaitingResponses) {
            if (response.id == command.id) {
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
                    response.respondUnsafe(responseEvent.response, responseEvent.responseClassName);

                    return;
                }

                new Thread(() -> {
                    debugger.receive(channel, event);
                    redisEvent.fireEvent();
                }).start();
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

    public void startRedisThread() {
        if (redisTread != null) {
            redisTread.interrupt();
        }

        redisTread = new Thread(() -> {
            try (Jedis subscriberJedis = jedisPool.getResource()) {
                subscriberJedis.subscribe(subscriberJedisPubSub, getConfig().getChannel(),
                        getConfig().getChannelBase() + "*");
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

    public <T> RedisResponse<T> send(RedisEvent<T> event) {
        event.originator = getConfig().getChannel();

        if (event.redisTarget.equals(event.originator)) {
            redisEventManager.fire(event);
            return null;
        }

        if (event instanceof ResponseEvent) {
            debugger.sendResponse(getConfig().getChannel(), event.serialize());

            try (Jedis jedis = jedisPool.getResource()) {
                jedis.publish(getConfig().getChannel(), event.serialize());
            } catch (Exception e) {
                if (debugger.isEnabled()) {
                    //noinspection CallToPrintStackTrace
                    e.printStackTrace();
                }
            }

            return null;
        }

        id++;
        event.id = id;
        debugger.send(getConfig().getChannel(), event.serialize());

        RedisResponse<T> redisResponse = new RedisResponse<>(event.id);
        awaitingResponses.add(redisResponse);

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(getConfig().getChannel(), event.serialize());
        } catch (JedisConnectionException e) {
            throw new RuntimeException("Unable to publish channel message", e);
        }

        return redisResponse;
    }

    private RedisConfig getConfig() {
        return RedisMain.getRedisMain().getRedisConfig();
    }
}