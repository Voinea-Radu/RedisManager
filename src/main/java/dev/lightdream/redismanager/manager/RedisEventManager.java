package dev.lightdream.redismanager.manager;

import dev.lightdream.redismanager.annotation.RedisEventHandler;
import dev.lightdream.redismanager.event.RedisEvent;
import dev.lightdream.redismanager.utils.ReflectionHelper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class RedisEventManager {

    private final List<EventMapper> eventMappers = new ArrayList<>();

    public RedisEventManager() {

    }

    public void register(Object object) {
        getEventMapper(object).register();
    }

    public EventMapper getEventMapper(Object object) {
        for (EventMapper eventMapper : eventMappers) {
            if (eventMapper.object == object) {
                return eventMapper;
            }
        }

        EventMapper eventMapper = new EventMapper(object);
        eventMappers.add(eventMapper);
        return eventMapper;
    }

    public void fire(RedisEvent event) {
        for (EventMapper eventMapper : eventMappers) {
            eventMapper.fire(event);
        }
    }

    public static class EventMapper {

        private final Object object;
        private final List<EventMethod> eventMethods;

        public EventMapper(Object object) {
            this.object = object;
            this.eventMethods = new ArrayList<>();
        }

        public void register() {
            ReflectionHelper.getMethodsAnnotatedWith(object.getClass(), RedisEventHandler.class).forEach(method -> {
                if (method.getParameters().length == 0) {
                    return;
                }

                Class<?> eventClassUnchecked = method.getParameters()[0].getType();
                Class<? extends RedisEvent> eventClass;

                try {
                    //noinspection unchecked
                    eventClass = (Class<? extends RedisEvent>) eventClassUnchecked;
                } catch (Exception e) {
                    return;
                }

                getEventMethod(eventClass).addMethod(method);
            });
        }

        public EventMethod getEventMethod(Class<? extends RedisEvent> clazz) {
            for (EventMethod eventMethod : eventMethods) {
                if (eventMethod.clazz.equals(clazz)) {
                    return eventMethod;
                }
            }

            EventMethod method = new EventMethod(clazz);
            eventMethods.add(method);
            return method;
        }

        public void fire(RedisEvent event) {
            getEventMethod(event.getClass()).fire(object, event);
        }

        @Override
        public String toString() {
            return "EventMapper{" +
                    "object=" + object +
                    ", eventMethods=" + eventMethods +
                    '}';
        }
    }

    public static class EventMethod {

        private final Class<? extends RedisEvent> clazz;
        private final List<Method> methods;

        public EventMethod(Class<? extends RedisEvent> clazz) {
            this.clazz = clazz;
            this.methods = new ArrayList<>();
        }

        public void addMethod(Method method) {
            methods.add(method);
        }

        public void fire(Object object, RedisEvent event) {
            sort();
            for (Method method : methods) {
                try {
                    method.invoke(object, event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void sort() {
            methods.sort((o1, o2) -> {
                int i1 = -o1.getAnnotation(RedisEventHandler.class).priority();
                int i2 = -o2.getAnnotation(RedisEventHandler.class).priority();
                return i1 - i2;
            });
        }

        @Override
        public String toString() {
            return "EventMethod{" +
                    "clazz=" + clazz +
                    ", methods=" + methods +
                    '}';
        }
    }


}
