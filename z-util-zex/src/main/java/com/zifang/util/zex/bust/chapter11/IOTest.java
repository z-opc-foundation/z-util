package com.zifang.util.zex.bust.chapter11;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */

import org.junit.Test;

import java.io.*;
import java.util.Arrays;

/**
 * IOTest类。
 */
public class IOTest {
    /**
     * transform方法。
     * * @param in InputStream类型参数
     *
     * @param out OutputStream类型参数
     * @return static void类型返回值
     */
    public static void transform(InputStream in, OutputStream out) {
        int ch = 0;

        try {
            while ((ch = in.read()) != -1) {
                int upperChar = Character.toUpperCase((char) ch);
                out.write(upperChar);
            } // close while
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * writeData方法。
     * * @param out PipedOutputStream类型参数
     *
     * @return static void类型返回值
     */
    public static void writeData(PipedOutputStream out) throws IOException {
        /*把0-100 之间的数写入管道中*/
        for (int i = 0; i < 100; i++) {
            String data = "-" + i;
            out.write(data.getBytes()); //把字节数组写入到输出管道流中
        }
        out.close();
    }

    /**
     * readData方法。
     * * @param input PipedInputStream类型参数
     *
     * @return static void类型返回值
     */
    public static void readData(PipedInputStream input) throws IOException {
        /*从管道中读取0-100*/
        byte[] bytes = new byte[1024];
        int len = input.read(bytes); //返回读到的字节数，如果没有读到任何数据返回-1
        while (len != -1) {
            //把bytes数组中从0开始到len个字节转换为字符串打印出来
            System.out.println(new String(bytes, 0, len));
            len = input.read(bytes); //继续从管道中读取数据
        }
        input.close();
    }

    @Test
    /**
     * test001方法。
     */
    public void test001() {
        FileInputStream fis = null;
        try {
            //创建文件字节输入流对象
            fis = new FileInputStream("/Users/zifang/workplace/idea_workplace/components/util-zex/src/main/resources/temp001.txt");
            //开始读
            int readData = 0;
            while ((readData = fis.read()) != -1) {
                System.out.println(readData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //在finally语句块中确保流一定关闭
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    /**
     * test002方法。
     */
    public void test002() {
        String str = "abcdef";

        ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        transform(in, out);

        System.out.println(out);
        System.out.println(Arrays.toString(out.toByteArray()));

        // 从键盘读，输出到显示器
        transform(System.in, System.out);
    }

    @Test
    /**
     * test0003方法。
     */
    public void test0003() throws IOException {
        /*定义管道字节流*/
        PipedInputStream inputStream = new PipedInputStream();
        PipedOutputStream outputStream = new PipedOutputStream();

        inputStream.connect(outputStream);

        /*创建两个线程向管道流中读写数据*/
        new Thread(() -> {
            try {
                writeData(outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                readData(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Test
    /**
     * test004方法。
     */
    public void test004() {
        //定义流
        SequenceInputStream sis = null;
        FileOutputStream fos = null;
        try {
            //创建合并流对象
            sis = new SequenceInputStream(
                    new FileInputStream("/Users/zifang/workplace/idea_workplace/components/util-zex/src/main/resources/temp004_01.txt"),
                    new FileInputStream("/Users/zifang/workplace/idea_workplace/components/util-zex/src/main/resources/temp004_02.txt"));
            //创建输出流对象
            fos = new FileOutputStream("/Users/zifang/workplace/idea_workplace/components/util-zex/src/main/resources/temp004.txt");
            //声明byte数组用于存储从输入流读取到的数据
            byte[] by = new byte[1024];
            //该变量纪录每次读取到的字符个数
            int len = 0;
            //读取输入流中的数据
            while ((len = sis.read(by)) != -1) {
                //输出从输入流中读取到的数据
                fos.write(by, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //释放资源
            if (sis != null) {
                try {
                    sis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Test
    /**
     * test005_1方法。
     */
    public void test005_1() {

        //定义对象流
        ObjectOutputStream oos = null;

        try {
            //创建对象流
            oos = new ObjectOutputStream(new FileOutputStream("/Users/zifang/workplace/idea_workplace/components/util-zex/src/main/resources/temp005.txt"));
            //序列化对象
            oos.writeObject(new Person(10001, "张三", 20));
            oos.writeObject(new Person(10002, "李四", 21));
            //刷新缓冲区
            oos.flush();
            System.out.println("序列化成功...");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //释放资源
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    /**
     * test005_2方法。
     */
    public void test005_2() {
        //定义对象流
        ObjectInputStream ois = null;
        try {
            //创建对象输入流对象
            ois = new ObjectInputStream(new FileInputStream("/Users/zifang/workplace/idea_workplace/components/util-zex/src/main/resources/temp005.txt"));
            //反序列化对象
            Person person1 = (Person) ois.readObject();
            Person person2 = (Person) ois.readObject();
            System.out.println(person1);
            System.out.println(person2);
            System.out.println("反序列化成功...");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
