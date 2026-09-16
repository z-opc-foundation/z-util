package com.zifang.util.http.net.bookdemo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.CacheRequest;

/**
 * SimpleCacheRequest类。
 */
public class SimpleCacheRequest extends CacheRequest {

    private ByteArrayOutputStream out = new ByteArrayOutputStream();

    @Override
    /**
     * getBody方法。
     * @return OutputStream类型返回值
     */
    public OutputStream getBody() throws IOException {
        return out;
    }

    @Override
    /**
     * abort方法。
     */
    public void abort() {
        out.reset();
    }

    /**
     * getData方法。
     *
     * @return byte[]类型返回值
     */
    public byte[] getData() {
        if (out.size() == 0)
            return null;
        else
            return out.toByteArray();
    }
}