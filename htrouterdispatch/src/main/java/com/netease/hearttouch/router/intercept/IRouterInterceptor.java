package com.netease.hearttouch.router.intercept;

import com.netease.hearttouch.router.IRouterCall;

/**
 * Created by zyl06 on 13/03/2018.
 */
public interface IRouterInterceptor {
    void intercept(IRouterCall call);
}
