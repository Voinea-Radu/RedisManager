package dev.lightdream.redismanager.dto;

import dev.lightdream.redismanager.manager.RedisManager;

public interface ISerializable {

    default String serialize() {
        return RedisManager.instance().gsonSettings().gson().toJson(this);
    }

    String toString();

}
