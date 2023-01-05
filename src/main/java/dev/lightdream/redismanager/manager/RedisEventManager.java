package dev.lightdream.redismanager.manager;

import dev.lightdream.logger.Debugger;
import dev.lightdream.logger.Logger;
import dev.lightdream.redismanager.RedisMain;
import dev.lightdream.redismanager.annotation.RedisEventHandler;
import dev.lightdream.redismanager.event.RedisEvent;
import dev.lightdream.reflections.Reflections;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
public class RedisEventManager {

    private final List<EventMapper> eventMappers = new ArrayList<>();

    public RedisEventManager(RedisMain redisMain) {
        new Reflections(redisMain.getMapper())
                .getMethodsAnnotatedWith(RedisEventHandler.class)
                .forEach(this::register);
    }

    @SuppressWarnings("unused")
    public void register(Object object) {
        getEventMapper(object).register();
    }

    public void register(Method method) {
        getEventMapper(method.getDeclaringClass()).register(method);
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

    public EventMapper getEventMapper(Class<?> clazz) {
        for (EventMapper eventMapper : eventMappers) {
            if (eventMapper.clazz == clazz) {
                return eventMapper;
            }
        }

        EventMapper eventMapper = new EventMapper(clazz);
        eventMappers.add(eventMapper);
        return eventMapper;
    }

    public void fire(RedisEvent event) {
        for (EventMapper eventMapper : eventMappers) {
            eventMapper.fire(event);
        }
    }

    public static class EventMapper {

        private final Class<? extends RedisEvent> clazz;
        private final Object object;
        private final List<EventMethod> eventMethods;

        public EventMapper(Object object) {
            //noinspection unchecked
            this.clazz = (Class<? extends RedisEvent>) object.getClass();
            this.object = object;
            this.eventMethods = new ArrayList<>();
        }

        public void register(Method method) {
            if (!method.isAnnotationPresent(RedisEventHandler.class)) {
                Logger.warn("Method " + method.getName() + " is not annotated with RedisEventHandler");
                return;
            }

            Parameter[] parameters = method.getParameters();

            if (parameters.length == 0) {
                Logger.error("Method " + method.getName() + " in class "
                        + method.getDeclaringClass().getName() + " has no parameters!");
                return;
            }

            Class<?> clazz = parameters[0].getType();

            if (!RedisEvent.class.isAssignableFrom(clazz)) {
                Logger.error("Method " + method.getName() + " in class "
                        + method.getDeclaringClass().getName() + " has the parameter not of type RedisEvent!");
                return;
            }

            Debugger.log("Registered method " + method.getName() + " in class "
                    + method.getDeclaringClass().getName() + " for event " + clazz.getName());

            //noinspection unchecked
            Class<? extends RedisEvent> eventClass = (Class<? extends RedisEvent>) parameters[0].getType();

            EventMethod eventMethod = getEventMethod(eventClass);
            eventMethod.addMethod(method);
        }

        public void register() {
            for (Method method : object.getClass().getMethods()) {
                if (method.isAnnotationPresent(RedisEventHandler.class)) {
                    if (method.getParameters().length == 0) {
                        return;
                    }

                    Class<?> eventClassUnchecked = method.getParameters()[0].getType();
                    Class<? extends RedisEvent> eventClass;

                    try {
                        //noinspection unchecked
                        eventClass = (Class<? extends RedisEvent>) eventClassUnchecked;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }

                    getEventMethod(eventClass).addMethod(method);
                }
            }
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

        private void addMethod(Method method) {
            methods.add(method);
        }

        public void fire(Object object, RedisEvent event) {
            sort();
            for (Method method : methods) {
                if (!method.getDeclaringClass().equals(object.getClass())) {
                    continue;
                }
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
    }


}