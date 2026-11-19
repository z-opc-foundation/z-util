package com.zifang.util.codegen.bean;

import com.zifang.util.codegen.core.model.AnnotationSpec;
import com.zifang.util.codegen.core.model.Modifier;
import com.zifang.util.codegen.core.model.TypeKind;
import com.zifang.util.codegen.core.model.TypeModel;
import com.zifang.util.codegen.core.spi.CodeGenHandler;
import com.zifang.util.codegen.core.spi.HandlerContext;

import java.util.Set;

/**
 * 三种构造器注解的处理器：{@code @ZAllArgsConstructor} / {@code @ZNoArgsConstructor} /
 * {@code @ZRequiredArgsConstructor}。
 *
 * @author zifang
 */
final class ConstructorHandler implements CodeGenHandler {

    static final String Z_ALL_ARGS = "com.zifang.util.codegen.annotation.ZAllArgsConstructor";
    static final String Z_NO_ARGS = "com.zifang.util.codegen.annotation.ZNoArgsConstructor";
    static final String Z_REQUIRED_ARGS = "com.zifang.util.codegen.annotation.ZRequiredArgsConstructor";

    @Override
    public Set<String> supportedAnnotations() {
        return Set.of(Z_ALL_ARGS, Z_NO_ARGS, Z_REQUIRED_ARGS);
    }

    @Override
    public void handle(HandlerContext context) {
        TypeModel type = context.type();
        if (type.isRecord() || type.kind() == TypeKind.INTERFACE
                || type.kind() == TypeKind.ENUM || type.kind() == TypeKind.ANNOTATION) {
            return;
        }

        AnnotationSpec allArgs = context.annotation(Z_ALL_ARGS);
        if (allArgs != null) {
            Constructors.generateAllArgs(context.members(), type,
                    BeanSupport.accessModifiers(accessOf(allArgs)));
        }

        AnnotationSpec noArgs = context.annotation(Z_NO_ARGS);
        if (noArgs != null) {
            Constructors.generateNoArgs(context.members(), type,
                    BeanSupport.accessModifiers(accessOf(noArgs)));
        }

        AnnotationSpec requiredArgs = context.annotation(Z_REQUIRED_ARGS);
        if (requiredArgs != null) {
            Constructors.generateRequiredArgs(context.members(), type,
                    BeanSupport.accessModifiers(accessOf(requiredArgs)));
        }
    }

    static com.zifang.util.codegen.annotation.ZAllArgsConstructor.Access accessOf(AnnotationSpec spec) {
        String name = spec.enumeration("access", "PUBLIC");
        try {
            return com.zifang.util.codegen.annotation.ZAllArgsConstructor.Access.valueOf(name);
        } catch (IllegalArgumentException e) {
            return com.zifang.util.codegen.annotation.ZAllArgsConstructor.Access.PUBLIC;
        }
    }
}
