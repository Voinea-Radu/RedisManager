package dev.lightdream.redismanager;

import dev.lightdream.redismanager.dto.RedisConfig;
import dev.lightdream.redismanager.manager.RedisManager;

public interface RedisMain {

    /**
     * @return The RedisManager instance
     */
    RedisManager getRedisManager();

    /**
     * @return The RedisConfig instance
     */
    RedisConfig getRedisConfig();

    /**
     * Time for event to be responded to in milliseconds
     * @return Time in milliseconds
     */
    default int getTimeout() {
        return 2 * 1000;
    }

    /**
     * Time between checks if a particular event has been responded to
     * @return Time in milliseconds
     */
    default int getWaitBeforeIteration() {
        return 50;
    }

    String getPackageName();

}
