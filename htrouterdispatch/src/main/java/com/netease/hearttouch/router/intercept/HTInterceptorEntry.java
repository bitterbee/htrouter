package com.netease.hearttouch.router.intercept;

import com.netease.hearttouch.router.HTUrlEntry;

/**
 * Created by zyl06 on 13/03/2018.
 */

public class HTInterceptorEntry extends HTUrlEntry {
    /** 本页面可以匹配的拦截器 */
    private IRouterInterceptor interceptor;

    public HTInterceptorEntry(String url, IRouterInterceptor interceptor) {
        super(url);
        this.interceptor = interceptor;
    }

    public IRouterInterceptor getInterceptor() {
        return interceptor;
    }
}
