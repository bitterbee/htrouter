package com.netease.hearttouch.router.method;

import com.netease.hearttouch.router.HTUrlEntry;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Created by zyl06 on 21/03/2018.
 */
public class HTMethodRouterEntry extends HTUrlEntry {

    public String className;
    public String methodName;
    public List<Class> paramTypes;
    private Method mMethod;
    private Boolean mIsValid;

    public HTMethodRouterEntry(String url, String className, String methodName, List<Class> paramTypes) {
        super(url);
        this.className = className;
        this.methodName = methodName;
        this.paramTypes = paramTypes;
    }

    public boolean isValid() {
        if (mIsValid != null) {
            return mIsValid;
        }

        try {
            Class clazz = Class.forName(className);
            Class[] types = new Class[paramTypes.size()];
            this.paramTypes.toArray(types);

            mMethod = clazz.getMethod(methodName, types);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mMethod == null) {
            mIsValid = false;
            return false;
        }

        if (Modifier.isStatic(mMethod.getModifiers())) {
            mIsValid = true;
            return true;
        }

        Class clazz = mMethod.getDeclaringClass();
        try {
            Method instanceMethod = clazz.getMethod("getInstance");
            Class returnCls = instanceMethod.getReturnType();
            return Modifier.isStatic(instanceMethod.getModifiers()) && (returnCls == clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mIsValid = false;
        return false;
    }

    public Method getMethod() {
        return mMethod;
    }
}
