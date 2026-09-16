package com.zifang.util.core.io;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;

/**
 * IOUtilTest类。
 */
public class IOUtilTest {

    private byte[] testData;
    private File tempFile;

    @Before
    /**
     * setUp方法。
     */
    public void setUp() throws Exception {
        testData = "hello world\nline2\nline3中文".getBytes(StandardCharsets.UTF_8);
        tempFile = File.createTempFile("io-util-test", ".txt");
        tempFile.deleteOnExit();
    }

    @After
    /**
     * tearDown方法。
     */
    public void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    // ==================== readString ====================

    @Test
    /**
     * testReadStringFromInputStream方法。
     */
    public void testReadStringFromInputStream() throws Exception {
        try (InputStream in = new ByteArrayInputStream(testData)) {
            String result = IOUtil.readString(in);
            assertEquals("hello world\nline2\nline3中文", result);
        }
    }

    @Test
    /**
     * testReadStringWithCharset方法。
     */
    public void testReadStringWithCharset() throws Exception {
        try (InputStream in = new ByteArrayInputStream(testData)) {
            String result = IOUtil.readString(in, "UTF-8");
            assertEquals("hello world\nline2\nline3中文", result);
        }
    }

    @Test
    /**
     * testReadStringWithGBKCharset方法。
     */
    public void testReadStringWithGBKCharset() throws Exception {
        byte[] gbkData = "你好世界".getBytes("GBK");
        try (InputStream in = new ByteArrayInputStream(gbkData)) {
            String result = IOUtil.readString(in, "GBK");
            assertEquals("你好世界", result);
        }
    }

    @Test
    /**
     * testReadStringEmptyStream方法。
     */
    public void testReadStringEmptyStream() throws Exception {
        try (InputStream in = new ByteArrayInputStream(new byte[0])) {
            String result = IOUtil.readString(in);
            assertEquals("", result);
        }
    }

    // ==================== readToBuffer ====================

    @Test
    /**
     * testReadToBuffer方法。
     */
    public void testReadToBuffer() throws Exception {
        try (InputStream in = new ByteArrayInputStream(testData)) {
            FastByteArrayOutputStream out = IOUtil.readToBuffer(in);
            assertArrayEquals(testData, out.toByteArray());
        }
    }

    @Test
    /**
     * testReadToBufferNotCloseInput方法。
     */
    public void testReadToBufferNotCloseInput() throws Exception {
        InputStream in = new ByteArrayInputStream(testData);
        IOUtil.readToBuffer(in, false);
        assertNotNull(in.read()); // still open
        in.close();
    }

    @Test
    /**
     * testReadToBufferClosesInputByDefault方法。
     */
    public void testReadToBufferClosesInputByDefault() throws Exception {
        InputStream in = new ByteArrayInputStream(testData);
        IOUtil.readToBuffer(in);
        assertEquals(-1, in.read()); // already closed
    }

    // ==================== readLines ====================

    @Test
    /**
     * testReadLinesFromReader方法。
     */
    public void testReadLinesFromReader() throws Exception {
        try (Reader reader = new StringReader("line1\nline2\nline3")) {
            List<String> lines = IOUtil.readLines(reader);
            assertEquals(3, lines.size());
            assertEquals("line1", lines.get(0));
            assertEquals("line2", lines.get(1));
            assertEquals("line3", lines.get(2));
        }
    }

    @Test
    /**
     * testReadLinesWithEmptyLines方法。
     */
    public void testReadLinesWithEmptyLines() throws Exception {
        try (Reader reader = new StringReader("line1\n\nline3")) {
            List<String> lines = IOUtil.readLines(reader);
            assertEquals(3, lines.size());
            assertEquals("", lines.get(1));
        }
    }

    @Test
    /**
     * testReadLinesConsumer方法。
     */
    public void testReadLinesConsumer() throws Exception {
        try (InputStream in = new ByteArrayInputStream("a\nb\nc".getBytes(StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            IOUtil.readLines(in, StandardCharsets.UTF_8, sb::append);
            assertEquals("abc", sb.toString());
        }
    }

    // ==================== copy ====================

    @Test
    /**
     * testCopySmallStream方法。
     */
    public void testCopySmallStream() throws Exception {
        try (InputStream in = new ByteArrayInputStream(testData);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            int count = IOUtil.copy(in, out);
            assertEquals(testData.length, count);
            assertArrayEquals(testData, out.toByteArray());
        }
    }

    @Test
    /**
     * testCopyLargeStream方法。
     */
    public void testCopyLargeStream() throws Exception {
        byte[] largeData = new byte[100_000];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }
        try (InputStream in = new ByteArrayInputStream(largeData);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            long count = IOUtil.copyLarge(in, out);
            assertEquals(largeData.length, count);
            assertArrayEquals(largeData, out.toByteArray());
        }
    }

    @Test
    /**
     * testCopyReturnsZeroForEmptyStream方法。
     */
    public void testCopyReturnsZeroForEmptyStream() throws Exception {
        try (InputStream in = new ByteArrayInputStream(new byte[0]);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            int count = IOUtil.copy(in, out);
            assertEquals(0, count);
        }
    }

    // ==================== channelCopy ====================

    @Test
    /**
     * testChannelCopy方法。
     */
    public void testChannelCopy() throws Exception {
        byte[] data = "channel test data".getBytes(StandardCharsets.UTF_8);
        try (InputStream in = new ByteArrayInputStream(data);
             ReadableByteChannel src = Channels.newChannel(in);
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             WritableByteChannel dest = Channels.newChannel(baos)) {
            IOUtil.channelCopy(src, dest);
            assertArrayEquals(data, baos.toByteArray());
        }
    }

    @Test
    /**
     * testChannelCopyClear方法。
     */
    public void testChannelCopyClear() throws Exception {
        byte[] data = "channel clear test".getBytes(StandardCharsets.UTF_8);
        try (InputStream in = new ByteArrayInputStream(data);
             ReadableByteChannel src = Channels.newChannel(in);
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             WritableByteChannel dest = Channels.newChannel(baos)) {
            IOUtil.channelCopyClear(src, dest);
            assertArrayEquals(data, baos.toByteArray());
        }
    }

    // ==================== skipFully ====================

    @Test
    /**
     * testSkipFully方法。
     */
    public void testSkipFully() throws Exception {
        byte[] data = "0123456789".getBytes(StandardCharsets.UTF_8);
        try (InputStream in = new ByteArrayInputStream(data)) {
            long skipped = IOUtil.skipFully(in, 5);
            assertEquals(5, skipped);
            assertEquals('5', in.read());
        }
    }

    @Test
    /**
     * testSkipFullyBeyondEOF方法。
     */
    public void testSkipFullyBeyondEOF() throws Exception {
        byte[] data = "123".getBytes(StandardCharsets.UTF_8);
        try (InputStream in = new ByteArrayInputStream(data)) {
            long skipped = IOUtil.skipFully(in, 10);
            assertEquals(3, skipped); // only 3 available
            assertEquals(-1, in.read());
        }
    }

    @Test
    /**
     * testSkipFullyZero方法。
     */
    public void testSkipFullyZero() throws Exception {
        byte[] data = "abc".getBytes(StandardCharsets.UTF_8);
        try (InputStream in = new ByteArrayInputStream(data)) {
            long skipped = IOUtil.skipFully(in, 0);
            assertEquals(0, skipped);
            assertEquals('a', in.read());
        }
    }

    // ==================== close ====================

    @Test
    /**
     * testCloseNull方法。
     */
    public void testCloseNull() {
        IOUtil.close(null); // should not throw
    }

    @Test
    /**
     * testCloseQuietly方法。
     */
    public void testCloseQuietly() {
        IOUtil.closeQuietly(null); // should not throw
    }

    @Test
    /**
     * testCloseAlreadyClosed方法。
     */
    public void testCloseAlreadyClosed() throws Exception {
        InputStream in = new ByteArrayInputStream(testData);
        in.close();
        IOUtil.close(in); // should not throw
    }

    @Test
    /**
     * testCloseIO方法。
     */
    public void testCloseIO() throws Exception {
        InputStream in = new ByteArrayInputStream(testData);
        IOUtil.close(in);
        assertEquals(-1, in.read()); // closed
    }

    @Test
    /**
     * testCloseOutputStream方法。
     */
    public void testCloseOutputStream() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        IOUtil.close(out);
        // writing after close may throw, but close itself shouldn't
        byte[] data = "test".getBytes(StandardCharsets.UTF_8);
        out.write(data);
    }

    // ==================== FastByteArrayOutputStream integration ====================

    @Test
    /**
     * testReadToBufferAndWriteToFile方法。
     */
    public void testReadToBufferAndWriteToFile() throws Exception {
        try (FileOutputStream fos = new FileOutputStream(tempFile);
             InputStream in = new ByteArrayInputStream(testData)) {
            FastByteArrayOutputStream buf = IOUtil.readToBuffer(in);
            buf.writeTo(fos);
        }
        byte[] readBack = java.nio.file.Files.readAllBytes(tempFile.toPath());
        assertArrayEquals(testData, readBack);
    }
}
