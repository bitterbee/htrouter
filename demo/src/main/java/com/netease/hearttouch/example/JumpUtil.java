package com.netease.hearttouch.example;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.netease.hearttouch.router.method.HTMethodRouter;

/**
 * Created by zyl06 on 21/03/2018.
 */

public class JumpUtil {

    private static final String TAG = "JumpUtil";
    private static JumpUtil sInstance = null;

    public static JumpUtil getInstance() {
        if (sInstance == null) {
            synchronized (JumpUtil.class) {
                if (sInstance == null) {
                    sInstance = new JumpUtil();
                }
            }
        }
        return sInstance;
    }

    private JumpUtil() {
    }

    @HTMethodRouter(url = {"http://www.you.163.com/jumpA"}, needLogin = true)
    public void jumpA(Context context, String str, int i) {
        String msg = "jumpA called: str=" + str + "; i=" + i;
        Log.i(TAG, msg);
        if (context != null) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
    }

    @HTMethodRouter(url = {"http://www.you.163.com/jumpB"})
    public static void jumpB(Context context, String str, int i) {
        String msg = "jumpB called: str=" + str + "; i=" + i;
        Log.i(TAG, msg);
        if (context != null) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
    }

    @HTMethodRouter(url = {"http://www.you.163.com/jumpC"})
    public void jumpC() {
        Log.i(TAG, "jumpC called");
    }
}
