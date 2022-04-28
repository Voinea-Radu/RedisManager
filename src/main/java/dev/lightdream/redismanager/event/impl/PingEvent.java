package dev.lightdream.redismanager.event.impl;

import dev.lightdream.redismanager.event.RedisEvent;
import dev.lightdream.redismanager.event.RedisEventTarget;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PingEvent extends RedisEvent {

    @SuppressWarnings("unused")
    public PingEvent(RedisEventTarget target) {
        super(target);
    }
}
