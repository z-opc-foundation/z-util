package com.zifang.util.codegen.core.spi;

import com.zifang.util.codegen.core.model.AnnotationSpec;
import com.zifang.util.codegen.core.model.TypeModel;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import java.util.List;

/**
 * handler 的执行上下文：一次"某个元素被某个注解命中"的处理现场。
 *
 * @author zifang
 */
public final class HandlerContext {

    private final ProcessingEnvironment environment;
    private final RoundEnvironment roundEnvironment;
    private final TypeModel type;
    private final Element target;
    private final List<AnnotationSpec> targetAnnotations;
    private final List<AnnotationSpec> typeAnnotations;
    private final MemberEmitter members;
    private final SourceEmitter sources;

    public HandlerContext(ProcessingEnvironment environment, RoundEnvironment roundEnvironment,
                          TypeModel type, Element target,
                          List<AnnotationSpec> targetAnnotations, List<AnnotationSpec> typeAnnotations,
                          MemberEmitter members, SourceEmitter sources) {
        this.environment = environment;
        this.roundEnvironment = roundEnvironment;
        this.type = type;
        this.target = target;
        this.targetAnnotations = targetAnnotations;
        this.typeAnnotations = typeAnnotations;
        this.members = members;
        this.sources = sources;
    }

    public ProcessingEnvironment environment() {
        return environment;
    }

    /** 当前处理轮次：handler 可以据此找同轮次的其它被注解元素（如嵌套 mapper）。 */
    public RoundEnvironment roundEnvironment() {
        return roundEnvironment;
    }

    /** 被注解元素所属类型的模型。 */
    public TypeModel type() {
        return type;
    }

    /** 被注解的元素本身：类型标注时是 TypeElement，字段标注时是 VariableElement。 */
    public Element target() {
        return target;
    }

    /** 被注解元素上的注解规格。 */
    public List<AnnotationSpec> targetAnnotations() {
        return targetAnnotations;
    }

    /** 所属类型上的注解规格（字段级 handler 想看类型级注解时用）。 */
    public List<AnnotationSpec> typeAnnotations() {
        return typeAnnotations;
    }

    public MemberEmitter members() {
        return members;
    }

    public SourceEmitter sources() {
        return sources;
    }

    /** 找出指定全限定名的注解规格。 */
    public AnnotationSpec annotation(String qualifiedName) {
        for (AnnotationSpec a : targetAnnotations) {
            if (a.qualifiedName().equals(qualifiedName)) {
                return a;
            }
        }
        for (AnnotationSpec a : typeAnnotations) {
            if (a.qualifiedName().equals(qualifiedName)) {
                return a;
            }
        }
        return null;
    }

    /** 找出指定全限定名的注解规格，命中即返回。 */
    public AnnotationSpec findAnnotation(String qualifiedName) {
        return annotation(qualifiedName);
    }
}
