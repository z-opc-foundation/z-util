package com.zifang.util.json.annotation;

import java.lang.annotation.*;

/**
 * 标记在 POJO 字段上，控制反序列化行为。
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonDeserialize {

    /**
     * 指定自定义反序列化器类。
     * 默认值为 {@link #NONE}，表示使用默认反序列化逻辑。
     */
    Class<?> using() default NONE.class;

    /**
     * 反序列化时的 JSON 属性名（用于从不同字段名读取）。
     */
    String name() default "";

    /**
     * 反序列化时使用的格式字符串（如日期格式）。
     * 默认空字符串，表示使用反序列化器内置默认格式。
     */
    String format() default "";

    /**
     * 空标记类，用于表示"不指定自定义反序列化器"。
     */
    final class NONE {}
}
