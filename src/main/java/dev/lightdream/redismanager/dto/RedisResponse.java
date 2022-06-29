package dev.lightdream.redismanager.dto;

import com.google.gson.annotations.Expose;
import dev.lightdream.redismanager.utils.Utils;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor
public class RedisResponse<T> {

    public int id;
    @Expose
    private String response;
    @Expose
    private String responseClass;
    private boolean finished = false;
    private boolean timeout = false;


    public RedisResponse(int id) {
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

    public void respond(T object) {
        this.response = Utils.toJson(object);
        this.responseClass = object.getClass().getName();
        markAsFinished();
    }

    @SuppressWarnings("unchecked")
    public void respondUnsafe(Object object) {
        respond((T) object);
    }

    @SuppressWarnings("unused")
    public <E> E getResponse(Class<E> clazz) {
        if (response == null) {
            return null;
        }
        return Utils.fromJson(response.replace("\\\"", "\\").replace("\"", "").replace("\\", "\""), clazz);
    }

    public T getResponse() {
        if (response == null) {
            return null;
        }
        return Utils.fromJson(response.replace("\\\"", "\\").replace("\"", "").replace("\\", "\""), getResponseClass());
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public Class<T> getResponseClass() {
        if (responseClass == null) {
            return null;
        }
        return (Class<T>) Class.forName(responseClass);
    }

    public boolean isFinished() {
        return finished;
    }

    @Override
    public String toString() {
        return Utils.toJson(this);
    }
}
