package com.zifang.util.codegen.bean;

import com.zifang.util.codegen.core.expr.Expr;
import com.zifang.util.codegen.core.expr.Stmt;
import com.zifang.util.codegen.core.expr.TypeRef;
import com.zifang.util.codegen.core.model.FieldModel;
import com.zifang.util.codegen.core.model.Modifier;
import com.zifang.util.codegen.core.model.TypeModel;
import com.zifang.util.codegen.core.spec.ConstructorSpec;
import com.zifang.util.codegen.core.spec.ParamSpec;
import com.zifang.util.codegen.core.spi.MemberEmitter;

import java.util.ArrayList;
import java.util.List;

/**
 * 构造器的生成逻辑。
 *
 * @author zifang
 */
final class Constructors {

    private Constructors() {
    }

    static void generateAllArgs(MemberEmitter members, TypeModel type, Modifier... access) {
        List<FieldModel> fields = BeanSupport.instanceFields(type);
        List<ParamSpec> params = new ArrayList<>();
        List<Stmt> body = new ArrayList<>();
        for (FieldModel field : fields) {
            params.add(new ParamSpec(field.name(), TypeRef.ofField(field.name())));
            body.add(Stmt.assign(Expr.field(Expr.thisRef(), field.name()), Expr.var(field.name())));
        }
        members.addConstructor(new ConstructorSpec(params, body, access));
    }

    static void generateNoArgs(MemberEmitter members, TypeModel type, Modifier... access) {
        members.addConstructor(new ConstructorSpec(List.of(), List.of(), access));
    }

    static void generateRequiredArgs(MemberEmitter members, TypeModel type, Modifier... access) {
        List<FieldModel> fields = BeanSupport.requiredFields(type);
        List<ParamSpec> params = new ArrayList<>();
        List<Stmt> body = new ArrayList<>();
        for (FieldModel field : fields) {
            params.add(new ParamSpec(field.name(), TypeRef.ofField(field.name())));
            if (BeanSupport.isNonNull(field)) {
                body.add(Stmt.ifThen(
                        Expr.binary("==", Expr.var(field.name()), Expr.nullLiteral()),
                        Stmt.thrown(Expr.create(
                                TypeRef.of("java.lang.NullPointerException"),
                                Expr.literal(field.name() + " is marked non-null but is null")))));
            }
            body.add(Stmt.assign(Expr.field(Expr.thisRef(), field.name()), Expr.var(field.name())));
        }
        members.addConstructor(new ConstructorSpec(params, body, access));
    }
}
