package dev.lightdream.redismanager.event.impl;

import dev.lightdream.redismanager.event.RedisEvent;
import lombok.NoArgsConstructor;

@SuppressWarnings("unused")
@NoArgsConstructor
public class PingEvent extends RedisEvent<Boolean> {

    public PingEvent(String target) {
        super(target);
    }
}
