package dev.lightdream.redismanager;

import dev.lightdream.redismanager.event.RedisEvent;
import dev.lightdream.redismanager.manager.RedisManager;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SimpleEvent2 extends RedisEvent<String> {

    private List<String> a;
    private String b;

    public SimpleEvent2(List<String> a, String b) {
        super(RedisManager.instance().redisConfig().getRedisID());

        this.a = a;
        this.b = b;
    }

}
