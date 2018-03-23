package com.netease.hearttouch.router.codegenerate;

import com.netease.hearttouch.router.HTAnnotatedClass;
import com.netease.hearttouch.router.HTAnnotatedMethod;
import com.netease.hearttouch.router.HTRouterEntry;
import com.netease.hearttouch.router.InterceptAnnoClass;
import com.netease.hearttouch.router.intercept.HTInterceptorEntry;
import com.netease.hearttouch.router.method.HTMethodRouterEntry;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.WARNING;

/**
 * Created by zyl06 on 13/03/2018.
 */

public class RouterTableGenerator implements IClassGenerator {

    private static final String HT_URL_PARAMS_KEY = "HT_URL_PARAMS_KEY";
    private static final String PAGE_ROUTERS = "PAGE_ROUTERS";
    private static final String INTERCEPTORS = "INTERCEPTORS";
    private static final String METHOD_ROUTERS = "METHOD_ROUTERS";

    private static final String PAGE_ROUTERS_METHOD = "pageRouters";
    private static final String INTERCEPTS_METHOD = "interceptors";
    private static final String METHOD_ROUTERS_METHOD = "methodRouters";

    private Messager messager;

    public RouterTableGenerator(Messager messager) {
        this.messager = messager;
    }

    @Override
    public String className() {
        return "HTRouterTable";
    }

    @Override
    public TypeSpec generate(String packageName,
                             List<HTAnnotatedClass> annoClasses,
                             List<InterceptAnnoClass> annoIntercepts,
                             List<HTAnnotatedMethod> annoMethods) {
        TypeSpec.Builder builder = classBuilder(className())
                .addModifiers(PUBLIC, FINAL);
        builder.addJavadoc("用于用户启动Activity或者通过URL获得可以跳转的目标\n");
        FieldSpec htUrlParamKey = FieldSpec
                .builder(String.class, HT_URL_PARAMS_KEY,
                        PUBLIC, STATIC, FINAL)
                .initializer("\"ht_url_params_map\"")
                .build();
        builder.addField(htUrlParamKey);

        FieldSpec pageRouterField = FieldSpec
                .builder(ParameterizedTypeName.get(List.class, HTRouterEntry.class), PAGE_ROUTERS,
                        PRIVATE, STATIC, FINAL)
                .initializer("new $T()", ParameterizedTypeName.get(LinkedList.class, HTRouterEntry.class))
                .build();
        builder.addField(pageRouterField);

        FieldSpec interceptorsField = FieldSpec
                .builder(ParameterizedTypeName.get(List.class, HTInterceptorEntry.class), INTERCEPTORS,
                        PRIVATE, STATIC, FINAL)
                .initializer("new $T()", ParameterizedTypeName.get(LinkedList.class, HTInterceptorEntry.class))
                .build();
        builder.addField(interceptorsField);

        FieldSpec methodRouterField = FieldSpec
                .builder(ParameterizedTypeName.get(List.class, HTMethodRouterEntry.class), METHOD_ROUTERS,
                        PRIVATE, STATIC, FINAL)
                .initializer("new $T()", ParameterizedTypeName.get(LinkedList.class, HTMethodRouterEntry.class))
                .build();
        builder.addField(methodRouterField);

        MethodSpec.Builder routerMapMethod = MethodSpec.methodBuilder(PAGE_ROUTERS_METHOD)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ParameterizedTypeName.get(List.class, HTRouterEntry.class));
        routerMapMethod.beginControlFlow("if (" + PAGE_ROUTERS + ".isEmpty())");
        for (HTAnnotatedClass annotatedClass : annoClasses) {
            for (String url : annotatedClass.urls) {
                routerMapMethod.addStatement(PAGE_ROUTERS + ".add(new HTRouterEntry($S, $S, $L, $L, $L))",
                        getClassName(annotatedClass.typeElement), url,
                        annotatedClass.exitAnim, annotatedClass.entryAnim,
                        annotatedClass.needLogin);
            }
        }
        routerMapMethod.endControlFlow();
        routerMapMethod.addStatement("return " + PAGE_ROUTERS);
        builder.addMethod(routerMapMethod.build());

        MethodSpec.Builder interceptorsMethod = MethodSpec.methodBuilder(INTERCEPTS_METHOD)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ParameterizedTypeName.get(List.class, HTInterceptorEntry.class));
        interceptorsMethod.beginControlFlow("if (" + INTERCEPTORS + ".isEmpty())");
        for (InterceptAnnoClass annotatedClass : annoIntercepts) {
            for (String url : annotatedClass.urls) {
                interceptorsMethod.addStatement(INTERCEPTORS + ".add(new HTInterceptorEntry($S, new $T()))",
                        url, annotatedClass.typeElement);
            }
        }
        interceptorsMethod.endControlFlow();
        interceptorsMethod.addStatement("return " + INTERCEPTORS);
        builder.addMethod(interceptorsMethod.build());

        MethodSpec.Builder methodRouterMethod = MethodSpec.methodBuilder(METHOD_ROUTERS_METHOD)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ParameterizedTypeName.get(List.class, HTMethodRouterEntry.class));
        methodRouterMethod.beginControlFlow("if (" + METHOD_ROUTERS + ".isEmpty())");
        for (HTAnnotatedMethod annoMethod : annoMethods) {
            for (String url : annoMethod.urls) {
                // String urls, Method method, List<Class> paramTypes
                TypeElement enclosingElement = (TypeElement) annoMethod.element.getEnclosingElement();

                methodRouterMethod.beginControlFlow("");
                methodRouterMethod.addStatement("$T paramTypes = new $T()",
                        ParameterizedTypeName.get(List.class, Class.class),
                        ParameterizedTypeName.get(ArrayList.class, Class.class));
                for (VariableElement varElement : annoMethod.element.getParameters()) {
                    messager.printMessage(WARNING, "varElement = " + varElement.asType());
                    String typeName = varElement.asType().toString();
                    Object className = primaryTypeGuess(typeName);
                    if (className == null) {
                        className = ClassName.bestGuess(typeName);
                    }
                    methodRouterMethod.addStatement("paramTypes.add($T.class)", className);
                }
                methodRouterMethod.addStatement(METHOD_ROUTERS + ".add(new HTMethodRouterEntry($S, $S, $S, paramTypes))",
                        url, getClassName(enclosingElement), annoMethod.element.getSimpleName());
                methodRouterMethod.endControlFlow();
            }
        }
        methodRouterMethod.endControlFlow();
        methodRouterMethod.addStatement("return " + METHOD_ROUTERS);
        builder.addMethod(methodRouterMethod.build());

        return builder.build();
    }

    @Override
    public void printError(Messager messager) {
        messager.printMessage(ERROR, "Couldn't generate HTRouterTable class");
    }

    private String getClassName(Element element) {
        ClassName className = ClassName.bestGuess(element.toString());

        // get class full name
        StringBuilder sbClazzName = new StringBuilder(32);
        sbClazzName.append(className.packageName()).append(".");
        for (String simplename : className.simpleNames()) {
            sbClazzName.append(simplename);
            sbClazzName.append("$");
        }
        sbClazzName.deleteCharAt(sbClazzName.length() - 1);

        return sbClazzName.toString();
    }

    private Class primaryTypeGuess(String typeName) {
        if ("int".equals(typeName)) {
            return int.class;
        } else if ("long".equals(typeName)) {
            return long.class;
        } else if ("float".equals(typeName)) {
            return float.class;
        } else if ("boolean".equals(typeName)) {
            return boolean.class;
        } else if ("double".equals(typeName)) {
            return double.class;
        } else if ("char".equals(typeName)) {
            return char.class;
        } else if ("short".equals(typeName)) {
            return short.class;
        } else if ("byte".equals(typeName)) {
            return byte.class;
        }

        return null;
    }
}
