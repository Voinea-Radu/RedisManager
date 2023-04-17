package dev.lightdream.redismanager;

import com.google.gson.Gson;
import dev.lightdream.redismanager.dto.RedisConfig;
import dev.lightdream.redismanager.manager.RedisManager;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

public interface RedisMain {

    @NotNull RedisManager getRedisManager();

    @NotNull RedisConfig getRedisConfig();

    @NotNull Reflections getReflections();

    @NotNull Gson getGson();

    @SuppressWarnings("unused")
    default void initializeRedisMain() {
        Statics.setMain(this);
    }

}
