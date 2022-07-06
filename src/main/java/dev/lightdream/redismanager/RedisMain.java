package dev.lightdream.redismanager;

import dev.lightdream.redismanager.dto.RedisConfig;
import dev.lightdream.redismanager.manager.RedisEventManager;
import dev.lightdream.redismanager.manager.RedisManager;

public interface RedisMain {

    RedisManager getRedisManager();

    RedisConfig getRedisConfig();

    String getRedisID();

    RedisEventManager getRedisEventManager();

    default int getTimeout() {
        return 2 * 1000;       // 2 seconds    (2000 milliseconds);
    }

    default int getWaitBeforeIteration() {
        return 50;             // 0.05 seconds (50 milliseconds);
    }

}
