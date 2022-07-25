import dev.lightdream.logger.Debugger;
import dev.lightdream.logger.LoggableMain;
import dev.lightdream.redismanager.RedisMain;
import dev.lightdream.redismanager.dto.RedisConfig;
import dev.lightdream.redismanager.manager.RedisEventManager;
import dev.lightdream.redismanager.manager.RedisManager;

public class RedisMainImpl implements RedisMain, LoggableMain {

    private RedisManager redisManager;
    private RedisEventManager redisEventManager;

    private RedisConfig config;

    public void enable() {
        Debugger.init(this);

        config = new RedisConfigGenerator().generateConfig();
        redisManager = new RedisManager(this);
        redisEventManager = new RedisEventManager();
    }

    @Override
    public RedisManager getRedisManager() {
        return redisManager;
    }

    @Override
    public RedisConfig getRedisConfig() {
        return config;
    }

    @Override
    public String getRedisID() {
        return "test_env_redis_manager";
    }

    @Override
    public RedisEventManager getRedisEventManager() {
        return redisEventManager;
    }

    @Override
    public boolean debug() {
        return true;
    }

    @Override
    public void log(String s) {
        System.out.println(s);
    }
}
