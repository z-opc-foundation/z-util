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

import org.junit.Test;

import java.nio.*;

/**
 * NioTest001类。
 */
public class NioTest001 {

    // capacity（容量）
    //·limit（限制）
    //·position（位置）
    //·mark（标记）
    // 0≤mark≤position≤limit≤capacity
    @Test
    /**
     * test001方法。
     */
    public void test001() {
        byte[] byteArray = new byte[]{1, 2, 3};
        short[] shortArray = new short[]{1, 2, 3, 4};
        int[] intArray = new int[]{1, 2, 3, 4, 5};
        long[] longArray = new long[]{1, 2, 3, 4, 5, 6};
        float[] floatArray = new float[]{1, 2, 3, 4, 5, 6, 7};
        double[] doubleArray = new double[]{1, 2, 3, 4, 5, 6, 7, 8};
        char[] charArray = new char[]{'a', 'b', 'c', 'd'};
        ByteBuffer bytebuffer = ByteBuffer.wrap(byteArray);
        ShortBuffer shortBuffer = ShortBuffer.wrap(shortArray);
        IntBuffer intBuffer = IntBuffer.wrap(intArray);
        LongBuffer longBuffer = LongBuffer.wrap(longArray);
        FloatBuffer floatBuffer = FloatBuffer.wrap(floatArray);
        DoubleBuffer doubleBuffer = DoubleBuffer.wrap(doubleArray);
        CharBuffer charBuffer = CharBuffer.wrap(charArray);
        System.out.println("bytebuffer=" + bytebuffer.getClass().getName());
        System.out.println("shortBuffer=" + shortBuffer.getClass().getName());
        System.out.println("intBuffer=" + intBuffer.getClass().getName());
        System.out.println("longBuffer=" + longBuffer.getClass().getName());
        System.out.println("floatBuffer=" + floatBuffer.getClass().getName());
        System.out.println("doubleBuffer=" + doubleBuffer.getClass().getName());
        System.out.println("charBuffer=" + charBuffer.getClass().getName());
        System.out.println();
        System.out.println("bytebuffer.capacity=" + bytebuffer.capacity());
        System.out.println("shortBuffer.capacity=" + shortBuffer.capacity());
        System.out.println("intBuffer.capacity=" + intBuffer.capacity());
        System.out.println("longBuffer.capacity=" + longBuffer.capacity());
        System.out.println("floatBuffer.capacity=" + floatBuffer.capacity());
        System.out.println("doubleBuffer.capacity=" + doubleBuffer.capacity());
        System.out.println("charBuffer.capacity=" + charBuffer.capacity());
    }

//    @Test
//    public void test1(){
//        byte[] byteArray = new byte[] { 1, 2, 3, 4, 5, 6, 7};
//        ByteBuffer bytebuffer = ByteBuffer.wrap(byteArray);
//        // 容量
//        assert bytebuffer.capacity() == 7;
//        assert bytebuffer.limit() == 7;
//        Assert.assertThrows(NullPointerException.class,()->System.out.println("sss"));
//    }
}
