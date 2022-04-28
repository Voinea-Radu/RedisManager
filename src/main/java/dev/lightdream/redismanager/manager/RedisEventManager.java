package dev.lightdream.redismanager.manager;

import dev.lightdream.lambda.LambdaExecutor;
import dev.lightdream.redismanager.event.RedisEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RedisEventManager {

    public HashMap<Class<? extends RedisEvent>, List<LambdaExecutor.NoReturnLambdaExecutor<RedisEvent>>> eventListeners = new HashMap<>();

    public void registerListener(Class<? extends RedisEvent> event, LambdaExecutor.NoReturnLambdaExecutor<RedisEvent> listener) {
        List<LambdaExecutor.NoReturnLambdaExecutor<RedisEvent>> listeners = new ArrayList<>(eventListeners.getOrDefault(event, new ArrayList<>()));
        listeners.add(listener);
        eventListeners.put(event, listeners);
    }

    public void fire(RedisEvent event) {
        eventListeners.getOrDefault(event.getClass(), new ArrayList<>())
                .forEach(listener -> listener.execute(event));
    }

}
