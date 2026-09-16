package com.zifang.util.xml.binding.annotation;

import java.lang.annotation.*;

/**
 * 为 List / Collection 类型属性添加外层包裹元素。
 * <pre>
 *   &#064;XmlElementWrapper(name = "SMSBODYS")
 *   &#064;XmlElement(name = "SMSBODY")
 *   public List&lt;SmsBody&gt; getSmsBodys() { ... }
 * </pre>
 * @see XmlElement
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XmlElementWrapper {
    /**
     * 包裹元素名，默认为空字符串。
     */
    String name() default "";
}
