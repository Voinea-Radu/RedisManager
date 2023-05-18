package dev.lightdream.redismanager.manager;

import dev.lightdream.lambda.lambda.ArgLambdaExecutor;
import dev.lightdream.logger.Logger;
import dev.lightdream.redismanager.Statics;
import dev.lightdream.redismanager.annotation.RedisEventHandler;
import dev.lightdream.redismanager.event.RedisEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("rawtypes")
public class RedisEventManager {

    private final RedisManager redisManager;

    private final HashMap<
            Class<? extends RedisEvent>,
            List<HandlerObject>
            > eventHandlers = new HashMap<>();

    @Getter
    @AllArgsConstructor
    public static class HandlerObject{
       private ArgLambdaExecutor executor;
       private RedisEventHandler annotation;
    }

    public RedisEventManager(RedisManager manager) {
        this.redisManager = manager;
        Statics.getMain().getReflections()
                .getMethodsAnnotatedWith(RedisEventHandler.class)
                .forEach(method -> register(method, true));
    }

    public <T extends RedisEvent> void register(Class<T> clazz, ArgLambdaExecutor<T> handler) {
        register(clazz, handler, 0);
    }

    public <T extends RedisEvent> void register(Class<T> clazz, ArgLambdaExecutor<T> handler, int priority) {
        List<HandlerObject> eventHandlers = this.eventHandlers.getOrDefault(clazz, new ArrayList<>());

        HandlerObject handlerObject = new HandlerObject(handler, createRedisEventAnnotation(priority));

        eventHandlers.add(handlerObject);
        this.eventHandlers.put(clazz, eventHandlers);
    }

    @SuppressWarnings("unchecked")
    private void register(Method method, boolean fromConstructor) {
        method.setAccessible(true);

        if (!method.isAnnotationPresent(RedisEventHandler.class)) {
            Logger.error("Method " + method.getName() + " from class " + method.getDeclaringClass() +
                    " is not annotated with RedisEventHandler");
            return;
        }

        RedisEventHandler redisEventAnnotation = method.getAnnotation(RedisEventHandler.class);
        if (!redisEventAnnotation.autoRegister() && fromConstructor) {
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

        List<HandlerObject> handlers = eventHandlers.getOrDefault(clazz, new ArrayList<>());

        HandlerObject handlerObject = new HandlerObject(
                event -> {
                    try {
                        method.invoke(null, event);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        Logger.error("There was an error executing a redis event");
                    }
                },
                redisEventAnnotation
        );

        handlers.add(handlerObject);
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

    @SuppressWarnings({"unused", "unchecked"})
    public void fire(RedisEvent event) {
        List<HandlerObject> handlers = eventHandlers.getOrDefault(event.getClass(), new ArrayList<>());
        handlers.sort(Comparator.comparingInt(o -> o.getAnnotation().priority()));

        for (HandlerObject handlerObject :handlers) {
            handlerObject.getExecutor().execute(event);
        }
    }

    private void printError(Method method, String text) {
        Logger.error("Method " + method.getName() + " from class " + method.getDeclaringClass() + " " + text);
    }

    private RedisEventHandler createRedisEventAnnotation(int priority){
        return new RedisEventHandler() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return RedisEventHandler.class;
            }

            @Override
            public int priority() {
                return priority;
            }

            @Override
            public boolean autoRegister() {
                return false;
            }
        };
    }
}