package com.zifang.util.codegen.core.diag;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

/**
 * 诊断出口：把编译期问题按 javac 的等级报出去。
 *
 * @author zifang
 */
public final class Diagnostics {

    private final Messager messager;

    public Diagnostics(Messager messager) {
        this.messager = messager;
    }

    public void error(Element element, String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    public void warn(Element element, String message) {
        messager.printMessage(Diagnostic.Kind.WARNING, message, element);
    }

    public void note(Element element, String message) {
        messager.printMessage(Diagnostic.Kind.NOTE, message, element);
    }
}
