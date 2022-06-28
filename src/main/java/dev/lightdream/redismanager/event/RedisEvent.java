package dev.lightdream.redismanager.event;

import dev.lightdream.redismanager.RedisMain;
import dev.lightdream.redismanager.dto.RedisResponse;
import dev.lightdream.redismanager.event.impl.ResponseEvent;
import dev.lightdream.redismanager.utils.Utils;
import lombok.SneakyThrows;

public class RedisEvent {

    public int id = -1;
    public String className;
    public String originator = "UNKNOWN";
    public String redisTarget = "*";

    /**
     * @param redisTarget the redis target that will listen for this event. You can use * for all.
     */
    public RedisEvent(String redisTarget) {
        this.className = getClass().getName();
        this.redisTarget = redisTarget;
    }

    public RedisEvent() {
        this.className = getClass().getName();
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public Class<? extends RedisEvent> getClassByName() {
        return (Class<? extends RedisEvent>) Class.forName(className);
    }

    public void fireEvent(RedisMain main) {
        main.getRedisEventManager().fire(this);
    }

    @Override
    public String toString() {
        return Utils.toJson(this);
    }

    public void respond(RedisMain main, Object response) {
        new ResponseEvent(this, response).send(main);
    }

    @SuppressWarnings("UnusedReturnValue")
    public RedisResponse send(RedisMain main) {
        return main.getRedisManager().send(this);
    }

    @SuppressWarnings("unused")
    @SneakyThrows
    public RedisResponse sendAndWait(RedisMain main) {
        return sendAndWait(main, Utils.defaultTimeout);
    }

    @SuppressWarnings("BusyWait")
    @SneakyThrows
    public RedisResponse sendAndWait(RedisMain main, int timeout) {
        int currentWait = 0;
        RedisResponse response = send(main);
        while (!response.isFinished()) {
            Thread.sleep(Utils.defaultWaitBeforeIteration);
            currentWait += Utils.defaultWaitBeforeIteration;
            if (currentWait > timeout) {
                response.timeout();
                break;
            }
        }

        return response;
    }

}
