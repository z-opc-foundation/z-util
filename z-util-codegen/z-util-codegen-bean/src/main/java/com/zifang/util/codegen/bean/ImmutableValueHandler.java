package com.zifang.util.codegen.bean;

import com.zifang.util.codegen.core.emit.JavaSource;
import com.zifang.util.codegen.core.model.MethodModel;
import com.zifang.util.codegen.core.model.TypeModel;
import com.zifang.util.codegen.core.spi.CodeGenHandler;
import com.zifang.util.codegen.core.spi.HandlerContext;
import com.zifang.util.codegen.core.spi.SourceEmitter;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code @ZImmutable} 的 handler：抽象值类型 → 生成 {@code Immutable{SimpleName}} 实现。
 *
 * <p>属性 = 无参抽象方法，按 bean / record 命名归一；生成物走标准 Filer 源码通道。</p>
 *
 * @author zifang
 */
final class ImmutableValueHandler implements CodeGenHandler {

    private static final String Z_IMMUTABLE = "com.zifang.util.codegen.annotation.ZImmutable";

    @Override
    public java.util.Set<String> supportedAnnotations() {
        return java.util.Set.of(Z_IMMUTABLE);
    }

    @Override
    public void handle(HandlerContext context) {
        if (context.annotation(Z_IMMUTABLE) == null) {
            return;
        }
        TypeModel type = context.type();
        List<Property> properties = propertiesOf(type);
        if (properties.isEmpty()) {
            context.environment().getMessager().printMessage(
                    javax.tools.Diagnostic.Kind.WARNING,
                    "@ZImmutable 未找到任何无参抽象方法属性，跳过生成", context.target());
            return;
        }
        generate(context, type, properties);
    }

    private record Property(String name, String typeName, String accessorName) {
    }

    private List<Property> propertiesOf(TypeModel type) {
        List<Property> properties = new ArrayList<>();
        for (MethodModel m : type.methods()) {
            if (m.isStatic() || !m.parameters().isEmpty()) {
                continue;
            }
            if (!m.isAbstract()) {
                continue;
            }
            String property = propertyName(m.name(), m.returnTypeName());
            if (property != null) {
                properties.add(new Property(property, m.returnTypeName(), m.name()));
            }
        }
        return properties;
    }

    /** getX()/isX()/x() 三种命名归一成属性名；不认识的（toString 等）返回 null。 */
    private String propertyName(String methodName, String returnTypeName) {
        if ("toString".equals(methodName) || "hashCode".equals(methodName)) {
            return null;
        }
        if (methodName.startsWith("get") && methodName.length() > 3
                && Character.isUpperCase(methodName.charAt(3))) {
            return decapitalize(methodName.substring(3));
        }
        if (methodName.startsWith("is") && methodName.length() > 2
                && Character.isUpperCase(methodName.charAt(2))
                && ("boolean".equals(returnTypeName) || "java.lang.Boolean".equals(returnTypeName))) {
            return decapitalize(methodName.substring(2));
        }
        if (Character.isLowerCase(methodName.charAt(0))) {
            return methodName;
        }
        return null;
    }

    private String decapitalize(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    private void generate(HandlerContext context, TypeModel type, List<Property> properties) {
        SourceEmitter sources = context.sources();
        String relative = relativeName(type);
        String implSimple = "Immutable" + type.simpleName();
        String implQualified = type.packageName().isEmpty()
                ? implSimple
                : type.packageName() + "." + implSimple;
        String typeParams = typeParameters(type);

        JavaSource source = JavaSource.of(implQualified);
        source.line("/**");
        source.line(" * {@link " + relative + "} 的编译期不可变实现。");
        source.line(" *");
        source.line(" * <p>由 z-util-codegen-bean 生成，勿手改。</p>");
        source.line(" */");
        source.line("@SuppressWarnings(\"unused\")");
        String kind = type.kind() == com.zifang.util.codegen.core.model.TypeKind.INTERFACE
                ? "implements" : "extends";
        source.line("public final class " + implSimple + typeParams + " " + kind + " " + relative + " {");
        source.indent();
        for (Property p : properties) {
            source.line("private final " + p.typeName() + " " + p.name() + ";");
        }
        source.blank();
        source.line("public " + implSimple + "(" + constructorParams(properties) + ") {");
        source.indent();
        for (Property p : properties) {
            source.line("this." + p.name() + " = " + p.name() + ";");
        }
        source.dedent();
        source.line("}");
        source.blank();

        source.line("public static " + relative + " of(" + constructorParams(properties) + ") {");
        source.indent();
        source.line("return new " + implSimple + "(" + arguments(properties) + ");");
        source.dedent();
        source.line("}");
        source.blank();

        for (Property p : properties) {
            source.line("@Override");
            source.line("public " + p.typeName() + " " + p.accessorName() + "() {");
            source.indent();
            source.line("return " + p.name() + ";");
            source.dedent();
            source.line("}");
            source.blank();
        }

        // equals：引用短路 + getClass() 严格相等 + 逐属性比较
        source.line("@Override");
        source.line("public boolean equals(Object o) {");
        source.indent();
        source.line("if (this == o) {");
        source.indent();
        source.line("return true;");
        source.dedent();
        source.line("}");
        source.line("if (o == null || getClass() != o.getClass()) {");
        source.indent();
        source.line("return false;");
        source.dedent();
        source.line("}");
        if (properties.isEmpty()) {
            source.line("return true;");
        } else {
            source.line(implSimple + " other = (" + implSimple + ") o;");
            List<String> comparisons = new ArrayList<>();
            for (Property p : properties) {
                comparisons.add(BeanSupport.isPrimitive(p.typeName())
                        ? p.name() + " == other." + p.name()
                        : "java.util.Objects.equals(" + p.name() + ", other." + p.name() + ")");
            }
            source.line("return " + String.join(" && ", comparisons) + ";");
        }
        source.dedent();
        source.line("}");
        source.blank();

        source.line("@Override");
        source.line("public int hashCode() {");
        source.indent();
        if (properties.isEmpty()) {
            source.line("return 0;");
        } else {
            List<String> names = new ArrayList<>();
            for (Property p : properties) {
                names.add(p.name());
            }
            source.line("return java.util.Objects.hash(" + String.join(", ", names) + ");");
        }
        source.dedent();
        source.line("}");
        source.blank();

        source.line("@Override");
        source.line("public String toString() {");
        source.indent();
        List<String> pairs = new ArrayList<>();
        for (Property p : properties) {
            pairs.add(p.name() + "=\" + " + p.name() + " + \"");
        }
        String body = pairs.isEmpty()
                ? implSimple + "{}"
                : implSimple + "{" + String.join(", ", pairs) + "}";
        source.line("return \"" + body + "\";");
        source.dedent();
        source.line("}");
        source.dedent();
        source.line("}");

        sources.generate(implQualified, source);
    }

    private String constructorParams(List<Property> properties) {
        List<String> params = new ArrayList<>();
        for (Property p : properties) {
            params.add(p.typeName() + " " + p.name());
        }
        return String.join(", ", params);
    }

    private String arguments(List<Property> properties) {
        List<String> names = new ArrayList<>();
        for (Property p : properties) {
            names.add(p.name());
        }
        return String.join(", ", names);
    }

    private String relativeName(TypeModel type) {
        String qualified = type.qualifiedName();
        if (!type.packageName().isEmpty() && qualified.startsWith(type.packageName() + ".")) {
            return qualified.substring(type.packageName().length() + 1);
        }
        return qualified;
    }

    private String typeParameters(TypeModel type) {
        if (type.typeParameters().isEmpty()) {
            return "";
        }
        return "<" + String.join(", ", type.typeParameters()) + ">";
    }
}
