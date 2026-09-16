package io.zifu.z.serialize.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个 enum 为可序列化。
 *
 * <p>Enum 在 wire format 上按 VARINT 编码其 ordinal（与 Protobuf 一致）。</p>
 *
 * <p><b>重要：</b>添加新枚举值时必须放在末尾并分配新 ID，以保持向后兼容。</p>
 *
 * @author zifang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ZEnum {

    /**
     * Enum ID（用于 schema registry 中的去重和命名空间管理）。
     */
    int id() default 0;

    /**
     * 人类可读名称。
     */
    String name() default "";
}
