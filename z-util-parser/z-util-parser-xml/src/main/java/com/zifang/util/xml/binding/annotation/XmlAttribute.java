package com.zifang.util.xml.binding.annotation;

import java.lang.annotation.*;

/**
 * 指定属性 / 字段输出为 XML 属性（而非子元素）。
 * <pre>
 *   &#064;XmlAttribute(name = "lang")
 *   public String getLang() { ... }
 * </pre>
 * 输出：&lt;root lang="en"&gt;...&lt;/root&gt;
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XmlAttribute {
    /**
     * 属性名，默认为空字符串（此时取 Java 属性名）。
     */
    String name() default "";
}
