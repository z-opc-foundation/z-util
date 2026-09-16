package io.zifu.z.serialize.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个字段为 oneof 分支（与 Protobuf 语义一致）。
 *
 * <pre>{@code
 * @ZMessage(id = 100)
 * public class Payment {
 *     @ZOneof({"cardNumber", "iban"})
 *     public Object paymentMethod;  // 可能是 String 或 Iban 对象
 * }
 * }</pre>
 *
 * <p>同组字段最多只有一个被设置。读时按字段 ID 解码匹配。</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ZOneof {

    /** Oneof 组名。 */
    String[] value();
}
