package com.netease.hearttouch.router.parser;

/**
 * Created by zyl06 on 10/12/16.
 */
public class StringStringParser extends IStringParser<String> {

    private static StringStringParser sInstance = null;

    public static StringStringParser getInstance() {
        if (sInstance == null) {
            synchronized (StringStringParser.class) {
                if (sInstance == null) {
                    sInstance = new StringStringParser();
                }
            }
        }
        return sInstance;
    }

    private StringStringParser() {
    }

    public String parse(String str, Class<String> clazz, String defaultValue) {
        return decode(str);
    }
}
