package com.zifang.util.ioc.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

/**
 * 注入点（字段或参数）的元数据，对标 Guice 的 {@code InjectionPoint}。
 * <p>
 * 携带被注入成员的位置、类型、限定符等信息，供 {@link FieldInjector} /
 * {@link ConstructorInjector} 等解析。
 */
public class InjectionPoint {

    private final Class<?> declaringClass;
    private final String memberName;
    private final Class<?> requiredType;
    private final Class<? extends Annotation> qualifier;
    private final boolean isProvider;
    private final AnnotatedElement element;

    private InjectionPoint(Class<?> declaringClass, String memberName, Class<?> requiredType,
                           Class<? extends Annotation> qualifier, boolean isProvider,
                           AnnotatedElement element) {
        this.declaringClass = declaringClass;
        this.memberName = memberName;
        this.requiredType = requiredType;
        this.qualifier = qualifier;
        this.isProvider = isProvider;
        this.element = element;
    }

    /**
     * 从字段创建 InjectionPoint。
     */
    public static InjectionPoint forField(Field field) {
        NamedFinder nf = NamedFinder.find(field.getAnnotations());
        Class<?> type = field.getType();
        boolean isProvider = javax.inject.Provider.class.isAssignableFrom(type);
        if (isProvider && field.getGenericType() instanceof java.lang.reflect.ParameterizedType) {
            java.lang.reflect.ParameterizedType pt = (java.lang.reflect.ParameterizedType) field.getGenericType();
            java.lang.reflect.Type[] args = pt.getActualTypeArguments();
            if (args.length == 1 && args[0] instanceof Class<?>) {
                Class<?> cls = (Class<?>) args[0];
                type = cls;
            }
        }
        return new InjectionPoint(field.getDeclaringClass(), field.getName(), type, nf.qualifier, isProvider, field);
    }

    /**
     * 从构造器参数创建 InjectionPoint。
     */
    public static InjectionPoint forParameter(Parameter param) {
        return forParameter(param, -1);
    }

    /**
     * 从构造器参数创建 InjectionPoint，并显式传入参数索引（Java 8 中无 {@code Parameter.getIndex()}）。
     */
    public static InjectionPoint forParameter(Parameter param, int index) {
        NamedFinder nf = NamedFinder.find(param.getAnnotations());
        Class<?> type = param.getType();
        boolean isProvider = javax.inject.Provider.class.isAssignableFrom(type);
        if (isProvider && param.getParameterizedType() instanceof java.lang.reflect.ParameterizedType) {
            java.lang.reflect.ParameterizedType pt = (java.lang.reflect.ParameterizedType) param.getParameterizedType();
            java.lang.reflect.Type[] args = pt.getActualTypeArguments();
            if (args.length == 1 && args[0] instanceof Class<?>) {
                Class<?> cls = (Class<?>) args[0];
                type = cls;
            }
        }
        String memberName = "param#" + param.getName();
        if (index >= 0) {
            memberName += "#" + index;
        }
        return new InjectionPoint(param.getDeclaringExecutable().getDeclaringClass(),
                memberName,
                type, nf.qualifier, isProvider, param);
    }

    public Class<?> getDeclaringClass() {
        return declaringClass;
    }

    public String getMemberName() {
        return memberName;
    }

    public Class<?> getRequiredType() {
        return requiredType;
    }

    public Class<? extends Annotation> getQualifier() {
        return qualifier;
    }

    public boolean isProvider() {
        return isProvider;
    }

    public AnnotatedElement getElement() {
        return element;
    }

    @Override
    public String toString() {
        return "InjectionPoint{" + declaringClass.getSimpleName() + "." + memberName
                + " required=" + requiredType.getSimpleName()
                + (qualifier != null ? " @" + qualifier.getSimpleName() : "")
                + (isProvider ? " (Provider)" : "")
                + '}';
    }

    /**
     * 内部辅助：从一组注解中找到 {@link javax.inject.Named} 或 {@link javax.inject.Qualifier}。
     */
    static class NamedFinder {
        Class<? extends Annotation> qualifier;

        static NamedFinder find(Annotation[] annos) {
            NamedFinder nf = new NamedFinder();
            for (Annotation a : annos) {
                if (a instanceof javax.inject.Named) {
                    nf.qualifier = javax.inject.Named.class;
                    break;
                }
                if (a.annotationType().isAnnotationPresent(javax.inject.Qualifier.class)) {
                    nf.qualifier = a.annotationType();
                    break;
                }
            }
            return nf;
        }
    }
}
