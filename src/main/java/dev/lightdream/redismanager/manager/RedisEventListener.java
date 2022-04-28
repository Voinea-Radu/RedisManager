package dev.lightdream.redismanager.manager;

import dev.lightdream.logger.Logger;
import dev.lightdream.redismanager.RedisMain;
import dev.lightdream.redismanager.annotation.RedisEventHandler;
import dev.lightdream.redismanager.event.RedisEvent;
import dev.lightdream.redismanager.event.impl.PingEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@SuppressWarnings("unused")
public class RedisEventListener {

    private final RedisMain main;


    @SuppressWarnings({"unchecked", "unused"})
    public RedisEventListener(RedisMain main) {
        this.main = main;
        for (Method method : getClass().getMethods()) {
            if (!method.isAnnotationPresent(RedisEventHandler.class)) {
                continue;
            }

            if (method.getParameterCount() != 1) {
                Logger.error("Method " + getClass().getSimpleName() + "#" + method.getName() + " from does not have the correct parameter count.");
                continue;
            }

            Parameter parameter = method.getParameters()[0];
            Class<?> clazz = parameter.getType();

            main.getRedisEventManager().registerListener((Class<? extends RedisEvent>) clazz, obj -> {
                try {
                    method.invoke(this, clazz.cast(obj));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }

            });

        }
    }

    @SuppressWarnings("unused")
    @RedisEventHandler
    public void onCommandExecute(PingEvent event) {
        event.respond(main, "Pong!");
    }

}
