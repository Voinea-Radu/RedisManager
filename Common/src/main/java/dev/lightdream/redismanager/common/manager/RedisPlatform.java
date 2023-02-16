package dev.lightdream.redismanager.common.manager;

import dev.lightdream.logger.Debugger;
import dev.lightdream.redismanager.common.RedisMain;
import dev.lightdream.redismanager.common.dto.RedisResponse;
import dev.lightdream.redismanager.common.event.RedisEvent;
import dev.lightdream.redismanager.common.event.impl.ResponseEvent;

import java.util.Queue;

public abstract class RedisPlatform {

    private final CommonRedisManager redisManager;

    public RedisPlatform(CommonRedisManager redisManager){
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

    public void onMessageReceive(String channel, final String command){
        redisManager.onMessageReceive(channel, command);
    }

    protected RedisMain getMain(){
        return redisManager.getMain();
    }

    // Platform methods

    public abstract void send(String channel, String message);

    public abstract void connect();

    public abstract void disconnect();

}
