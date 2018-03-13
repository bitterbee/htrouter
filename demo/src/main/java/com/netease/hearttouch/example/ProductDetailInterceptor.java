package com.netease.hearttouch.example;

import com.netease.hearttouch.router.HTLogUtil;
import com.netease.hearttouch.router.HTRouterCall;
import com.netease.hearttouch.router.HTRouterParams;
import com.netease.hearttouch.router.IRouterCall;
import com.netease.hearttouch.router.intercept.HTInterceptAnno;
import com.netease.hearttouch.router.intercept.IRouterInterceptor;

/**
 * Created by zyl06 on 13/03/2018.
 */
@HTInterceptAnno(url = {"http://www.kaola.com/activity/detail/{id}.shtml", "http://m.kaola.com/activity/detail/{id}.shtml", "http://m.kaola.com/product/{id}.html", "http://www.kaola.com/product/{id}.html"})
public class ProductDetailInterceptor implements IRouterInterceptor {

    @Override
    public void intercept(IRouterCall call) {
        HTRouterParams params = ((HTRouterCall)call).getParams();
        HTLogUtil.d("Anno Interceptor 统计数据：" + params.getContext().getClass().getSimpleName() + "-->跳转url-->" + params.getUrl() +
                "  参数intent" + params.getSourceIntent());
        //如果需要拦截或者改变跳转的目标可以直接改变url或者sourceIntent
//                routerParams.url = "http://www.kaola.com/pay?a=b&c=d";
        call.proceed();
    }
}
