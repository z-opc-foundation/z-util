package com.zifang.util.codegen.meta;

import com.zifang.util.codegen.annotation.ZMetaModel;
import com.zifang.util.codegen.core.emit.JavaSource;
import com.zifang.util.codegen.core.model.FieldModel;
import com.zifang.util.codegen.core.model.TypeModel;
import com.zifang.util.codegen.core.spi.CodeGenHandler;
import com.zifang.util.codegen.core.spi.HandlerContext;
import com.zifang.util.codegen.core.spi.SourceEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * {@code @ZMetaModel} 的 handler：实体类 → 同包 {@code {SimpleName}_} 字段常量类。
 *
 * @author zifang
 */
public final class MetaModelHandler implements CodeGenHandler {

    private static final String Z_META_MODEL = "com.zifang.util.codegen.annotation.ZMetaModel";

    @Override
    public Set<String> supportedAnnotations() {
        return Set.of(Z_META_MODEL);
    }

    @Override
    public void handle(HandlerContext context) {
        if (context.annotation(Z_META_MODEL) == null) {
            return;
        }
        TypeModel type = context.type();
        List<FieldModel> fields = instanceFields(type);
        if (fields.isEmpty()) {
            context.environment().getMessager().printMessage(
                    javax.tools.Diagnostic.Kind.WARNING,
                    "@ZMetaModel 未找到任何实例字段，跳过生成", context.target());
            return;
        }
        generate(context.sources(), type, fields);
    }

    private List<FieldModel> instanceFields(TypeModel type) {
        List<FieldModel> fields = new ArrayList<>();
        for (FieldModel f : type.fields()) {
            if (f.isInstanceField()) {
                fields.add(f);
            }
        }
        return fields;
    }

    private void generate(SourceEmitter sources, TypeModel type, List<FieldModel> fields) {
        String simple = type.simpleName() + "_";
        String qualified = type.packageName().isEmpty()
                ? simple
                : type.packageName() + "." + simple;

        JavaSource source = JavaSource.of(qualified);
        source.line("/**");
        source.line(" * {@link " + type.simpleName() + "} 的编译期字段元模型。");
        source.line(" *");
        source.line(" * <p>由 z-util-codegen-meta 生成，勿手改。</p>");
        source.line(" */");
        source.line("public final class " + simple + " {");
        source.indent();
        source.line("/** 实体类全名。 */");
        source.line("public static final String ENTITY = \"" + type.qualifiedName() + "\";");
        for (FieldModel f : fields) {
            source.blank();
            source.line("/** 字段 {@code " + f.name() + "} 的名字。 */");
            source.line("public static final String " + constantName(f.name())
                    + " = \"" + f.name() + "\";");
        }
        if (!fields.isEmpty()) {
            List<String> names = new ArrayList<>();
            for (FieldModel f : fields) {
                names.add("\"" + f.name() + "\"");
            }
            source.blank();
            source.line("/** 全部字段名，声明顺序。 */");
            source.line("public static final String[] ALL = { " + String.join(", ", names) + " };");
        }
        source.blank();
        source.line("private " + simple + "() {");
        source.line("}");
        source.dedent();
        source.line("}");

        sources.generate(qualified, source);
    }

    /** camelCase → UPPER_SNAKE_CASE：userName → USER_NAME，URLValue → URL_VALUE。 */
    static String constantName(String fieldName) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fieldName.length(); i++) {
            char c = fieldName.charAt(i);
            if (Character.isUpperCase(c) && i > 0
                    && (Character.isLowerCase(fieldName.charAt(i - 1))
                    || (i + 1 < fieldName.length() && Character.isLowerCase(fieldName.charAt(i + 1))))) {
                sb.append('_');
            }
            sb.append(Character.toUpperCase(c));
        }
        return sb.toString();
    }
}
