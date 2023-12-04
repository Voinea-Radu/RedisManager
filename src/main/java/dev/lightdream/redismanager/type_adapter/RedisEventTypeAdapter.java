package dev.lightdream.redismanager.type_adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import dev.lightdream.filemanager.GsonSerializer;
import dev.lightdream.logger.Debugger;
import dev.lightdream.logger.Logger;
import dev.lightdream.redismanager.event.RedisEvent;

import java.lang.reflect.Type;

@SuppressWarnings("rawtypes")
public class RedisEventTypeAdapter implements GsonSerializer<RedisEvent> {

    @Override
    public Class<RedisEvent> getClazz() {
        return RedisEvent.class;
    }

    @Override
    public RedisEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        try {
            boolean __RedisEventTypeAdapter = json.getAsJsonObject().has("__RedisEventTypeAdapter#deserialize");
            String className = json.getAsJsonObject().get("className").getAsString();

            if (!__RedisEventTypeAdapter && !className.equals(RedisEvent.class.getName())) {
                Class<? extends RedisEvent<?>> clazz;

                try {
                    //noinspection unchecked
                    clazz = (Class<? extends RedisEvent<?>>) Class.forName(className);
                } catch (Throwable e) {
                    Logger.error("Class " + className + " was not found in the current JVM context. Please make sure" +
                            "the exact class exists in the project. If you want to have different classes in the sender and " +
                            "receiver override RedisEvent#getClassName and specify the class name there.");
                    if (Debugger.isEnabled()) {
                        //noinspection CallToPrintStackTrace
                        e.printStackTrace();
                    }
                    return null;
                }

                JsonObject object = json.getAsJsonObject();
                object.addProperty("__RedisEventTypeAdapter#deserialize", true);
                return context.deserialize(json, clazz);
            }

            long id = json.getAsJsonObject().get("id").getAsLong();
            String originator = json.getAsJsonObject().get("originator").getAsString();
            String redisTarget = json.getAsJsonObject().get("redisTarget").getAsString();

            return new RedisEvent(className, id, originator, redisTarget);
        } catch (Exception e) {
            Logger.error("Error while deserializing RedisEvent");
            Logger.error("Json:");
            Logger.error(json.toString());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public JsonElement serialize(RedisEvent src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();

        object.addProperty("className", src.getClassName());
        object.addProperty("id", src.getId());
        object.addProperty("originator", src.getOriginator());
        object.addProperty("redisTarget", src.getRedisTarget());

        return object;
    }
}
