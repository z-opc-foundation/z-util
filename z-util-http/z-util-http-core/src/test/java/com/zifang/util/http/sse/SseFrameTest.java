package com.zifang.util.http.sse;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * SseFrame的单元测试，含帧原子性要求。
 */
public class SseFrameTest {

    @Test
    public void testDefaultSpacing() {
        assertEquals("data: hello\n\n", SseFrame.data("hello").wire());
        assertEquals("event: delta\ndata: {\"a\":1}\n\n",
                SseFrame.create().event("delta").appendData("{\"a\":1}").wire());
    }

    @Test
    public void testCompactMatchesExistingZMcpBytes() {
        String wire = SseFrame.create().comment("z-mcp stream open").retry(100).id("1")
                .appendData("").compact().wire();
        assertEquals(":z-mcp stream open\nretry:100\nid:1\ndata:\n\n", wire);
    }

    @Test
    public void testFieldOrderIsCommentRetryIdEventData() {
        assertEquals(":hb\nretry: 5\nid: 9\nevent: msg\ndata: body\n\n",
                SseFrame.create().comment("hb").retry(5).id("9").event("msg")
                        .appendData("body").wire());
    }

    @Test
    public void testMultiLineDataBecomesSeveralDataLines() {
        assertEquals("data: line1\ndata: line2\ndata: \n\n",
                SseFrame.data("line1\nline2\n").wire());
        assertEquals("data: a\ndata: b\n\n", SseFrame.data("a\r\nb").wire());
    }

    @Test
    public void testNewlinesInsideFieldValuesAreFlattened() {
        assertEquals("id: a b\nevent: c d\ndata: keep\n\n",
                SseFrame.create().id("a\nb").event("c\r\nd").appendData("keep").wire());
    }

    @Test
    public void testNullDataBecomesEmptyValue() {
        assertEquals("data: \n\n", SseFrame.data(null).wire());
    }

    @Test
    public void testWriteToStreamIsSingleWriteAndFlushed() throws Exception {
        CountingStream stream = new CountingStream();
        SseFrame.create().event("a").appendData("b").writeTo(stream);
        assertEquals("一帧只能有一次非零 write", 1, stream.writes);
        assertTrue("写出后必须 flush", stream.flushed);
        assertEquals("event: a\ndata: b\n\n", new String(stream.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    public void testWriteDataConvenienceAndUtf8() throws Exception {
        CountingStream stream = new CountingStream();
        SseFrame.writeData(stream, "中文 payload ✓");
        assertEquals(1, stream.writes);
        byte[] expected = "data: 中文 payload ✓\n\n".getBytes(StandardCharsets.UTF_8);
        assertArrayEqualsByte(expected, stream.toByteArray());
    }

    @Test
    public void testRoundTripThroughParser() {
        List<SseFrame> frames = new ArrayList<>();
        frames.add(SseFrame.create().event("token").appendData("{\"text\":\"你好\"}").id("7"));
        frames.add(SseFrame.data("[DONE]"));
        frames.add(SseFrame.create().comment("keep-alive"));

        StringBuilder wire = new StringBuilder();
        for (SseFrame frame : frames) {
            wire.append(frame.wire());
        }
        List<SseEvent> events = SseParser.parse(wire.toString());
        assertEquals("心跳注释帧不产出事件", 2, events.size());
        assertEquals("token", events.get(0).event());
        assertEquals("{\"text\":\"你好\"}", events.get(0).data());
        assertEquals("7", events.get(0).id());
        assertTrue(events.get(1).isDone());
    }

    @Test
    public void testNegativeRetryRejected() {
        try {
            SseFrame.create().retry(-1);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().startsWith("retry"));
        }
    }

    private static void assertArrayEqualsByte(byte[] expected, byte[] actual) {
        assertEquals(new String(expected, StandardCharsets.UTF_8),
                new String(actual, StandardCharsets.UTF_8));
    }

    /**
     * 统计 write 调用次数的输出流。
     */
    private static final class CountingStream extends OutputStream {

        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private int writes = 0;
        private boolean flushed = false;

        @Override
        public void write(int b) {
            writes++;
            buffer.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) {
            if (len > 0) {
                writes++;
            }
            buffer.write(b, off, len);
        }

        @Override
        public void flush() {
            flushed = true;
        }

        @Override
        public void close() throws IOException {
            buffer.close();
        }

        private byte[] toByteArray() {
            return buffer.toByteArray();
        }
    }
}
