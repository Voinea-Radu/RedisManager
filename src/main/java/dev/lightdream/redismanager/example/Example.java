package dev.lightdream.redismanager.example;

import dev.lightdream.redismanager.RedisMain;
import dev.lightdream.redismanager.dto.RedisConfig;
import dev.lightdream.redismanager.manager.RedisEventManager;
import dev.lightdream.redismanager.manager.RedisManager;

@SuppressWarnings("unused")
public class Example implements RedisMain {

    private final RedisConfig redisConfig;
    private final RedisManager redisManager;
    private final RedisEventManager redisEventManager;

    public Example() {
        redisConfig = new RedisConfig();
        redisManager = new RedisManager(this);
        redisEventManager = new RedisEventManager();
    }

    @Override
    public RedisManager getRedisManager() {
        return redisManager;
    }

    @Override
    public RedisConfig getRedisConfig() {
        return redisConfig;
    }

    @Override
    public String getRedisID() {
        return "unique_redis_id";
    }

    @Override
    public RedisEventManager getRedisEventManager() {
        return redisEventManager;
    }
}
