package dev.lightdream.redismanager.dto;

import com.google.gson.annotations.Expose;
import lombok.Getter;

import java.util.UUID;

@Getter
@SuppressWarnings("FieldMayBeFinal")
public class RedisConfig {

    private @Expose String host = "127.0.0.1";
    private @Expose int port = 6379;
    private @Expose String password = "password";
    private @Expose String channel = "channel";

    // Advanced settings
    private @Expose String redisID = UUID.randomUUID().toString();
    private @Expose int timeout = 2000; // 2s
    private @Expose int waitBeforeIteration = 50; // 50ms

}



