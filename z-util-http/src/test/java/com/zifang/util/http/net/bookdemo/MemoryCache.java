package com.zifang.util.http.net.bookdemo;

import java.io.IOException;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MemoryCache类。
 */
public class MemoryCache extends ResponseCache {

    private final Map<URI, SimpleCacheResponse> responses = new ConcurrentHashMap<URI, SimpleCacheResponse>();
    private final int maxEntries;

    /**
     * MemoryCache方法。
     */
    public MemoryCache() {
        this(100);
    }

    /**
     * MemoryCache方法。
     * * @param maxEntries int类型参数
     */
    public MemoryCache(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    @Override
    /**
     * put方法。
     *      * @param uri URI类型参数
     * @param conn URLConnection类型参数
     * @return CacheRequest类型返回值
     */
    public CacheRequest put(URI uri, URLConnection conn) throws IOException {

        if (responses.size() >= maxEntries)
            return null;

        CacheControl control = new CacheControl(conn.getHeaderField("Cache-Control"));
        if (control.noStore()) {
            return null;
        } else if (!conn.getHeaderField(0).startsWith("GET ")) {
            // only cache GET
            return null;
        }

        SimpleCacheRequest request = new SimpleCacheRequest();
        SimpleCacheResponse response = new SimpleCacheResponse(request, conn, control);

        responses.put(uri, response);
        return request;
    }

    @Override
    /**
     * get方法。
     *      * @param uri URI类型参数
     * @param requestMethod String类型参数
     * @param requestHeaders MapString,类型参数
     * @return CacheResponse类型返回值
     */
    public CacheResponse get(URI uri, String requestMethod, Map<String, List<String>> requestHeaders)
            throws IOException {

        if ("GET".equals(requestMethod)) {
            SimpleCacheResponse response = responses.get(uri);
            // check expiration date
            if (response != null && response.isExpired()) {
                responses.remove(response);
                response = null;
            }
            return response;
        } else {
            return null;
        }
    }
}