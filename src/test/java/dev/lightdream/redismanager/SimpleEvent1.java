package dev.lightdream.redismanager;

import dev.lightdream.redismanager.event.RedisEvent;
import dev.lightdream.redismanager.manager.RedisManager;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SimpleEvent1 extends RedisEvent<Integer> {

    private int a;
    private int b;

    public SimpleEvent1( int a, int b) {
        super(RedisManager.instance().redisConfig().getRedisID());

        this.a = a;
        this.b = b;
    }

}
