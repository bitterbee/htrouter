package com.netease.hearttouch.router;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by zyl06 on 13/03/2018.
 */

public class HTRouterParams {

    Context context;
    Fragment fragment;
    String url;
    Intent sourceIntent;
    boolean isFinish;
    int entryAnim;
    int exitAnim;
    boolean forResult;
    int requestCode;

    public Context getContext() {
        Context result = context;
        if (result == null && fragment != null) {
            result = fragment.getContext();
        }
        return result;
    }

    public String getUrl() {
        return url;
    }

    public Intent getSourceIntent() {
        return sourceIntent;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public int getEntryAnim() {
        return entryAnim;
    }

    public int getExitAnim() {
        return exitAnim;
    }

    public boolean isForResult() {
        return forResult;
    }

    public int getRequestCode() {
        return requestCode;
    }
}
