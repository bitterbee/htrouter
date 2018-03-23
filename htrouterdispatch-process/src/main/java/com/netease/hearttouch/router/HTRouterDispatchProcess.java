/*
 * This source code is licensed under the MIT-style license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.netease.hearttouch.router;

import com.google.auto.service.AutoService;
import com.netease.hearttouch.pushcmd.PushCmdAnno;
import com.netease.hearttouch.pushcmd.PushCmdAnnoClass;
import com.netease.hearttouch.pushcmd.PushCmdCodeGenerator;
import com.netease.hearttouch.router.codegenerate.IClassGenerator;
import com.netease.hearttouch.router.codegenerate.RouterTableGenerator;
import com.netease.hearttouch.router.intercept.HTInterceptAnno;
import com.netease.hearttouch.router.method.HTMethodRouter;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
    private String packageName = "com.netease.hearttouch.router";

    private static final List<IClassGenerator> CLASS_GENERATORS = new ArrayList<IClassGenerator>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        CLASS_GENERATORS.add(new RouterTableGenerator(messager));
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
            JavaFile javaFile = builder(packageName, generatedClass).build();
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean processHtRouter(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<HTAnnotatedClass> routerClasses = new ArrayList<>();
        //获取所有通过HTRouter注解的项，遍历
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(HTRouter.class)) {
            TypeElement annotatedClass = (TypeElement) annotatedElement;
            //检测是否是支持的注解类型，如果不是里面会报错
            if (!isValidElement(annotatedClass, HTRouter.class)) {
                return true;
            }
            //获取到信息，把注解类的信息加入到列表中
            HTRouter htRouter = annotatedElement.getAnnotation(HTRouter.class);
            routerClasses.add(new HTAnnotatedClass(annotatedClass, htRouter.url(), htRouter.entryAnim(), htRouter.exitAnim(), htRouter.needLogin()));
        }

        List<HTAnnotatedMethod> routerMethods = new ArrayList<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(HTMethodRouter.class)) {
            ExecutableElement exeElement = (ExecutableElement) element;
            if (!isValidElement(exeElement, HTMethodRouter.class)) {
                return true;
            }

//            messager.printMessage(ERROR, "htmethodrouter = " + exeElement.getSimpleName());
            HTMethodRouter router = exeElement.getAnnotation(HTMethodRouter.class);
//            element.getEnclosingElement()
            routerMethods.add(new HTAnnotatedMethod(router.url(), exeElement, router.needLogin()));
        }

        List<InterceptAnnoClass> interceptAnnos = new ArrayList<>();
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(HTInterceptAnno.class)) {
            TypeElement annotatedClass = (TypeElement) annotatedElement;
            //检测是否是支持的注解类型，如果不是里面会报错
            if (!isValidElement(annotatedClass, HTInterceptAnno.class)) {
                return true;
            }
            //获取到信息，把注解类的信息加入到列表中
            HTInterceptAnno interceptAnno = annotatedElement.getAnnotation(HTInterceptAnno.class);
            interceptAnnos.add(new InterceptAnnoClass(annotatedClass, interceptAnno.url()));
        }

        for (IClassGenerator generator : CLASS_GENERATORS) {
            try {
                if (!routerClasses.isEmpty() || !interceptAnnos.isEmpty()) {
                    TypeSpec generatedClass = generator.generate(packageName,
                            routerClasses, interceptAnnos, routerMethods);

                    JavaFile javaFile = builder(packageName, generatedClass).build();
                    javaFile.writeTo(filer);
                }
            } catch (IOException e) {
                generator.printError(messager);
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
