package dev.lightdream.redismanager.dto;

import com.google.gson.annotations.Expose;
import dev.lightdream.redismanager.utils.Utils;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class RedisResponse {

    public int id;
    @Expose
    private String response;
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

    public void respond(Object object) {
        this.response = Utils.toJson(object);
        markAsFinished();
    }

    @SuppressWarnings("unused")
    public <T> T getResponse(Class<T> clazz) {
        if (response == null) {
            return null;
        }
        return Utils.fromJson(response.replace("\\\"", "\\").replace("\"", "").replace("\\", "\""), clazz);
    }

    public boolean isFinished() {
        return finished;
    }

    @Override
    public String toString() {
        return Utils.toJson(this);
    }
}
