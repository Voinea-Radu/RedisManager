package dev.lightdream.redismanager.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Utils {

    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }


}
