package dev.lightdream.redismanager.redisson;

import dev.lightdream.messagebuilder.MessageBuilder;
import dev.lightdream.redismanager.common.manager.CommonRedisManager;
import dev.lightdream.redismanager.common.manager.RedisPlatform;
import org.redisson.Redisson;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class RedissonPlatform extends RedisPlatform {

    private RTopic topic;

    public RedissonPlatform(CommonRedisManager redisManager) {
        super(redisManager);
    }

    @Override
    public void send(String channel, String message) {
        topic.publish(message);
    }

    @Override
    public void connect() {
        Config config = new Config();
        config.useSingleServer().setAddress(
                new MessageBuilder("redis://%ip%:%port%")
                        .parse("ip", getMain().getRedisConfig().host )
                        .parse("port", getMain().getRedisConfig().port )
                        .parse()
        ).setPassword(getMain().getRedisConfig().password);

        RedissonClient client = Redisson.create(config);

        topic = client.getTopic(getMain().getRedisConfig().channel);
        topic.addListener(String.class, (channel, msg) -> onMessageReceive(channel.toString(), msg));
    }
}
