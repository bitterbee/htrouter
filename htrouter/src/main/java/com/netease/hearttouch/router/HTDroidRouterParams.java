package com.netease.hearttouch.router;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by zyl06 on 13/03/2018.
 */

public class HTDroidRouterParams extends HTRouterParams {

    Context context;
    Fragment fragment;
    public Intent sourceIntent;

    public Context getContext() {
        Context result = context;
        if (result == null && fragment != null) {
            result = fragment.getContext();
        }
        return result;
    }
}
