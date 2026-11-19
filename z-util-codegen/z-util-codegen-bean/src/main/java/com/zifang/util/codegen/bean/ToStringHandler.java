package com.zifang.util.codegen.bean;

import com.zifang.util.codegen.core.model.AnnotationSpec;
import com.zifang.util.codegen.core.model.TypeKind;
import com.zifang.util.codegen.core.model.TypeModel;
import com.zifang.util.codegen.core.spi.CodeGenHandler;
import com.zifang.util.codegen.core.spi.HandlerContext;

import java.util.Set;

/**
 * {@code @ZToString} 的处理器。
 *
 * @author zifang
 */
final class ToStringHandler implements CodeGenHandler {

    static final String Z_TO_STRING = "com.zifang.util.codegen.annotation.ZToString";

    @Override
    public Set<String> supportedAnnotations() {
        return Set.of(Z_TO_STRING);
    }

    @Override
    public void handle(HandlerContext context) {
        TypeModel type = context.type();
        if (type.kind() == TypeKind.INTERFACE || type.kind() == TypeKind.ANNOTATION) {
            return;
        }
        AnnotationSpec spec = context.annotation(Z_TO_STRING);
        ToStrings.generate(context.members(), type,
                spec.bool("includeFieldNames", true),
                spec.bool("callSuper", false));
    }
}
