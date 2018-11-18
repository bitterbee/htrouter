package com.netease.hearttouch.router.codegenerate;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;

/**
 * Created by zyl06 on 2018/11/18.
 */

public abstract class BaseClassGenerator {

    public BaseClassGenerator() {
    }

    public abstract TypeSpec generate();

    public void writeTo(JavaFile javaFile, Filer filer) throws IOException {
        javaFile.writeTo(filer);
    }

    public abstract String packageName();

    public abstract String className();

    protected Class primaryTypeGuess(String typeName) {
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

    protected String getClassName(Element element) {
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

    public ClassName getClassType() {
        return ClassName.get(packageName(), className());
    }
}
