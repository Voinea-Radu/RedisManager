package dev.lightdream.redismanager.manager;

import dev.lightdream.lambda.lambda.ArgLambdaExecutor;
import dev.lightdream.logger.Logger;
import dev.lightdream.redismanager.RedisMain;
import dev.lightdream.redismanager.annotation.RedisEventHandler;
import dev.lightdream.redismanager.event.RedisEvent;
import dev.lightdream.reflections.Reflections;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RedisEventManager {

    public List<EventObject> eventObjects = new ArrayList<>();
    @SuppressWarnings("rawtypes")
    public HashMap<
            Class<? extends RedisEvent>,
            List<ArgLambdaExecutor<? extends RedisEvent>>
            > eventHandlers = new HashMap<>();

    public RedisEventManager(RedisMain redisMain) {
        new Reflections(redisMain.getMapper())
                .getMethodsAnnotatedWith(RedisEventHandler.class)
                .forEach(method -> register(method, true));
    }

    @SuppressWarnings("rawtypes")
    public <T extends RedisEvent> void register(Class<T> clazz, ArgLambdaExecutor<T> handler) {
        List<ArgLambdaExecutor<? extends RedisEvent>> eventHandlers = this.eventHandlers.getOrDefault(clazz, new ArrayList<>());
        eventHandlers.add(handler);
        this.eventHandlers.put(clazz, eventHandlers);
    }

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

        EventObject eventObject = getEventClass(method.getDeclaringClass());
        eventObject.register(method);
    }

    public void register(Object object) {
        for (Method declaredMethod : object.getClass().getDeclaredMethods()) {
            if (!declaredMethod.isAnnotationPresent(RedisEventHandler.class)) {
                Logger.error("Method " + declaredMethod.getName() + " from class " + declaredMethod.getDeclaringClass() +
                        " is not annotated with RedisEventHandler");
                return;
            }

            register(declaredMethod, false);
        }
    }

    /**
     * @param clazz The class of the object that has the methods
     * @return EventObject
     */
    @SuppressWarnings("deprecation")
    @SneakyThrows
    private EventObject getEventClass(Class<?> clazz) {
        for (EventObject eventObject : eventObjects) {
            if (eventObject.parentObject.getClass().equals(clazz)) {
                return eventObject;
            }
        }
        EventObject eventObject = new EventObject(clazz.newInstance());
        eventObjects.add(eventObject);
        return eventObject;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void fire(RedisEvent event) {
        for (EventObject eventObject : eventObjects) {
            eventObject.fire(event);
        }
        for (ArgLambdaExecutor eventHandler :
                eventHandlers.getOrDefault(event.getClass(), new ArrayList<>())) {
            eventHandler.execute(event);
        }
    }

    public static class EventObject {
        public Object parentObject;
        public List<EventClass> eventClasses = new ArrayList<>();

        private EventObject(Object parentObject) {
            this.parentObject = parentObject;
        }

        private void register(Method method) {
            Class<?>[] params = method.getParameterTypes();

            if (params.length != 1) {
                Logger.error("Method " + method.getName() + " from class " + method.getDeclaringClass() +
                        " has more than one parameter");
                return;
            }

            Class<?> paramClass = params[0];

            if (!RedisEvent.class.isAssignableFrom(paramClass)) {
                Logger.warn("Parameter from method " + method.getName() + " from class " + method.getDeclaringClass() +
                        " is not an instance of RedisEvent");
                return;
            }

            //noinspection unchecked,rawtypes
            Class<? extends RedisEvent> clazz = (Class<? extends RedisEvent>) paramClass;

            EventClass eventClass = getEventClass(clazz);
            eventClass.register(method);
        }

        @SuppressWarnings("rawtypes")
        private EventClass getEventClass(Class<? extends RedisEvent> clazz) {
            for (EventClass eventClass : eventClasses) {
                if (eventClass.eventClass.equals(clazz)) {
                    return eventClass;
                }
            }
            EventClass eventClass = new EventClass(clazz);
            eventClasses.add(eventClass);
            return eventClass;
        }

        @SuppressWarnings("rawtypes")
        private void fire(RedisEvent event) {
            for (EventClass eventClass : eventClasses) {
                eventClass.fire(event, parentObject);
            }
        }
    }

    public static class EventClass {
        @SuppressWarnings("rawtypes")
        public Class<? extends RedisEvent> eventClass;
        public List<Method> methods = new ArrayList<>();

        @SuppressWarnings("rawtypes")
        private EventClass(Class<? extends RedisEvent> eventClass) {
            this.eventClass = eventClass;
        }

        private void register(Method method) {
            if (methods.contains(method)) {
                return;
            }
            methods.add(method);
        }

        @SuppressWarnings("rawtypes")
        private void fire(RedisEvent event, Object parentObject) {
            if (eventClass.isAssignableFrom(event.getClass())) {
                methods.sort((o1, o2) -> {
                    RedisEventHandler annotation1 = o1.getAnnotation(RedisEventHandler.class);
                    RedisEventHandler annotation2 = o2.getAnnotation(RedisEventHandler.class);
                    return annotation1.priority() - annotation2.priority();
                });
                for (Method method : methods) {
                    try {
                        method.invoke(parentObject, event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}