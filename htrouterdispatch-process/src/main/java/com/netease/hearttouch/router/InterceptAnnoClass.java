package com.netease.hearttouch.router;

import javax.lang.model.element.TypeElement;

/**
 * Created by zyl06 on 13/03/2018.
 */

public class InterceptAnnoClass {

    public InterceptAnnoClass(TypeElement typeElement, String[] url) {
        this.typeElement = typeElement;
        this.urls = url;
    }

    /**被标注的类的类型信息*/
    public TypeElement typeElement;
    /**被标注的类的跳转URL*/
    public String[] urls;

    public String getClassName() {
        return typeElement.toString();
    }

    public String[] getUrls() {
        return urls;
    }
}
