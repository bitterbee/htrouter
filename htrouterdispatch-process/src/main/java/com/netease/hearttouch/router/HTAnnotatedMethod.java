package com.netease.hearttouch.router;

import javax.lang.model.element.ExecutableElement;

/**
 * Created by zyl06 on 21/03/2018.
 */

public class HTAnnotatedMethod {
    /**被标注的类的跳转URL*/
    public String[] urls;
    /**被标注的类的类型信息*/
    public ExecutableElement element;
    /**进入页面前是否需要登录*/
    public boolean needLogin;

    public HTAnnotatedMethod(String[] urls, ExecutableElement element, boolean needLogin) {
        this.urls = urls;
        this.element = element;
        this.needLogin = needLogin;
    }
}
