/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.netease.hearttouch.router;

import javax.lang.model.element.TypeElement;

/**
 * 用于记录被标注的类的设置
 * @author hzshengxueming
 */
public class HTAnnotatedClass {
    /**被标注的类的跳转URL*/
    public String[] urls;
    /**被标注的类的类型信息*/
    public TypeElement typeElement;
    /**退出动画资源id*/
    public int exitAnim;
    /**进入动画资源id*/
    public int entryAnim;
    /**进入页面前是否需要登录*/
    public boolean needLogin;

    public HTAnnotatedClass(TypeElement typeElement, String[] urls, int entryAnim, int exitAnim, boolean needLogin) {
        this.typeElement = typeElement;
        this.urls = urls;
        this.entryAnim = entryAnim;
        this.exitAnim = exitAnim;
        this.needLogin = needLogin;
    }
}
