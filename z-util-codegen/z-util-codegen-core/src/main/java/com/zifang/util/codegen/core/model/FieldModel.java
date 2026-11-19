package com.zifang.util.codegen.core.model;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 字段模型：目标类中一个字段的归一化视图。
 *
 * @author zifang
 */
public final class FieldModel {

    private final String name;
    private final String typeName;
    private final Set<Modifier> modifiers;
    private final List<AnnotationSpec> annotations;
    private final String declaringTypeName;

    public FieldModel(String name, String typeName, Set<Modifier> modifiers,
                      List<AnnotationSpec> annotations, String declaringTypeName) {
        this.name = name;
        this.typeName = typeName;
        this.modifiers = Collections.unmodifiableSet(modifiers);
        this.annotations = Collections.unmodifiableList(annotations);
        this.declaringTypeName = declaringTypeName;
    }

    public String name() {
        return name;
    }

    /** 字段声明类型，形如 {@code java.util.List<java.lang.String>}。 */
    public String typeName() {
        return typeName;
    }

    public Set<Modifier> modifiers() {
        return modifiers;
    }

    public List<AnnotationSpec> annotations() {
        return annotations;
    }

    public String declaringTypeName() {
        return declaringTypeName;
    }

    public boolean isStatic() {
        return modifiers.contains(Modifier.STATIC);
    }

    public boolean isFinal() {
        return modifiers.contains(Modifier.FINAL);
    }

    public boolean isTransient() {
        return modifiers.contains(Modifier.TRANSIENT);
    }

    public boolean isVolatile() {
        return modifiers.contains(Modifier.VOLATILE);
    }

    /** 是否参与 getter/setter/toString/equals 的默认字段集。 */
    public boolean isInstanceField() {
        return !isStatic() && !isTransient();
    }

    /** 是否必填：final 或标注了非空约束。 */
    public boolean isRequired() {
        return isFinal();
    }

    @Override
    public String toString() {
        return "FieldModel{" + typeName + " " + name + "}";
    }
}
