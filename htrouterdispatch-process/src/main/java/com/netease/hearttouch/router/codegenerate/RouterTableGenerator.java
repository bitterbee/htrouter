package com.netease.hearttouch.router.codegenerate;

import com.netease.hearttouch.router.HTAnnotatedMethod;
import com.netease.hearttouch.router.IRouterGroup;
import com.netease.hearttouch.router.InterceptAnnoClass;
import com.netease.hearttouch.router.Logger;
import com.netease.hearttouch.router.intercept.HTInterceptorEntry;
import com.netease.hearttouch.router.method.HTMethodRouterEntry;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Created by zyl06 on 13/03/2018.
 */

public class RouterTableGenerator extends BaseClassGenerator {

    private static final String HT_URL_PARAMS_KEY = "HT_URL_PARAMS_KEY";
    private static final String ROUTER_GROUPS = "mRouterGroups";
    private static final String INTERCEPTORS = "mInterceptors";
    private static final String METHOD_ROUTERS = "mMethodRouters";

    private static final String INIT_METHOD = "init";
    private static final String PAGE_ROUTERS_METHOD = "pageRouterGroup";
    private static final String INTERCEPTS_METHOD = "interceptors";
    private static final String METHOD_ROUTERS_METHOD = "methodRouters";

    private String mPkgName;
    private List<RouterGroupGenerator> mRouterGroupGens;
    private List<InterceptAnnoClass> mAnnoIntercepts;
    private List<HTAnnotatedMethod> mAnnoMethods;

    public RouterTableGenerator(String pkgName,
                                List<RouterGroupGenerator> routerGroupGens,
                                List<InterceptAnnoClass> annoIntercepts,
                                List<HTAnnotatedMethod> annoMethods) {
        this.mPkgName = pkgName;
        this.mRouterGroupGens = routerGroupGens;
        this.mAnnoIntercepts = annoIntercepts;
        this.mAnnoMethods = annoMethods;
    }

    @Override
    public TypeSpec generate() {
        TypeSpec.Builder builder = classBuilder(className())
                .addModifiers(PUBLIC, FINAL)
                .addAnnotation(ClassName.get("org.aspectj.lang.annotation", "Aspect"));
        builder.addJavadoc("用于用户启动Activity或者通过URL获得可以跳转的目标\n");

        FieldSpec htUrlParamKey = FieldSpec
                .builder(String.class, HT_URL_PARAMS_KEY,
                        PUBLIC, STATIC, FINAL)
                .initializer("\"ht_url_params_map\"")
                .build();
        builder.addField(htUrlParamKey);

        ParameterizedTypeName mapTypeName = ParameterizedTypeName.get(ClassName.get(HashMap.class), ClassName.get(String.class),
                ClassName.get(IRouterGroup.class));
        FieldSpec pageRouterField = FieldSpec
                .builder(mapTypeName, ROUTER_GROUPS,
                        PRIVATE, FINAL)
                .initializer("new $T()", mapTypeName)
                .build();
        builder.addField(pageRouterField);

        FieldSpec interceptorsField = FieldSpec
                .builder(ParameterizedTypeName.get(List.class, HTInterceptorEntry.class), INTERCEPTORS,
                        PRIVATE, FINAL)
                .initializer("new $T()", ParameterizedTypeName.get(LinkedList.class, HTInterceptorEntry.class))
                .build();
        builder.addField(interceptorsField);

        FieldSpec methodRouterField = FieldSpec
                .builder(ParameterizedTypeName.get(List.class, HTMethodRouterEntry.class), METHOD_ROUTERS,
                        PRIVATE, FINAL)
                .initializer("new $T()", ParameterizedTypeName.get(LinkedList.class, HTMethodRouterEntry.class))
                .build();
        builder.addField(methodRouterField);

        ///////////////////////////////////
        // void init()
        MethodSpec.Builder initMethod = MethodSpec.methodBuilder(INIT_METHOD)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get("org.aspectj.lang", "JoinPoint"), "joinPoint")
                .returns(TypeName.VOID);

        AnnotationSpec.Builder annoBuilder = AnnotationSpec.builder(ClassName.get("org.aspectj.lang.annotation", "After"));
        annoBuilder.addMember("value", "\"execution(void com.netease.hearttouch.router.HTRouterCall.init())\"");
        initMethod.addAnnotation(annoBuilder.build());

        StringBuilder sb = new StringBuilder(32);
        sb.append("$T.init(")
                .append(PAGE_ROUTERS_METHOD).append("(), ")
                .append(METHOD_ROUTERS_METHOD).append("(), ")
                .append(INTERCEPTS_METHOD).append("())");
        initMethod.addStatement(sb.toString(),
                ClassName.get("com.netease.hearttouch.router", "HTRouterManager"));
        builder.addMethod(initMethod.build());

        ///////////////////////////////////
        // Map<String, IRouterGroup> pageRouterGroup()
        MethodSpec.Builder routerMapMethod = MethodSpec.methodBuilder(PAGE_ROUTERS_METHOD)
                .addModifiers(Modifier.PRIVATE)
                .returns(ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(String.class), ClassName.get(IRouterGroup.class)));
        routerMapMethod.beginControlFlow("if (" + ROUTER_GROUPS + ".isEmpty())");
        for (RouterGroupGenerator gen : mRouterGroupGens) {
            routerMapMethod.addStatement(ROUTER_GROUPS + ".put($S, new $T())",
                    gen.getGroup(), gen.getClassType());
        }
        routerMapMethod.endControlFlow();
        routerMapMethod.addStatement("return " + ROUTER_GROUPS);
        builder.addMethod(routerMapMethod.build());

        ///////////////////////////////////
        // List<HTInterceptorEntry> interceptors()
        MethodSpec.Builder interceptorsMethod = MethodSpec.methodBuilder(INTERCEPTS_METHOD)
                .addModifiers(Modifier.PRIVATE)
                .returns(ParameterizedTypeName.get(List.class, HTInterceptorEntry.class));
        interceptorsMethod.beginControlFlow("if (" + INTERCEPTORS + ".isEmpty())");
        for (InterceptAnnoClass annotatedClass : mAnnoIntercepts) {
            for (String url : annotatedClass.urls) {
                interceptorsMethod.addStatement(INTERCEPTORS + ".add(new HTInterceptorEntry($S, new $T()))",
                        url, annotatedClass.typeElement);
            }
        }
        interceptorsMethod.endControlFlow();
        interceptorsMethod.addStatement("return " + INTERCEPTORS);
        builder.addMethod(interceptorsMethod.build());

        ///////////////////////////////////
        // List<HTMethodRouterEntry> methodRouters()
        MethodSpec.Builder methodRouterMethod = MethodSpec.methodBuilder(METHOD_ROUTERS_METHOD)
                .addModifiers(Modifier.PRIVATE)
                .returns(ParameterizedTypeName.get(List.class, HTMethodRouterEntry.class));
        methodRouterMethod.beginControlFlow("if (" + METHOD_ROUTERS + ".isEmpty())");
        for (HTAnnotatedMethod annoMethod : mAnnoMethods) {
            for (String url : annoMethod.urls) {
                // String urls, Method method, List<Class> paramTypes
                TypeElement enclosingElement = (TypeElement) annoMethod.element.getEnclosingElement();

                methodRouterMethod.beginControlFlow("");
                methodRouterMethod.addStatement("$T paramTypes = new $T()",
                        ParameterizedTypeName.get(List.class, Class.class),
                        ParameterizedTypeName.get(ArrayList.class, Class.class));
                for (VariableElement varElement : annoMethod.element.getParameters()) {
                    Logger.w("varElement = " + varElement.asType());
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
    public String packageName() {
        return mPkgName;
    }

    @Override
    public String className() {
        return "HTRouterTable";
    }
}
