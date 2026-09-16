package com.zifang.util.core.trace;

import org.junit.After;
import org.junit.Test;
import org.slf4j.MDC;

import static org.junit.Assert.*;

/**
 * 自研 trace 测试。
 */
public class TraceContextHolderTest {

    @After
    public void cleanup() {
        TraceContextHolder.stop();
    }

    @Test
    public void testStartNew_setsMdc() {
        TraceContext ctx = TraceContextHolder.startNew();
        assertEquals(ctx.traceId(), TraceContextHolder.currentTraceId());
        assertEquals(ctx.spanId(), TraceContextHolder.currentSpanId());
        // MDC 同步
        assertEquals(ctx.traceId(), MDC.get(TraceContextHolder.MDC_TRACE_ID));
        assertEquals(ctx.spanId(), MDC.get(TraceContextHolder.MDC_SPAN_ID));
        assertEquals("0000000000000000", ctx.parentSpanId());
    }

    @Test
    public void testChildSpanInheritsTrace() {
        TraceContext parent = TraceContextHolder.startNew();
        TraceContext child = TraceContextHolder.startChild();
        assertEquals(parent.traceId(), child.traceId());
        assertNotEquals(parent.spanId(), child.spanId());
        assertEquals(parent.spanId(), child.parentSpanId());
    }

    @Test
    public void testWithChild_restoresParent() {
        TraceContext parent = TraceContextHolder.startNew();
        String parentSpan = parent.spanId();
        TraceContextHolder.withChild(() -> {
            TraceContext c = TraceContextHolder.current();
            assertNotNull(c);
            assertNotEquals(parentSpan, c.spanId());
        });
        // 子执行完应恢复父
        assertEquals(parentSpan, TraceContextHolder.currentSpanId());
    }

    @Test
    public void testStop_clearsMdc() {
        TraceContextHolder.startNew();
        TraceContextHolder.stop();
        assertNull(TraceContextHolder.current());
        assertNull(MDC.get(TraceContextHolder.MDC_TRACE_ID));
        assertEquals("no-trace", TraceContextHolder.currentTraceId());
    }

    @Test
    public void testParseTraceparent() {
        TraceContext ctx = TraceIds.parseTraceparent("00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01");
        assertNotNull(ctx);
        assertEquals("4bf92f3577b34da6a3ce929d0e0e4736", ctx.traceId());
        assertEquals("00f067aa0ba902b7", ctx.parentSpanId());
        // spanId 是新生成的
        assertNotEquals("00f067aa0ba902b7", ctx.spanId());
    }

    @Test
    public void testFormatTraceparent() {
        TraceContext ctx = new TraceContext("4bf92f3577b34da6a3ce929d0e0e4736", "00f067aa0ba902b7", null);
        String tp = TraceIds.formatTraceparent(ctx);
        assertEquals("00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01", tp);
    }
}
