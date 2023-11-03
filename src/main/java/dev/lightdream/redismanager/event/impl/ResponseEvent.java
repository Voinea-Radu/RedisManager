package dev.lightdream.redismanager.event.impl;

import com.google.gson.reflect.TypeToken;
import dev.lightdream.redismanager.Statics;
import dev.lightdream.redismanager.event.RedisEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
public class ResponseEvent extends RedisEvent<Object> {

    private static final String EMPTY_LIST = "EMPTY_LIST";

    private String response;
    private String responseClassName;
    private String additionalData;

    public ResponseEvent(RedisEvent<?> command, Object response) {
        super(command.getOriginator());
        this.setId(command.getId());

        if (response == null) {
            this.response = "";
            this.responseClassName = "";
            return;
        }

        this.response = Statics.getMain().getGson().toJson(response);
        this.responseClassName = response.getClass().getName();

        if (response.getClass().isAssignableFrom(List.class)) {
            ArrayList<?> list = (ArrayList<?>) response;

            if (list.isEmpty()) {
                additionalData = EMPTY_LIST; // TODO To static storage
                return;
            }

            additionalData = list.get(0).getClass().getName();
        }
    }

    @SneakyThrows(value = {ClassNotFoundException.class})
    public Object deserialize() {
        Class<?> clazz = Class.forName(responseClassName);

        if (clazz.isAssignableFrom(List.class)) {
            if (additionalData.equals(EMPTY_LIST)) {
                return new ArrayList<>();
            }

            Class<?> aditionalClass = Class.forName(additionalData);

            return Statics.getMain().getGson().fromJson(response, TypeToken.getParameterized(List.class, aditionalClass));
        }

        return Statics.getMain().getGson().fromJson(response, clazz);
    }

    @SneakyThrows(value = {ClassNotFoundException.class})
    public Class<?> getResponseClass() {
        return Class.forName(responseClassName);
    }

}
