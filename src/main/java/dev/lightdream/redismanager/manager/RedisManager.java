package dev.lightdream.redismanager.manager;

import dev.lightdream.logger.Debugger;
import dev.lightdream.logger.Logger;
import dev.lightdream.redismanager.RedisMain;
import dev.lightdream.redismanager.dto.RedisResponse;
import dev.lightdream.redismanager.event.RedisEvent;
import dev.lightdream.redismanager.event.impl.ResponseEvent;
import dev.lightdream.redismanager.utils.JsonUtils;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RedisManager {

    private final Queue<RedisResponse<?>> awaitingResponses = new ConcurrentLinkedQueue<>();
    private final RedisMain main;
    public JedisPool jedisPool;
    public Thread redisTread = null;
    public RedisEventManager redisEventManager;
    private JedisPubSub subscriberJedisPubSub;
    private int id = 0;
    private boolean debug = false;

    public RedisManager(RedisMain main) {
        this.main = main;
        redisEventManager = new RedisEventManager();
        debug("Creating RedisManager with listenID: " + main.getRedisConfig().redisID);

        connectJedis();
        subscribe();
    }

    public void register(Object listener) {
        redisEventManager.register(listener);
    }

    private void connectJedis() {
        if (jedisPool != null) {
            jedisPool.destroy();
        }

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(16);

        this.jedisPool = new JedisPool(config, main.getRedisConfig().host, main.getRedisConfig().port, 0, main.getRedisConfig().password);
    }

    @SuppressWarnings("unused")
    public void enableDebugMessage() {
        debug = true;
    }

    private void debug(String s) {
        if (debug) {
            Debugger.info(s);
        }
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

            @SuppressWarnings("unchecked")
            @Override
            public void onMessage(String channel, final String command) {
                if (command.trim().length() == 0) {
                    return;
                }

                Class<? extends RedisEvent<?>> clazz = JsonUtils.fromJson(command, RedisEvent.class).getClassByName();

                if (clazz.equals(ResponseEvent.class)) {
                    ResponseEvent responseEvent = JsonUtils.fromJson(command, ResponseEvent.class);
                    if (!responseEvent.redisTarget.equals(main.getRedisConfig().redisID)) {
                        debug("[Receive-Not-Allowed] [" + channel + "] HIDDEN");
                        return;
                    }

                    debug("[Receive-Response   ] [" + channel + "] " + command);
                    RedisResponse<?> response = getResponse(responseEvent);
                    if (response == null) {
                        return;
                    }
                    response.respondUnsafe(responseEvent.response, responseEvent.responseClassName);

                    return;
                }

                new Thread(() -> {
                    RedisEvent<?> redisEvent = JsonUtils.fromJson(command, clazz);
                    if (!redisEvent.redisTarget.equals(main.getRedisConfig().redisID)) {
                        debug("[Receive-Not-Allowed] [" + channel + "] HIDDEN");
                        return;
                    }
                    debug("[Receive            ] [" + channel + "] " + command);
                    redisEvent.fireEvent(main);
                }).start();
            }

            @Override
            public void onSubscribe(String channel, int subscribedChannels) {
                debug("Subscribed to channel " + channel);
            }

            @Override
            public void onUnsubscribe(String channel, int subscribedChannels) {
                debug("Unsubscribed from channel " + channel);
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
                subscriberJedis.subscribe(subscriberJedisPubSub, main.getRedisConfig().channel);
            } catch (Exception e) {
                Logger.error("Lost connection to redis server. Retrying in 3 seconds...");
                if (debug) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {

                }
                Logger.good("Reconnected to redis server.");
                startRedisThread();
            }
        });
        redisTread.start();
    }

    @SuppressWarnings("unused")
    public void unsubscribe() {
        subscriberJedisPubSub.unsubscribe();
    }

    public <T> RedisResponse<T> send(RedisEvent<T> command) {
        command.originator = main.getRedisConfig().redisID;

        if (command instanceof ResponseEvent) {
            debug("[Send-Response      ] [" + main.getRedisConfig().channel + "] " + command);

            try (Jedis jedis = jedisPool.getResource()) {
                jedis.publish(main.getRedisConfig().channel, command.toString());
            } catch (Exception e) {
                if (debug) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        command.id = ++id;
        debug("[Send               ] [" + main.getRedisConfig().channel + "] " + command);

        RedisResponse<T> redisResponse = new RedisResponse<>(command.id);
        awaitingResponses.add(redisResponse);

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(main.getRedisConfig().channel, command.toString());
        } catch (JedisConnectionException e) {
            throw new RuntimeException("Unable to publish channel message", e);
        }

        return redisResponse;
    }

    public Queue<RedisResponse<?>> getAwaitingResponses() {
        return awaitingResponses;
    }


}