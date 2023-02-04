package dev.lightdream.redismanager.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RedisEventHandler {

    int priority() default 0;

    boolean autoRegister() default false;


}
