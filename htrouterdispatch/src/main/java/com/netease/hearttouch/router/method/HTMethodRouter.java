package com.netease.hearttouch.router.method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by zyl06 on 21/03/2018.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HTMethodRouter {
    /** activity对应url */
    String[] url();

    /** Activity显示的时候，需要先登录 */
    boolean needLogin() default false;
}
