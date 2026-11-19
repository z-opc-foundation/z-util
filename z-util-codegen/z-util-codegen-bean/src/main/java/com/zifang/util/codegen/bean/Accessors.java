package com.zifang.util.codegen.bean;

import com.zifang.util.codegen.core.expr.Expr;
import com.zifang.util.codegen.core.expr.Stmt;
import com.zifang.util.codegen.core.expr.TypeRef;
import com.zifang.util.codegen.core.model.FieldModel;
import com.zifang.util.codegen.core.model.Modifier;
import com.zifang.util.codegen.core.model.TypeModel;
import com.zifang.util.codegen.core.spec.MethodSpec;
import com.zifang.util.codegen.core.spec.ParamSpec;
import com.zifang.util.codegen.core.spi.MemberEmitter;

import java.util.ArrayList;
import java.util.List;

/**
 * getter / setter 的生成逻辑。
 *
 * @author zifang
 */
final class Accessors {

    private Accessors() {
    }

    static void generateGetters(MemberEmitter members, TypeModel type, boolean booleanIsPrefix) {
        for (FieldModel field : BeanSupport.instanceFields(type)) {
            MethodSpec spec = MethodSpec.named(BeanSupport.getterName(field, booleanIsPrefix))
                    .returns(TypeRef.ofField(field.name()))
                    .modifiers(Modifier.PUBLIC)
                    .body(List.of(Stmt.ret(Expr.field(Expr.thisRef(), field.name()))))
                    .build();
            members.addMethod(spec);
        }
    }

    static void generateSetters(MemberEmitter members, TypeModel type) {
        for (FieldModel field : BeanSupport.instanceFields(type)) {
            if (field.isFinal()) {
                continue;
            }
            List<Stmt> body = new ArrayList<>();
            if (BeanSupport.isNonNull(field)) {
                body.add(Stmt.ifThen(
                        Expr.binary("==", Expr.var(field.name()), Expr.nullLiteral()),
                        Stmt.expr(Expr.create(
                                TypeRef.of("java.lang.NullPointerException"),
                                Expr.literal(field.name() + " is marked non-null but is null")))));
            }
            body.add(Stmt.assign(Expr.field(Expr.thisRef(), field.name()), Expr.var(field.name())));

            MethodSpec spec = MethodSpec.named(BeanSupport.setterName(field))
                    .returns(TypeRef.primitive("void"))
                    .param(new ParamSpec(field.name(), TypeRef.ofField(field.name())))
                    .modifiers(Modifier.PUBLIC)
                    .body(body)
                    .build();
            members.addMethod(spec);
        }
    }
}
