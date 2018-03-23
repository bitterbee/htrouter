package com.netease.hearttouch.router;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by zyl06 on 22/03/2018.
 */

class ParamConvertor {

    public static Object toObj(String in, Class type) throws ParamConvertException {
        if (type == String.class) {
            return in;
        }
        if (type == int.class) {
            try {
                return (in == null) ? 0 : Integer.parseInt(in);
            } catch (NumberFormatException e) {
                throw new ParamConvertException(e);
            }
        }
        if (type == long.class) {
            try {
                return (in == null) ? 0 : Long.parseLong(in);
            } catch (NumberFormatException e) {
                throw new ParamConvertException(e);
            }
        }
        if (type == boolean.class) {
            return (in == null) ? false : Boolean.parseBoolean(in);
        }
        if (type == float.class) {
            try {
                return (in == null) ? 0 : Float.parseFloat(in);
            } catch (NumberFormatException e) {
                throw new ParamConvertException(e);
            }
        }
        if (type == double.class) {
            try {
                return (in == null) ? 0 : Double.parseDouble(in);
            } catch (NumberFormatException e) {
                throw new ParamConvertException(e);
            }
        }
        if (type == short.class) {
            try {
                return (in == null) ? 0 : Short.parseShort(in);
            } catch (NumberFormatException e) {
                throw new ParamConvertException(e);
            }
        }

        try {
            return JSONObject.parseObject(in, type);
        } catch (Throwable th) {
            HTLogUtil.d(th.toString());
        }
        return null;
    }

    static class ParamConvertException extends Exception {

        public ParamConvertException(Throwable throwable) {
            super(throwable);
        }
    }
}
