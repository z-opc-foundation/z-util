package com.zifang.util.codegen.bean;

import com.zifang.util.codegen.core.model.AnnotationSpec;
import com.zifang.util.codegen.core.model.TypeKind;
import com.zifang.util.codegen.core.model.TypeModel;
import com.zifang.util.codegen.core.spi.CodeGenHandler;
import com.zifang.util.codegen.core.spi.HandlerContext;

import java.util.Set;

/**
 * {@code @ZSlf4j} 的处理器。
 *
 * @author zifang
 */
final class LoggerHandler implements CodeGenHandler {

    static final String Z_SLF4J = "com.zifang.util.codegen.annotation.ZSlf4j";

    @Override
    public Set<String> supportedAnnotations() {
        return Set.of(Z_SLF4J);
    }

    @Override
    public void handle(HandlerContext context) {
        TypeModel type = context.type();
        if (type.kind() == TypeKind.INTERFACE || type.kind() == TypeKind.ANNOTATION) {
            return;
        }
        AnnotationSpec spec = context.annotation(Z_SLF4J);
        Loggers.generate(context.members(), type,
                spec.string("fieldName", "log"),
                spec.string("loggerType", "org.slf4j.Logger"),
                spec.string("factoryType", "org.slf4j.LoggerFactory"));
    }
}
