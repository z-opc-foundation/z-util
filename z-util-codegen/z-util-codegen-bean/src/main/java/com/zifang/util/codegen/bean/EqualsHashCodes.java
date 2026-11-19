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
 * equals / hashCode 的生成逻辑。
 *
 * <p>语义取"严格相等"：先比引用、再比运行时类，然后逐字段比。基本类型用 {@code ==}，
 * 引用类型用 {@code java.util.Objects.equals}（天然吃 null）。</p>
 *
 * @author zifang
 */
final class EqualsHashCodes {

    private static final String OTHER = "other";

    private EqualsHashCodes() {
    }

    static void generate(MemberEmitter members, TypeModel type) {
        List<FieldModel> fields = BeanSupport.comparableFields(type);
        generateEquals(members, type, fields);
        generateHashCode(members, fields);
    }

    private static void generateEquals(MemberEmitter members, TypeModel type, List<FieldModel> fields) {
        List<Stmt> body = new ArrayList<>();
        body.add(Stmt.ifThen(Expr.binary("==", Expr.var("o"), Expr.thisRef()),
                Stmt.ret(Expr.literal(true))));

        Expr otherClass = Expr.call(Expr.var("o"), "getClass");
        Expr thisClass = Expr.call(Expr.thisRef(), "getClass");
        body.add(Stmt.ifThen(
                Expr.binary("||",
                        Expr.binary("==", Expr.var("o"), Expr.nullLiteral()),
                        Expr.binary("!=", thisClass, otherClass)),
                Stmt.ret(Expr.literal(false))));

        body.add(Stmt.var(TypeRef.of(type.simpleName()), OTHER,
                Expr.cast(TypeRef.of(type.simpleName()), Expr.var("o"))));

        if (fields.isEmpty()) {
            body.add(Stmt.ret(Expr.literal(true)));
        } else {
            Expr comparison = null;
            for (FieldModel field : fields) {
                Expr mine = Expr.field(Expr.thisRef(), field.name());
                Expr theirs = Expr.field(Expr.var(OTHER), field.name());
                Expr item = BeanSupport.isPrimitive(field.typeName())
                        ? Expr.binary("==", mine, theirs)
                        : Expr.call(Expr.typeName("java.util.Objects"), "equals", mine, theirs);
                comparison = comparison == null ? item : Expr.binary("&&", comparison, item);
            }
            body.add(Stmt.ret(comparison));
        }

        MethodSpec spec = MethodSpec.named("equals")
                .returns(TypeRef.primitive("boolean"))
                .param("o", TypeRef.of("java.lang.Object"))
                .modifiers(Modifier.PUBLIC)
                .body(body)
                .build();
        members.addMethod(spec);
    }

    private static void generateHashCode(MemberEmitter members, List<FieldModel> fields) {
        List<Expr> args = new ArrayList<>();
        for (FieldModel field : fields) {
            args.add(Expr.field(Expr.thisRef(), field.name()));
        }

        Expr hash = Expr.call(Expr.typeName("java.util.Objects"), "hash", args.toArray(new Expr[0]));

        MethodSpec spec = MethodSpec.named("hashCode")
                .returns(TypeRef.primitive("int"))
                .modifiers(Modifier.PUBLIC)
                .body(List.of(Stmt.ret(hash)))
                .build();
        members.addMethod(spec);
    }
}
