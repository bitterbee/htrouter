package com.netease.hearttouch.router.parser;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

/**
 * Created by zyl06 on 10/10/16.
 */
public abstract class IStringParser<T> {
    public static final String TAG = "IStringParser";
    public abstract T parse(String str, Class<T> clazz, T defaultValue);

    public List<T> parseArray(String str, Class<T> clazz, List<T> defaultValue) {
        if (str == null) {
            return defaultValue;
        }

        try {
            return JSONArray.parseArray(decode(str), clazz);
        } catch (NumberFormatException e) {

        }

        return defaultValue;
    }

    protected String decode(String str) {
        try {
            if (!TextUtils.isEmpty(str)) {
                return URLDecoder.decode(str, "UTF-8");
            }
        } catch (UnsupportedEncodingException e) {

        }

        return str;
    }
}
