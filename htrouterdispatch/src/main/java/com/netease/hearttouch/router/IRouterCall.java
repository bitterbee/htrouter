package com.netease.hearttouch.router;

/**
 * Created by zyl06 on 13/03/2018.
 */
public interface IRouterCall {
    void proceed();
    void cancel();
    HTRouterParams getParams();
}
