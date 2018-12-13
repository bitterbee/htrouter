/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.netease.hearttouch.router;

import com.google.auto.service.AutoService;
import com.netease.hearttouch.pushcmd.PushCmdAnno;
import com.netease.hearttouch.pushcmd.PushCmdAnnoClass;
import com.netease.hearttouch.pushcmd.PushCmdCodeGenerator;
import com.netease.hearttouch.router.codegenerate.BaseClassGenerator;
import com.netease.hearttouch.router.codegenerate.RouterGroupGenerator;
import com.netease.hearttouch.router.codegenerate.RouterTableGenerator;
import com.netease.hearttouch.router.intercept.HTInterceptAnno;
import com.netease.hearttouch.router.method.HTMethodRouter;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import static com.squareup.javapoet.JavaFile.builder;
import static java.util.Collections.singleton;
import static javax.lang.model.SourceVersion.latestSupported;
import static javax.tools.Diagnostic.Kind.ERROR;

/**
 * 识别和预处理注解的类，会在编译期生成代码
 */
@AutoService(Processor.class)
public class HTRouterDispatchProcess extends AbstractProcessor {

    private Messager messager;
    private Filer filer;

    private String mRouterPkgName = "com.netease.hearttouch.router";
    private String mPushCmdPkgName = null;

    private static final String ROUTER_PKG_KEY = "routerPkg";
    private static final String PUSH_CMD_PKG_KEY = "pushCmdPkg";

    private static final List<BaseClassGenerator> CLASS_GENERATORS = new ArrayList<>();
    private Map<String, List<HTAnnotatedClass>> mAnnoInfos = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        Logger.sMessager = messager;
        mAnnoInfos.clear();

        Map<String, String> options = processingEnv.getOptions();
        if (options != null) {
            if (options.containsKey(ROUTER_PKG_KEY)) {
                mRouterPkgName = options.get(ROUTER_PKG_KEY);
            }
            if (options.containsKey(PUSH_CMD_PKG_KEY)) {
                mPushCmdPkgName = options.get(PUSH_CMD_PKG_KEY);
            }
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return singleton(HTRouter.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return latestSupported();
    }

    /**
     * 主要的处理注解的类，会拿到所有的注解相关的类
     *
     * @param annotations
     * @param roundEnv
     * @return 处理成功返回true
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processHtRouter(annotations, roundEnv);
        processPushCmd(annotations, roundEnv);
        return true;
    }

    private void processPushCmd(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (mPushCmdPkgName == null) {
            return;
        }

        List<PushCmdAnnoClass> annotatedClasses = new ArrayList<>();
        //获取所有通过HTRouter注解的项，遍历
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(PushCmdAnno.class)) {
            TypeElement annotatedClass = (TypeElement) annotatedElement;
            //检测是否是支持的注解类型，如果不是里面会报错
            if (!isValidElement(annotatedClass, PushCmdAnno.class)) {
                continue;
            }
            //获取到信息，把注解类的信息加入到列表中
            PushCmdAnno pushCmdAnno = annotatedElement.getAnnotation(PushCmdAnno.class);
            annotatedClasses.add(new PushCmdAnnoClass(annotatedClass, pushCmdAnno.cmd()));
        }

        try {
            TypeSpec generatedClass = PushCmdCodeGenerator.generatePushCmdDispatcherClass(annotatedClasses);
            JavaFile javaFile = builder(mPushCmdPkgName, generatedClass).build();
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean processHtRouter(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 获取所有通过HTRouter注解的项，遍历
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(HTRouter.class)) {
            TypeElement annotatedClass = (TypeElement) annotatedElement;

            //检测是否是支持的注解类型，如果不是里面会报错
            if (!isValidElement(annotatedClass, HTRouter.class)) {
                return true;
            }
            //获取到信息，把注解类的信息加入到列表中
            HTRouter anno = annotatedElement.getAnnotation(HTRouter.class);
            for (String url : anno.url()) {
                HTAnnotatedClass annoClass = new HTAnnotatedClass(annotatedClass,
                        url, anno.entryAnim(), anno.exitAnim(), anno.needLogin());
                String group = annoClass.group();

                List<HTAnnotatedClass> routerAnnos = mAnnoInfos.get(group);
                if (routerAnnos == null) {
                    routerAnnos = new LinkedList<>();
                    mAnnoInfos.put(group, routerAnnos);
                }
                routerAnnos.add(annoClass);
            }
        }

        List<RouterGroupGenerator> groupGens = new ArrayList<>();
        for (String key : mAnnoInfos.keySet()) {
            List<HTAnnotatedClass> routerAnnos = mAnnoInfos.get(key);
            groupGens.add(new RouterGroupGenerator(mRouterPkgName, key, routerAnnos));
        }
        CLASS_GENERATORS.addAll(groupGens);

        List<HTAnnotatedMethod> routerMethods = new ArrayList<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(HTMethodRouter.class)) {
            ExecutableElement exeElem = (ExecutableElement) element;
            if (!isValidElement(exeElem, HTMethodRouter.class)) {
                Logger.e("htrouter exeElem invalid: " + exeElem);
                return true;
            }

//            messager.printMessage(ERROR, "htmethodrouter = " + exeElement.getSimpleName());
            HTMethodRouter router = exeElem.getAnnotation(HTMethodRouter.class);
//            element.getEnclosingElement()
            routerMethods.add(new HTAnnotatedMethod(router.url(), exeElem, router.needLogin()));
        }

        List<InterceptAnnoClass> interceptAnnos = new ArrayList<>();
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(HTInterceptAnno.class)) {
            TypeElement annotatedClass = (TypeElement) annotatedElement;
            //检测是否是支持的注解类型，如果不是里面会报错
            if (!isValidElement(annotatedClass, HTInterceptAnno.class)) {
                Logger.e("htrouter HTInterceptElem invalid: " + annotatedClass);
                return true;
            }
            //获取到信息，把注解类的信息加入到列表中
            HTInterceptAnno interceptAnno = annotatedElement.getAnnotation(HTInterceptAnno.class);
            interceptAnnos.add(new InterceptAnnoClass(annotatedClass, interceptAnno.url()));
        }

        CLASS_GENERATORS.add(new RouterTableGenerator(mRouterPkgName, groupGens, interceptAnnos, routerMethods));

        for (BaseClassGenerator generator : CLASS_GENERATORS) {
            try {
                TypeSpec generatedClass = generator.generate();
                JavaFile javaFile = builder(generator.packageName(), generatedClass).build();
                generator.writeTo(javaFile, filer);
            } catch (IOException e) {
                Logger.e(String.format(Locale.CHINA, "htrouter create %s failed: %s", generator.className(), e.toString()));
            }
        }

        return true;
    }

    private boolean isValidElement(Element annotatedClass, Class annoClass) {

        if (!HTClassValidator.isPublic(annotatedClass)) {
            String message = String.format("Classes annotated with %s must be public.", "@" + annoClass.getSimpleName());
            messager.printMessage(ERROR, message, annotatedClass);
            return false;
        }

        if (HTClassValidator.isAbstract(annotatedClass)) {
            String message = String.format("Classes annotated with %s must not be abstract.", "@" + annoClass.getSimpleName());
            messager.printMessage(ERROR, message, annotatedClass);
            return false;
        }

        return true;
    }
}