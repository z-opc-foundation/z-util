package com.zifang.util.http.net.bookdemo;

import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;

/**
 * NoGovernmentCookies类。
 */
public class NoGovernmentCookies implements CookiePolicy {

    @Override
    /**
     * shouldAccept方法。
     *      * @param uri URI类型参数
     * @param cookie HttpCookie类型参数
     * @return boolean类型返回值
     */
    public boolean shouldAccept(URI uri, HttpCookie cookie) {
        return !uri.getAuthority().toLowerCase().endsWith(".gov") && !cookie.getDomain().toLowerCase().endsWith(".gov");
    }
}