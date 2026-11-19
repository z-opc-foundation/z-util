package com.zifang.util.codegen.core.spec;

import com.zifang.util.codegen.core.expr.Expr;
import com.zifang.util.codegen.core.expr.TypeRef;
import com.zifang.util.codegen.core.model.Modifier;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * 字段规格：注入到目标类里的字段。
 *
 * @author zifang
 */
public final class FieldSpec {

    private final String name;
    private final TypeRef type;
    private final Set<Modifier> modifiers;
    private final Expr initializer;

    public FieldSpec(String name, TypeRef type, Expr initializer, Modifier... modifiers) {
        this.name = name;
        this.type = type;
        this.initializer = initializer;
        this.modifiers = Collections.unmodifiableSet(modifierSet(modifiers));
    }

    public String name() {
        return name;
    }

    public TypeRef type() {
        return type;
    }

    public Set<Modifier> modifiers() {
        return modifiers;
    }

    public Expr initializer() {
        return initializer;
    }

    private static Set<Modifier> modifierSet(Modifier... mods) {
        Set<Modifier> set = EnumSet.noneOf(Modifier.class);
        Collections.addAll(set, mods);
        return set;
    }
}
