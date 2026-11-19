package com.zifang.util.codegen.core.model;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 方法模型。
 *
 * @author zifang
 */
public final class MethodModel {

    private final String name;
    private final String returnTypeName;
    private final List<ParamModel> parameters;
    private final Set<Modifier> modifiers;
    private final List<String> typeParameters;
    private final List<String> thrownTypes;
    private final List<AnnotationSpec> annotations;

    public MethodModel(String name, String returnTypeName, List<ParamModel> parameters,
                       Set<Modifier> modifiers, List<String> typeParameters,
                       List<String> thrownTypes, List<AnnotationSpec> annotations) {
        this.name = name;
        this.returnTypeName = returnTypeName;
        this.parameters = Collections.unmodifiableList(parameters);
        this.modifiers = Collections.unmodifiableSet(modifiers);
        this.typeParameters = Collections.unmodifiableList(typeParameters);
        this.thrownTypes = Collections.unmodifiableList(thrownTypes);
        this.annotations = Collections.unmodifiableList(annotations);
    }

    public String name() {
        return name;
    }

    public String returnTypeName() {
        return returnTypeName;
    }

    public List<ParamModel> parameters() {
        return parameters;
    }

    public Set<Modifier> modifiers() {
        return modifiers;
    }

    public List<String> typeParameters() {
        return typeParameters;
    }

    public List<String> thrownTypes() {
        return thrownTypes;
    }

    public List<AnnotationSpec> annotations() {
        return annotations;
    }

    public boolean isStatic() {
        return modifiers.contains(Modifier.STATIC);
    }

    public boolean isAbstract() {
        return modifiers.contains(Modifier.ABSTRACT);
    }

    @Override
    public String toString() {
        return "MethodModel{" + returnTypeName + " " + name + "}";
    }
}
