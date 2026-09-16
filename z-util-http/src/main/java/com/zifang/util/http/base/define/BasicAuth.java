package com.zifang.util.http.base.define;

/**
 * Basic认证注解
 * <p>
 * 用于在HTTP请求中携带Basic认证信息。
 * </p>
 *
 * @author zifang
 */
public @interface BasicAuth {

    /**
     * 用户名。
     *
     * @return 用户名
     */
    String userName();

    /**
     * 密码。
     *
     * @return 密码
     */
    String password();
}
