package com.zifang.util.codegen.bean;

import com.zifang.util.codegen.core.model.AnnotationSpec;
import com.zifang.util.codegen.core.model.FieldModel;
import com.zifang.util.codegen.core.model.Modifier;
import com.zifang.util.codegen.core.model.TypeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Lombok 性质特性集共用的判定与命名规则。
 *
 * @author zifang
 */
final class BeanSupport {

    static final String Z_NON_NULL = "com.zifang.util.codegen.annotation.ZNonNull";

    private BeanSupport() {
    }

    /** 目标类自身声明的实例字段（排除静态与 transient）。 */
    static List<FieldModel> instanceFields(TypeModel type) {
        List<FieldModel> fields = new ArrayList<>();
        for (FieldModel f : type.fields()) {
            if (f.isInstanceField()) {
                fields.add(f);
            }
        }
        return fields;
    }

    /** 参与 toString/equals 的字段：实例字段且非 transient。 */
    static List<FieldModel> comparableFields(TypeModel type) {
        return instanceFields(type);
    }

    /** 必填字段：final 或标注了非空约束。 */
    static List<FieldModel> requiredFields(TypeModel type) {
        List<FieldModel> fields = new ArrayList<>();
        for (FieldModel f : instanceFields(type)) {
            if (f.isFinal() || isNonNull(f)) {
                fields.add(f);
            }
        }
        return fields;
    }

    static boolean isNonNull(FieldModel field) {
        return hasAnnotation(field, Z_NON_NULL);
    }

    static boolean hasAnnotation(FieldModel field, String qualifiedName) {
        for (AnnotationSpec a : field.annotations()) {
            if (a.qualifiedName().equals(qualifiedName)) {
                return true;
            }
        }
        return false;
    }

    /** getter 名：布尔字段走 isXxx，其余走 getXxx。 */
    static String getterName(FieldModel field, boolean booleanIsPrefix) {
        String name = field.name();
        if (booleanIsPrefix && isBoolean(field)) {
            if (name.startsWith("is") && name.length() > 2 && Character.isUpperCase(name.charAt(2))) {
                return name;
            }
            return "is" + capitalize(name);
        }
        return "get" + capitalize(name);
    }

    /** setter 名：setXxx。 */
    static String setterName(FieldModel field) {
        return "set" + capitalize(field.name());
    }

    static boolean isBoolean(FieldModel field) {
        return "boolean".equals(field.typeName()) || "java.lang.Boolean".equals(field.typeName());
    }

    static boolean isPrimitive(String typeName) {
        switch (typeName) {
            case "boolean":
            case "byte":
            case "char":
            case "short":
            case "int":
            case "long":
            case "float":
            case "double":
                return true;
            default:
                return false;
        }
    }

    static String capitalize(String s) {
        if (s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    static Modifier[] accessModifiers(com.zifang.util.codegen.annotation.ZAllArgsConstructor.Access access) {
        switch (access) {
            case PROTECTED:
                return new Modifier[]{Modifier.PROTECTED};
            case PACKAGE:
                return new Modifier[]{Modifier.PACKAGE};
            case PRIVATE:
                return new Modifier[]{Modifier.PRIVATE};
            case PUBLIC:
            default:
                return new Modifier[]{Modifier.PUBLIC};
        }
    }
}
