package com.netease.hearttouch.router;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.netease.hearttouch.router.intercept.HTInterceptorEntry;
import com.netease.hearttouch.router.intercept.IRouterInterceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by zyl06 on 13/03/2018.
 */
public class HTRouterCall implements IRouterCall {
    /*package*/ static List<IRouterInterceptor> sGlobalInterceptors = new ArrayList<>();
    /*package*/ static final List<HTInterceptorEntry> ANNO_INTERCEPTORS = new LinkedList<>();

    HTDroidRouterParams params = new HTDroidRouterParams();

    private List<IRouterInterceptor> interceptors = new ArrayList<>();

    private int index = 0;

    static void initAnnoInterceptors(List<HTInterceptorEntry> inInterceptors) {
        if (inInterceptors != null) {
            ANNO_INTERCEPTORS.addAll(inInterceptors);
        }
    }

    public void addGlobalInterceptors(IRouterInterceptor... interceptors) {
        Collections.addAll(sGlobalInterceptors, interceptors);
    }

    public static void call(Context context, String url) {
        newBuilder(url)
                .context(context)
                .build()
                .start();
    }

    public static void call(Fragment fragment, String url) {
        newBuilder(url)
                .fragment(fragment)
                .build()
                .start();
    }

    public static void call(Context context, String url, int requestCode) {
        newBuilder(url)
                .context(context)
                .forResult(true)
                .requestCode(requestCode)
                .build()
                .start();
    }

    public static void call(Fragment fragment, String url, int requestCode) {
        newBuilder(url)
                .fragment(fragment)
                .forResult(true)
                .requestCode(requestCode)
                .build()
                .start();
    }

    public void start() {
        if (index > 0) {
            return;
        }
        proceed();
    }

    @Override
    public void proceed() {
        IRouterInterceptor interceptor = index < interceptors.size() ?
                interceptors.get(index) : null;
        index++;

        if (interceptor != null) {
            interceptor.intercept(HTRouterCall.this);
        } else {
            realProceed();
        }
    }

    @Override
    public void cancel() {
        // do nothing
    }

    public HTRouterParams getParams() {
        return params;
    }

    private void realProceed() {
        if (params.forResult) {
            if (params.context != null) {
                HTRouterManager.startActivityForResult((Activity) params.context, params.url,
                        params.sourceIntent, params.isFinish, params.requestCode,
                        params.entryAnim, params.exitAnim);
            } else {
                HTRouterManager.startActivityForResult(params.fragment, params.url,
                        params.sourceIntent, params.isFinish, params.requestCode,
                        params.entryAnim, params.exitAnim);
            }
        } else {
            if (params.context != null) {
                HTRouterManager.startActivity(params.context, params.url,
                        params.sourceIntent, params.isFinish,
                        params.entryAnim, params.exitAnim);
            }
        }
    }

    public static Builder newBuilder(String url) {
        return new Builder(url);
    }

    public static class Builder {
        private HTRouterCall call;

        public Builder(String url) {
            this.call = new HTRouterCall();
            this.call.params.url = url;
        }

        public Builder context(Context context) {
            call.params.context = context;
            return this;
        }

        public Builder fragment(Fragment fragment) {
            call.params.fragment = fragment;
            return this;
        }

        public Builder url(String url) {
            call.params.url = url;
            return this;
        }

        public Builder sourceIntent(Intent sourceIntent) {
            call.params.sourceIntent = sourceIntent;
            return this;
        }

        public Builder isFinish(boolean isFinish) {
            call.params.isFinish = isFinish;
            return this;
        }

        public Builder entryAnim(int entryAnim) {
            call.params.entryAnim = entryAnim;
            return this;
        }

        public Builder exitAnim(int exitAnim) {
            call.params.exitAnim = exitAnim;
            return this;
        }

        public Builder forResult(boolean forResult) {
            call.params.forResult = forResult;
            return this;
        }

        public Builder requestCode(int requestCode) {
            call.params.requestCode = requestCode;
            return this;
        }

        public Builder interceptors(IRouterInterceptor... interceptor) {
            Collections.addAll(call.interceptors, interceptor);
            return this;
        }

        public HTRouterCall build() {
            for (HTInterceptorEntry entry : ANNO_INTERCEPTORS) {
                if (entry.matches(call.params.url)) {
                    call.interceptors.add(entry.getInterceptor());
                }
            }

            if (!sGlobalInterceptors.isEmpty()) {
                call.interceptors.addAll(sGlobalInterceptors);
            }
            if (call.params.requestCode != 0) {
                call.params.forResult = true;
            }

            return call;
        }
    }
}
