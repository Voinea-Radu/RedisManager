package dev.lightdream.redismanager.common.manager;

import dev.lightdream.logger.Debugger;
import dev.lightdream.redismanager.common.RedisMain;
import dev.lightdream.redismanager.common.dto.RedisResponse;
import dev.lightdream.redismanager.common.event.RedisEvent;
import dev.lightdream.redismanager.common.event.impl.ResponseEvent;

import java.util.Queue;

public abstract class RedisPlatform {

    private final CommonRedisManager redisManager;

    protected RedisPlatform(CommonRedisManager redisManager){
        this.redisManager=redisManager;
        connect();
    }

    // RedisManager Wrapper

    protected boolean isDebugEnabled(){
        return redisManager.isDebugEnabled();
    }

    protected void debug(String s) {
        if (isDebugEnabled()) {
            Debugger.info(s);
        }
    }

    protected void onMessageReceive(String channel, final String command){
        redisManager.onMessageReceive(channel, command);
    }

    protected RedisMain getMain(){
        return redisManager.getMain();
    }

    // Platform methods

    protected abstract void send(String channel, String message);

    protected abstract void connect();

    protected abstract void disconnect();

}
