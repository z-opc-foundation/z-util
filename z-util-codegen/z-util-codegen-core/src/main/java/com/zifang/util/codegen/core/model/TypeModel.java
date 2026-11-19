package com.zifang.util.codegen.core.model;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 类型模型：目标类型的归一化视图，handler 只面向它编程，不直接接触 {@code javax.lang.model}。
 *
 * @author zifang
 */
public final class TypeModel {

    private final String qualifiedName;
    private final String simpleName;
    private final String packageName;
    private final TypeKind kind;
    private final String superclassName;
    private final List<String> interfaceNames;
    private final List<String> typeParameters;
    private final List<FieldModel> fields;
    private final List<MethodModel> methods;
    private final Set<Modifier> modifiers;
    private final List<AnnotationSpec> annotations;
    private final String enclosingTypeName;

    public TypeModel(String qualifiedName, String simpleName, String packageName, TypeKind kind,
                     String superclassName, List<String> interfaceNames, List<String> typeParameters,
                     List<FieldModel> fields, List<MethodModel> methods, Set<Modifier> modifiers,
                     List<AnnotationSpec> annotations, String enclosingTypeName) {
        this.qualifiedName = qualifiedName;
        this.simpleName = simpleName;
        this.packageName = packageName;
        this.kind = kind;
        this.superclassName = superclassName;
        this.interfaceNames = Collections.unmodifiableList(interfaceNames);
        this.typeParameters = Collections.unmodifiableList(typeParameters);
        this.fields = Collections.unmodifiableList(fields);
        this.methods = Collections.unmodifiableList(methods);
        this.modifiers = Collections.unmodifiableSet(modifiers);
        this.annotations = Collections.unmodifiableList(annotations);
        this.enclosingTypeName = enclosingTypeName;
    }

    public String qualifiedName() {
        return qualifiedName;
    }

    public String simpleName() {
        return simpleName;
    }

    public String packageName() {
        return packageName;
    }

    public TypeKind kind() {
        return kind;
    }

    public String superclassName() {
        return superclassName;
    }

    public List<String> interfaceNames() {
        return interfaceNames;
    }

    public List<String> typeParameters() {
        return typeParameters;
    }

    public List<FieldModel> fields() {
        return fields;
    }

    public List<MethodModel> methods() {
        return methods;
    }

    public Set<Modifier> modifiers() {
        return modifiers;
    }

    public List<AnnotationSpec> annotations() {
        return annotations;
    }

    /** 外部类全限定名；顶层类为 null。 */
    public String enclosingTypeName() {
        return enclosingTypeName;
    }

    public boolean isRecord() {
        return kind == TypeKind.RECORD;
    }

    public boolean isEnum() {
        return kind == TypeKind.ENUM;
    }

    /** 找出名为 {@code name} 的字段。 */
    public FieldModel field(String name) {
        for (FieldModel f : fields) {
            if (f.name().equals(name)) {
                return f;
            }
        }
        return null;
    }

    /** 找出名为 {@code name}、参数个数为 {@code arity} 的方法。 */
    public MethodModel method(String name, int arity) {
        for (MethodModel m : methods) {
            if (m.name().equals(name) && m.parameters().size() == arity) {
                return m;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "TypeModel{" + qualifiedName + "}";
    }
}
