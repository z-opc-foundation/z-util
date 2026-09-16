package com.zifang.util.zex.bust.chapter11.case2;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * TransformInputStreamOutputStream类。
 */
public class TransformInputStreamOutputStream {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws IOException {
        InputStream in = new FileInputStream("XXX.txt");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        //out写的时候，每次写1024个字节，如果in有2048个字节数，则读2048/1024=2次
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
    }
}
