package dev.lightdream.redismanager.event.impl;

import dev.lightdream.redismanager.event.RedisEvent;
import dev.lightdream.redismanager.utils.Utils;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ResponseEvent extends RedisEvent<Object> {

    public String response;

    public ResponseEvent(RedisEvent command, Object response) {
        super(command.originator);
        this.id = command.id;
        this.response = Utils.toJson(response);
    }

}
