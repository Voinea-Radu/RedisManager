package dev.lightdream.redismanager.common.event;

import dev.lightdream.lambda.ScheduleUtils;
import dev.lightdream.lambda.lambda.ArgLambdaExecutor;
import dev.lightdream.lambda.lambda.LambdaExecutor;
import dev.lightdream.logger.Debugger;
import dev.lightdream.logger.Logger;
import dev.lightdream.redismanager.common.RedisMain;
import dev.lightdream.redismanager.common.dto.RedisResponse;
import dev.lightdream.redismanager.common.event.impl.ResponseEvent;
import dev.lightdream.redismanager.common.manager.CommonRedisManager;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

/**
 * @param <T> The type of the response
 */
public class RedisEvent<T> {

    public int id = -1;
    public String className;
    public String originator = "UNKNOWN";
    public String redisTarget = "*";

    /**
     * @param redisTarget the redis target that will listen for this event. You can use * for all.
     */
    public RedisEvent(String redisTarget) {
        this();
        this.redisTarget = redisTarget;
    }

    public RedisEvent() {
        this.className = getClassName();
    }

    @SuppressWarnings("unchecked")
    public @Nullable Class<? extends RedisEvent<T>> getClassByName() {
        try {
            return (Class<? extends RedisEvent<T>>) Class.forName(className);
        } catch (Throwable e) {
            Logger.error("Class " + className + " was not found in the current JVM context. Please make sure" +
                    "the exact class exists in the project. If you want to have different classes in the sender and " +
                    "receiver override RedisEvent#getClassName and specify the class name there.");
            if (Debugger.isEnabled()) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public String getClassName() {
        return getClass().getName();
    }

    /**
     * Fires the event (internally)
     * Does NOT send it to the redis target
     *
     * @param main RedisMain main instance
     */
    public void fireEvent(RedisMain main) {
        main.getRedisManager().redisEventManager.fire(this);
    }

    @Override
    public String toString() {
        return CommonRedisManager.toJson(this);
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
    public void sendAndExecute(RedisMain main, ArgLambdaExecutor<RedisResponse<T>> executor) {
        ScheduleUtils.runTaskAsync((LambdaExecutor) () -> {
            RedisResponse<T> response = sendAndWait(main);
            executor.execute(response);
        });
    }

    @SuppressWarnings("unused")
    public void sendAndExecuteIfSuccessful(RedisMain main, ArgLambdaExecutor<T> executor) {
        ScheduleUtils.runTaskAsync((LambdaExecutor) () -> {
            RedisResponse<T> response = this.sendAndWait(main);

            if (response.hasTimeout()) {
                return;
            }

            executor.execute(response.getResponse());
        });
    }

    @SuppressWarnings({"unused", "UnusedReturnValue"})
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
