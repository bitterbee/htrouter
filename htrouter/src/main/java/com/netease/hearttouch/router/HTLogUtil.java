package com.netease.hearttouch.router;

import android.util.Log;

import java.lang.String;

public final class HTLogUtil {
    private static final String TAG = "HT";

    private static boolean sDebug = false;

    public static void setDebugMode(boolean debug) {
        sDebug = debug;
    }

    public static void d(String message) {
        if (sDebug) {
            Log.d(TAG, message);
        }
    }
}
