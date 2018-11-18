/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.netease.hearttouch.router;

import com.netease.hearttouch.router.codegenerate.RouterGroupGenerator;

import javax.lang.model.element.TypeElement;

/**
 * 用于记录被标注的类的设置
 * @author hzshengxueming
 */
public class HTAnnotatedClass {
    // 路由 group 名
    private String group;
    // 被标注的类的跳转URL
    public String url;
    // 被标注的类的类型信息
    public TypeElement typeElement;
    // 退出动画资源id
    public int exitAnim;
    // 进入动画资源id
    public int entryAnim;
    // 进入页面前是否需要登录
    public boolean needLogin;

    public HTAnnotatedClass(TypeElement typeElement,
                            String url,
                            int entryAnim,
                            int exitAnim,
                            boolean needLogin) {
        this.typeElement = typeElement;
        this.url = url;
        this.entryAnim = entryAnim;
        this.exitAnim = exitAnim;
        this.needLogin = needLogin;
    }

    public String group() {
        if (group == null) {
            group = RouterGroupHelper.getGroup(url);
        }
        return group;
    }
}
