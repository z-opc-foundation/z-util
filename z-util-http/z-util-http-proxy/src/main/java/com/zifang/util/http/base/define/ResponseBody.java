package com.zifang.util.http.base.define;

import java.lang.annotation.*;

/**
 * 响应体注解
 * <p>
 * 用于标记方法的返回值应作为响应体直接返回，而不是作为视图渲染。
 * </p>
 *
 * @author zifang
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
/**
 * ResponseBody注解。
 */
public @interface ResponseBody {

}
