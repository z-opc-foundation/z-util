package io.zifu.z.serialize.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个 POJO 字段参与 Z-serialization 序列化。
 *
 * <p>字段 ID 是核心 schema 演进机制：</p>
 * <ul>
 *   <li>读旧消息时遇到未知 ID → 安全跳过</li>
 *   <li>读新消息时遇到未知字段 → 用默认值填充</li>
 *   <li>字段名可改但 ID 不可改，否则破坏兼容性</li>
 * </ul>
 *
 * <pre>{@code
 * @ZMessage(id = 100)
 * public class User {
 *     @ZField(id = 1, type = FieldType.VARINT)
 *     public Long id;
 *
 *     @ZField(id = 2, type = FieldType.LENGTH_DELIMITED)
 *     public String name;
 *
 *     // 字段已废弃但保留 ID（避免新版本重用）
 *     @ZField(id = 3, deprecated = true, since = "1.0", removed = "2.0")
 *     public Integer oldAge;
 * }
 * }</pre>
 *
 * @author zifang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ZField {

    /**
     * 字段 ID（1..2^29-1）。
     * <p>取值范围限制与 Protobuf 兼容（保留 0 和 [19000, 19999]）。</p>
     */
    int id();

    /**
     * Wire format 上的物理类型。
     * <p>缺省时由字段的 Java 类型推导。</p>
     */
    FieldType type() default FieldType.VARINT;

    /**
     * 字段是否已废弃（仍可读，但写时不输出；codegen 会警告）。
     */
    boolean deprecated() default false;

    /**
     * 自哪个版本起被标记为 deprecated。
     */
    String since() default "";

    /**
     * 自哪个版本起彻底移除（写时不再写，读时直接跳过）。
     */
    String removed() default "";

    /**
     * 字段为 packed repeated（仅对 VARINT 有效）。
     * <p>为 true 时，列表写入使用 length-delimited + 单字节分隔的紧凑布局，
     * 比逐元素写效率高 50%+。</p>
     */
    boolean packed() default false;

    /**
     * 默认值（字符串）。
     * <p>支持："0", "false", ""(空字符串), "null"。复杂类型不支持。</p>
     */
    String defaultValue() default "";
}
