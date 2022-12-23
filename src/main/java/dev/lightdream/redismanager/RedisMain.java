package dev.lightdream.redismanager;

import dev.lightdream.lambda.reflection.ReflectionMain;
import dev.lightdream.redismanager.dto.RedisConfig;
import dev.lightdream.redismanager.manager.RedisEventManager;
import dev.lightdream.redismanager.manager.RedisManager;

public interface RedisMain extends ReflectionMain {

    RedisManager getRedisManager();

    RedisConfig getRedisConfig();

    String getRedisID();

    default int getTimeout() {
        return 2 * 1000;       // 2 seconds    (2000 milliseconds);
    }

    default int getWaitBeforeIteration() {
        return 50;             // 0.05 seconds (50 milliseconds);
    }

    String getPackage();

}
