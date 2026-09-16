package com.zifang.util.zex.bust.ex;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * MyZip类。
 */
public class MyZip { // 创建类
    /**
     * main方法。
     * * @param temp String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] temp) { // 主方法
        MyZip book = new MyZip(); // 创建本例对象
        try {
            // 调用方法，参数为压缩后文件与要压缩文件
            book.zip("hello.zip", new File("src"));
            System.out.println("压缩完成"); // 输出信息
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void zip(String zipFileName, File inputFile) throws Exception {
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName)); // 创建ZipOutputStream类对象
        zip(out, inputFile, ""); // 调用方法
        System.out.println("压缩中…"); // 输出信息
        out.close(); // 将流关闭
    }

    private void zip(ZipOutputStream out, File f, String base) throws Exception { // 方法重载
        if (f.isDirectory()) { // 测试此抽象路径名表示的文件是否是一个目录
            File[] fl = f.listFiles(); // 获取路径数组
            out.putNextEntry(new ZipEntry(base + "/")); // 写入此目录的entry
            base = base.length() == 0 ? "" : base + "/"; // 判断参数是否为空
            for (int i = 0; i < fl.length; i++) { // 循环遍历数组中文件
                zip(out, fl[i], fl[i].toString());
            }
        } else {
            out.putNextEntry(new ZipEntry(base)); // 创建新的进入点
            // 创建FileInputStream对象
            FileInputStream in = new FileInputStream(f);
            int b; // 定义int型变量
            System.out.println(base);
            while ((b = in.read()) != -1) { // 如果没有到达流的尾部
                out.write(b); // 将字节写入当前ZIP条目
            }
            in.close(); // 关闭流
        }
    }
}