package dev.lightdream.redismanager.dto;

import dev.lightdream.redismanager.Statics;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor
@Getter
public class RedisResponse<T> {

    private long id;
    private T response;
    private String responseClassName;
    private boolean finished = false;
    private boolean timeout = false;

    public RedisResponse(long id) {
        this.id = id;
    }

    public void markAsFinished() {
        finished = true;
    }

    public void timeout() {
        timeout = true;
    }

    @SuppressWarnings("unused")
    public boolean hasTimeout() {
        return timeout;
    }

    public void respond(T object, String responseClass) {
        this.response = object;
        this.responseClassName = responseClass;
        markAsFinished();
    }

    public void respondUnsafe(String objectJson, String responseClass) {
        this.responseClassName = responseClass;
        T object;

        if (objectJson.isEmpty() || responseClass.isEmpty()) {
            object = null;
        } else {
            object = Statics.getMain().getGson().fromJson(objectJson, getResponseClassName());
        }

        respond(object, responseClass);
    }

    @SuppressWarnings("unused")
    public T getResponse() {
        return response;
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public Class<T> getResponseClassName() {
        if (responseClassName == null) {
            return null;
        }
        return (Class<T>) Class.forName(responseClassName);
    }

    @Override
    public String toString() {
        throw new RuntimeException(getClass().getName() + "#toString has been called. Please use #serialzie instead");
    }

}
