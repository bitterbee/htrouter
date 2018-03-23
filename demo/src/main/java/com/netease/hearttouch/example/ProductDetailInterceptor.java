package com.netease.hearttouch.example;

import com.netease.hearttouch.router.HTDroidRouterParams;
import com.netease.hearttouch.router.HTLogUtil;
import com.netease.hearttouch.router.IRouterCall;
import com.netease.hearttouch.router.intercept.HTInterceptAnno;
import com.netease.hearttouch.router.intercept.IRouterInterceptor;

/**
 * Created by zyl06 on 13/03/2018.
 */
@HTInterceptAnno(url = {"http://www.you.163.com/activity/detail/{id}.shtml", "http://m.you.163.com/activity/detail/{id}.shtml", "http://m.you.163.com/product/{id}.html", "http://www.you.163.com/product/{id}.html"})
public class ProductDetailInterceptor implements IRouterInterceptor {

    @Override
    public void intercept(IRouterCall call) {
        HTDroidRouterParams params = (HTDroidRouterParams) call.getParams();
        HTLogUtil.d("Anno Interceptor 统计数据：" + params.getContext().getClass().getSimpleName() + "-->跳转url-->" + params.url +
                "  参数intent" + params.sourceIntent);
        //如果需要拦截或者改变跳转的目标可以直接改变url或者sourceIntent
        call.proceed();
    }
}
