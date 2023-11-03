package dev.lightdream.redismanager;

import dev.lightdream.redismanager.event.RedisEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ComplexEvent1 extends RedisEvent<List<String>> {

    private List<String> a;
    private String b;

    public ComplexEvent1(RedisMain main, List<String> a, String b) {
        super(main.getRedisConfig().getRedisID());

        this.a = a;
        this.b = b;
    }

}