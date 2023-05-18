package dev.lightdream.redismanager.manager;

import dev.lightdream.lambda.lambda.ArgLambdaExecutor;
import dev.lightdream.logger.Logger;
import dev.lightdream.redismanager.Statics;
import dev.lightdream.redismanager.annotation.RedisEventHandler;
import dev.lightdream.redismanager.event.RedisEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RedisEventManager {

    private final RedisManager redisManager;

    @SuppressWarnings("rawtypes")
    private final HashMap<
            Class<? extends RedisEvent>,
            List<ArgLambdaExecutor<? extends RedisEvent>>
            > eventHandlers = new HashMap<>();

    public RedisEventManager(RedisManager manager) {
        this.redisManager = manager;
        Statics.getMain().getReflections()
                .getMethodsAnnotatedWith(RedisEventHandler.class)
                .forEach(method -> register(method, true));
    }

    @SuppressWarnings("rawtypes")
    public <T extends RedisEvent> void register(Class<T> clazz, ArgLambdaExecutor<T> handler) {
        List<ArgLambdaExecutor<? extends RedisEvent>> eventHandlers =
                this.eventHandlers.getOrDefault(clazz, new ArrayList<>());
        eventHandlers.add(handler);
        this.eventHandlers.put(clazz, eventHandlers);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void register(Method method, boolean fromConstructor) {
        method.setAccessible(true);

        if (!method.isAnnotationPresent(RedisEventHandler.class)) {
            Logger.error("Method " + method.getName() + " from class " + method.getDeclaringClass() +
                    " is not annotated with RedisEventHandler");
            return;
        }

        RedisEventHandler redisEventHandler = method.getAnnotation(RedisEventHandler.class);
        if (!redisEventHandler.autoRegister() && fromConstructor) {
            return;
        }

        Class<?>[] params = method.getParameterTypes();

        if (!Modifier.isStatic(method.getModifiers())) {
            printError(method, "is not static");
            return;
        }

        if (params.length != 1) {
            printError(method, "does not meet the definition requirements. It must have one argument");
            return;
        }

        Class<?> paramClass = params[0];

        if (!RedisEvent.class.isAssignableFrom(paramClass)) {
            printError(method, "has a parameter is not an instance of RedisEvent");
            return;
        }

        redisManager.getDebugger().registeringMethod(method.getName(), method.getDeclaringClass().getName());

        Class<? extends RedisEvent> clazz = (Class<? extends RedisEvent>) paramClass;

        List<ArgLambdaExecutor<? extends RedisEvent>> handlers = eventHandlers.getOrDefault(clazz, new ArrayList<>());

        handlers.add(event -> {
            try {
                method.invoke(null, event);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                Logger.error("There was an error executing a redis event");
            }
        });

        eventHandlers.put(clazz, handlers);
    }

    public void register(Object object) {
        for (Method declaredMethod : object.getClass().getDeclaredMethods()) {
            if (!declaredMethod.isAnnotationPresent(RedisEventHandler.class)) {
                continue;
            }

            register(declaredMethod, false);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked", "unused"})
    public void fire(RedisEvent event) {
        for (ArgLambdaExecutor eventHandler :
                eventHandlers.getOrDefault(event.getClass(), new ArrayList<>())) {
            eventHandler.execute(event);
        }
    }

    private void printError(Method method, String text) {
        Logger.error("Method " + method.getName() + " from class " + method.getDeclaringClass() + " " + text);
    }
}