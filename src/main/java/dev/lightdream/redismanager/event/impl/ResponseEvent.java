package dev.lightdream.redismanager.event.impl;

import dev.lightdream.redismanager.Statics;
import dev.lightdream.redismanager.event.RedisEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ResponseEvent extends RedisEvent<Object> {

    private String response;
    private String responseClassName;

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
    }

}
