package com.zifang.util.http.base.define;


import java.lang.annotation.*;

/**
 * 路径变量注解
 * <p>
 * 用于将URL路径中的变量绑定到方法参数。
 * 例如 /users/{id} 中的 {id} 可以通过 @PathVariable 绑定到参数。
 * </p>
 *
 * @author zifang
 */
@Target({ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
/**
 * PathVariable注解。
 */
public @interface PathVariable {

    /**
     * 路径变量的名称。
     *
     * @return 变量名称
     */
    String value();
}
