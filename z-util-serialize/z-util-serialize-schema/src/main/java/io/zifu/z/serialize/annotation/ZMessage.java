package io.zifu.z.serialize.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个 POJO 类为可序列化 message。
 *
 * <p>每个 message 必须有全局唯一 ID（命名空间内唯一）。</p>
 *
 * <pre>{@code
 * @ZMessage(id = 100, name = "user.v1.User")
 * public class User {
 *     @ZField(id = 1, type = FieldType.VARINT)
 *     public Long id;
 * }
 * }</pre>
 *
 * @author zifang
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ZMessage {

    /**
     * Message 全局唯一 ID（[1, 2^29-1]，[19000, 19999] 保留）。
     * <p>用于 cross-reference、版本路由、schema registry 关联。</p>
     */
    int id();

    /**
     * 人类可读名称（如 "user.v1.User"），跨语言 IDL 用。
     */
    String name() default "";

    /**
     * Schema 版本字符串（"1.0.0"）。
     */
    String version() default "1.0.0";

    /**
     * 是否使用压缩编码（写时整体压缩，读时整体解压）。
     */
    boolean compressed() default false;

    /**
     * 压缩算法（"zstd", "lz4", "gzip"）。仅 compressed=true 时生效。
     */
    String compression() default "zstd";

    /**
     * 是否加密（写时 AES-GCM 加密 + 完整性校验）。
     */
    boolean encrypted() default false;

    /**
     * 字段索引策略："none", "minimal", "full"。
     * <ul>
     *   <li>none: 不索引</li>
     *   <li>minimal: 仅记录每个字段的 offset/length（跳过解码用）</li>
     *   <li>full: 解析所有字段的 byte 值（范围查询用，体积大 5-15%）</li>
     * </ul>
     */
    String index() default "minimal";
}
