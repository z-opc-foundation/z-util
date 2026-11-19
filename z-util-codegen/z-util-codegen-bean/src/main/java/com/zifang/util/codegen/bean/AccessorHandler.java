package com.zifang.util.codegen.bean;

import com.zifang.util.codegen.core.expr.Expr;
import com.zifang.util.codegen.core.expr.Stmt;
import com.zifang.util.codegen.core.expr.TypeRef;
import com.zifang.util.codegen.core.model.AnnotationSpec;
import com.zifang.util.codegen.core.model.FieldModel;
import com.zifang.util.codegen.core.model.Modifier;
import com.zifang.util.codegen.core.model.TypeKind;
import com.zifang.util.codegen.core.model.TypeModel;
import com.zifang.util.codegen.core.spec.MethodSpec;
import com.zifang.util.codegen.core.spi.CodeGenHandler;
import com.zifang.util.codegen.core.spi.HandlerContext;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * {@code @ZGetter} / {@code @ZSetter} 的处理器。
 *
 * @author zifang
 */
final class AccessorHandler implements CodeGenHandler {

    static final String Z_GETTER = "com.zifang.util.codegen.annotation.ZGetter";
    static final String Z_SETTER = "com.zifang.util.codegen.annotation.ZSetter";

    @Override
    public Set<String> supportedAnnotations() {
        return Set.of(Z_GETTER, Z_SETTER);
    }

    @Override
    public void handle(HandlerContext context) {
        TypeModel type = context.type();
        if (type.isRecord() || type.kind() == TypeKind.INTERFACE || type.kind() == TypeKind.ENUM) {
            return;
        }

        AnnotationSpec getter = context.annotation(Z_GETTER);
        AnnotationSpec setter = context.annotation(Z_SETTER);
        boolean fieldLevel = context.target() instanceof VariableElement;

        if (getter != null) {
            if (fieldLevel) {
                FieldModel field = fieldOf(context);
                if (field != null) {
                    String explicit = getter.string("value", "");
                    if (explicit.isEmpty()) {
                        addGetter(context, field, getter.bool("booleanIsPrefix", true));
                    } else {
                        addGetter(context, field, getter.bool("booleanIsPrefix", true), explicit);
                    }
                }
            } else {
                Accessors.generateGetters(context.members(), type, getter.bool("booleanIsPrefix", true));
            }
        }

        if (setter != null) {
            if (fieldLevel) {
                FieldModel field = fieldOf(context);
                if (field != null) {
                    addSetter(context, field, setter.string("value", ""));
                }
            } else {
                Accessors.generateSetters(context.members(), type);
            }
        }
    }

    private static void addGetter(HandlerContext context, FieldModel field, boolean booleanIsPrefix) {
        addGetter(context, field, booleanIsPrefix, BeanSupport.getterName(field, booleanIsPrefix));
    }

    private static void addGetter(HandlerContext context, FieldModel field, boolean booleanIsPrefix, String name) {
        MethodSpec spec = MethodSpec.named(name)
                .returns(TypeRef.ofField(field.name()))
                .modifiers(Modifier.PUBLIC)
                .body(List.of(Stmt.ret(Expr.field(Expr.thisRef(), field.name()))))
                .build();
        context.members().addMethod(spec);
    }

    private static void addSetter(HandlerContext context, FieldModel field, String explicitName) {
        if (field.isFinal()) {
            return;
        }
        List<Stmt> body = new ArrayList<>();
        if (BeanSupport.isNonNull(field)) {
            body.add(Stmt.ifThen(
                    Expr.binary("==", Expr.var(field.name()), Expr.nullLiteral()),
                    Stmt.thrown(Expr.create(
                            TypeRef.of("java.lang.NullPointerException"),
                            Expr.literal(field.name() + " is marked non-null but is null")))));
        }
        body.add(Stmt.assign(Expr.field(Expr.thisRef(), field.name()), Expr.var(field.name())));

        String name = explicitName.isEmpty() ? BeanSupport.setterName(field) : explicitName;
        MethodSpec spec = MethodSpec.named(name)
                .returns(TypeRef.primitive("void"))
                .param(field.name(), TypeRef.ofField(field.name()))
                .modifiers(Modifier.PUBLIC)
                .body(body)
                .build();
        context.members().addMethod(spec);
    }

    private static FieldModel fieldOf(HandlerContext context) {
        Element target = context.target();
        if (!(target instanceof VariableElement)) {
            return null;
        }
        String name = target.getSimpleName().toString();
        for (FieldModel field : context.type().fields()) {
            if (field.name().equals(name)) {
                return field;
            }
        }
        return null;
    }
}
