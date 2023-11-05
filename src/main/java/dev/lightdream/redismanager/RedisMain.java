package dev.lightdream.redismanager;

import com.google.gson.Gson;
import dev.lightdream.filemanager.FileManager;
import dev.lightdream.logger.Debugger;
import dev.lightdream.redismanager.dto.RedisConfig;
import dev.lightdream.redismanager.manager.RedisManager;
import dev.lightdream.redismanager.type_adapter.RedisEventTypeAdapter;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;

public interface RedisMain {

    static RedisMain getRedisMain() {
        return Statics.getMain();
    }

    @NotNull RedisManager getRedisManager();

    @NotNull RedisConfig getRedisConfig();

    @NotNull Reflections getReflections();

    @NotNull Gson getGson();

    @SuppressWarnings("unused")
    default void initializeRedisMain() {
        Statics.setMain(this);
        if(FileManager.get()!=null){
            Debugger.good("Registering RedisManager type adapter(s)");
            FileManager.get().registerSerializer(new RedisEventTypeAdapter());
        }
    }

}
