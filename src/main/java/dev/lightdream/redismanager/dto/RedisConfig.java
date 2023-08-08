package dev.lightdream.redismanager.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@SuppressWarnings("FieldMayBeFinal")
public class RedisConfig {

    private String host = "127.0.0.1";
    private int port = 6379;
    private String password = "password";
    private String channelBase = "channel";

    // Advanced settings
    private String redisID = UUID.randomUUID().toString();
    private int timeout = 2000; // 2s
    private int waitBeforeIteration = 50; // 50ms

    public String getChannel() {
        return channelBase + "#" + redisID;
    }
}



