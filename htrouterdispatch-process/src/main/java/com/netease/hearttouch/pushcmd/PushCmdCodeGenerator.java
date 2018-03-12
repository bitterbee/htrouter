package com.netease.hearttouch.pushcmd;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.lang.model.element.Modifier;

/**
 * Created by zyl06 on 20/06/2017.
 */
public class PushCmdCodeGenerator {

    public static TypeSpec generatePushCmdDispatcherClass(List<PushCmdAnnoClass> annotatedClasses) {
        TypeSpec.Builder builder = TypeSpec.classBuilder("PushCmdDispatcher")
                .addModifiers(Modifier.PUBLIC);

        FieldSpec entries = FieldSpec
                .builder(ParameterizedTypeName.get(HashMap.class, String.class, BaseCmdHandler.class), "entries",
                        Modifier.PRIVATE)
                .initializer("new $T<>()", TypeName.get(LinkedHashMap.class))
                .build();
        builder.addField(entries);

        MethodSpec.Builder initMethod = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);
        for (PushCmdAnnoClass annotatedClass : annotatedClasses) {
            ClassName pushCmdHandler = ClassName.bestGuess(annotatedClass.typeElement.toString());
            initMethod.addStatement("entries.put($S, new $T())", annotatedClass.cmd, pushCmdHandler);
        }
        builder.addMethod(initMethod.build());

        MethodSpec.Builder handleMethod = MethodSpec.methodBuilder("handle")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "cmd")
                .addParameter(String.class, "params")
                .returns(boolean.class);
        handleMethod.addStatement("$T entry = entries.get(cmd)", ClassName.get(BaseCmdHandler.class))
                .beginControlFlow("if (entry == null)")
                .addStatement("return false")
                .endControlFlow()
                .addStatement("return entry.handle(params)");
        builder.addMethod(handleMethod.build());

        return builder.build();
    }
}
