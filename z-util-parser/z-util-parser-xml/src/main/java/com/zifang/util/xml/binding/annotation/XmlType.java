package com.zifang.util.xml.binding.annotation;

import java.lang.annotation.*;

/**
 * 指定序列化时子元素的输出顺序。
 * <pre>
 *   &#064;XmlType(propOrder = {"code", "province", "city", "district"})
 *   public static class Item { ... }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface XmlType {
    /**
     * 属性名数组，按顺序输出。
     */
    String[] propOrder() default {};
}
