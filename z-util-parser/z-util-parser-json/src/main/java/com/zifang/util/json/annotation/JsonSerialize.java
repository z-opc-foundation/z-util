package com.zifang.util.json.annotation;

import java.lang.annotation.*;

/**
 * 标记在 POJO 字段上，控制序列化行为。
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonSerialize {

    /**
     * 指定自定义序列化器类。
     * 默认值为 {@link #NONE}，表示使用默认序列化逻辑。
     */
    Class<?> using() default NONE.class;

    /**
     * 序列化时是否忽略此字段（为 true 则不输出）。
     */
    boolean ignore() default false;

    /**
     * 序列化后的 JSON 属性名（默认使用字段名）。
     */
    String name() default "";

    /**
     * 序列化时使用的格式字符串（如日期格式）。
     * 默认空字符串，表示使用序列化器内置默认格式。
     */
    String format() default "";

    /**
     * 空标记类，用于表示"不指定自定义序列化器"。
     */
    final class NONE {}
}
