package com.netease.hearttouch.router.codegenerate;

import com.netease.hearttouch.router.HTAnnotatedClass;
import com.netease.hearttouch.router.HTAnnotatedMethod;
import com.netease.hearttouch.router.InterceptAnnoClass;
import com.squareup.javapoet.TypeSpec;

import java.util.List;

import javax.annotation.processing.Messager;

/**
 * Created by zyl06 on 12/03/2018.
 */

public interface IClassGenerator {
    String className();
    TypeSpec generate(String packageName,
                      List<HTAnnotatedClass> annoClasses,
                      List<InterceptAnnoClass> annoIntercepts,
                      List<HTAnnotatedMethod> annoMethods);
    void printError(Messager messager);
}
