package com.zifang.util.xml.binding.annotation;

import java.lang.annotation.*;

/**
 * 指定属性 / 字段 / setter 对应的 XML 子元素标签名。
 * <p>
 * 可标注在字段、getter 方法、setter 方法上，按此优先级读取。
 * <pre>
 *   &#064;XmlElement(name = "SYSID")
 *   public void setSysId(String sysId) { ... }
 * </pre>
 * 若不加此注解，元素名取属性名（setter 中 "set" 后首字母小写后的名称）。
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XmlElement {
    /**
     * 元素名，默认为空字符串（此时取属性名）。
     */
    String name() default "";
}
