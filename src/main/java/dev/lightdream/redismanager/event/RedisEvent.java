package dev.lightdream.redismanager.event;

import dev.lightdream.redismanager.RedisMain;
import dev.lightdream.redismanager.dto.RedisResponse;
import dev.lightdream.redismanager.event.impl.ResponseEvent;
import dev.lightdream.redismanager.utils.Utils;
import lombok.SneakyThrows;

public class RedisEvent<T> {

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
    public Class<? extends RedisEvent<T>> getClassByName() {
        return (Class<? extends RedisEvent<T>>) Class.forName(className);
    }

    /**
     * Fires the event (internally)
     * Does NOT send it to the redis target
     *
     * @param main RedisMain main instance
     */
    public void fireEvent(RedisMain main) {
        main.getRedisEventManager().fire(this);
    }

    @Override
    public String toString() {
        return Utils.toJson(this);
    }

    @SuppressWarnings("unused")
    public void respond(RedisMain main, T response) {
        new ResponseEvent(this, response).send(main);
    }

    /**
     * Send the event through the redis manager to the target
     *
     * @param main RedisMain main instance
     * @return response
     */
    @SuppressWarnings("UnusedReturnValue")
    public RedisResponse<T> send(RedisMain main) {
        return main.getRedisManager().send(this);
    }

    @SuppressWarnings("unused")
    @SneakyThrows
    public RedisResponse<T> sendAndWait(RedisMain main) {
        return sendAndWait(main, main.getTimeout());
    }

    @SuppressWarnings("BusyWait")
    @SneakyThrows
    public RedisResponse<T> sendAndWait(RedisMain main, int timeout) {
        int currentWait = 0;
        RedisResponse<T> response = send(main);
        while (!response.isFinished()) {
            Thread.sleep(main.getWaitBeforeIteration());
            currentWait += main.getWaitBeforeIteration();
            if (currentWait > timeout) {
                response.timeout();
                break;
            }
        }

        //TODO: Maybe implement logic for trying again, however for now simply remove the response afterwards
        main.getRedisManager().getAwaitingResponses().remove(response);

        return response;
    }

}
