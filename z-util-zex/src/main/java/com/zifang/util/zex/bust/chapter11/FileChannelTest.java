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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * FileChannelTest类。
 */
public class FileChannelTest {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        FileOutputStream fosRef = new FileOutputStream("/Users/zifang/workplace/idea_workplace/components/util-zex/src/main/resources/FileChannelTest001.txt");
        FileChannel fileChannel = fosRef.getChannel();
        try {

            ByteBuffer buffer1 = ByteBuffer.wrap("abcde".getBytes());
            ByteBuffer buffer2 = ByteBuffer.wrap("12345".getBytes());


            fileChannel.write(buffer1);
            buffer2.position(1);
            buffer2.limit(3);
            fileChannel.position(2);
            fileChannel.write(buffer2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileChannel.close();
        fosRef.close();
    }


    @Test
    /**
     * test001方法。
     */
    public void test001() throws IOException {
        FileOutputStream fosRef = new FileOutputStream("/Users/zifang/workplace/idea_workplace/components/util-zex/src/main/resources/FileChannelTest001.txt");
        FileChannel fileChannel = fosRef.getChannel();
        try {

            System.out.println("当前文件大小：" + fileChannel.size());
            System.out.println("当前通道位置：" + fileChannel.position());


            ByteBuffer buffer1 = ByteBuffer.wrap("abcde".getBytes());

            // 写入abcde
            fileChannel.write(buffer1);
            System.out.println("写入buffer1后，文件大小：" + fileChannel.size());
            System.out.println("写入buffer1后，通道位置：" + fileChannel.position());

            // 将 '23' 从 c位置开始写，最终会成为 ab23e
            ByteBuffer buffer2 = ByteBuffer.wrap("12345".getBytes());
            buffer2.position(1);
            buffer2.limit(3);
            fileChannel.position(2);
            fileChannel.write(buffer2, 2);

            // 将buffer3 从 1的位置开始写，最终会得到ag23e
            ByteBuffer buffer3 = ByteBuffer.wrap("g".getBytes());
            fileChannel.write(buffer3, 1);

            // 制造三个ByteBuffer的数组，分别位置定位到1，批量写入，会写入stvw
            // 最终得到 ag23estvw
            ByteBuffer b4 = ByteBuffer.wrap("opq".getBytes());
            b4.position(1);
            ByteBuffer b5 = ByteBuffer.wrap("rst".getBytes());
            b5.position(1);
            ByteBuffer b6 = ByteBuffer.wrap("ovw".getBytes());
            b6.position(1);

            ByteBuffer[] buffers = new ByteBuffer[]{b4, b5, b6};
            // 定位到尾部
            fileChannel.position(fileChannel.size());
            fileChannel.write(buffers, 1, 2);

        } catch (IOException e) {
            e.printStackTrace();
        }
        fileChannel.close();
        fosRef.close();
    }

    @Test
    /**
     * test002方法。
     */
    public void test002() throws IOException {
        FileInputStream ref = new FileInputStream("/Users/zifang/workplace/idea_workplace/components/util-zex/src/main/resources/FileChannelTest002.txt");
        FileChannel fileChannel = ref.getChannel();
        try {

            System.out.println("当前文件大小：" + fileChannel.size());
            System.out.println("当前通道位置：" + fileChannel.position());

            ByteBuffer buffer1 = ByteBuffer.allocate(3);
            buffer1.position(1);
            fileChannel.read(buffer1);
            System.out.println("buffer1读取结果" + new String(buffer1.array()));


            // 定位到开始
            fileChannel.position(0);
            ByteBuffer buffer2 = ByteBuffer.allocate(3);
            fileChannel.read(buffer2, 1);
            System.out.println("buffer2读取结果" + new String(buffer2.array()));

            // 定位到开始
            fileChannel.position(0);
            // 制造两个ByteBuffer的数组，分别位置定位到1，批量读取
            ByteBuffer b3 = ByteBuffer.allocate(3);
            b3.position(1);
            ByteBuffer b4 = ByteBuffer.allocate(3);
            b4.position(1);
            fileChannel.read(new ByteBuffer[]{b3, b4});
            System.out.println("b3,b4读取结果:" + new String(b3.array()) + new String(b4.array()));


        } catch (IOException e) {
            e.printStackTrace();
        }
        fileChannel.close();
        ref.close();
    }

    @Test
    /**
     * test003方法。
     */
    public void test003() throws IOException {
        ByteBuffer byteBuffer1 = ByteBuffer.wrap("12345678".getBytes());
        FileOutputStream fosRef = new FileOutputStream("/Users/zifang/workplace/idea_workplace/components/util-zex/src/main/resources/FileChannelTest003.txt");

        FileChannel fileChannel = fosRef.getChannel();
        fileChannel.write(byteBuffer1);
        System.out.println("A size=" + fileChannel.size() + " position=" + fileChannel.position());
        fileChannel.truncate(3);
        System.out.println("B size=" + fileChannel.size() + " position=" + fileChannel.position());
        fileChannel.close();
        fosRef.flush();
        fosRef.close();
    }

    @Test
    /**
     * test004方法。
     */
    public void test004() throws IOException {
        RandomAccessFile file1 = new RandomAccessFile("/Users/zifang/workplace/idea_workplace/components/util-zex/src/main/resources/FileChannelTest004_1.txt", "rw");
        RandomAccessFile file2 = new RandomAccessFile("/Users/zifang/workplace/idea_workplace/components/util-zex/src/main/resources/FileChannelTest004_2.txt", "rw");
        RandomAccessFile file3 = new RandomAccessFile("/Users/zifang/workplace/idea_workplace/components/util-zex/src/main/resources/FileChannelTest004_3.txt", "rw");

        FileChannel fileChannel1 = file1.getChannel();
        FileChannel fileChannel2 = file2.getChannel();
        FileChannel fileChannel3 = file3.getChannel();

        long readLength = fileChannel2.transferFrom(fileChannel1, 0, 4);
        long writeLength = fileChannel2.transferTo(1, 2, fileChannel3);

        fileChannel1.close();
        fileChannel2.close();
        fileChannel3.close();

        file1.close();
        file2.close();
        file3.close();

    }
}
