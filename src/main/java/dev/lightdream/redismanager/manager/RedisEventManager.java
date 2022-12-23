package dev.lightdream.redismanager.manager;

import dev.lightdream.lambda.reflection.Reflector;
import dev.lightdream.redismanager.RedisMain;
import dev.lightdream.redismanager.annotation.RedisEventHandler;
import dev.lightdream.redismanager.event.RedisEvent;
import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class RedisEventManager {

    public List<EventMapper> eventMappers = new ArrayList<>();

    public RedisEventManager(RedisMain main) {
        new Reflector(main, main.getPackage())
                .getMethodsAnnotatedWith(RedisEventHandler.class)
                .forEach(this::register);
    }

    @SneakyThrows
    public void register(Method method) {
        EventMapper mapper = null;

        for (EventMapper eventMapper : eventMappers) {
            if (eventMapper.object.getClass().equals(method.getDeclaringClass())) {
                mapper = eventMapper;
                break;
            }
        }

        if (mapper == null) {
            mapper = new EventMapper(method.getDeclaringClass().newInstance());
            eventMappers.add(mapper);
        }

        mapper.register(method);
    }


    public void fire(RedisEvent event) {
        for (EventMapper eventMapper : eventMappers) {
            eventMapper.fire(event);
        }
    }

    public static class EventMapper {
        private final Object object;
        private final List<EventMethod> eventMethods = new ArrayList<>();

        public EventMapper(Object object) {
            this.object = object;
        }

        public void register(Method method) {
            if (method.getParameters().length == 0) {
                return;
            }

            Class<?> eventClassUnchecked = method.getParameters()[0].getType();

            if (RedisEvent.class.isAssignableFrom(eventClassUnchecked)) {
                return;
            }

            //noinspection unchecked
            Class<? extends RedisEvent> eventClass = (Class<? extends RedisEvent>) eventClassUnchecked;

            getEventMethod(eventClass).addMethod(method);
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
