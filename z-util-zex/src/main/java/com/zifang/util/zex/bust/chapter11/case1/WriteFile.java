package com.zifang.util.zex.bust.chapter11.case1;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * WriteFile类。
 */
public class WriteFile {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws IOException {
        FileOutputStream fout = null;
        try {
            String filePath = "/Users/zifang/workplace/idea_workplace/JavaBust/src/main/resources/test_out.txt";
            //创建字节输入流
            fout = new FileOutputStream(filePath);
            fout.write("第一行".getBytes());
            fout.write("第二行".getBytes());
            fout.write("第三行".getBytes());
            fout.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fout.close();
        }
    }
}
