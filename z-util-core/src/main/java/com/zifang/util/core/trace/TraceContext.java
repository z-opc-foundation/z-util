package com.zifang.util.core.trace;

/**
 * 单次追踪上下文。
 * <p>
 * 字段对应 W3C trace context。
 */
public final class TraceContext {

    final String traceId;       // 32 hex
    final String spanId;        // 16 hex
    final String parentSpanId;  // 16 hex
    private final long startNanos;

    public TraceContext(String traceId, String spanId, String parentSpanId) {
        this.traceId = traceId;
        this.spanId = spanId;
        this.parentSpanId = parentSpanId == null || parentSpanId.isEmpty() ? "0000000000000000" : parentSpanId;
        this.startNanos = System.nanoTime();
    }

    public String traceId() {
        return traceId;
    }

    public String spanId() {
        return spanId;
    }

    public String parentSpanId() {
        return parentSpanId;
    }

    public long startNanos() {
        return startNanos;
    }

    public long elapsedNanos() {
        return System.nanoTime() - startNanos;
    }

    @Override
    public String toString() {
        return "TraceContext{traceId=" + traceId + " spanId=" + spanId + " parent=" + parentSpanId + "}";
    }
}
