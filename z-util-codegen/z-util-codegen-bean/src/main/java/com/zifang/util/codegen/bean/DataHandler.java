package com.zifang.util.codegen.bean;

import com.zifang.util.codegen.core.model.AnnotationSpec;
import com.zifang.util.codegen.core.model.Modifier;
import com.zifang.util.codegen.core.model.TypeKind;
import com.zifang.util.codegen.core.model.TypeModel;
import com.zifang.util.codegen.core.spi.CodeGenHandler;
import com.zifang.util.codegen.core.spi.HandlerContext;

import java.util.Set;

/**
 * {@code @ZData} 的处理器：一并展开成 getter + setter + toString + equals/hashCode + 必填参构造器。
 *
 * @author zifang
 */
final class DataHandler implements CodeGenHandler {

    static final String Z_DATA = "com.zifang.util.codegen.annotation.ZData";

    @Override
    public Set<String> supportedAnnotations() {
        return Set.of(Z_DATA);
    }

    @Override
    public void handle(HandlerContext context) {
        TypeModel type = context.type();
        if (type.isRecord() || type.kind() == TypeKind.INTERFACE
                || type.kind() == TypeKind.ENUM || type.kind() == TypeKind.ANNOTATION) {
            return;
        }
        AnnotationSpec spec = context.annotation(Z_DATA);
        boolean includeFieldNames = spec.bool("includeFieldNames", true);

        Accessors.generateGetters(context.members(), type, true);
        Accessors.generateSetters(context.members(), type);
        ToStrings.generate(context.members(), type, includeFieldNames, false);
        EqualsHashCodes.generate(context.members(), type);
        Constructors.generateRequiredArgs(context.members(), type, Modifier.PUBLIC);
    }
}
