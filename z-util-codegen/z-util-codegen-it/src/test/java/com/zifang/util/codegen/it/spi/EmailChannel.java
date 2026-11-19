package com.zifang.util.codegen.it.spi;

import com.zifang.util.codegen.annotation.ZAutoService;

/**
 * 一个 provider 同时注册两个 service（数组形式）。
 */
@ZAutoService({AlertChannel.class, LogSink.class})
public class EmailChannel implements AlertChannel, LogSink {

    @Override
    public String send(String message) {
        return "email:" + message;
    }

    @Override
    public void write(String line) {
        // no-op
    }
}
