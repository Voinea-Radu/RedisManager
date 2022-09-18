import dev.lightdream.redismanager.dto.RedisConfig;

public interface IRedisConfigGenerator {

    default RedisConfig generateConfig() {
        return generate();
    }

    RedisConfig generate();

}
