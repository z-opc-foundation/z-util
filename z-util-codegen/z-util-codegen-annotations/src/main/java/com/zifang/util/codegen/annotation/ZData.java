package com.zifang.util.codegen.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据类快捷注解：一并生成 getter、setter、toString、equals/hashCode 与必填参构造器。
 *
 * <p>等价于 {@link ZGetter} + {@link ZSetter} + {@link ZToString} + {@link ZEqualsAndHashCode}
 * + {@link ZRequiredArgsConstructor}。</p>
 *
 * @author zifang
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface ZData {

    /** toString 中是否包含非静态字段，默认包含。 */
    boolean includeFieldNames() default true;
}
