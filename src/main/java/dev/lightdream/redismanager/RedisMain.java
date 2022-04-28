package dev.lightdream.redismanager;

import dev.lightdream.redismanager.dto.RedisConfig;
import dev.lightdream.redismanager.manager.RedisEventManager;
import dev.lightdream.redismanager.manager.RedisManager;

public interface RedisMain {

    RedisEventManager getRedisEventManager();

    RedisManager getRedisManager();

    RedisConfig getRedisConfig();

    String getRedisID();

}
