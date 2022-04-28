package dev.lightdream.redismanager.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Utils {

    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    public static int defaultTimeout = 2 * 1000;       // 2 seconds    (2000 milliseconds)
    public static int defaultWaitBeforeIteration = 50; // 0.05 seconds (50 milliseconds  )


    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }


}
