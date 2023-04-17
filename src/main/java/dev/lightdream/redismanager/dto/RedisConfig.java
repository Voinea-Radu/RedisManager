package dev.lightdream.redismanager.dto;

import lombok.Getter;

import java.util.UUID;

@SuppressWarnings("FieldMayBeFinal")
public class RedisConfig {

    @Getter
    private String host = "127.0.0.1";
    @Getter
    private int port = 6379;
    @Getter
    private String password = "password";
    @Getter
    private String channel = "channel";
    @Getter
    private String redisID = UUID.randomUUID().toString();

}



