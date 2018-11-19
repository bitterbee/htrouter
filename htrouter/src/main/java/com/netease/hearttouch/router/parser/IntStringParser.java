package com.netease.hearttouch.router.parser;

import com.netease.yxlogger.Logger;

/**
 * Created by zyl06 on 10/10/16.
 */
public class IntStringParser extends IStringParser<Integer> {

    private static IntStringParser sInstance = null;

    public static IntStringParser getInstance() {
        if (sInstance == null) {
            synchronized (IntStringParser.class) {
                if (sInstance == null) {
                    sInstance = new IntStringParser();
                }
            }
        }
        return sInstance;
    }

    private IntStringParser() {
    }


    @Override
    public Integer parse(String str, Class<Integer> clazz, Integer defaultValue) {
        if (str == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(decode(str));
        } catch (NumberFormatException e) {
            Logger.e(TAG, e.toString());
        }

        return defaultValue;
    }
}
