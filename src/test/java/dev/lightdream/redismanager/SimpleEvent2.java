package dev.lightdream.redismanager;

import dev.lightdream.redismanager.event.RedisEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SimpleEvent2 extends RedisEvent<String> {

    private List<String> a;
    private String b;

    public SimpleEvent2(RedisMain main, List<String> a, String b) {
        super(main.getRedisConfig().getRedisID());

        this.a = a;
        this.b = b;
    }

}
