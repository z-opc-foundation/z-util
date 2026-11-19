package com.zifang.util.codegen.core.spec;

import com.zifang.util.codegen.core.expr.Stmt;
import com.zifang.util.codegen.core.expr.TypeRef;
import com.zifang.util.codegen.core.model.Modifier;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * 方法规格：注入到目标类里的方法。
 *
 * @author zifang
 */
public final class MethodSpec {

    private final String name;
    private final TypeRef returnType;
    private final List<ParamSpec> parameters;
    private final Set<Modifier> modifiers;
    private final List<Stmt> body;
    private final List<String> annotations;

    private MethodSpec(Builder builder) {
        this.name = builder.name;
        this.returnType = builder.returnType;
        this.parameters = Collections.unmodifiableList(builder.parameters);
        this.modifiers = builder.modifiers.isEmpty()
                ? Collections.emptySet()
                : Collections.unmodifiableSet(EnumSet.copyOf(builder.modifiers));
        this.body = Collections.unmodifiableList(builder.body);
        this.annotations = Collections.unmodifiableList(builder.annotations);
    }

    public static Builder named(String name) {
        return new Builder(name);
    }

    public String name() {
        return name;
    }

    public TypeRef returnType() {
        return returnType;
    }

    public List<ParamSpec> parameters() {
        return parameters;
    }

    public Set<Modifier> modifiers() {
        return modifiers;
    }

    public List<Stmt> body() {
        return body;
    }

    public List<String> annotations() {
        return annotations;
    }

    /** 方法规格构造器。 */
    public static final class Builder {

        private final String name;
        private TypeRef returnType = TypeRef.primitive("void");
        private final List<ParamSpec> parameters = new java.util.ArrayList<>();
        private final Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
        private final List<Stmt> body = new java.util.ArrayList<>();
        private final List<String> annotations = new java.util.ArrayList<>();

        private Builder(String name) {
            this.name = name;
        }

        public Builder returns(TypeRef type) {
            this.returnType = type;
            return this;
        }

        public Builder param(ParamSpec param) {
            this.parameters.add(param);
            return this;
        }

        public Builder param(String name, TypeRef type) {
            return param(new ParamSpec(name, type));
        }

        public Builder modifiers(Modifier... mods) {
            Collections.addAll(this.modifiers, mods);
            return this;
        }

        public Builder body(List<Stmt> statements) {
            this.body.addAll(statements);
            return this;
        }

        public Builder annotation(String qualifiedName) {
            this.annotations.add(qualifiedName);
            return this;
        }

        public MethodSpec build() {
            return new MethodSpec(this);
        }
    }
}
