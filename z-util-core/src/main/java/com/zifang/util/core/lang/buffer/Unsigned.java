package com.zifang.util.core.lang.buffer;

import java.nio.ByteBuffer;

/**
 * ByteBuffer 无符号值读写工具类
 * <p>
 * Java 基础类型（byte、short、int）不带符号扩展，
 * 当需要读写无符号值时，需要使用本工具类进行掩码处理。
 * </p>
 * <p>
 * 该工具类主要用于：
 * <ul>
 *   <li>网络协议解析（通常使用无符号值）</li>
 *   <li>二进制文件读取</li>
 *   <li>位操作处理</li>
 * </ul>
 * </p>
 * <p>
 * 注意：所有无符号值在Java中以更宽的有符号类型存储：
 * <ul>
 *   <li>无符号byte (0-255) → short (0-255)</li>
 *   <li>无符号short (0-65535) → int (0-65535)</li>
 *   <li>无符号int (0-4294967295) → long (0-4294967295)</li>
 * </ul>
 * </p>
 *
 * @author zifang
 * @see java.nio.ByteBuffer
 */
public final class Unsigned {

    /**
     * 私有构造函数，防止实例化
     */
    private Unsigned() {
    }

    // -------------------- byte --------------------

    /**
     * 从ByteBuffer当前位置读取一个无符号byte值
     * <p>
     * 将byte值(带符号：-128~127)转换为无符号short值(0~255)
     *
     * @param bb 源ByteBuffer，不能为null
     * @return 无符号byte值（0~255）
     * @throws NullPointerException if bb is null
     */
    public static short getUnsignedByte(ByteBuffer bb) {
        return (short) (bb.get() & 0xff);
    }

    /**
     * 从ByteBuffer指定位置读取一个无符号byte值
     * <p>
     * 将byte值(带符号：-128~127)转换为无符号short值(0~255)
     *
     * @param bb       源ByteBuffer，不能为null
     * @param position 读取位置（绝对位置）
     * @return 无符号byte值（0~255）
     * @throws NullPointerException      if bb is null
     * @throws IndexOutOfBoundsException if position is out of bounds
     */
    public static short getUnsignedByte(ByteBuffer bb, int position) {
        return (short) (bb.get(position) & 0xff);
    }

    /**
     * 向ByteBuffer当前位置写入一个无符号byte值
     * <p>
     * 将无符号值(0~255)截断为有符号byte值(-128~127)后写入
     *
     * @param bb    目标ByteBuffer，不能为null
     * @param value 要写入的值（0~255）
     * @throws NullPointerException if bb is null
     */
    public static void putUnsignedByte(ByteBuffer bb, int value) {
        bb.put((byte) (value & 0xff));
    }

    /**
     * 向ByteBuffer指定位置写入一个无符号byte值
     * <p>
     * 将无符号值(0~255)截断为有符号byte值(-128~127)后写入
     *
     * @param bb       目标ByteBuffer，不能为null
     * @param position 写入位置（绝对位置）
     * @param value    要写入的值（0~255）
     * @throws NullPointerException      if bb is null
     * @throws IndexOutOfBoundsException if position is out of bounds
     */
    public static void putUnsignedByte(ByteBuffer bb, int position, int value) {
        bb.put(position, (byte) (value & 0xff));
    }

    // -------------------- short --------------------

    /**
     * 从ByteBuffer当前位置读取一个无符号short值
     * <p>
     * 将short值(带符号：-32768~32767)转换为无符号int值(0~65535)
     *
     * @param bb 源ByteBuffer，不能为null
     * @return 无符号short值（0~65535）
     * @throws NullPointerException if bb is null
     */
    public static int getUnsignedShort(ByteBuffer bb) {
        return bb.getShort() & 0xffff;
    }

    /**
     * 从ByteBuffer指定位置读取一个无符号short值
     * <p>
     * 将short值(带符号：-32768~32767)转换为无符号int值(0~65535)
     *
     * @param bb       源ByteBuffer，不能为null
     * @param position 读取位置（绝对位置）
     * @return 无符号short值（0~65535）
     * @throws NullPointerException      if bb is null
     * @throws IndexOutOfBoundsException if position is out of bounds
     */
    public static int getUnsignedShort(ByteBuffer bb, int position) {
        return bb.getShort(position) & 0xffff;
    }

    /**
     * 向ByteBuffer当前位置写入一个无符号short值
     * <p>
     * 将无符号值(0~65535)截断为有符号short值(-32768~32767)后写入
     *
     * @param bb    目标ByteBuffer，不能为null
     * @param value 要写入的值（0~65535）
     * @throws NullPointerException if bb is null
     */
    public static void putUnsignedShort(ByteBuffer bb, int value) {
        bb.putShort((short) (value & 0xffff));
    }

    /**
     * 向ByteBuffer指定位置写入一个无符号short值
     * <p>
     * 将无符号值(0~65535)截断为有符号short值(-32768~32767)后写入
     *
     * @param bb       目标ByteBuffer，不能为null
     * @param position 写入位置（绝对位置）
     * @param value    要写入的值（0~65535）
     * @throws NullPointerException      if bb is null
     * @throws IndexOutOfBoundsException if position is out of bounds
     */
    public static void putUnsignedShort(ByteBuffer bb, int position, int value) {
        bb.putShort(position, (short) (value & 0xffff));
    }

    // -------------------- int --------------------

    /**
     * 从ByteBuffer当前位置读取一个无符号int值
     * <p>
     * 将int值(带符号：-2147483648~2147483647)转换为无符号long值(0~4294967295)
     *
     * @param bb 源ByteBuffer，不能为null
     * @return 无符号int值（0~4294967295）
     * @throws NullPointerException if bb is null
     */
    public static long getUnsignedInt(ByteBuffer bb) {
        return bb.getInt() & 0xffffffffL;
    }

    /**
     * 从ByteBuffer指定位置读取一个无符号int值
     * <p>
     * 将int值(带符号：-2147483648~2147483647)转换为无符号long值(0~4294967295)
     *
     * @param bb       源ByteBuffer，不能为null
     * @param position 读取位置（绝对位置）
     * @return 无符号int值（0~4294967295）
     * @throws NullPointerException      if bb is null
     * @throws IndexOutOfBoundsException if position is out of bounds
     */
    public static long getUnsignedInt(ByteBuffer bb, int position) {
        return bb.getInt(position) & 0xffffffffL;
    }

    /**
     * 向ByteBuffer当前位置写入一个无符号int值
     * <p>
     * 将无符号值(0~4294967295)截断为有符号int值(-2147483648~2147483647)后写入
     *
     * @param bb    目标ByteBuffer，不能为null
     * @param value 要写入的值（0~4294967295）
     * @throws NullPointerException if bb is null
     */
    public static void putUnsignedInt(ByteBuffer bb, long value) {
        bb.putInt((int) (value & 0xffffffffL));
    }

    /**
     * 向ByteBuffer指定位置写入一个无符号int值
     * <p>
     * 将无符号值(0~4294967295)截断为有符号int值(-2147483648~2147483647)后写入
     *
     * @param bb       目标ByteBuffer，不能为null
     * @param position 写入位置（绝对位置）
     * @param value    要写入的值（0~4294967295）
     * @throws NullPointerException      if bb is null
     * @throws IndexOutOfBoundsException if position is out of bounds
     */
    public static void putUnsignedInt(ByteBuffer bb, int position, long value) {
        bb.putInt(position, (int) (value & 0xffffffffL));
    }
}
