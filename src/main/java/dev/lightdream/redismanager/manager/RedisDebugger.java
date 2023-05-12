package dev.lightdream.redismanager.manager;

import dev.lightdream.logger.Debugger;
import dev.lightdream.messagebuilder.MessageBuilder;

public class RedisDebugger {

    private final MessageBuilder creatingListener = new MessageBuilder("Creating RedisManager with listenID: %id%");
    private final MessageBuilder receiveNotAllowed = new MessageBuilder("[Receive-Not-Allowed] [%channel%] HIDDEN");
    private final MessageBuilder receiveResponse = new MessageBuilder("[Receive-Response   ] [%channel%] %response%");
    private final MessageBuilder receive = new MessageBuilder("[Receive            ] [%channel%] %event%");
    private final MessageBuilder subscribed = new MessageBuilder("Subscribed to channel %channel%");
    private final MessageBuilder unsubscribed = new MessageBuilder("Unsubscribed to channel %channel%");
    private final MessageBuilder sendResponse = new MessageBuilder("[Send-Response      ] [%channel%] %response%");
    private final MessageBuilder send = new MessageBuilder("[Send               ] [%channel%] %event%");
    private final MessageBuilder registeringMethod = new MessageBuilder("Registering method %method% from class %class%");
    private boolean enabled;

    public RedisDebugger(boolean enabled) {
        this.enabled = enabled;
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void creatingListener(String id) {
        print(creatingListener
                .parse("id", id)
                .parse());
    }

    public void receiveNotAllowed(String channel) {
        print(receiveNotAllowed
                .parse("channel", channel)
                .parse());
    }

    public void receiveResponse(String channel, String response) {
        print(receiveResponse
                .parse("channel", channel)
                .parse("response", response)
                .parse());
    }

    public void receive(String channel, String event) {
        print(receive
                .parse("channel", channel)
                .parse("event", event)
                .parse());
    }

    public void subscribed(String channel) {
        print(subscribed
                .parse("channel", channel)
                .parse());
    }

    public void unsubscribed(String channel) {
        print(unsubscribed
                .parse("channel", channel)
                .parse());
    }

    public void sendResponse(String channel, String response) {
        print(sendResponse
                .parse("channel", channel)
                .parse("response", response)
                .parse());
    }

    public void send(String channel, String event) {
        print(send
                .parse("channel", channel)
                .parse("event", event)
                .parse());
    }

    public void registeringMethod(String method, String clazz) {
        print(registeringMethod
                .parse("method", method)
                .parse("class", clazz)
                .parse());
    }


    private void print(String message) {
        if (!enabled) {
            return;
        }
        Debugger.log(message);
    }

}
