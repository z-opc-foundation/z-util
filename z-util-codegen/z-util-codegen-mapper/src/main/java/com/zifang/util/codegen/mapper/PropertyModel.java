package com.zifang.util.codegen.mapper;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 属性模型：一个可读可写的 JavaBean 属性，类型用 TypeMirror 表达以支持可赋值性判断。
 *
 * @author zifang
 */
final class PropertyModel {

    private final String name;
    private TypeMirror type;
    private String getterName;
    private String setterName;
    private String fieldName;
    private boolean publicField;

    PropertyModel(String name, TypeMirror type) {
        this.name = name;
        this.type = type;
    }

    String name() {
        return name;
    }

    TypeMirror type() {
        return type;
    }

    void type(TypeMirror type) {
        this.type = type;
    }

    boolean readable() {
        return getterName != null || publicField;
    }

    boolean writable() {
        return setterName != null || publicField;
    }

    /** 从源对象上取值的表达式。 */
    String read(String sourceVar) {
        if (getterName != null) {
            return sourceVar + "." + getterName + "()";
        }
        return sourceVar + "." + fieldName;
    }

    /** 往目标对象上写值的语句。 */
    String write(String targetVar, String value) {
        if (setterName != null) {
            return targetVar + "." + setterName + "(" + value + ");";
        }
        return targetVar + "." + fieldName + " = " + value + ";";
    }

    /** 从类型元素里抽出属性表：先收字段，再用 getter/setter 补齐读写方式。 */
    static Map<String, PropertyModel> of(TypeElement type) {
        Map<String, PropertyModel> properties = new LinkedHashMap<>();
        for (Element element : type.getEnclosedElements()) {
            if (element.getKind() != ElementKind.FIELD || !(element instanceof VariableElement)) {
                continue;
            }
            VariableElement field = (VariableElement) element;
            if (field.getModifiers().contains(Modifier.STATIC)) {
                continue;
            }
            PropertyModel property = properties.computeIfAbsent(field.getSimpleName().toString(),
                    n -> new PropertyModel(n, field.asType()));
            property.fieldName = field.getSimpleName().toString();
            property.publicField = field.getModifiers().contains(Modifier.PUBLIC);
        }
        for (Element element : type.getEnclosedElements()) {
            if (element.getKind() != ElementKind.METHOD || !(element instanceof ExecutableElement)) {
                continue;
            }
            ExecutableElement method = (ExecutableElement) element;
            if (method.getModifiers().contains(Modifier.STATIC)) {
                continue;
            }
            String name = method.getSimpleName().toString();
            if (method.getParameters().isEmpty() && name.startsWith("get") && name.length() > 3) {
                property(properties, name.substring(3), method.getReturnType()).getterName = name;
            } else if (method.getParameters().isEmpty() && name.startsWith("is") && name.length() > 2
                    && isBoolean(method.getReturnType())) {
                property(properties, name.substring(2), method.getReturnType()).getterName = name;
            } else if (method.getParameters().size() == 1 && name.startsWith("set") && name.length() > 3) {
                property(properties, name.substring(3), method.getParameters().get(0).asType()).setterName = name;
            }
        }
        return properties;
    }

    private static PropertyModel property(Map<String, PropertyModel> properties, String suffix, TypeMirror type) {
        String name = decapitalize(suffix);
        PropertyModel property = properties.computeIfAbsent(name, n -> new PropertyModel(n, type));
        property.type = type;
        return property;
    }

    private static boolean isBoolean(TypeMirror type) {
        String name = type.toString();
        return "boolean".equals(name) || "java.lang.Boolean".equals(name);
    }

    static String decapitalize(String s) {
        if (s.isEmpty()) {
            return s;
        }
        if (s.length() > 1 && Character.isUpperCase(s.charAt(0)) && Character.isUpperCase(s.charAt(1))) {
            return s;
        }
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }
}
