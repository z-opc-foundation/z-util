package com.zifang.util.core.io.file;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * FileContentUtilTest类。
 */
public class FileContentUtilTest {

    private File tempFile;
    private File tempFileGBK;
    private File tempDir;

    @Before
    /**
     * setUp方法。
     */
    public void setUp() throws Exception {
        tempFile = File.createTempFile("content-test", ".txt");
        tempFile.deleteOnExit();
        Files.write(tempFile.toPath(), "hello world\nline2\n中文内容".getBytes(StandardCharsets.UTF_8));

        tempFileGBK = File.createTempFile("content-test-gbk", ".txt");
        tempFileGBK.deleteOnExit();
        Files.write(tempFileGBK.toPath(), "你好世界".getBytes(java.nio.charset.Charset.forName("GBK")));

        tempDir = Files.createTempDirectory("content-test-dir").toFile();
        tempDir.deleteOnExit();
    }

    @After
    /**
     * tearDown方法。
     */
    public void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
        if (tempFileGBK != null && tempFileGBK.exists()) {
            tempFileGBK.delete();
        }
        if (tempDir != null && tempDir.exists()) {
            for (File f : tempDir.listFiles()) {
                f.delete();
            }
            tempDir.delete();
        }
    }

    // ==================== readString ====================

    @Test
    /**
     * testReadStringDefaultCharset方法。
     */
    public void testReadStringDefaultCharset() throws Exception {
        String result = FileContentUtil.readString(tempFile);
        assertEquals("hello world\nline2\n中文内容", result);
    }

    @Test
    /**
     * testReadStringWithCharset方法。
     */
    public void testReadStringWithCharset() throws Exception {
        String result = FileContentUtil.readString(tempFile, StandardCharsets.UTF_8);
        assertEquals("hello world\nline2\n中文内容", result);
    }

    @Test
    /**
     * testReadStringGBK方法。
     */
    public void testReadStringGBK() throws Exception {
        String result = FileContentUtil.readString(tempFileGBK, java.nio.charset.Charset.forName("GBK"));
        assertEquals("你好世界", result);
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testReadStringNullFile方法。
     */
    public void testReadStringNullFile() throws Exception {
        FileContentUtil.readString((File) null);
    }

    @Test(expected = FileNotFoundException.class)
    /**
     * testReadStringNonExistent方法。
     */
    public void testReadStringNonExistent() throws Exception {
        FileContentUtil.readString(new File("/non/existent/file.txt"));
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testReadStringNullCharset方法。
     */
    public void testReadStringNullCharset() throws Exception {
        FileContentUtil.readString(tempFile, null);
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testReadStringNotAFile方法。
     */
    public void testReadStringNotAFile() throws Exception {
        FileContentUtil.readString(tempDir);
    }

    // ==================== readLines ====================

    @Test
    /**
     * testReadLines方法。
     */
    public void testReadLines() throws Exception {
        List<String> lines = FileContentUtil.readLines(tempFile);
        assertEquals(3, lines.size());
        assertEquals("hello world", lines.get(0));
        assertEquals("line2", lines.get(1));
        assertEquals("中文内容", lines.get(2));
    }

    @Test
    /**
     * testReadLinesWithEmptyFile方法。
     */
    public void testReadLinesWithEmptyFile() throws Exception {
        File empty = File.createTempFile("empty", ".txt");
        empty.deleteOnExit();
        List<String> lines = FileContentUtil.readLines(empty);
        assertEquals(0, lines.size());
        empty.delete();
    }

    @Test
    /**
     * testReadLinesWithTrailingNewline方法。
     */
    public void testReadLinesWithTrailingNewline() throws Exception {
        File tmp = File.createTempFile("trailing", ".txt");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), "a\nb\n".getBytes(StandardCharsets.UTF_8));
        List<String> lines = FileContentUtil.readLines(tmp);
        assertEquals(2, lines.size());
        assertEquals("a", lines.get(0));
        assertEquals("b", lines.get(1));
        tmp.delete();
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testReadLinesNullFile方法。
     */
    public void testReadLinesNullFile() throws Exception {
        FileContentUtil.readLines(null);
    }

    @Test(expected = FileNotFoundException.class)
    /**
     * testReadLinesNonExistent方法。
     */
    public void testReadLinesNonExistent() throws Exception {
        FileContentUtil.readLines(new File("/non/existent/file.txt"));
    }

    // ==================== readBytes ====================

    @Test
    /**
     * testReadBytes方法。
     */
    public void testReadBytes() throws Exception {
        byte[] content = "hello world".getBytes(StandardCharsets.UTF_8);
        File tmp = File.createTempFile("bytes-test", ".bin");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), content);

        byte[] result = FileContentUtil.readBytes(tmp);
        assertArrayEquals(content, result);
        tmp.delete();
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testReadBytesNullFile方法。
     */
    public void testReadBytesNullFile() throws Exception {
        FileContentUtil.readBytes(null);
    }

    // ==================== writeString ====================

    @Test
    /**
     * testWriteString方法。
     */
    public void testWriteString() throws Exception {
        File tmp = File.createTempFile("write-test", ".txt");
        tmp.deleteOnExit();
        FileContentUtil.writeString(tmp, "new content\nline2");
        String result = FileContentUtil.readString(tmp);
        assertEquals("new content\nline2", result);
        tmp.delete();
    }

    @Test
    /**
     * testWriteStringWithCharset方法。
     */
    public void testWriteStringWithCharset() throws Exception {
        File tmp = File.createTempFile("write-charset-test", ".txt");
        tmp.deleteOnExit();
        FileContentUtil.writeString(tmp, "中文内容", java.nio.charset.Charset.forName("GBK"));
        String result = FileContentUtil.readString(tmp, java.nio.charset.Charset.forName("GBK"));
        assertEquals("中文内容", result);
        tmp.delete();
    }

    @Test
    /**
     * testWriteStringOverwrite方法。
     */
    public void testWriteStringOverwrite() throws Exception {
        File tmp = File.createTempFile("overwrite-test", ".txt");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), "original".getBytes(StandardCharsets.UTF_8));
        FileContentUtil.writeString(tmp, "overwritten");
        assertEquals("overwritten", FileContentUtil.readString(tmp));
        tmp.delete();
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testWriteStringNullFile方法。
     */
    public void testWriteStringNullFile() throws Exception {
        FileContentUtil.writeString(null, "content");
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testWriteStringNullContent方法。
     */
    public void testWriteStringNullContent() throws Exception {
        FileContentUtil.writeString(tempFile, null);
    }

    // ==================== writeLines ====================

    @Test
    /**
     * testWriteLines方法。
     */
    public void testWriteLines() throws Exception {
        File tmp = File.createTempFile("lines-test", ".txt");
        tmp.deleteOnExit();
        FileContentUtil.writeLines(tmp, java.util.Arrays.asList("line1", "line2", "line3"));
        List<String> lines = FileContentUtil.readLines(tmp);
        assertEquals(3, lines.size());
        assertEquals("line1", lines.get(0));
        assertEquals("line2", lines.get(1));
        assertEquals("line3", lines.get(2));
        tmp.delete();
    }

    @Test
    /**
     * testWriteLinesEmpty方法。
     */
    public void testWriteLinesEmpty() throws Exception {
        File tmp = File.createTempFile("empty-lines-test", ".txt");
        tmp.deleteOnExit();
        FileContentUtil.writeLines(tmp, java.util.Collections.emptyList());
        List<String> lines = FileContentUtil.readLines(tmp);
        assertEquals(0, lines.size());
        tmp.delete();
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testWriteLinesNullFile方法。
     */
    public void testWriteLinesNullFile() throws Exception {
        FileContentUtil.writeLines(null, java.util.Collections.emptyList());
    }

    // ==================== appendString ====================

    @Test
    /**
     * testAppendString方法。
     */
    public void testAppendString() throws Exception {
        File tmp = File.createTempFile("append-test", ".txt");
        tmp.deleteOnExit();
        FileContentUtil.writeString(tmp, "original");
        FileContentUtil.appendString(tmp, "_added");
        assertEquals("original_added", FileContentUtil.readString(tmp));
        tmp.delete();
    }

    @Test
    /**
     * testAppendStringToNewFile方法。
     */
    public void testAppendStringToNewFile() throws Exception {
        File tmp = new File(tempDir, "append-new.txt");
        FileContentUtil.appendString(tmp, "first");
        assertEquals("first", FileContentUtil.readString(tmp));
        tmp.delete();
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testAppendStringNullFile方法。
     */
    public void testAppendStringNullFile() throws Exception {
        FileContentUtil.appendString(null, "content");
    }

    // ==================== appendLine ====================

    @Test
    /**
     * testAppendLine方法。
     */
    public void testAppendLine() throws Exception {
        File tmp = File.createTempFile("appendline-test", ".txt");
        tmp.deleteOnExit();
        FileContentUtil.writeString(tmp, "line1");
        FileContentUtil.appendLine(tmp, "line2");
        assertEquals("line1" + System.lineSeparator() + "line2", FileContentUtil.readString(tmp));
        tmp.delete();
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testAppendLineNullFile方法。
     */
    public void testAppendLineNullFile() throws Exception {
        FileContentUtil.appendLine(null, "line");
    }

    // ==================== countLines ====================

    @Test
    /**
     * testCountLines方法。
     */
    public void testCountLines() throws Exception {
        assertEquals(3, FileContentUtil.countLines(tempFile));
    }

    @Test
    /**
     * testCountLinesEmptyFile方法。
     */
    public void testCountLinesEmptyFile() throws Exception {
        File empty = File.createTempFile("empty-count", ".txt");
        empty.deleteOnExit();
        assertEquals(0, FileContentUtil.countLines(empty));
        empty.delete();
    }

    @Test
    /**
     * testCountLinesSingleLineNoNewline方法。
     */
    public void testCountLinesSingleLineNoNewline() throws Exception {
        File tmp = File.createTempFile("single-count", ".txt");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), "no newline".getBytes(StandardCharsets.UTF_8));
        assertEquals(1, FileContentUtil.countLines(tmp));
        tmp.delete();
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testCountLinesNullFile方法。
     */
    public void testCountLinesNullFile() throws Exception {
        FileContentUtil.countLines(null);
    }

    // ==================== readFully ====================

    @Test
    /**
     * testReadFully方法。
     */
    public void testReadFully() throws Exception {
        byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
        File tmp = File.createTempFile("readfully-test", ".bin");
        tmp.deleteOnExit();
        Files.write(tmp.toPath(), data);

        byte[] result = FileContentUtil.readBytes(tmp);
        assertArrayEquals(data, result);
        tmp.delete();
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testReadFullyNullFile方法。
     */
    public void testReadFullyNullFile() throws Exception {
        FileContentUtil.readBytes(null);
    }
}
