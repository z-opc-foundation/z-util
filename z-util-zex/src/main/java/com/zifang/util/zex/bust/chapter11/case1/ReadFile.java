package com.zifang.util.zex.bust.chapter11.case1;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author zifang
 */
public class ReadFile {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws IOException {
        FileInputStream fis = null;
        try {
            String filePath = "/Users/zifang/workplace/idea_workplace/JavaBust/src/main/resources/test_in.txt";
            //创建字节输入流
            fis = new FileInputStream(filePath);
            //创建一个长度为1024的数组
            byte[] b = new byte[1024];
            //用于保存的实际字节数
            int hasRead = 0;
            //使用循环来重复抠数据
            while ((hasRead = fis.read(b)) > 0) {
                //取出流里面的数据，然后将数据打印出来
                System.out.print(new String(b, 0, hasRead));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fis.close();
        }
    }
}
