package com.netease.hearttouch.router.parser;

import com.netease.yxlogger.Logger;

/**
 * Created by hzhuangzhuoyin on 2017/3/7.
 * 仿造 IntStringParser 写的 Long 型 Parser
 */

public class LongStringParser extends IStringParser<Long>{

    private static LongStringParser sInstance = null;

    public static LongStringParser getInstance() {
        if (sInstance == null) {
            synchronized (LongStringParser.class) {
                if (sInstance == null) {
                    sInstance = new LongStringParser();
                }
            }
        }
        return sInstance;
    }

    private LongStringParser() {
    }

    @Override
    public Long parse(String str, Class<Long> clazz, Long defaultValue) {
        if (str == null) {
            return defaultValue;
        }

        try {
            return Long.parseLong(decode(str));
        } catch (NumberFormatException e) {
            Logger.e(TAG, e.toString());
        }

        return defaultValue;
    }
}
