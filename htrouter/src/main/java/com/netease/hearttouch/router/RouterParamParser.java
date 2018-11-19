package com.netease.hearttouch.router;

import android.content.Intent;

import com.netease.hearttouch.router.parser.BooleanStringParser;
import com.netease.hearttouch.router.parser.IStringParser;
import com.netease.hearttouch.router.parser.IntStringParser;
import com.netease.hearttouch.router.parser.LongStringParser;
import com.netease.hearttouch.router.parser.MapStringParser;
import com.netease.hearttouch.router.parser.ObjectStringParser;
import com.netease.hearttouch.router.parser.StringStringParser;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zyl06 on 10/10/16.
 */
public class RouterParamParser {

    private static final String TAG = "HTRouter";

    /////////////////////////////////////////////
    // Router 参数解析相关
    /////////////////////////////////////////////
    public static int getIntRouterParam(Intent intent, String key, int defaultValue) {
        return getRouterParam(intent, key, defaultValue, Integer.TYPE, IntStringParser.getInstance());
    }

    public static long getLongRouterParam(Intent intent, String key, long defaultValue) {
        return getRouterParam(intent, key, defaultValue, Long.TYPE, LongStringParser.getInstance());
    }

    public static String getStringRouterParam(Intent intent, String key, String defaultValue) {
        return getRouterParam(intent, key, defaultValue, String.class, StringStringParser.getInstance());
    }

    public static Boolean getBooleanRouterParam(Intent intent, String key, Boolean defaultValue) {
        return getRouterParam(intent, key, defaultValue, Boolean.class, BooleanStringParser.getInstance());
    }

    public static Map<String, String> getMapRouterParam(Intent intent, String key, Map<String, String> defaultValue) {
        return getRouterParam(intent, key, defaultValue, Map.class, MapStringParser.getInstance());
    }

    public static <T> T getJsonObjRouterParam(Intent intent, String key, T defaultValue, Class<T> clazz) {
        return getRouterParam(intent, key, defaultValue, clazz, new ObjectStringParser<T>());
    }

    public static <T> List<T> getJsonArrayRouterParam(Intent intent, String key, List<T> defaultValue, Class<T> clazz) {
        if (key == null) {
            return defaultValue;
        }

        String value = getRouterParam(intent, key);
        if (value == null) {
            return defaultValue;
        }

        try {
            IStringParser<T> parser = new ObjectStringParser<T>();
            return parser.parseArray(value, clazz, defaultValue);
        } catch (Exception e) {

        }

        return defaultValue;
    }

    private static <T> T getRouterParam(Intent intent, String key, T defaultValue, Class<T> clazz, IStringParser<T> parser) {
        if (key == null) {
            return defaultValue;
        }

        String value = getRouterParam(intent, key);
        if (value == null) {
            return defaultValue;
        }

        try {
            return parser.parse(value, clazz, defaultValue);
        } catch (Exception e) {

        }

        return defaultValue;
    }

    private static String getRouterParam(Intent intent, String key) {
        if (intent == null || key == null) {
            return null;
        }

        //获取URL参数
        Map<String, String> params = null;
        try {
            params = (HashMap<String, String>) intent.getSerializableExtra(HTRouterManager.HT_URL_PARAMS_KEY);
        } catch (Exception e) {

        }

        return params != null ? params.get(key) : null;
    }

    public static Map<String, String> getRouterParamsMap(Intent intent) {
        Map<String, String> result = new HashMap<>();

        if (intent == null) {
            return result;
        }

        //获取URL参数
        Map<String, String> params = (HashMap<String, String>) intent.getSerializableExtra(HTRouterManager.HT_URL_PARAMS_KEY);
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String value = entry.getValue();
                try {
                    value = URLDecoder.decode(entry.getValue(), "UTF-8");
                } catch (UnsupportedEncodingException e) {

                }
                result.put(entry.getKey(), value);
            }
        }

        return result;
    }

    public static boolean hasParam(Intent intent, String key) {
        if (intent == null || key == null) {
            return false;
        }

        //获取URL参数
        Map<String, String> params = (HashMap<String, String>) intent.getSerializableExtra(HTRouterManager.HT_URL_PARAMS_KEY);
        return params != null && params.containsKey(key);
    }
}
