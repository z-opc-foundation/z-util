package com.zifang.util.parser.csv;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * CsvCharsetDetector类。
 * CSV 字节流编码探测与解码：带 UTF-8 BOM 的字节流直接按 UTF-8 解码（去除 BOM）；
 * 无 BOM 时对 UTF-8 与 GB18030 双解码并按乱码程度择优，同分优先 UTF-8，
 * 兼容常见表格软件以中文本地编码导出的 CSV 文件。
 */
public final class CsvCharsetDetector {

    /**
     * 覆盖 GBK 的中文字符集
     */
    private static final Charset CHARSET_GB18030 = Charset.forName("GB18030");

    /**
     * UTF-8 BOM 字节
     */
    private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private CsvCharsetDetector() {
    }

    /**
     * decode方法。
     * 探测字节流编码并解码为文本：带 UTF-8 BOM 时去 BOM 后按 UTF-8 解码；
     * 无 BOM 时先按 UTF-8 解码，乱码分为 0 则直接采用，否则与 GB18030 解码
     * 结果比较乱码分，取更低者，同分优先 UTF-8。
     *
     * @param bytes 字节流，为 null 或空时返回空串
     * @return 解码后的文本
     */
    public static String decode(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        if (startsWithUtf8Bom(bytes)) {
            return new String(stripUtf8Bom(bytes), StandardCharsets.UTF_8);
        }
        String utf8Text = new String(bytes, StandardCharsets.UTF_8);
        int utf8Score = garbleScore(utf8Text);
        if (utf8Score == 0) {
            return utf8Text;
        }
        String gb18030Text = new String(bytes, CHARSET_GB18030);
        if (garbleScore(gb18030Text) < utf8Score) {
            return gb18030Text;
        }
        return utf8Text;
    }

    /**
     * garbleScore方法。
     * 估算文本乱码程度：替换符 U+FFFD 每出现一个计 10 分；
     * 连续替换符被中文编码误解出的典型乱码片段每出现一次计 50 分。
     *
     * @param text 待评估文本，为 null 时返回 0
     * @return 乱码分值，越低越接近正常文本
     */
    public static int garbleScore(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        int score = 0;
        int index = 0;
        while (index < text.length()) {
            char ch = text.charAt(index);
            if (ch == '\uFFFD') {
                score += 10;
            }
            index++;
        }
        score += countOccurrences(text, "锟斤拷") * 50;
        return score;
    }

    /**
     * countOccurrences方法。
     * 统计片段出现次数。
     */
    private static int countOccurrences(String text, String fragment) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(fragment, index)) != -1) {
            count++;
            index += fragment.length();
        }
        return count;
    }

    /**
     * startsWithUtf8Bom方法。
     * 判断字节流是否以 UTF-8 BOM 开头。
     */
    private static boolean startsWithUtf8Bom(byte[] bytes) {
        if (bytes == null || bytes.length < UTF8_BOM.length) {
            return false;
        }
        for (int i = 0; i < UTF8_BOM.length; i++) {
            if (bytes[i] != UTF8_BOM[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * stripUtf8Bom方法。
     * 去除字节流开头的 UTF-8 BOM。
     */
    private static byte[] stripUtf8Bom(byte[] bytes) {
        if (!startsWithUtf8Bom(bytes)) {
            return bytes;
        }
        byte[] withoutBom = new byte[bytes.length - UTF8_BOM.length];
        System.arraycopy(bytes, UTF8_BOM.length, withoutBom, 0, withoutBom.length);
        return withoutBom;
    }
}
