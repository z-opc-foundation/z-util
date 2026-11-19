package com.zifang.util.codegen.bean;

import com.zifang.util.codegen.core.expr.Expr;
import com.zifang.util.codegen.core.expr.TypeRef;
import com.zifang.util.codegen.core.model.FieldModel;
import com.zifang.util.codegen.core.model.Modifier;
import com.zifang.util.codegen.core.model.TypeModel;
import com.zifang.util.codegen.core.spec.FieldSpec;
import com.zifang.util.codegen.core.spi.MemberEmitter;

/**
 * logger 字段的生成逻辑。
 *
 * @author zifang
 */
final class Loggers {

    private Loggers() {
    }

    static void generate(MemberEmitter members, TypeModel type, String fieldName,
                         String loggerType, String factoryType) {
        FieldSpec spec = new FieldSpec(
                fieldName,
                TypeRef.of(loggerType),
                Expr.call(Expr.typeName(factoryType), "getLogger",
                        Expr.classLiteral(TypeRef.variable(type.simpleName()))),
                Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
        members.addField(spec);
    }
}
