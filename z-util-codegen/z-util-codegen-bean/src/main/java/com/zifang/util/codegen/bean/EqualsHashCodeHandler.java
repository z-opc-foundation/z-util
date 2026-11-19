package com.zifang.util.codegen.bean;

import com.zifang.util.codegen.core.model.TypeKind;
import com.zifang.util.codegen.core.model.TypeModel;
import com.zifang.util.codegen.core.spi.CodeGenHandler;
import com.zifang.util.codegen.core.spi.HandlerContext;

import java.util.Set;

/**
 * {@code @ZEqualsAndHashCode} 的处理器。
 *
 * @author zifang
 */
final class EqualsHashCodeHandler implements CodeGenHandler {

    static final String Z_EQUALS_AND_HASH_CODE = "com.zifang.util.codegen.annotation.ZEqualsAndHashCode";

    @Override
    public Set<String> supportedAnnotations() {
        return Set.of(Z_EQUALS_AND_HASH_CODE);
    }

    @Override
    public void handle(HandlerContext context) {
        TypeModel type = context.type();
        if (type.kind() == TypeKind.INTERFACE || type.kind() == TypeKind.ANNOTATION) {
            return;
        }
        EqualsHashCodes.generate(context.members(), type);
    }
}
