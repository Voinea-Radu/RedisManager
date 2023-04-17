package dev.lightdream.redismanager.dto;

import lombok.Getter;

import java.util.UUID;

@Getter
@SuppressWarnings("FieldMayBeFinal")
public class RedisConfig {

    private String host = "127.0.0.1";
    private int port = 6379;
    private String password = "password";
    private String channel = "channel";

    // Advanced settings
    private String redisID = UUID.randomUUID().toString();
    private int timeout = 2000; // 2s
    private int waitBeforeIteration = 50; // 50ms

}



