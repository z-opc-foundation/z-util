package com.zifang.util.http.sse;

import okhttp3.sse.EventSource;

/**
 * SSE 流句柄 - sendSse 的返回值，调用方持有它即可控制流的生命周期。
 * <p>
 * 之前 sendSse 返回 void，EventSource 在闭包内创建后被丢弃，调用方无法取消流；
 * 现在返回本句柄，cancel() 主动断开（幂等，流已结束/失败后再调是无害 no-op）。
 */
public class SseStreamHandle {

    private final EventSource source;
    private volatile boolean cancelled;

    public SseStreamHandle(EventSource source) {
        this.source = source;
    }

    /**
     * 取消流：断开连接、停止回调（幂等）。
     */
    public void cancel() {
        cancelled = true;
        if (source != null) {
            source.cancel();
        }
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
