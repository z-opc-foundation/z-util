package com.zifang.util.codegen.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 转换策略配置：作用于同包下所有 {@link ZMapper} 接口。
 *
 * @author zifang
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ZMapperConfig {

    /** 源属性 → 目标属性的默认命名策略。 */
    NamingStrategy namingStrategy() default NamingStrategy.PROPERTY_NAME;

    /** 属性名匹配是否忽略大小写，默认 false。 */
    boolean caseInsensitive() default false;

    /** 未被映射的源属性时的策略，默认忽略。 */
    UnmappedSourcePolicy unmappedSourcePolicy() default UnmappedSourcePolicy.IGNORE;

    /** 属性命名策略。 */
    enum NamingStrategy {
        /** 直接用属性名。 */
        PROPERTY_NAME,
        /** 下划线转驼峰。 */
        UNDERSCORE_TO_CAMEL_CASE,
        /** 驼峰转下划线。 */
        CAMEL_CASE_TO_UNDERSCORE
    }

    /** 未被映射的源属性时的策略。 */
    enum UnmappedSourcePolicy {
        ERROR, WARN, IGNORE
    }
}
