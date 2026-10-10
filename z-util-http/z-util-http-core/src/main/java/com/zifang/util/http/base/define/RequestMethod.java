package com.zifang.util.http.base.define;

/**
 * HTTP请求方法枚举
 * <p>
 * 定义了标准HTTP请求方法，包括GET、HEAD、POST、PUT、PATCH、DELETE、OPTIONS、TRACE。
 * </p>
 *
 * @author zifang
 */
public enum RequestMethod {

    /**
     * GET方法，用于请求指定的资源。
     */
    GET,

    /**
     * HEAD方法，类似于GET，但只返回头部信息。
     */
    HEAD,

    /**
     * POST方法，用于提交数据到指定资源。
     */
    POST,

    /**
     * PUT方法，用于更新指定资源。
     */
    PUT,

    /**
     * PATCH方法，用于部分更新指定资源。
     */
    PATCH,

    /**
     * DELETE方法，用于删除指定资源。
     */
    DELETE,

    /**
     * OPTIONS方法，返回服务器支持的HTTP方法。
     */
    OPTIONS,

    /**
     * TRACE方法，用于回显服务器收到的请求。
     */
    TRACE

}