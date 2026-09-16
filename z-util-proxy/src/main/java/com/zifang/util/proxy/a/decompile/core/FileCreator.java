package com.zifang.util.proxy.a.decompile.core;

import java.io.*;

/**
 * 文件创建器
 * <p>
 * 将反编译后的Java源码写入文件。
 */
public class FileCreator {

    /**
     * 源码写入到指定后缀的源文件中（使用当前目录）
     *
     * @param name 源文件名称（不含扩展名）
     * @param src  源代码
     */
    public static void createFile(String name, String src) {
        createFile(name, src, null);
    }

    /**
     * 源码写入到指定目录的源文件中
     *
     * @param name      源文件名称（不含扩展名）
     * @param src       源代码
     * @param outputDir 输出目录，为 null 时使用当前目录
     */
    public static void createFile(String name, String src, String outputDir) {
        // 确定输出目录
        String dirPath = (outputDir != null && !outputDir.isEmpty())
                ? outputDir
                : System.getProperty("user.dir");

        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, name + ".java");

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file));
            bw.write(src);
            bw.flush();
        } catch (FileNotFoundException e) {
            System.err.println("文件未找到: " + file.getAbsolutePath());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("写入文件失败: " + file.getAbsolutePath());
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}