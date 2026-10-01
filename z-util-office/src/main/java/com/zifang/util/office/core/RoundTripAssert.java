package com.zifang.util.office.core;

import java.util.Objects;

/**
 * 跨格式往返验证工具：把"写入 → 读回"两步折成一个调用，
 * 让单元测试只写一行就能完成"创建 + 修改 + 验证"的最小闭环。
 *
 * <p>实现无任何依赖，可被 Excel / Word / PDF / PPT 任意子包的测试复用。
 */
public final class RoundTripAssert {

    private RoundTripAssert() {
    }

    /**
     * 执行一次往返：写者把内容写入缓冲区，读者从同一缓冲区读回，得到读回结果。
     * 写者抛异常会原样透传；读者抛异常同样原样透传。
     *
     * @param <T>     写出去的内容类型
     * @param writer  写者（接收 byte[] 缓冲并写入）
     * @param reader  读者（接收 byte[] 缓冲并读回为 T）
     * @return 读回结果
     * @throws Exception 写者或读者抛出的任何异常
     */
    public static <T> T assertRoundTrip(Writer writer, Reader<T> reader) throws Exception {
        if (writer == null) {
            throw new IllegalArgumentException("writer must not be null");
        }
        if (reader == null) {
            throw new IllegalArgumentException("reader must not be null");
        }
        java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
        writer.write(buf);
        byte[] bytes = buf.toByteArray();
        return reader.read(new java.io.ByteArrayInputStream(bytes));
    }

    /**
     * 往返 + 断言相等。读回结果与 expected 用 {@link Objects#equals} 比对；
     * 不等时抛 {@link AssertionError}，消息中带 expected / actual。
     */
    public static <T> void assertEquals(Object expected, Writer writer, Reader<T> reader) throws Exception {
        T actual = assertRoundTrip(writer, reader);
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("round-trip mismatch: expected=" + safe(expected)
                    + " actual=" + safe(actual));
        }
    }

    /**
     * 往返 + 断言读回结果 contains needle（用于"包含子串"型断言，PDF 文本提取常用）。
     */
    public static <T> void assertContains(String needle, Writer writer, Reader<T> reader) throws Exception {
        T actual = assertRoundTrip(writer, reader);
        String text = actual == null ? "" : actual.toString();
        if (!text.contains(needle)) {
            throw new AssertionError("round-trip mismatch: expected to contain <" + needle
                    + "> actual=<" + text + ">");
        }
    }

    private static String safe(Object o) {
        if (o == null) {
            return "null";
        }
        String s = o.toString();
        if (s.length() > 200) {
            return s.substring(0, 200) + "...";
        }
        return s;
    }

    /** 写者：把内容写到缓冲输出流。 */
    public interface Writer {
        void write(java.io.OutputStream out) throws Exception;
    }

    /** 读者：从缓冲输入流读回为结果。 */
    public interface Reader<T> {
        T read(java.io.InputStream in) throws Exception;
    }
}
