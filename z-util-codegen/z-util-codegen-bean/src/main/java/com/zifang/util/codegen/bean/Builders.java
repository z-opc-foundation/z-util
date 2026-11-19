package com.zifang.util.codegen.bean;

import com.zifang.util.codegen.core.emit.JavaSource;
import com.zifang.util.codegen.core.expr.Expr;
import com.zifang.util.codegen.core.expr.Stmt;
import com.zifang.util.codegen.core.expr.TypeRef;
import com.zifang.util.codegen.core.model.FieldModel;
import com.zifang.util.codegen.core.model.Modifier;
import com.zifang.util.codegen.core.model.TypeModel;
import com.zifang.util.codegen.core.spec.MethodSpec;
import com.zifang.util.codegen.core.spi.MemberEmitter;
import com.zifang.util.codegen.core.spi.SourceEmitter;

import java.util.ArrayList;
import java.util.List;

/**
 * builder 的生成逻辑。
 *
 * <p>产出两样东西：目标类上注入一个静态 {@code builder()} 工厂，同包下另生成一个
 * {@code {ClassName}Builder} 顶层类（builder 主体是纯源码，不走 AST 注入）。</p>
 *
 * @author zifang
 */
final class Builders {

    private Builders() {
    }

    static void generate(MemberEmitter members, SourceEmitter sources, TypeModel type,
                         String builderClassName, String builderMethodName, Modifier[] access) {
        List<FieldModel> fields = BeanSupport.instanceFields(type);
        String targetTypeName = relativeName(type);

        // 目标类上没有同参数构造器时补一个包级全参构造器，builder 靠它落地。
        if (!hasConstructor(type, fields.size())) {
            Constructors.generateAllArgs(members, type, Modifier.PACKAGE);
        }

        JavaSource source = JavaSource.of(type.packageName() + "." + builderClassName);
        source.line("/**");
        source.line(" * {@link " + targetTypeName + "} 的编译期生成 builder。");
        source.line(" *");
        source.line(" * <p>由 z-util-codegen-bean 生成，勿手改。</p>");
        source.line(" */");
        String typeParameters = typeParameters(type);
        source.line("public final class " + builderClassName + typeParameters + " {");
        source.indent();
        for (FieldModel field : fields) {
            source.line("private " + field.typeName() + " " + field.name() + ";");
        }
        if (!fields.isEmpty()) {
            source.blank();
        }
        for (FieldModel field : fields) {
            source.line("public " + builderClassName + typeParameters + " " + field.name()
                    + "(" + field.typeName() + " " + field.name() + ") {");
            source.indent();
            source.line("this." + field.name() + " = " + field.name() + ";");
            source.line("return this;");
            source.dedent();
            source.line("}");
            source.blank();
        }
        source.line("public " + targetTypeName + typeParameters + " build() {");
        source.indent();
        source.line("return new " + targetTypeName + diamond(type) + "(" + arguments(fields) + ");");
        source.dedent();
        source.line("}");
        source.dedent();
        source.line("}");
        sources.generate(type.packageName() + "." + builderClassName, source);

        MethodSpec factory = MethodSpec.named(builderMethodName)
                .returns(TypeRef.of(builderClassName))
                .modifiers(Modifier.PUBLIC, Modifier.STATIC)
                .body(List.of(Stmt.ret(Expr.create(TypeRef.of(builderClassName)))))
                .build();
        members.addMethod(factory);
    }

    private static boolean hasConstructor(TypeModel type, int arity) {
        for (com.zifang.util.codegen.core.model.MethodModel m : type.methods()) {
            if ("<init>".equals(m.name()) && m.parameters().size() == arity) {
                return true;
            }
        }
        return false;
    }

    /** 去掉包前缀的类型名：嵌套类保留 {@code Outer.Inner} 形式。 */
    private static String relativeName(TypeModel type) {
        String qualified = type.qualifiedName();
        if (!type.packageName().isEmpty() && qualified.startsWith(type.packageName() + ".")) {
            return qualified.substring(type.packageName().length() + 1);
        }
        return qualified;
    }

    private static String typeParameters(TypeModel type) {
        if (type.typeParameters().isEmpty()) {
            return "";
        }
        return "<" + String.join(", ", type.typeParameters()) + ">";
    }

    private static String diamond(TypeModel type) {
        return type.typeParameters().isEmpty() ? "" : "<>";
    }

    private static String arguments(List<FieldModel> fields) {
        List<String> names = new ArrayList<>();
        for (FieldModel field : fields) {
            names.add(field.name());
        }
        return String.join(", ", names);
    }
}
