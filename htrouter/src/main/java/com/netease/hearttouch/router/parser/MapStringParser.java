package com.netease.hearttouch.router.parser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by zyl06 on 4/13/2017.
 */

public class MapStringParser extends IStringParser<Map> {

    private static MapStringParser sInstance = null;

    public static MapStringParser getInstance() {
        if (sInstance == null) {
            synchronized (MapStringParser.class) {
                if (sInstance == null) {
                    sInstance = new MapStringParser();
                }
            }
        }
        return sInstance;
    }

    private MapStringParser() {
    }

    @Override
    public Map parse(String str, Class<Map> clazz, Map defaultValue) {
        if (str == null) {
            return defaultValue;
        }

        try {
            JSONObject jsonObject = JSON.parseObject(decode(str), Feature.IgnoreNotMatch);
            if (jsonObject == null) {
                return defaultValue;
            }

            Map<String, String> result = new HashMap<>();
            Set<String> keys = jsonObject.keySet();
            for (String key : keys) {
                result.put(key, jsonObject.getString(key));
            }
            return result;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return defaultValue;
    }
}
