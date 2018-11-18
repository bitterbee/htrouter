package com.netease.hearttouch.router.codegenerate;

import com.netease.hearttouch.router.HTAnnotatedClass;
import com.netease.hearttouch.router.HTRouterEntry;
import com.netease.hearttouch.router.IRouterGroup;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.Modifier;

import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * Created by zyl06 on 2018/11/18.
 */

public class RouterGroupGenerator extends BaseClassGenerator {

    private String mPkgName;
    private String mClassName;
    private List<HTAnnotatedClass> mAnnos;
    private String mGroup;

    private static final String PAGE_ROUTERS = "mPageRouters";
    private static final String PAGE_ROUTERS_METHOD = "pageRouters";

    public RouterGroupGenerator(String pkgName, String group, List<HTAnnotatedClass> routerAnnos) {
        this.mPkgName = pkgName;
        this.mClassName = "HTRouterGroup$$" + group;
        this.mAnnos = routerAnnos;
        this.mGroup = group;
    }

    @Override
    public TypeSpec generate() {
        TypeSpec.Builder builder = classBuilder(className())
                .addModifiers(PUBLIC, FINAL)
                .addSuperinterface(ClassName.get(IRouterGroup.class));
        builder.addJavadoc("用于用户启动 Activity 或者通过URL获得可以跳转的目标\n");

        FieldSpec pageRouterField = FieldSpec
                .builder(ParameterizedTypeName.get(List.class, HTRouterEntry.class), PAGE_ROUTERS,
                        PRIVATE, FINAL)
                .initializer("new $T()", ParameterizedTypeName.get(LinkedList.class, HTRouterEntry.class))
                .build();
        builder.addField(pageRouterField);

        MethodSpec.Builder routerMapMethod = MethodSpec.methodBuilder(PAGE_ROUTERS_METHOD)
                .addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(List.class, HTRouterEntry.class));
        routerMapMethod.beginControlFlow("if (" + PAGE_ROUTERS + ".isEmpty())");
        for (HTAnnotatedClass annotatedClass : mAnnos) {
            routerMapMethod.addStatement(PAGE_ROUTERS + ".add(new HTRouterEntry($S, $S, $L, $L, $L))",
                    getClassName(annotatedClass.typeElement), annotatedClass.url,
                    annotatedClass.exitAnim, annotatedClass.entryAnim,
                    annotatedClass.needLogin);
        }
        routerMapMethod.endControlFlow();
        routerMapMethod.addStatement("return " + PAGE_ROUTERS);
        builder.addMethod(routerMapMethod.build());

        return builder.build();
    }

    @Override
    public String packageName() {
        return mPkgName;
    }

    @Override
    public String className() {
        return mClassName;
    }

    public String getGroup() {
        return mGroup;
    }
}
