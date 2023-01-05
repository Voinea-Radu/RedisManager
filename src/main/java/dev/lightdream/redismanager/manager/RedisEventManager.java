package dev.lightdream.redismanager.manager;

import dev.lightdream.logger.Logger;
import dev.lightdream.redismanager.RedisMain;
import dev.lightdream.redismanager.annotation.RedisEventHandler;
import dev.lightdream.redismanager.event.RedisEvent;
import dev.lightdream.reflections.Reflections;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class RedisEventManager {

    public List<EventObject> eventObjects = new ArrayList<>();

    public RedisEventManager(RedisMain redisMain) {
        new Reflections(redisMain.getMapper())
                .getMethodsAnnotatedWith(RedisEventHandler.class)
                .forEach(this::register);
    }

    private void register(Method method) {
        if (!method.isAnnotationPresent(RedisEventHandler.class)) {
            Logger.error("Method " + method.getName() + " from class " + method.getDeclaringClass() +
                    " is not annotated with RedisEventHandler");
            return;
        }

        EventObject eventObject = getEventClass(method.getDeclaringClass());
        eventObject.register(method);
    }

    @Deprecated
    public void register(Object object) {
        for (Method declaredMethod : object.getClass().getDeclaredMethods()) {
            register(declaredMethod);
        }
    }

    /**
     * @param clazz The class of the object that has the methods
     * @return EventObject
     */
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

            //noinspection unchecked
            Class<? extends RedisEvent> clazz = (Class<? extends RedisEvent>) paramClass;

            EventClass eventClass = getEventClass(clazz);
            eventClass.register(method);
        }

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

        private void fire(RedisEvent event) {
            for (EventClass eventClass : eventClasses) {
                eventClass.fire(event, parentObject);
            }
        }
    }

    public static class EventClass {
        public Class<? extends RedisEvent> eventClass;
        public List<Method> methods = new ArrayList<>();

        private EventClass(Class<? extends RedisEvent> eventClass) {
            this.eventClass = eventClass;
        }

        private void register(Method method) {
            if (methods.contains(method)) {
                return;
            }
            methods.add(method);
        }

        private void fire(RedisEvent event, Object parentObject) {
            if (eventClass.isAssignableFrom(event.getClass())) {
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