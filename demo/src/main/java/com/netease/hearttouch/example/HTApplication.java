package com.netease.hearttouch.example;

import android.app.Application;

import com.netease.hearttouch.router.HTRouterManager;
import com.netease.hearttouch.router.HTRouterTable;
//import com.netease.hearttouch.router.HTRouterTable;

public class HTApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        HTRouterManager.init(HTRouterTable.routerMap(), HTRouterTable.interceptors());
        //注册绑定默认的降级页面
        String customUrlKey = "customUrlKey"; //HTWebActivity接收参数key
        HTRouterManager.registerWebActivity(WebActivity.class, customUrlKey);
        //开启Debug模式，输出相应日志
        HTRouterManager.setDebugMode(true);
        //处理每次跳转监听 用户打点统计等
        HTRouterManager.setGloablInterceptor(new GlobalRouterInterceptor());
    }
}
