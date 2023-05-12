package dev.lightdream.redismanager.manager;

import dev.lightdream.lambda.lambda.ArgLambdaExecutor;
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

public class RedisManager {
    private final Queue<RedisResponse<?>> awaitingResponses = new ConcurrentLinkedQueue<>();

    public JedisPool jedisPool;
    public Thread redisTread = null;
    public RedisEventManager redisEventManager;
    private JedisPubSub subscriberJedisPubSub;
    private int id = 0;

    private final @Getter RedisDebugger debugger;

    @SuppressWarnings("unused")
    public RedisManager() {
        this(false);
    }

    public RedisManager(boolean debug) {
        debugger = new RedisDebugger(debug);

        redisEventManager = new RedisEventManager(this);
        debugger.creatingListener(config().getRedisID());

        connectJedis();
        subscribe();
    }

    @SuppressWarnings("unused")
    public void register(Object listener) {
        redisEventManager.register(listener);
    }

    @SuppressWarnings({"rawtypes", "unused"})
    public <E extends RedisEvent> void register(Class<E> clazz, ArgLambdaExecutor<E> method) {
        redisEventManager.register(clazz, method);
    }

    private void connectJedis() {
        if (jedisPool != null) {
            jedisPool.destroy();
        }

        JedisPoolConfig jedisConfig = new JedisPoolConfig();
        jedisConfig.setMaxTotal(16);

        this.jedisPool = new JedisPool(
                jedisConfig,
                config().getHost(),
                config().getPort(),
                0,
                config().getPassword());
    }

    @SuppressWarnings("unused")
    public void enableDebug() {
        debugger.enable();
    }

    @SuppressWarnings("unused")
    public void disableDebug() {
        debugger.disable();
    }

    @SuppressWarnings("unused")
    public void setEnableDebug(boolean enable) {
        debugger.setEnabled(enable);
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
                } catch (Throwable t) {
                    t.printStackTrace();
                    Logger.error("There was an error while receiving a message from Redis.");
                }
            }

            @SuppressWarnings("unchecked")
            public void onMessageReceive(String channel, final String event) {
                if (event.trim().length() == 0) {
                    return;
                }


                Class<? extends RedisEvent<?>> clazz = Statics.getMain().getGson().fromJson(event, RedisEvent.class).getClassByName();

                if (clazz == null) {
                    Logger.error("An error occurred while creating the class instance of the RedisEvent. " +
                            "Please refer to the error above if there is any.");
                    return;
                }

                if (clazz.equals(ResponseEvent.class)) {
                    ResponseEvent responseEvent = Statics.getMain().getGson().fromJson(event, ResponseEvent.class);
                    if (!shouldReceive(responseEvent)) {
                        debugger.receiveNotAllowed(channel);
                        return;
                    }

                    debugger.receiveResponse(channel, event);
                    RedisResponse<?> response = getResponse(responseEvent);
                    if (response == null) {
                        return;
                    }
                    response.respondUnsafe(responseEvent.response, responseEvent.responseClassName);

                    return;
                }

                new Thread(() -> {
                    RedisEvent<?> redisEvent = Statics.getMain().getGson().fromJson(event, clazz);
                    if (!shouldReceive(redisEvent)) {
                        debugger.receiveNotAllowed(channel);
                        return;
                    }
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
                subscriberJedis.subscribe(subscriberJedisPubSub, config().getChannel());
            } catch (Exception e) {
                Logger.error("Lost connection to redis server. Retrying in 3 seconds...");
                if (debugger.isEnabled()) {
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

    public <T> RedisResponse<T> send(RedisEvent<T> event) {
        event.originator = config().getRedisID();

        if (event instanceof ResponseEvent) {
            debugger.sendResponse(config().getChannel(), event.serialize());

            try (Jedis jedis = jedisPool.getResource()) {
                jedis.publish(config().getChannel(), event.serialize());
            } catch (Exception e) {
                if (debugger.isEnabled()) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        event.id = ++id;
        debugger.send(config().getChannel(), event.serialize());

        RedisResponse<T> redisResponse = new RedisResponse<>(event.id);
        awaitingResponses.add(redisResponse);

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(config().getChannel(), event.serialize());
        } catch (JedisConnectionException e) {
            throw new RuntimeException("Unable to publish channel message", e);
        }

        return redisResponse;
    }

    public Queue<RedisResponse<?>> getAwaitingResponses() {
        return awaitingResponses;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean shouldReceive(RedisEvent<?> event){
        return event.redisTarget.equals("*") ||
                event.redisTarget.equals(config().getRedisID());
    }

    private RedisConfig config(){
        return RedisMain.getRedisMain().getRedisConfig();
    }

}