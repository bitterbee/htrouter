package com.netease.hearttouch.router.codegenerate;

import com.netease.hearttouch.router.HTAnnotatedClass;
import com.netease.hearttouch.router.HTRouterEntry;
import com.netease.hearttouch.router.InterceptAnnoClass;
import com.netease.hearttouch.router.intercept.HTInterceptorEntry;
import com.netease.hearttouch.router.intercept.IRouterInterceptor;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;

import static com.squareup.javapoet.TypeSpec.classBuilder;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.tools.Diagnostic.Kind.ERROR;

/**
 * Created by zyl06 on 13/03/2018.
 */

public class RouterTableGenerator implements IClassGenerator {

    @Override
    public String className() {
        return "HTRouterTable";
    }

    @Override
    public TypeSpec generate(String packageName, List<HTAnnotatedClass> routerAnnos, List<InterceptAnnoClass> interceptAnnos) {
        TypeSpec.Builder builder = classBuilder(className())
                .addModifiers(PUBLIC, FINAL);
        builder.addJavadoc("用于用户启动Activity或者通过URL获得可以跳转的目标\n");
        FieldSpec htUrlParamKey = FieldSpec
                .builder(String.class, "HT_URL_PARAMS_KEY",
                        PUBLIC, STATIC, FINAL)
                .initializer("\"ht_url_params_map\"")
                .build();
        builder.addField(htUrlParamKey);

        FieldSpec routerMap = FieldSpec
                .builder(ParameterizedTypeName.get(List.class, HTRouterEntry.class), "ROUTERS",
                        PRIVATE, STATIC, FINAL)
                .initializer("new $T()", ParameterizedTypeName.get(LinkedList.class, HTRouterEntry.class))
                .build();
        builder.addField(routerMap);

        FieldSpec interceptors = FieldSpec
                .builder(ParameterizedTypeName.get(List.class, HTInterceptorEntry.class), "INTERCEPTORS",
                        PRIVATE, STATIC, FINAL)
                .initializer("new $T()", ParameterizedTypeName.get(LinkedList.class, HTInterceptorEntry.class))
                .build();
        builder.addField(interceptors);

        MethodSpec.Builder routerMapMethod = MethodSpec.methodBuilder("routers")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ParameterizedTypeName.get(List.class, HTRouterEntry.class));
        routerMapMethod.beginControlFlow("if (ROUTERS.isEmpty())");
        for (HTAnnotatedClass annotatedClass : routerAnnos) {
            ClassName activity = ClassName.bestGuess(annotatedClass.getActivity());

            // get class full name
            StringBuilder sbClazzName = new StringBuilder(32);
            sbClazzName.append(activity.packageName()).append(".");
            for (String simplename : activity.simpleNames()) {
                sbClazzName.append(simplename);
                sbClazzName.append("$");
            }
            sbClazzName.deleteCharAt(sbClazzName.length() - 1);

            for (String url : annotatedClass.getUrl()) {
                routerMapMethod.addStatement("ROUTERS.add(new HTRouterEntry($S, $S, $L, $L, $L))",
                        sbClazzName.toString(), url,
                        annotatedClass.getExitAnim(), annotatedClass.getEntryAnim(),
                        annotatedClass.isNeedLogin());
            }
        }
        routerMapMethod.endControlFlow();
        routerMapMethod.addStatement("return ROUTERS");
        builder.addMethod(routerMapMethod.build());

        MethodSpec.Builder interceptorsMethod = MethodSpec.methodBuilder("interceptors")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ParameterizedTypeName.get(List.class, HTInterceptorEntry.class));
        interceptorsMethod.beginControlFlow("if (INTERCEPTORS.isEmpty())");
        for (InterceptAnnoClass annotatedClass : interceptAnnos) {
            for (String url : annotatedClass.urls) {
                interceptorsMethod.addStatement("INTERCEPTORS.add(new HTInterceptorEntry($S, new $T()))",
                        url, annotatedClass.typeElement);
            }
        }
        interceptorsMethod.endControlFlow();
        interceptorsMethod.addStatement("return INTERCEPTORS");
        builder.addMethod(interceptorsMethod.build());


        return builder.build();
    }

    @Override
    public void printError(Messager messager) {
        messager.printMessage(ERROR, "Couldn't generate HTRouterTable class");
    }
}
