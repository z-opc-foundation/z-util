package com.zifang.util.core.pattern.visitor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Visitor 契约: 元素 accept 应把自身回传给 visitor.visit, visitor.visit 拿到正确运行时类型.
 */
class VisitorTest {

    interface Animal extends Visitable {
        @Override
        <R> R accept(Visitor<R> v);
    }

    static final class Dog implements Animal {
        @Override public <R> R accept(Visitor<R> v) { return v.visit(this); }
        public String bark() { return "woof"; }
    }

    static final class Cat implements Animal {
        @Override public <R> R accept(Visitor<R> v) { return v.visit(this); }
        public String meow() { return "meow"; }
    }

    static final class SoundVisitor implements Visitor<String> {
        @Override public String visit(Visitable element) {
            if (element instanceof Dog) return ((Dog) element).bark();
            if (element instanceof Cat) return ((Cat) element).meow();
            return "?";
        }
    }

    @Test
    @DisplayName("visitor.visit 收到的 Visitable 是真实运行时类型")
    void visitDispatchesByRuntimeType() {
        List<Animal> animals = new ArrayList<Animal>();
        animals.add(new Dog());
        animals.add(new Cat());
        SoundVisitor v = new SoundVisitor();
        assertEquals("woof", animals.get(0).accept(v));
        assertEquals("meow", animals.get(1).accept(v));
    }
}