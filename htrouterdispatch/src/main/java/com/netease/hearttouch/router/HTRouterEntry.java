/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.netease.hearttouch.router;

import java.util.HashMap;
import java.util.Map;

/**
 * 保存每个被标注的Activity的信息，包括可以匹配的URL，进入及退出动画等
 * 主要提供匹配URL功能，判断目标URL是否可以跳转到当前Activity
 *
 * @author hzshengxueming
 */

public class HTRouterEntry extends HTUrlEntry {
    /** 跳转页面的类型信息 */
    private Class<?> activity;
    /** 跳转页面的类名 */
    private String activityClassName;
    /** 退场动画 */
    private int exitAnim;
    /** 进场动画 */
    private int entryAnim;
    /**进入页面前是否需要登录*/
    private boolean needLogin;

    private static Map<String, Class<?>> sAcitivityClasses = new HashMap<>();

    /**
     * 构造一个用于保存URL与页面对应关系的类
     *
     * @param activityClassName  目标页面的类型信息
     * @param url       用于匹配的URL信息
     * @param exitAnim  退出动画的资源id
     * @param entryAnim 进入动画的资源id
     */
    public HTRouterEntry(String activityClassName, String url, int exitAnim, int entryAnim, boolean needLogin) {
        super(url);
        this.activityClassName = activityClassName;
        this.exitAnim = exitAnim;
        this.entryAnim = entryAnim;
        this.needLogin = needLogin;
    }

    /**
     * 构造一个用于保存URL与页面对应关系的类,默认进出场动画
     *
     * @param activityClassName 目标页面的类型信息
     * @param url      用于匹配的URL信息
     */
    public HTRouterEntry(String activityClassName, String url) {
        super(url);
        this.activityClassName = activityClassName;
        this.exitAnim = 0;
        this.entryAnim = 0;
        this.needLogin = false;
    }

    public Class<?> getActivity() {
        if (activity == null) {
            this.activity = sAcitivityClasses.get(activityClassName);
        }
        if (activity == null) {
            try {
                this.activity = Class.forName(activityClassName);
                sAcitivityClasses.put(activityClassName, activity);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return activity;
    }

    public void setActivity(Class<?> activity) {
        this.activity = activity;
    }

    public int getEntryAnim() {
        return entryAnim;
    }

    public void setEntryAnim(int entryAnim) {
        this.entryAnim = entryAnim;
    }

    public int getExitAnim() {
        return exitAnim;
    }

    public void setExitAnim(int exitAnim) {
        this.exitAnim = exitAnim;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isNeedLogin() {
        return needLogin;
    }
}
