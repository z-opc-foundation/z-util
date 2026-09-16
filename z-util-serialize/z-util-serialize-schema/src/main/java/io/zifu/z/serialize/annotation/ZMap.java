package io.zifu.z.serialize.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个字段为 map（key-value 集合）。
 *
 * <p>Map 在 wire format 上等价于 repeated message，
 * 每个 entry 包含 key (field id=1) 和 value (field id=2)。
 * 兼容标准 protobuf map 编码，可被任何 protobuf 实现解析。</p>
 *
 * @author zifang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ZMap {

    /** Key 的物理类型。 */
    FieldType keyType() default FieldType.LENGTH_DELIMITED;

    /** Value 的物理类型。 */
    FieldType valueType() default FieldType.LENGTH_DELIMITED;

    /** Key 是否 packed（仅 VARINT 有效）。 */
    boolean keyPacked() default false;

    /** Value 是否 packed（仅 VARINT 有效）。 */
    boolean valuePacked() default false;
}
