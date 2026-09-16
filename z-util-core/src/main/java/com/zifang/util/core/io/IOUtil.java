package com.zifang.util.core.io;

import com.zifang.util.core.lang.StringUtil;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 流读写与 Channel 复制工具类
 */
public class IOUtil {

    public static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    // -------------------- 流读取 --------------------

    /**
     * 将输入流读为字符串
     *
     * @param in          输入流
     * @param charsetName 字符编码，null 则使用 UTF-8
     * @return 字符串内容
     * @throws IOException 读取异常
     */
    public static String readString(InputStream in, String charsetName) throws IOException {
        FastByteArrayOutputStream out = readToBuffer(in);
        if (StringUtil.isBlank(charsetName)) {
            return out.toString();
        }
        return out.toString(charsetName);
    }

    /**
     * 将输入流读为字符串，默认 UTF-8
     *
     * @param in 输入流
     * @return 字符串内容
     * @throws IOException 读取异常
     */
    public static String readString(InputStream in) throws IOException {
        return readString(in, StandardCharsets.UTF_8.name());
    }

    /**
     * 读取输入流到缓冲区，读取完毕后关闭输入流
     *
     * @param in 输入流
     * @return 包含读取数据的缓冲区
     * @throws IOException 读取异常
     */
    public static FastByteArrayOutputStream readToBuffer(InputStream in) throws IOException {
        return readToBuffer(in, true);
    }

    /**
     * 读取输入流到缓冲区
     *
     * @param in      输入流
     * @param closeIn 读取完毕后是否关闭输入流
     * @return 包含读取数据的缓冲区
     * @throws IOException 读取异常
     */
    public static FastByteArrayOutputStream readToBuffer(InputStream in, boolean closeIn) throws IOException {
        FastByteArrayOutputStream out;
        if (in instanceof FileInputStream) {
            out = new FastByteArrayOutputStream(in.available());
        } else {
            out = new FastByteArrayOutputStream();
        }
        try {
            copy(in, out);
        } finally {
            if (closeIn) {
                close(in);
            }
        }
        return out;
    }

    /**
     * 读取Reader所有行
     *
     * @param reader Reader
     * @return 行列表
     * @throws IOException 读取异常
     */
    public static java.util.List<String> readLines(Reader reader) throws IOException {
        java.util.List<String> lines = new java.util.ArrayList<>();
        try (BufferedReader br = reader instanceof BufferedReader
                ? (BufferedReader) reader
                : new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    /**
     * 逐行读取流并处理
     *
     * @param in       输入流
     * @param charset  字符编码
     * @param consumer 行处理器
     * @throws IOException 读取异常
     */
    public static void readLines(InputStream in, Charset charset, java.util.function.Consumer<String> consumer) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(in, charset == null ? StandardCharsets.UTF_8 : charset))) {
            String line;
            while ((line = br.readLine()) != null) {
                consumer.accept(line);
            }
        }
    }

    // -------------------- 流复制 --------------------

    /**
     * 将输入流内容复制到输出流
     *
     * @param input  输入流
     * @param output 输出流
     * @return 复制的字节数
     * @throws IOException 读取或写入异常
     */
    public static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    /**
     * 大规模流复制，使用较大缓冲区
     *
     * @param input  输入流
     * @param output 输出流
     * @return 复制的总字节数
     * @throws IOException 读取或写入异常
     */
    public static long copyLarge(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /**
     * 利用 NIO Channel 复制数据（compact 方式）
     * 适合大文件复制，零拷贝场景
     *
     * @param src  源 Channel
     * @param dest 目标 Channel
     * @throws IOException 复制异常
     */
    public static void channelCopy(ReadableByteChannel src, WritableByteChannel dest) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
        while (src.read(buffer) != -1) {
            buffer.flip();
            dest.write(buffer);
            buffer.compact();
        }
        buffer.flip();
        while (buffer.hasRemaining()) {
            dest.write(buffer);
        }
    }

    /**
     * 利用 NIO Channel 复制数据（clear 方式）
     * 确保每次读取前 buffer 已清空，适合小文件
     *
     * @param src  源 Channel
     * @param dest 目标 Channel
     * @throws IOException 复制异常
     */
    public static void channelCopyClear(ReadableByteChannel src, WritableByteChannel dest) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
        while (src.read(buffer) != -1) {
            buffer.flip();
            while (buffer.hasRemaining()) {
                dest.write(buffer);
            }
            buffer.clear();
        }
    }

    /**
     * 从 stdin 复制到 stdout（演示用）
     */
    public static void channelCopyStdInToStdOut() throws IOException {
        ReadableByteChannel src = Channels.newChannel(System.in);
        WritableByteChannel dest = Channels.newChannel(System.out);
        channelCopy(src, dest);
    }

    // -------------------- 跳过 --------------------

    /**
     * 跳过指定字节数
     *
     * @param input  输入流
     * @param toSkip 要跳过的字节数
     * @return 实际跳过的字节数
     * @throws IOException 跳过异常
     */
    public static long skipFully(InputStream input, long toSkip) throws IOException {
        if (toSkip <= 0) {
            return 0;
        }
        long remain = toSkip;
        while (remain > 0) {
            long skipped = input.skip(remain);
            if (skipped == 0) {
                // skip() 可能返回 0，需用 read() 推进一位
                int b = input.read();
                if (b == EOF) {
                    break;
                }
                skipped = 1;
            }
            remain -= skipped;
        }
        return toSkip - remain;
    }

    // -------------------- 关闭 --------------------

    /**
     * 安全关闭 Closeable，异常被静默吞掉
     *
     * @param closeable 要关闭的对象，null 被忽略
     */
    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                if (closeable instanceof ByteArrayInputStream) {
                    // ByteArrayInputStream.close() is a no-op
                    // Skip all remaining bytes to make read() return -1
                    ByteArrayInputStream bais = (ByteArrayInputStream) closeable;
                    bais.skip(Long.MAX_VALUE);
                }
                closeable.close();
            } catch (IOException e) {
                // swallow
            }
        }
    }

    /**
     * 安全关闭 Closeable（别名，语义更明确）
     */
    public static void closeQuietly(Closeable closeable) {
        close(closeable);
    }
}
