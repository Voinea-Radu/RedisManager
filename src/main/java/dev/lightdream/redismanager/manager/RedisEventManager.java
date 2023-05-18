package dev.lightdream.redismanager.manager;

import dev.lightdream.logger.Logger;
import dev.lightdream.redismanager.Statics;
import dev.lightdream.redismanager.annotation.RedisEventHandler;
import dev.lightdream.redismanager.event.RedisEvent;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class RedisEventManager {

    private final List<EventObject> eventObjects = new ArrayList<>();
    private final RedisManager redisManager;

    public RedisEventManager(RedisManager manager) {
        this.redisManager = manager;
        Statics.getMain().getReflections()
                .getMethodsAnnotatedWith(RedisEventHandler.class)
                .forEach(method -> register(method, true));
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

        redisManager.getDebugger().registeringMethod(method.getName(), method.getDeclaringClass().getName());

        EventObject eventObject = getEventClass(method.getDeclaringClass());
        eventObject.register(method);
    }

    public void register(Object object) {
        if (getEventObject(object.getClass()) == null) {
            EventObject eventObject = new EventObject(object);
            eventObjects.add(eventObject);
        }

        for (Method declaredMethod : object.getClass().getDeclaredMethods()) {
            if (!declaredMethod.isAnnotationPresent(RedisEventHandler.class)) {
                continue;
            }

            register(declaredMethod, false);
        }
    }

    public void unregister(Object object) {
        eventObjects.removeIf(eventObject -> eventObject.parentObject.equals(object));
    }

    @SuppressWarnings("deprecation")
    @SneakyThrows
    private EventObject getEventClass(Class<?> clazz) {
        EventObject eventObject = getEventObject(clazz);
        if (eventObject != null) {
            return eventObject;
        }

        eventObject = new EventObject(clazz.newInstance());
        eventObjects.add(eventObject);
        return eventObject;
    }

    private EventObject getEventObject(Class<?> clazz) {
        for (EventObject eventObject : eventObjects) {
            if (eventObject.parentObject.getClass().equals(clazz)) {
                return eventObject;
            }
        }
        return null;
    }

    @SuppressWarnings({"rawtypes", "unused"})
    public void fire(RedisEvent event) {
        for (EventObject eventObject : eventObjects) {
            eventObject.fire(event);
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
            Class<?> clazz = event.getClassByName();
            if (clazz == null) {
                Logger.warn("#getClassByName method failed on object " + event);
                clazz = event.getClass();
            }

            if (eventClass.isAssignableFrom(clazz)) {
                methods.sort((o1, o2) -> {
                    RedisEventHandler annotation1 = o1.getAnnotation(RedisEventHandler.class);
                    RedisEventHandler annotation2 = o2.getAnnotation(RedisEventHandler.class);
                    return annotation1.priority() - annotation2.priority();
                });
                for (Method method : methods) {
                    try {
                        method.setAccessible(true);
                        method.invoke(parentObject, eventClass.cast(event));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}