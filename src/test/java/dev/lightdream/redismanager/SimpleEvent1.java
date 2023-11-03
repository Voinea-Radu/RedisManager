package dev.lightdream.redismanager;

import dev.lightdream.redismanager.event.RedisEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SimpleEvent1 extends RedisEvent<Integer> {

    private int a;
    private int b;

    public SimpleEvent1(RedisMain main, int a, int b) {
        super(main.getRedisConfig().getRedisID());

        this.a = a;
        this.b = b;
    }

}
