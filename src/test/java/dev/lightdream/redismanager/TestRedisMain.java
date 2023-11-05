package dev.lightdream.redismanager;

import com.google.gson.Gson;
import dev.lightdream.filemanager.FileManager;
import dev.lightdream.filemanager.FileManagerMain;
import dev.lightdream.logger.LoggableMain;
import dev.lightdream.logger.Logger;
import dev.lightdream.messagebuilder.MessageBuilder;
import dev.lightdream.redismanager.dto.RedisConfig;
import dev.lightdream.redismanager.manager.RedisManager;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

public class TestRedisMain implements RedisMain, LoggableMain, FileManagerMain {

    private final RedisManager redisManager;
    private final Reflections reflections;
    private final RedisConfig config;

    public TestRedisMain() {
        Logger.init(this);
        new FileManager(this).setStatic();
        MessageBuilder.init();

        initializeRedisMain();

        reflections = new Reflections(
                new ConfigurationBuilder()
                        .forPackages("dev.lightdream.redismanager")
                        .setScanners(Scanners.MethodsAnnotated, Scanners.TypesAnnotated, Scanners.SubTypes)
        );
        config = new RedisConfig();

        redisManager = new RedisManager(true, true);
    }

    @Override
    public @NotNull RedisManager getRedisManager() {
        return redisManager;
    }

    @Override
    public @NotNull RedisConfig getRedisConfig() {
        return config;
    }

    @Override
    public @NotNull Reflections getReflections() {
        return reflections;
    }

    @Override
    public @NotNull Gson getGson() {
        if (FileManager.get() == null) {
            return new Gson();
        }
        return FileManager.get().getGson();
    }

    @Override
    public boolean debugToConsole() {
        return true;
    }

    @Override
    public @NotNull String getPath() {
        return "";
    }
}
