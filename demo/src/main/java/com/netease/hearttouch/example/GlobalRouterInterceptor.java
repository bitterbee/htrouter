package com.netease.hearttouch.example;

import com.netease.hearttouch.router.HTDroidRouterParams;
import com.netease.hearttouch.router.HTLogUtil;
import com.netease.hearttouch.router.IRouterCall;
import com.netease.hearttouch.router.intercept.IRouterInterceptor;

/**
 * Created by zyl06 on 13/03/2018.
 */

public class GlobalRouterInterceptor implements IRouterInterceptor {

    @Override
    public void intercept(IRouterCall call) {
        HTDroidRouterParams params = (HTDroidRouterParams) call.getParams();
        HTLogUtil.d("统计数据：" + params.getContext().getClass().getSimpleName() + "-->跳转url-->" + params.url +
                "  参数intent" + params.sourceIntent);
        //如果需要拦截或者改变跳转的目标可以直接改变url或者sourceIntent
//                routerParams.url = "http://www.kaola.com/pay?a=b&c=d";
        //如果返回true则表示由监听中进行处理，不需要HTRouter负责跳转
        call.proceed();
    }
}
