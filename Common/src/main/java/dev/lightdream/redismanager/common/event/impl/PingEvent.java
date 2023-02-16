package dev.lightdream.redismanager.common.event.impl;

import dev.lightdream.redismanager.common.event.RedisEvent;
import lombok.NoArgsConstructor;

@SuppressWarnings("unused")
@NoArgsConstructor
public class PingEvent extends RedisEvent<Object> {

    public PingEvent(String target) {
        super(target);
    }
}
