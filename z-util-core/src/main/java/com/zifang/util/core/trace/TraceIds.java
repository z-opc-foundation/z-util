package com.zifang.util.core.trace;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 自研轻量 traceId/spanId 生成器（无 OpenTelemetry / Brave / Zipkin 依赖）。
 * <p>
 * 借鉴 W3C Trace Context 概念（{@code traceparent}）：
 * <ul>
 *   <li>traceId：16 hex (64-bit)，全链路唯一</li>
 *   <li>spanId：8 hex (32-bit)，单个操作唯一</li>
 *   <li>parentSpanId：8 hex（0 表示无父）</li>
 * </ul>
 */
public final class TraceIds {

    /**
     * 进程内递增 spanId 计数器（备选；用 counter 而非 random 也行）。
     */
    private static final AtomicLong COUNTER = new AtomicLong();

    private TraceIds() {
    }

    /**
     * 生成新 traceId。
     */
    public static String newTraceId() {
        // 16 字节 → 32 hex
        return longToHex16(randomLong());
    }

    /**
     * 生成新 spanId。
     */
    public static String newSpanId() {
        return intToHex8((int) (randomLong() & 0xFFFFFFFFL));
    }

    /**
     * 用相同 traceId 创建子 span。
     */
    public static String childSpanId(String parentSpanId) {
        if (parentSpanId == null) return newSpanId();
        return intToHex8((int) (randomLong() & 0xFFFFFFFFL));
    }

    public static String sequentialSpanId() {
        return intToHex8((int) (COUNTER.incrementAndGet() & 0xFFFFFFFFL));
    }

    // ===== W3C traceparent 解析 =====
    // 格式：<version>-<traceId>-<parentSpanId>-<flags>
    // 例：00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01

    /**
     * 解析 traceparent header。
     */
    public static TraceContext parseTraceparent(String header) {
        if (header == null || header.isEmpty()) return null;
        String[] parts = header.split("-");
        if (parts.length != 4) return null;
        if (parts[0].length() != 2 || parts[1].length() != 32 || parts[2].length() != 16) return null;
        return new TraceContext(parts[1], newSpanId(), parts[2]);
    }

    /**
     * 格式化为 traceparent header。
     */
    public static String formatTraceparent(TraceContext ctx) {
        if (ctx == null) return null;
        return "00-" + ctx.traceId + "-" + ctx.spanId + "-01";
    }

    private static long randomLong() {
        return UUID.randomUUID().getLeastSignificantBits() ^ UUID.randomUUID().getMostSignificantBits();
    }

    private static String longToHex16(long v) {
        return String.format("%016x", v);
    }

    private static String intToHex8(int v) {
        return String.format("%08x", v);
    }
}
