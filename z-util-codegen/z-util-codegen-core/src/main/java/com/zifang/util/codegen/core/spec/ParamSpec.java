package com.zifang.util.codegen.core.spec;

import com.zifang.util.codegen.core.model.Modifier;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * 形参规格。
 *
 * @author zifang
 */
public final class ParamSpec {

    private final String name;
    private final com.zifang.util.codegen.core.expr.TypeRef type;
    private final Set<Modifier> modifiers;

    public ParamSpec(String name, com.zifang.util.codegen.core.expr.TypeRef type, Modifier... modifiers) {
        this.name = name;
        this.type = type;
        this.modifiers = Collections.unmodifiableSet(modifierSet(modifiers));
    }

    public String name() {
        return name;
    }

    public com.zifang.util.codegen.core.expr.TypeRef type() {
        return type;
    }

    public Set<Modifier> modifiers() {
        return modifiers;
    }

    private static Set<Modifier> modifierSet(Modifier... mods) {
        Set<Modifier> set = EnumSet.noneOf(Modifier.class);
        Collections.addAll(set, mods);
        return set;
    }
}
