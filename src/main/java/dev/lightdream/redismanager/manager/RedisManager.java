package dev.lightdream.redismanager.manager;

import dev.lightdream.lambda.LambdaExecutor;
import dev.lightdream.logger.Debugger;
import dev.lightdream.logger.Logger;
import dev.lightdream.redismanager.RedisMain;
import dev.lightdream.redismanager.dto.RedisResponse;
import dev.lightdream.redismanager.event.RedisEvent;
import dev.lightdream.redismanager.event.impl.ResponseEvent;
import dev.lightdream.redismanager.utils.Utils;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.List;

public class RedisManager {

    private final Jedis subscriberJedis;
    private final List<RedisResponse<?>> awaitingResponses = new ArrayList<>();
    private final RedisMain main;
    public Jedis jedis;
    private JedisPubSub subscriberJedisPubSub;
    private int id = 0;
    private boolean disabledDebug = false;

    public RedisManager(RedisMain main) {
        this.main = main;
        debug("Creating RedisManager with listenID: " + main.getRedisID());
        this.jedis = new Jedis(main.getRedisConfig().host, main.getRedisConfig().port);
        this.jedis.auth(main.getRedisConfig().username, main.getRedisConfig().password);

        this.subscriberJedis = new Jedis(main.getRedisConfig().host, main.getRedisConfig().port);
        this.subscriberJedis.auth(main.getRedisConfig().username, main.getRedisConfig().password);

        subscribe();
    }

    public void disableDebugMessage(){
        disabledDebug = true;
    }

    private void debug(String s){
        if(!disabledDebug){
            Debugger.info(s);
        }
    }

    @Nullable
    private RedisResponse<?> getResponse(ResponseEvent command) {
        return awaitingResponses.stream().filter(response -> response.id == command.id).findAny().orElse(null);
    }

    private void subscribe() {
        subscriberJedisPubSub = new JedisPubSub() {

            @SuppressWarnings("unchecked")
            @Override
            public void onMessage(String channel, final String command) {
                Class<? extends RedisEvent<?>> clazz = Utils.fromJson(command, RedisEvent.class).getClassByName();

                if (clazz.equals(ResponseEvent.class)) {
                    ResponseEvent responseEvent = Utils.fromJson(command, ResponseEvent.class);
                    if (!responseEvent.redisTarget.equals(main.getRedisID())) {
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

                RedisEvent<?> redisEvent = Utils.fromJson(command, clazz);
                if (!redisEvent.redisTarget.equals(main.getRedisID())) {
                    debug("[Receive-Not-Allowed] [" + channel + "] HIDDEN");
                    return;
                }
                debug("[Receive            ] [" + channel + "] " + command);
                redisEvent.fireEvent(main);
            }

            @Override
            public void onSubscribe(String channel, int subscribedChannels) {
                debug("Subscribed to channel " + channel);
            }

            @Override
            public void onUnsubscribe(String channel, int subscribedChannels) {
                debug("Unsubscribed from channel " + channel);
            }

        };


        startRedisThread();
    }

    public void startRedisThread() {
        new Thread(() -> {
            try {
                subscriberJedis.subscribe(subscriberJedisPubSub, main.getRedisConfig().channel);
            } catch (Exception e) {
                LambdaExecutor.LambdaCatch.NoReturnLambdaCatch.executeCatch(() -> {
                    Logger.error("Lost connection to redis server. Retrying in 3 seconds...");
                    if(Debugger.isEnabled()){
                        e.printStackTrace();
                    }
                    Thread.sleep(3000);
                    Logger.good("Reconnected to redis server.");
                    startRedisThread();
                });
            }
        }).start();
    }

    @SuppressWarnings("unused")
    public void unsubscribe() {
        subscriberJedisPubSub.unsubscribe();
    }

    public <T> RedisResponse<T> send(RedisEvent<T> command) {
        command.originator = main.getRedisID();

        if (command instanceof ResponseEvent) {
            debug("[Send-Response      ] [" + main.getRedisConfig().channel + "] " + command);

            jedis.publish(main.getRedisConfig().channel, command.toString());
            return null;
        }

        command.id = ++id;
        debug("[Send               ] [" + main.getRedisConfig().channel + "] " + command);

        RedisResponse<T> redisResponse = new RedisResponse<>(command.id);
        jedis.publish(main.getRedisConfig().channel, command.toString());

        awaitingResponses.add(redisResponse);

        return redisResponse;
    }


}