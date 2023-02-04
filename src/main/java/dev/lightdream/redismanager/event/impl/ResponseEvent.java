package dev.lightdream.redismanager.event.impl;

import dev.lightdream.redismanager.event.RedisEvent;
import dev.lightdream.redismanager.manager.RedisManager;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ResponseEvent extends RedisEvent<Object> {

    public String response;
    public String responseClassName;

    public ResponseEvent(RedisEvent<?> command, Object response) {
        super(command.originator);
        this.id = command.id;
        this.response = RedisManager.toJson(response);
        this.responseClassName = response.getClass().getName();
    }

}
