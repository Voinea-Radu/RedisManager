package dev.lightdream.redismanager.manager;

import dev.lightdream.logger.Logger;
import dev.lightdream.redismanager.Statics;
import dev.lightdream.redismanager.annotation.RedisEventHandler;
import dev.lightdream.redismanager.event.RedisEvent;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class RedisEventManager {

    private final List<EventMethod> eventMethods = new ArrayList<>();
    private final RedisManager redisManager;

    public RedisEventManager(RedisManager manager) {
        this.redisManager = manager;
        Statics.getMain().getReflections()
                .getMethodsAnnotatedWith(RedisEventHandler.class)
                .forEach(method -> register(method, true));
    }

    @SneakyThrows
    @SuppressWarnings("rawtypes")
    private void register(Method method, boolean fromConstructor) {
        if (!method.isAnnotationPresent(RedisEventHandler.class)) {
            Logger.error("Method " + method.getName() + " from class " + method.getDeclaringClass() +
                    " is not annotated with RedisEventHandler");
            return;
        }

        RedisEventHandler redisEventHandler = method.getAnnotation(RedisEventHandler.class);
        if (!redisEventHandler.autoRegister() && fromConstructor) {
            return;
        }

        redisManager.getDebugger().registeringMethod(method.getName(), method.getDeclaringClass().getName());

        Object parentObject = null;
        Class<?> parentClass = method.getDeclaringClass();

        for (EventMethod eventMethod : eventMethods) {
            if (eventMethod.parentObject.getClass().equals(parentClass)) {
                parentObject = eventMethod.parentObject;
            }
        }

        if (parentObject == null) {
            parentObject = parentClass.getConstructor().newInstance();
        }

        Class<?>[] params = method.getParameterTypes();

        if (params.length != 1) {
            Logger.error("Method " + method.getName() + " from class " + method.getDeclaringClass() + " has more than one parameter");
            return;
        }

        Class<?> paramClass = params[0];

        if (!RedisEvent.class.isAssignableFrom(paramClass)) {
            Logger.warn("Parameter from method " + method.getName() + " from class " + method.getDeclaringClass() + " is not an instance of RedisEvent");
            return;
        }

        //noinspection unchecked
        Class<? extends RedisEvent> redisEventClass = (Class<? extends RedisEvent>) paramClass;

        EventMethod eventMethod = new EventMethod(parentObject, redisEventClass, method);
        eventMethods.add(eventMethod);
    }

    public void register(Object object) {
        for (Method declaredMethod : object.getClass().getDeclaredMethods()) {
            if (!declaredMethod.isAnnotationPresent(RedisEventHandler.class)) {
                continue;
            }

            register(declaredMethod, false);
        }
    }

    public void unregister(Object object) {
        eventMethods.removeIf(eventObject -> eventObject.parentObject.equals(object));
    }

    @SneakyThrows
    @SuppressWarnings({"rawtypes", "unused"})
    public void fire(RedisEvent event) {

        eventMethods.sort((o1, o2) -> {
            RedisEventHandler annotation1 = o1.method.getAnnotation(RedisEventHandler.class);
            RedisEventHandler annotation2 = o2.method.getAnnotation(RedisEventHandler.class);
            return annotation1.priority() - annotation2.priority();
        });

        for (EventMethod eventMethod : eventMethods) {
            if(!event.getClass().isAssignableFrom(eventMethod.eventClass)){
                continue;
            }

            eventMethod.method.setAccessible(true);
            eventMethod.method.invoke(eventMethod.parentObject, eventMethod.eventClass.cast(event));
        }
    }

    @SuppressWarnings("rawtypes")
    @AllArgsConstructor
    public static class EventMethod {
        public Object parentObject;
        public Class<? extends RedisEvent> eventClass;
        public Method method;
    }
}