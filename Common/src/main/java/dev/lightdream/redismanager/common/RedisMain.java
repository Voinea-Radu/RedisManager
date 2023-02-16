package dev.lightdream.redismanager.common;

import dev.lightdream.redismanager.common.dto.RedisConfig;
import dev.lightdream.redismanager.common.manager.CommonRedisManager;
import dev.lightdream.reflections.Mapper;
import org.jetbrains.annotations.NotNull;

public interface RedisMain {

    /**
     * @return The RedisManager instance
     */
    CommonRedisManager getRedisManager();

    /**
     * @return The RedisConfig instance
     */
    RedisConfig getRedisConfig();

    /**
     * Time for event to be responded to in milliseconds
     *
     * @return Time in milliseconds
     */
    default int getTimeout() {
        return 2 * 1000;
    }

    /**
     * Time between checks if a particular event has been responded to
     *
     * @return Time in milliseconds
     */
    default int getWaitBeforeIteration() {
        return 50;
    }

    /**
     * @return The Mapper instance
     */
    @NotNull Mapper getMapper();

}
