package dev.lightdream.redismanager.redisson;

import dev.lightdream.redismanager.common.RedisMain;
import dev.lightdream.redismanager.common.manager.CommonRedisManager;
import dev.lightdream.redismanager.common.manager.RedisPlatform;

public class RedisManager extends CommonRedisManager {
    public RedisManager(RedisMain main) {
        super(main);
    }

    @Override
    protected RedisPlatform getRedisPlatform() {
        return new RedissonPlatform(this);
    }
}
