package dev.lightdream.redismanager;

import lombok.Setter;

public class Statics {

    private static @Setter RedisMain main;

    public static RedisMain getMain() {
        if (main == null) {
            throw new RuntimeException("RedisMain has not been initialised. Please call #initializeRedisMain from inside your" +
                    "RedisMain instance");
        }
        return main;
    }

}
