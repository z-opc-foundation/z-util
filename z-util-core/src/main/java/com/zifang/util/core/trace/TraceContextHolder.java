package com.zifang.util.core.trace;

import org.slf4j.MDC;

import java.util.concurrent.Callable;

/**
 * 自研轻量追踪上下文管理器（无 OpenTelemetry SDK 依赖）。
 * <p>
 * 用法：
 * <pre>{@code
 *   TraceContext ctx = TraceContextHolder.startNew();
 *   try {
 *       log.info("...");   // 模板中可用 %X{traceId} %X{spanId}
 *       TraceContextHolder.withChild(() -> doWork());
 *   } finally {
 *       TraceContextHolder.stop();
 *   }
 * }</pre>
 *
 * <h3>线程模型</h3>
 * 使用 {@link ThreadLocal} 存储当前 trace 上下文；父子线程之间不自动传递（需要用户手动 {@code child.fork()}）。
 *
 * <h3>MDC 集成</h3>
 * 启动新 trace 时自动写入 {@code traceId}/{@code spanId} 到 SLF4J MDC，
 * 关闭时清空。
 */
public final class TraceContextHolder {

    public static final String MDC_TRACE_ID = "traceId";
    public static final String MDC_SPAN_ID = "spanId";
    public static final String MDC_PARENT_SPAN_ID = "parentSpanId";

    private static final ThreadLocal<TraceContext> CURRENT = new ThreadLocal<>();

    private TraceContextHolder() {
    }

    /**
     * 当前线程的 trace 上下文（可能为 null）。
     */
    public static TraceContext current() {
        return CURRENT.get();
    }

    /**
     * 当前 traceId（无上下文时返回 "no-trace"）。
     */
    public static String currentTraceId() {
        TraceContext c = CURRENT.get();
        return c == null ? "no-trace" : c.traceId();
    }

    /**
     * 当前 spanId（无上下文时返回 "no-span"）。
     */
    public static String currentSpanId() {
        TraceContext c = CURRENT.get();
        return c == null ? "no-span" : c.spanId();
    }

    /**
     * 启动新 trace，并设为当前上下文。返回新创建的上下文。
     */
    public static TraceContext startNew() {
        TraceContext ctx = new TraceContext(TraceIds.newTraceId(), TraceIds.newSpanId(), null);
        bind(ctx);
        return ctx;
    }

    /**
     * 设置已有上下文（如从 HTTP 头解析得到）。
     */
    public static TraceContext startWith(TraceContext ctx) {
        bind(ctx);
        return ctx;
    }

    /**
     * 创建子 span，traceId 不变，parentSpanId = 当前 spanId。
     */
    public static TraceContext startChild() {
        TraceContext parent = CURRENT.get();
        String parentId = parent == null ? null : parent.spanId();
        TraceContext child = new TraceContext(
                parent == null ? TraceIds.newTraceId() : parent.traceId(),
                TraceIds.newSpanId(), parentId);
        bind(child);
        return child;
    }

    /**
     * 清除当前上下文（关闭 trace）。
     */
    public static void stop() {
        CURRENT.remove();
        MDC.remove(MDC_TRACE_ID);
        MDC.remove(MDC_SPAN_ID);
        MDC.remove(MDC_PARENT_SPAN_ID);
    }

    /**
     * 在子上下文里执行任务。
     */
    public static <T> T withChild(Callable<T> task) throws Exception {
        TraceContext previous = CURRENT.get();
        startChild();
        try {
            return task.call();
        } finally {
            if (previous == null) stop();
            else bind(previous);
        }
    }

    public static void withChild(Runnable task) {
        try {
            withChild(() -> {
                task.run();
                return null;
            });
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void bind(TraceContext ctx) {
        CURRENT.set(ctx);
        MDC.put(MDC_TRACE_ID, ctx.traceId());
        MDC.put(MDC_SPAN_ID, ctx.spanId());
        MDC.put(MDC_PARENT_SPAN_ID, ctx.parentSpanId());
    }
}
