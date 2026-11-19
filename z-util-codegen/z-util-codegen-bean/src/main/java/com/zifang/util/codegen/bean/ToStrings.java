package com.zifang.util.codegen.bean;

import com.zifang.util.codegen.core.expr.Expr;
import com.zifang.util.codegen.core.expr.Stmt;
import com.zifang.util.codegen.core.expr.TypeRef;
import com.zifang.util.codegen.core.model.FieldModel;
import com.zifang.util.codegen.core.model.Modifier;
import com.zifang.util.codegen.core.model.TypeModel;
import com.zifang.util.codegen.core.spec.MethodSpec;
import com.zifang.util.codegen.core.spi.MemberEmitter;

import java.util.ArrayList;
import java.util.List;

/**
 * toString 的生成逻辑：{@code "User{name=" + this.name + ", age=" + this.age + "}"}。
 *
 * @author zifang
 */
final class ToStrings {

    private ToStrings() {
    }

    static void generate(MemberEmitter members, TypeModel type, boolean includeFieldNames, boolean callSuper) {
        List<FieldModel> fields = BeanSupport.comparableFields(type);

        Expr result = Expr.literal(type.simpleName() + "{");
        for (int i = 0; i < fields.size(); i++) {
            FieldModel field = fields.get(i);
            if (i > 0) {
                result = Expr.binary("+", result, Expr.literal(", "));
            }
            if (includeFieldNames) {
                result = Expr.binary("+", result, Expr.literal(field.name() + "="));
            }
            result = Expr.binary("+", result, Expr.field(Expr.thisRef(), field.name()));
        }
        result = Expr.binary("+", result, Expr.literal("}"));

        if (callSuper) {
            result = Expr.binary("+", Expr.call(Expr.superRef(), "toString"), result);
        }

        List<Stmt> body = new ArrayList<>();
        body.add(Stmt.ret(result));

        MethodSpec spec = MethodSpec.named("toString")
                .returns(TypeRef.of("java.lang.String"))
                .modifiers(Modifier.PUBLIC)
                .body(body)
                .build();
        members.addMethod(spec);
    }
}
