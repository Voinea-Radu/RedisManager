package dev.lightdream.redismanager.event;

import dev.lightdream.lambda.ScheduleUtils;
import dev.lightdream.lambda.lambda.ArgLambdaExecutor;
import dev.lightdream.lambda.lambda.LambdaExecutor;
import dev.lightdream.logger.Debugger;
import dev.lightdream.logger.Logger;
import dev.lightdream.redismanager.Statics;
import dev.lightdream.redismanager.dto.RedisResponse;
import dev.lightdream.redismanager.event.impl.ResponseEvent;
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

    public static RedisEvent<?> deserialize(String data) {
        RedisEvent<?> inferiorRedisEvent = Statics.getMain().getGson().fromJson(data, RedisEvent.class);
        Class<? extends RedisEvent<?>> clazz = inferiorRedisEvent.getClassByName();

        if (clazz == null) {
            return null;
        }

        return Statics.getMain().getGson().fromJson(data, clazz);

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
     */
    public void fireEvent() {
        Statics.getMain().getRedisManager().redisEventManager.fire(this);
    }

    @Override
    public String toString() {
        throw new RuntimeException(getClass().getName() + "#toString has been called. Please use #serialzie instead");
    }

    public String serialize() {
        return Statics.getMain().getGson().toJson(this);
    }

    @SuppressWarnings("unused")
    public void respond(T response) {
        new ResponseEvent(this, response).send();
    }

    /**
     * Send the event through the redis manager to the target
     *
     * @return response
     */
    @SuppressWarnings("UnusedReturnValue")
    public RedisResponse<T> send() {
        return Statics.getMain().getRedisManager().send(this);
    }

    public void sendAndExecuteSync(ArgLambdaExecutor<T> success, LambdaExecutor fail) {
        RedisResponse<T> response = this.sendAndWait();

        if (response.hasTimeout()) {
            fail.execute();
            return;
        }

        success.execute(response.getResponse());
    }

    public @Nullable T sendAndGet(LambdaExecutor fail) {
        RedisResponse<T> response = this.sendAndWait();

        if (response.hasTimeout()) {
            fail.execute();
            return null;
        }

        return response.getResponse();
    }

    @SuppressWarnings("unused")
    public @Nullable T sendAndGet() {
        return sendAndGet(() -> {
        });
    }

    @SuppressWarnings("unused")
    public void sendAndExecute(ArgLambdaExecutor<T> success) {
        sendAndExecute(success, () -> {
        });
    }

    public void sendAndExecute(ArgLambdaExecutor<T> success, LambdaExecutor fail) {
        ScheduleUtils.runTaskAsync(() -> sendAndExecuteSync(success, fail));
    }

    @SuppressWarnings({"unused", "UnusedReturnValue"})
    @SneakyThrows
    public RedisResponse<T> sendAndWait() {
        return sendAndWait(Statics.getMain().getRedisConfig().getTimeout());
    }

    @SuppressWarnings("BusyWait")
    @SneakyThrows
    public RedisResponse<T> sendAndWait(int timeout) {
        int currentWait = 0;
        RedisResponse<T> response = send();
        while (!response.isFinished()) {
            Thread.sleep(Statics.getMain().getRedisConfig().getWaitBeforeIteration());
            currentWait += Statics.getMain().getRedisConfig().getWaitBeforeIteration();
            if (currentWait > timeout) {
                response.timeout();
                break;
            }
        }

        //TODO Maybe implement logic for trying again, however for now simply remove the response afterwards
        //TODO This will need to have an option in the config to enable / disable packet resending
        Statics.getMain().getRedisManager().getAwaitingResponses().remove(response);

        return response;
    }

}
