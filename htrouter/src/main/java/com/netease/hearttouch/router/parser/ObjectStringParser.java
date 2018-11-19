package com.netease.hearttouch.router.parser;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;

/**
 * Created by zyl06 on 10/10/16.
 */
public class ObjectStringParser<T> extends IStringParser<T> {

    @Override
    public T parse(String str, Class<T> clazz, T defaultValue) {
        if (str == null) {
            return defaultValue;
        }

        try {
            return JSONObject.parseObject(decode(str), clazz, Feature.IgnoreNotMatch);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return defaultValue;
    }
}