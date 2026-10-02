package com.zifang.util.parser.csv;

import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

/**
 * CsvCharsetDetectorTest类。
 * CSV 字节流编码探测与解码测试。
 */
public class CsvCharsetDetectorTest {

    /**
     * testDecodeUtf8Text方法：无 BOM 的 UTF-8 字节流按 UTF-8 解码。
     */
    @Test
    public void testDecodeUtf8Text() {
        byte[] bytes = "姓名,编号".getBytes(StandardCharsets.UTF_8);
        assertEquals("姓名,编号", CsvCharsetDetector.decode(bytes));
    }

    /**
     * testDecodeUtf8WithBom方法：带 UTF-8 BOM 的字节流去 BOM 后解码。
     */
    @Test
    public void testDecodeUtf8WithBom() {
        byte[] raw = "first,second".getBytes(StandardCharsets.UTF_8);
        byte[] withBom = new byte[3 + raw.length];
        withBom[0] = (byte) 0xEF;
        withBom[1] = (byte) 0xBB;
        withBom[2] = (byte) 0xBF;
        System.arraycopy(raw, 0, withBom, 3, raw.length);
        String decoded = CsvCharsetDetector.decode(withBom);
        assertEquals("first,second", decoded);
    }

    /**
     * testDecodeGb18030方法：GB18030 编码字节流探测为中文编码并正确解码。
     */
    @Test
    public void testDecodeGb18030() {
        String text = "中文编码测试,第二列";
        byte[] bytes = text.getBytes(Charset.forName("GB18030"));
        assertEquals(text, CsvCharsetDetector.decode(bytes));
    }

    /**
     * testDecodeAscii方法：纯 ASCII 字节流两种解码等价，返回原文。
     */
    @Test
    public void testDecodeAscii() {
        byte[] bytes = "a,b,c".getBytes(StandardCharsets.US_ASCII);
        assertEquals("a,b,c", CsvCharsetDetector.decode(bytes));
    }

    /**
     * testDecodeEmpty方法：null 或空字节流返回空串。
     */
    @Test
    public void testDecodeEmpty() {
        assertEquals("", CsvCharsetDetector.decode(null));
        assertEquals("", CsvCharsetDetector.decode(new byte[0]));
    }

    /**
     * testGarbleScore方法：正常文本 0 分，替换符与典型乱码片段按权重计分。
     */
    @Test
    public void testGarbleScore() {
        assertEquals(0, CsvCharsetDetector.garbleScore(null));
        assertEquals(0, CsvCharsetDetector.garbleScore(""));
        assertEquals(0, CsvCharsetDetector.garbleScore("正常文本abc"));
        assertEquals(10, CsvCharsetDetector.garbleScore("a\uFFFDb"));
        assertEquals(20, CsvCharsetDetector.garbleScore("\uFFFD\uFFFDx"));
        assertEquals(50, CsvCharsetDetector.garbleScore("锟斤拷"));
        assertEquals(50, CsvCharsetDetector.garbleScore("x锟斤拷y"));
        // 替换符与乱码片段叠加计分
        assertEquals(60, CsvCharsetDetector.garbleScore("\uFFFD锟斤拷"));
    }
}
