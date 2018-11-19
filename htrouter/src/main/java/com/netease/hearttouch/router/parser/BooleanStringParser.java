package com.netease.hearttouch.router.parser;

/**
 * Created by zyl06 on 10/12/16.
 */
public class BooleanStringParser extends IStringParser<Boolean> {

    private static BooleanStringParser sInstance = null;

    public static BooleanStringParser getInstance() {
        if (sInstance == null) {
            synchronized (BooleanStringParser.class) {
                if (sInstance == null) {
                    sInstance = new BooleanStringParser();
                }
            }
        }
        return sInstance;
    }

    private BooleanStringParser() {
    }

    @Override
    public Boolean parse(String str, Class<Boolean> clazz, Boolean defaultValue) {
        if (str == null) {
            return defaultValue;
        }

        try {
            return Boolean.parseBoolean(decode(str));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return defaultValue;
    }
}
