package dev.lightdream.redismanager.common.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.lightdream.lambda.lambda.ArgLambdaExecutor;
import dev.lightdream.logger.Debugger;
import dev.lightdream.logger.Logger;
import dev.lightdream.redismanager.common.RedisMain;
import dev.lightdream.redismanager.common.dto.RedisResponse;
import dev.lightdream.redismanager.common.event.RedisEvent;
import dev.lightdream.redismanager.common.event.impl.ResponseEvent;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class CommonRedisManager {
    @Getter
    @Setter
    private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    private @Getter final Queue<RedisResponse<?>> awaitingResponses = new ConcurrentLinkedQueue<>();
    public RedisEventManager redisEventManager;
    private boolean debug = false;
    private @Getter final RedisMain main;
    private int id = 0;

    private final RedisPlatform redisPlatform;

    public CommonRedisManager(RedisMain main) {
        this.main=main;
        redisEventManager = new RedisEventManager(main);
        debug("Creating RedisManager with listenID: " + main.getRedisConfig().redisID);

        redisPlatform = getRedisPlatform();
    }

    protected abstract RedisPlatform getRedisPlatform();

    public static String toJson(Object object) {
        return getGson().toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> type) {
        return getGson().fromJson(json, type);
    }

    @SuppressWarnings("unused")
    public void register(Object listener) {
        redisEventManager.register(listener);
    }

    @SuppressWarnings({"rawtypes", "unused"})
    public <E extends RedisEvent> void register(Class<E> clazz, ArgLambdaExecutor<E> method) {
        redisEventManager.register(clazz, method);
    }

    @SuppressWarnings("unused")
    public void enableDebugMessage() {
        debug = true;
    }

    public boolean isDebugEnabled() {
        return debug;
    }

    private void debug(String s) {
        if (debug) {
            Debugger.info(s);
        }
    }

    @SuppressWarnings("rawtypes")
    @Nullable
    protected RedisResponse<?> getResponse(ResponseEvent command) {
        //Remove streams, these are slow when called a lot
        for (RedisResponse response : awaitingResponses) {
            if (response.id == command.id) {
                return response;
            }
        }

        return null;
    }

    public <T> RedisResponse<T> send(RedisEvent<T> command) {
        command.originator = main.getRedisConfig().redisID;

        if (command instanceof ResponseEvent) {
            debug("[Send-Response      ] [" + main.getRedisConfig().channel + "] " + command);
            redisPlatform.send(main.getRedisConfig().channel, command.toString());

            return null;
        }

        command.id = ++id;
        debug("[Send               ] [" + main.getRedisConfig().channel + "] " + command);

        RedisResponse<T> redisResponse = new RedisResponse<>(command.id);
        getAwaitingResponses().add(redisResponse);

        redisPlatform.send(main.getRedisConfig().channel, command.toString());

        return redisResponse;
    }

    @SuppressWarnings("unchecked")
    protected void onMessageReceive(String channel, final String command) {
        if (command.trim().length() == 0) {
            return;
        }

        Class<? extends RedisEvent<?>> clazz = fromJson(command, RedisEvent.class).getClassByName();

        if (clazz == null) {
            Logger.error("An error occurred while creating the class instance of the RedisEvent. " +
                    "Please refer to the error above if there is any.");
            return;
        }

        if (clazz.equals(ResponseEvent.class)) {
            ResponseEvent responseEvent = fromJson(command, ResponseEvent.class);
            if (!checkRedisTarget(responseEvent.redisTarget)) {
                debug("[Receive-Not-Allowed] [" + channel + "] HIDDEN");
                return;
            }

            debug("[Receive-Response   ] [" + channel + "] " + command);
            RedisResponse<?> response = getResponse(responseEvent);
            if (response == null) {
                return;
            }
            response.respondUnsafe(responseEvent.response, responseEvent.responseClassName);

            return;
        }

        new Thread(() -> {
            RedisEvent<?> redisEvent = fromJson(command, clazz);
            if (checkRedisTarget(redisEvent.redisTarget)) {
                debug("[Receive-Not-Allowed] [" + channel + "] HIDDEN");
                return;
            }
            debug("[Receive            ] [" + channel + "] " + command);
            redisEvent.fireEvent(main);
        }).start();
    }

    private boolean checkRedisTarget(String redisTarget) {
        return main.getRedisConfig().redisID.matches(redisTarget);
    }

    public void disconnect(){
        redisPlatform.disconnect();
    }

}