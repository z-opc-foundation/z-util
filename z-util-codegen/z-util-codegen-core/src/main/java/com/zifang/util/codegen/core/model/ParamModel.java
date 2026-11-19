package com.zifang.util.codegen.core.model;

import java.util.Collections;
import java.util.List;

/**
 * 形参模型。
 *
 * @author zifang
 */
public final class ParamModel {

    private final String name;
    private final String typeName;
    private final List<AnnotationSpec> annotations;

    public ParamModel(String name, String typeName, List<AnnotationSpec> annotations) {
        this.name = name;
        this.typeName = typeName;
        this.annotations = Collections.unmodifiableList(annotations);
    }

    public String name() {
        return name;
    }

    public String typeName() {
        return typeName;
    }

    public List<AnnotationSpec> annotations() {
        return annotations;
    }

    @Override
    public String toString() {
        return "ParamModel{" + typeName + " " + name + "}";
    }
}
