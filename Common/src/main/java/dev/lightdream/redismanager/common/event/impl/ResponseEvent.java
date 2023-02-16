package dev.lightdream.redismanager.common.event.impl;

import dev.lightdream.redismanager.common.event.RedisEvent;
import dev.lightdream.redismanager.common.manager.CommonRedisManager;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ResponseEvent extends RedisEvent<Object> {

    public String response;
    public String responseClassName;

    public ResponseEvent(RedisEvent<?> command, Object response) {
        super(command.originator);
        this.id = command.id;
        this.response = CommonRedisManager.toJson(response);
        this.responseClassName = response.getClass().getName();
    }

}
