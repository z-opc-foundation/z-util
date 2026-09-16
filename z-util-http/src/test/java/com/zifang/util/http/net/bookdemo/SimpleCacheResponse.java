package com.zifang.util.http.net.bookdemo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CacheResponse;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * SimpleCacheResponse类。
 */
public class SimpleCacheResponse extends CacheResponse {

    private final Map<String, List<String>> headers;
    private final SimpleCacheRequest request;
    private final Date expires;
    private final CacheControl control;

    /**
     * SimpleCacheResponse方法。
     * * @param request SimpleCacheRequest类型参数
     *
     * @param uc      URLConnection类型参数
     * @param control CacheControl类型参数
     */
    public SimpleCacheResponse(SimpleCacheRequest request, URLConnection uc, CacheControl control) throws IOException {

        this.request = request;
        this.control = control;
        this.expires = new Date(uc.getExpiration());
        this.headers = Collections.unmodifiableMap(uc.getHeaderFields());
    }

    @Override
    /**
     * getBody方法。
     * @return InputStream类型返回值
     */
    public InputStream getBody() {
        return new ByteArrayInputStream(request.getData());
    }

    @Override
    /**
     * getHeaders方法。
     * @return Map<String, List<String>>类型返回值
     */
    public Map<String, List<String>> getHeaders() throws IOException {
        return headers;
    }

    /**
     * getControl方法。
     *
     * @return CacheControl类型返回值
     */
    public CacheControl getControl() {
        return control;
    }

    /**
     * isExpired方法。
     *
     * @return boolean类型返回值
     */
    public boolean isExpired() {
        Date now = new Date();
        if (control.getMaxAge().before(now))
            return true;
        else if (expires != null && control.getMaxAge() != null) {
            return expires.before(now);
        } else {
            return false;
        }
    }
}