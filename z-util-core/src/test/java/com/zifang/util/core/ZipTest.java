package com.zifang.util.core;

import java.io.File;
import java.util.zip.ZipFile;

/**
 * ZipTest类。
 */
public class ZipTest {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        ZipFile zipFile;
        {
            File file2 = new File(".");
            System.out.println(file2.getAbsolutePath());
            try {
                //zipFile("/Users/zifang/workplace/idea_workplace/components/README.MD", "/Users/zifang/workplace/idea_workplace/components/a.zip", "ss.md");
                //unZip("file.zip", "d:/test/");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
