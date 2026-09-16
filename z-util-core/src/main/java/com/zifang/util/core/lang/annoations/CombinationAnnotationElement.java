package com.zifang.util.core.lang.annoations;

import com.zifang.util.core.lang.collection.Sets;

import java.io.Serializable;
import java.lang.annotation.*;
import java.lang.reflect.AnnotatedElement;
import java.util.*;

/**
 * 组合注解 对JDK的原生注解机制做一个增强，支持类似Spring的组合注解。<br>
 * 核心实现使用了递归获取指定元素上的注解以及注解的注解，以实现复合注解的获取。
 * <p>
 * 这个类提供了可以被解析的类型 的递归 全解析过程
 *
 * @author zifang
 */
public class CombinationAnnotationElement implements AnnotatedElement, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 元注解
     */
    private static final Set<Class<? extends Annotation>> META_ANNOTATIONS = Sets.newHashSet(
            Target.class,
            Retention.class,
            Inherited.class,
            Documented.class,
            SuppressWarnings.class,
            Override.class,
            Deprecated.class
    );

    /**
     * 注解类型与注解对象对应表
     */
    private Map<Class<? extends Annotation>, Annotation> annotationMap;

    /**
     * 直接注解类型与注解对象对应表
     */
    private Map<Class<? extends Annotation>, Annotation> declaredAnnotationMap;

    /**
     * @param element 需要解析注解的元素：可以是Class、Method、Field、Constructor、ReflectPermission
     */
    public CombinationAnnotationElement(AnnotatedElement element) {
        init(element);
    }

    @Override
    /**
     * isAnnotationPresent方法。
     *      * @param annotationClass Class?类型参数
     * @return boolean类型返回值
     */
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {

        return annotationMap.containsKey(annotationClass);

    }

    @Override
    /**
     * getAnnotation方法。
     *      * @param annotationClass ClassT类型参数
     * @return <T extends Annotation> T类型返回值
     */
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        Annotation annotation = annotationMap.get(annotationClass);
        return (annotation == null) ? null : (T) annotation;
    }

    @Override
    /**
     * getAnnotations方法。
     * @return Annotation[]类型返回值
     */
    public Annotation[] getAnnotations() {

        final Collection<Annotation> annotations = this.annotationMap.values();
        return annotations.toArray(new Annotation[annotations.size()]);
    }

    @Override
    /**
     * getDeclaredAnnotations方法。
     * @return Annotation[]类型返回值
     */
    public Annotation[] getDeclaredAnnotations() {

        final Collection<Annotation> annotations = this.declaredAnnotationMap.values();
        return annotations.toArray(new Annotation[annotations.size()]);
    }

    /**
     * 初始化
     *
     * @param element 元素
     */
    private void init(AnnotatedElement element) {

        final Annotation[] declaredAnnotations = element.getDeclaredAnnotations();
        this.declaredAnnotationMap = new HashMap<>();
        parseDeclared(declaredAnnotations);

        final Annotation[] annotations = element.getAnnotations();
        if (Arrays.equals(declaredAnnotations, annotations)) {

            this.annotationMap = this.declaredAnnotationMap;
        } else {

            this.annotationMap = new HashMap<>();
            parse(annotations);

        }
    }

    /**
     * 进行递归解析注解，直到全部都是元注解为止
     *
     * @param annotations Class, Method, Field等
     */
    private void parseDeclared(Annotation[] annotations) {
        Class<? extends Annotation> annotationType;
        // 直接注解
        for (Annotation annotation : annotations) {

            annotationType = annotation.annotationType();
            if (!META_ANNOTATIONS.contains(annotationType)) {

                declaredAnnotationMap.put(annotationType, annotation);

                parseDeclared(annotationType.getDeclaredAnnotations());
            }
        }
    }

    /**
     * 进行递归解析注解，直到全部都是元注解为止
     *
     * @param annotations Class, Method, Field等
     */
    private void parse(Annotation[] annotations) {

        Class<? extends Annotation> annotationType;
        for (Annotation annotation : annotations) {
            annotationType = annotation.annotationType();
            if (!META_ANNOTATIONS.contains(annotationType)) {

                annotationMap.put(annotationType, annotation);

                parse(annotationType.getAnnotations());
            }
        }
    }
}