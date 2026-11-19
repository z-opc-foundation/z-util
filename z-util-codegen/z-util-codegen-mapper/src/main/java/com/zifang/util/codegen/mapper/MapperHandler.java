package com.zifang.util.codegen.mapper;

import com.zifang.util.codegen.annotation.ZMapper;
import com.zifang.util.codegen.annotation.ZMapping;
import com.zifang.util.codegen.core.diag.Diagnostics;
import com.zifang.util.codegen.core.emit.JavaSource;
import com.zifang.util.codegen.core.spi.CodeGenHandler;
import com.zifang.util.codegen.core.spi.HandlerContext;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@code @ZMapper} 的处理器：为转换接口生成零反射的实现类。
 *
 * @author zifang
 */
final class MapperHandler implements CodeGenHandler {

    /** 注解全限定名：SPI 契约要的是字符串。 */
    static final String Z_MAPPER = "com.zifang.util.codegen.annotation.ZMapper";

    private final Diagnostics diagnostics;

    MapperHandler(Diagnostics diagnostics) {
        this.diagnostics = diagnostics;
    }

    @Override
    public Set<String> supportedAnnotations() {
        return Set.of(Z_MAPPER);
    }

    @Override
    public void handle(HandlerContext context) {
        if (!(context.target() instanceof TypeElement)) {
            return;
        }
        TypeElement mapperType = (TypeElement) context.target();
        if (mapperType.getKind() != ElementKind.INTERFACE) {
            diagnostics.error(mapperType, "@ZMapper 只能标注在接口上");
            return;
        }

        Elements elements = context.environment().getElementUtils();
        ZMapper annotation = mapperType.getAnnotation(ZMapper.class);
        String implName = annotation.implClassName();
        if (implName.isEmpty()) {
            implName = mapperType.getSimpleName() + "Impl";
        }
        String packageName = elements.getPackageOf(mapperType).getQualifiedName().toString();
        String qualifiedImplName = packageName.isEmpty() ? implName : packageName + "." + implName;

        JavaSource source = JavaSource.of(qualifiedImplName);
        source.line("/**");
        source.line(" * {@link " + relativeName(mapperType) + "} 的编译期生成实现。");
        source.line(" *");
        source.line(" * <p>由 z-util-codegen-mapper 生成，勿手改。</p>");
        source.line(" */");
        source.line("public class " + implName + " implements " + relativeName(mapperType) + " {");
        source.indent();

        Map<String, String> nestedMapperFields = new LinkedHashMap<>();
        for (ExecutableElement method : abstractMethods(mapperType)) {
            generateMethod(context, source, mapperType, method, nestedMapperFields);
        }

        for (Map.Entry<String, String> entry : nestedMapperFields.entrySet()) {
            source.line("private final " + entry.getValue() + " " + entry.getKey() + " = new " + entry.getValue() + "();");
        }
        if (!nestedMapperFields.isEmpty()) {
            source.blank();
        }

        source.dedent();
        source.line("}");
        context.sources().generate(qualifiedImplName, source);
    }

    private void generateMethod(HandlerContext context, JavaSource source, TypeElement mapperType,
                                ExecutableElement method, Map<String, String> nestedMapperFields) {
        Elements elements = context.environment().getElementUtils();
        Types types = context.environment().getTypeUtils();

        List<? extends VariableElement> params = method.getParameters();
        if (params.size() != 1) {
            diagnostics.error(method, "@ZMapper 的转换方法只接受一个源参数，当前有 " + params.size() + " 个");
            return;
        }
        TypeMirror sourceType = params.get(0).asType();
        TypeMirror targetType = method.getReturnType();
        if (!(sourceType instanceof DeclaredType) || !(targetType instanceof DeclaredType)) {
            diagnostics.error(method, "@ZMapper 的转换方法只支持对象类型之间的转换");
            return;
        }
        TypeElement sourceElement = elements.getTypeElement(
                ((DeclaredType) sourceType).asElement().toString());
        TypeElement targetElement = elements.getTypeElement(
                ((DeclaredType) targetType).asElement().toString());
        if (sourceElement == null || targetElement == null) {
            diagnostics.error(method, "无法解析转换方法的源类型或目标类型");
            return;
        }

        String sourceVar = params.get(0).getSimpleName().toString();
        String targetVar = "target";

        Map<String, PropertyModel> sourceProperties = PropertyModel.of(sourceElement);
        Map<String, PropertyModel> targetProperties = PropertyModel.of(targetElement);

        String policy = mapperType.getAnnotation(ZMapper.class).unmappedTargetPolicy().name();
        Map<String, ZMapping> explicit = new LinkedHashMap<>();
        for (ZMapping mapping : method.getAnnotationsByType(ZMapping.class)) {
            if (!mapping.target().isEmpty()) {
                explicit.put(mapping.target(), mapping);
            }
        }

        source.line("@Override");
        source.line("public " + targetType + " " + method.getSimpleName() + "("
                + sourceType + " " + sourceVar + ") {");
        source.indent();
        source.line("if (" + sourceVar + " == null) {");
        source.indent();
        source.line("return null;");
        source.dedent();
        source.line("}");
        source.blank();
        source.line(targetType + " " + targetVar + " = new " + targetType + "();");

        Set<String> mapped = new LinkedHashSet<>();
        for (Map.Entry<String, PropertyModel> entry : targetProperties.entrySet()) {
            String name = entry.getKey();
            PropertyModel targetProperty = entry.getValue();
            if (!targetProperty.writable()) {
                continue;
            }
            ZMapping mapping = explicit.get(name);
            if (mapping != null && mapping.ignore()) {
                continue;
            }
            String value = valueFor(context, types, sourceVar, sourceProperties, targetProperty,
                    mapping, nestedMapperFields);
            if (value == null) {
                continue;
            }
            source.line(targetProperty.write(targetVar, value));
            mapped.add(name);
        }

        reportUnmapped(context, method, targetElement, targetProperties, mapped, policy);

        source.blank();
        source.line("return " + targetVar + ";");
        source.dedent();
        source.line("}");
        source.blank();
    }

    private void reportUnmapped(HandlerContext context, ExecutableElement method, TypeElement targetElement,
                                Map<String, PropertyModel> targetProperties, Set<String> mapped, String policy) {
        for (Map.Entry<String, PropertyModel> entry : targetProperties.entrySet()) {
            if (!entry.getValue().writable() || mapped.contains(entry.getKey())) {
                continue;
            }
            String message = "目标属性未映射：" + relativeName(targetElement) + "#" + entry.getKey();
            if ("ERROR".equals(policy)) {
                diagnostics.error(method, message);
            } else if ("WARN".equals(policy)) {
                diagnostics.warn(method, message);
            }
        }
    }

    private String valueFor(HandlerContext context, Types types, String sourceVar,
                             Map<String, PropertyModel> sourceProperties, PropertyModel targetProperty,
                             ZMapping mapping, Map<String, String> nestedMapperFields) {
        if (mapping != null) {
            if (!mapping.expression().isEmpty()) {
                return unwrapJava(mapping.expression(), sourceVar);
            }
            if (!mapping.constant().isEmpty()) {
                return mapping.constant();
            }
        }

        String sourceName = mapping != null ? mapping.source() : "";
        if (sourceName.isEmpty()) {
            sourceName = targetProperty.name();
        }
        PropertyModel sourceProperty = sourceProperties.get(sourceName);
        if (sourceProperty == null || !sourceProperty.readable()) {
            return null;
        }
        String value = sourceProperty.read(sourceVar);
        if (types.isAssignable(types.erasure(sourceProperty.type()), types.erasure(targetProperty.type()))) {
            return value;
        }
        NestedMapper nested = findNestedMapper(context, sourceProperty.type(), targetProperty.type());
        if (nested == null) {
            return null;
        }
        nestedMapperFields.put(nested.fieldName, nested.implName);
        return nested.fieldName + "." + nested.methodName + "(" + value + ")";
    }

    private NestedMapper findNestedMapper(HandlerContext context, TypeMirror sourceType, TypeMirror targetType) {
        TypeElement annotationType = context.environment().getElementUtils()
                .getTypeElement(ZMapper.class.getCanonicalName());
        if (annotationType == null) {
            return null;
        }
        for (Element element : context.roundEnvironment().getElementsAnnotatedWith(annotationType)) {
            if (!(element instanceof TypeElement)) {
                continue;
            }
            TypeElement candidate = (TypeElement) element;
            for (ExecutableElement method : abstractMethods(candidate)) {
                if (method.getParameters().size() != 1) {
                    continue;
                }
                boolean matches = method.getParameters().get(0).asType().toString().equals(sourceType.toString())
                        && method.getReturnType().toString().equals(targetType.toString());
                if (matches) {
                    String implName = candidate.getSimpleName() + "Impl";
                    return new NestedMapper(PropertyModel.decapitalize(implName), implName, method.getSimpleName().toString());
                }
            }
        }
        return null;
    }




    private List<ExecutableElement> abstractMethods(TypeElement type) {
        List<ExecutableElement> result = new ArrayList<>();
        for (Element element : type.getEnclosedElements()) {
            if (element.getKind() != ElementKind.METHOD || !(element instanceof ExecutableElement)) {
                continue;
            }
            ExecutableElement method = (ExecutableElement) element;
            if (!method.getModifiers().contains(Modifier.STATIC)
                    && !method.getModifiers().contains(Modifier.DEFAULT)) {
                result.add(method);
            }
        }
        return result;
    }

    private static String unwrapJava(String expression, String sourceVar) {
        String text = expression.trim();
        if (text.startsWith("java(") && text.endsWith(")")) {
            text = text.substring("java(".length(), text.length() - 1);
        }
        return text.replace("src", sourceVar);
    }

    private static String relativeName(TypeElement type) {
        String qualified = type.getQualifiedName().toString();
        String packageName = packageOf(type);
        if (!packageName.isEmpty() && qualified.startsWith(packageName + ".")) {
            return qualified.substring(packageName.length() + 1);
        }
        return qualified;
    }

    private static String packageOf(TypeElement type) {
        Element enclosing = type.getEnclosingElement();
        while (enclosing != null && !(enclosing instanceof PackageElement)) {
            enclosing = enclosing.getEnclosingElement();
        }
        return enclosing == null ? "" : ((PackageElement) enclosing).getQualifiedName().toString();
    }

    /** 嵌套 mapper：字段名与实现类名。 */
    private static final class NestedMapper {

        private final String fieldName;
        private final String implName;
        private final String methodName;

        NestedMapper(String fieldName, String implName, String methodName) {
            this.fieldName = fieldName;
            this.implName = implName;
            this.methodName = methodName;
        }
    }
}
