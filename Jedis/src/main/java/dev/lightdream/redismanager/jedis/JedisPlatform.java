package dev.lightdream.redismanager.jedis;

import dev.lightdream.logger.Logger;
import dev.lightdream.redismanager.common.manager.RedisPlatform;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class JedisPlatform extends RedisPlatform {

    private JedisPool jedisPool;
    private JedisPubSub jedisPubSub;

    private Thread redisTread = null;

    public JedisPlatform(RedisManager manager) {
        super(manager);
    }

    public void startRedisThread() {
        if (redisTread != null) {
            redisTread.interrupt();
        }
        redisTread = new Thread(() -> {
            try (Jedis subscriberJedis = jedisPool.getResource()) {
                subscriberJedis.subscribe(jedisPubSub, getMain().getRedisConfig().channel);
            } catch (Exception e) {
                Logger.error("Lost connection to redis server. Retrying in 3 seconds...");
                if (isDebugEnabled()) {
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

    @Override
    public void send(String channel, String message) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(channel, message);
        } catch (JedisConnectionException e) {
            if (isDebugEnabled()) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void connect() {

        // Create the config
        if (jedisPool != null) {
            jedisPool.destroy();
        }

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(16);

        this.jedisPool = new JedisPool(
                config,
                getMain().getRedisConfig().host,
                getMain().getRedisConfig().port,
                0,
                getMain().getRedisConfig().password
        );

        // Subscribe
        jedisPubSub = new JedisPubSub() {
            public void onMessage(String channel, final String command) {
                try {
                    onMessageReceive(channel, command);
                } catch (Throwable t) {
                    t.printStackTrace();
                    Logger.error("There was an error while receiving a message from Redis.");
                }
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

    @Override
    public void disconnect() {
        if (jedisPool != null) {
            jedisPool.destroy();
        }

        if (jedisPubSub != null) {
            jedisPubSub.unsubscribe();
        }

        if (redisTread != null) {
            redisTread.interrupt();
        }
    }

}
