package com.netease.hearttouch.router;

/**
 * Created by zyl06 on 2018/11/18.
 */

public class RouterGroupHelper {

    public static String getGroup(String url) {
        if (url != null) {
            int idx = url.indexOf("://");
            if (idx != -1 && idx < url.length() - 4) {
                return url.substring(idx + 3, idx + 4);
            }
        }
        return "default";
    }
}
