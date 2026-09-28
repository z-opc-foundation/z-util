package com.zifang.util.http.sse;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * SseParser的单元测试，重点是分包与容错。
 */
public class SseParserTest {

    @Test
    public void testBasicFrame() {
        List<SseEvent> events = new SseParser().feed("data: hello\n\n");
        assertEquals(1, events.size());
        assertEquals("message", events.get(0).event());
        assertEquals("hello", events.get(0).data());
    }

    @Test
    public void testTornAcrossChunksIncludingBlankLine() {
        SseParser parser = new SseParser();
        assertTrue(parser.feed("event: token\nda").isEmpty());
        List<SseEvent> part = parser.feed("ta: {\"a\":1}\n");
        assertTrue("数据行已完整但缺结束空行，不该提前发帧", part.isEmpty());
        part = parser.feed("\n");
        assertEquals(1, part.size());
        assertEquals("token", part.get(0).event());
        assertEquals("{\"a\":1}", part.get(0).data());
    }

    @Test
    public void testOneBytePerFeedStillWorks() {
        String wire = "id: 3\nretry: 200\nevent: done\ndata: 中文 ✓\n\n";
        SseParser parser = new SseParser();
        byte[] bytes = wire.getBytes(StandardCharsets.UTF_8);
        List<SseEvent> all = new java.util.ArrayList<>();
        for (byte b : bytes) {
            all.addAll(parser.feed(new byte[]{b}));
        }
        assertEquals(1, all.size());
        assertEquals("中文 ✓", all.get(0).data());
        assertEquals("3", all.get(0).id());
        assertEquals(Long.valueOf(200), all.get(0).retryMillis());
    }

    @Test
    public void testMultipleFramesInOneChunk() {
        List<SseEvent> events = new SseParser().feed("data: a\n\ndata: b\n\ndata: c\n\n");
        assertEquals(3, events.size());
        assertEquals("a", events.get(0).data());
        assertEquals("c", events.get(2).data());
    }

    @Test
    public void testCommentsAreIgnoredAndDoNotFormEvents() {
        SseParser parser = new SseParser();
        assertTrue(parser.feed(": keep-alive\n\n").isEmpty());
        List<SseEvent> events = parser.feed("data: after-heartbeat\n\n");
        assertEquals(1, events.size());
        assertEquals("after-heartbeat", events.get(0).data());
    }

    @Test
    public void testMultiLineDataJoinedWithNewline() {
        List<SseEvent> events = new SseParser().feed("data: l1\ndata: l2\ndata: l3\n\n");
        assertEquals("l1\nl2\nl3", events.get(0).data());
    }

    @Test
    public void testExactlyOneSpaceIsStrippedNotTheRest() {
        assertEquals("   kept", new SseParser().feed("data:    kept\n\n").get(0).data());
        assertEquals("no-space", new SseParser().feed("data:no-space\n\n").get(0).data());
        assertEquals("a:b:c", new SseParser().feed("data: a:b:c\n\n").get(0).data());
    }

    @Test
    public void testCrLfAndLoneCr() {
        List<SseEvent> events = new SseParser().feed("event: e\r\ndata: d\r\n\r\n");
        assertEquals(1, events.size());
        assertEquals("e", events.get(0).event());
        assertEquals("d", events.get(0).data());
    }

    @Test
    public void testEmptyDataBufferNeverDispatches() {
        SseParser parser = new SseParser();
        assertTrue("无冒号的行按字段名+空值处理，data 缓冲仍为空", parser.feed("data\n\n").isEmpty());
        assertTrue(parser.feed("data: \n\n").isEmpty());
        assertTrue("只带 id/retry 的帧不产事件", parser.feed("id: 7\nretry: 100\n\n").isEmpty());
        assertEquals("但状态要保留", "7", parser.lastEventId());
        assertEquals(Long.valueOf(100), parser.lastRetryMillis());
        List<SseEvent> events = parser.feed("data: real\n\n");
        assertEquals(1, events.size());
        assertEquals("real", events.get(0).data());
        assertEquals("7", events.get(0).id());
    }

    @Test
    public void testUnknownFieldsIgnored() {
        List<SseEvent> events = new SseParser().feed("foo: bar\ndata: kept\n\n");
        assertEquals(1, events.size());
        assertEquals("kept", events.get(0).data());
    }

    @Test
    public void testIdAndRetryPersistAcrossFrames() {
        SseParser parser = new SseParser();
        assertEquals("7", parser.feed("id: 7\nretry: 3000\ndata: first\n\n").get(0).id());
        SseEvent second = parser.feed("data: second\n\n").get(0);
        assertEquals("后续帧没写 id 时沿用上一次的值", "7", second.id());
        assertEquals(Long.valueOf(3000), second.retryMillis());
        assertEquals("7", parser.lastEventId());
        assertEquals(Long.valueOf(3000), parser.lastRetryMillis());
    }

    @Test
    public void testNonNumericRetryIgnored() {
        List<SseEvent> events = new SseParser().feed("retry: soon\ndata: d\n\n");
        assertNull(events.get(0).retryMillis());
    }

    @Test
    public void testFinishFlushesUnterminatedFrame() {
        SseParser parser = new SseParser();
        assertTrue(parser.feed("event: partial\ndata: tail").isEmpty());
        List<SseEvent> tail = parser.finish();
        assertEquals(1, tail.size());
        assertEquals("partial", tail.get(0).event());
        assertEquals("tail", tail.get(0).data());
        assertTrue(parser.finish().isEmpty());
    }

    @Test
    public void testResetDropsPartialFrame() {
        SseParser parser = new SseParser();
        parser.feed("data: half");
        parser.reset();
        assertTrue(parser.finish().isEmpty());
    }

    @Test
    public void testDoneMarkerDetected() {
        assertTrue(new SseParser().feed("data: [DONE]\n\n").get(0).isDone());
        assertFalse(new SseParser().feed("data: [DONE] extra\n\n").get(0).isDone());
    }

    @Test
    public void testParseBulkBodyWithTrailingPartialFrame() {
        List<SseEvent> events = SseParser.parse("data: a\n\ndata: b\n\ndata: c");
        assertEquals(3, events.size());
        assertEquals("c", events.get(2).data());
    }

    @Test
    public void testUtf8SplitInsideMultiByteSequence() {
        // "你" = E4 BD A0，故意切成 E4 | BD A0 两块
        byte[] utf8 = "data: 你\n\n".getBytes(StandardCharsets.UTF_8);
        SseParser parser = new SseParser();
        assertTrue(parser.feed(java.util.Arrays.copyOfRange(utf8, 0, 7)).isEmpty());
        List<SseEvent> events = parser.feed(java.util.Arrays.copyOfRange(utf8, 7, utf8.length));
        assertEquals(1, events.size());
        assertEquals("你", events.get(0).data());
    }
}
