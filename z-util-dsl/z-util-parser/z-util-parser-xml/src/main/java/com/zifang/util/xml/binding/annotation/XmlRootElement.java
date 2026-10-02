package com.zifang.util.xml.binding.annotation;

import java.lang.annotation.*;

/**
 * 指定 XML 根元素名称。
 * <p>
 * 用法：标注在类上，value 值即为 XML 根元素标签名。
 * <pre>
 *   &#064;XmlRootElement(name = "SMSDELIVERREQ")
 *   public class SmsDeliverReq { ... }
 * </pre>
 * 若不加此注解，根元素名取简单类名。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XmlRootElement {
    /**
     * 根元素名，默认为空字符串（此时取简单类名）。
     */
    String name() default "";
}
