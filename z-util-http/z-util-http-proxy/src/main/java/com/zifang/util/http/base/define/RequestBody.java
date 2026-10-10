package com.zifang.util.http.base.define;

import java.lang.annotation.*;

/**
 * 请求体注解
 * <p>
 * 用于标记方法参数应从HTTP请求体中获取。
 * </p>
 *
 * @author zifang
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
/**
 * RequestBody注解。
 */
public @interface RequestBody {

}
