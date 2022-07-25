import dev.lightdream.redismanager.dto.RedisConfig;

public class RedisConfigGenerator implements IRedisConfigGenerator {

    @Override
    public RedisConfig generate() {
        RedisConfig config = new RedisConfig();

        config.host = "185.150.189.29";
        config.port = 6379;
        config.database = "kingdoms";
        config.username = "default";
        config.password = "snCuqG6MxQD9k4NR";
        config.channel = "redis_tests";

        return config;
    }
}
