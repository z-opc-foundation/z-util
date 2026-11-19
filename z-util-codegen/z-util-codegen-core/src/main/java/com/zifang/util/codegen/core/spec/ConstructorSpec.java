package com.zifang.util.codegen.core.spec;

import com.zifang.util.codegen.core.expr.TypeRef;
import com.zifang.util.codegen.core.model.Modifier;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * 构造器规格：注入到目标类里的构造器。
 *
 * @author zifang
 */
public final class ConstructorSpec {

    private final Set<Modifier> modifiers;
    private final java.util.List<ParamSpec> parameters;
    private final java.util.List<com.zifang.util.codegen.core.expr.Stmt> body;

    public ConstructorSpec(java.util.List<ParamSpec> parameters,
                           java.util.List<com.zifang.util.codegen.core.expr.Stmt> body,
                           Modifier... modifiers) {
        this.parameters = Collections.unmodifiableList(parameters);
        this.body = Collections.unmodifiableList(body);
        this.modifiers = Collections.unmodifiableSet(modifierSet(modifiers));
    }

    public Set<Modifier> modifiers() {
        return modifiers;
    }

    public java.util.List<ParamSpec> parameters() {
        return parameters;
    }

    public java.util.List<com.zifang.util.codegen.core.expr.Stmt> body() {
        return body;
    }

    private static Set<Modifier> modifierSet(Modifier... mods) {
        Set<Modifier> set = EnumSet.noneOf(Modifier.class);
        Collections.addAll(set, mods);
        return set;
    }
}
