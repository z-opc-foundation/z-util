package com.zifang.util.codegen.bean;

import com.zifang.util.codegen.core.model.AnnotationSpec;
import com.zifang.util.codegen.core.model.TypeKind;
import com.zifang.util.codegen.core.model.TypeModel;
import com.zifang.util.codegen.core.spi.CodeGenHandler;
import com.zifang.util.codegen.core.spi.HandlerContext;

import java.util.Set;

/**
 * {@code @ZBuilder} 的处理器。
 *
 * @author zifang
 */
final class BuilderHandler implements CodeGenHandler {

    static final String Z_BUILDER = "com.zifang.util.codegen.annotation.ZBuilder";

    @Override
    public Set<String> supportedAnnotations() {
        return Set.of(Z_BUILDER);
    }

    @Override
    public void handle(HandlerContext context) {
        TypeModel type = context.type();
        if (type.kind() == TypeKind.INTERFACE || type.kind() == TypeKind.ENUM
                || type.kind() == TypeKind.ANNOTATION || type.isRecord()) {
            return;
        }
        AnnotationSpec spec = context.annotation(Z_BUILDER);
        String builderClassName = spec.string("builderClassName", "");
        if (builderClassName.isEmpty()) {
            builderClassName = type.simpleName() + "Builder";
        }
        String builderMethodName = spec.string("builderMethodName", "builder");
        Builders.generate(context.members(), context.sources(), type,
                builderClassName, builderMethodName,
                BeanSupport.accessModifiers(ConstructorHandler.accessOf(spec)));
    }
}
