package dev.lightdream.redismanager.event;

import dev.lightdream.lambda.ScheduleManager;
import dev.lightdream.lambda.lambda.ArgLambdaExecutor;
import dev.lightdream.lambda.lambda.LambdaExecutor;
import dev.lightdream.redismanager.RedisMain;
import dev.lightdream.redismanager.Statics;
import dev.lightdream.redismanager.dto.RedisResponse;
import dev.lightdream.redismanager.event.impl.ResponseEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

/**
 * @param <T> The type of the response
 */
@Getter
@AllArgsConstructor
public class RedisEvent<T> {

    private final String className;
    private @Setter long id = -1;
    private @Setter String originator = "UNKNOWN";
    private String redisTarget;

    /**
     * @param redisID the redis target that will listen for this event. You can use * for all.
     */
    public RedisEvent(String redisID) {
        this();
        setRedisTarget(redisID);
    }

    public RedisEvent() {
        setRedisTarget("*");
        this.className = getClass().getName();
    }

    public static RedisEvent<?> deserialize(String data) {
        return Statics.getMain().getGson().fromJson(data, RedisEvent.class);
    }

    public void setRedisTarget(String redisID) {
        if (redisID.contains("#")) {
            this.redisTarget = redisID;
        } else {
            this.redisTarget = RedisMain.getRedisMain().getRedisConfig().getChannelBase() + "#" + redisID;
        }
    }

    /**
     * Fires the event (internally)
     * Does NOT send it to the redis target
     */
    public void fireEvent() {
        Statics.getMain().getRedisManager().getRedisEventManager().fire(this);
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
        ScheduleManager.get().runTaskAsync(() -> sendAndExecuteSync(success, fail));
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

    @SuppressWarnings("unused")
    public String getRedisTargetID() {
        String[] split = redisTarget.split("#");
        return split[split.length - 1];
    }

    @SuppressWarnings("unused")
    public String getOriginatorID() {
        String[] split = originator.split("#");
        return split[split.length - 1];
    }

}
