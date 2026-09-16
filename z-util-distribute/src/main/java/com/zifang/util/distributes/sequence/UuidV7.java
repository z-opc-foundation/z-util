package com.zifang.util.distributes.sequence;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * UUIDv7 生成器（自研，对标 java.util.UUID v7 in JDK 17 / RFC 9562）。
 * <p>
 * UUIDv7 结构（128 bit）：
 * <ul>
 *   <li>48 bit：Unix epoch 毫秒（时序性）</li>
 *   <li>4 bit：version（=7）</li>
 *   <li>12 bit：随机 a</li>
 *   <li>2 bit：variant（=10，RFC 4122）</li>
 *   <li>62 bit：随机 b</li>
 * </ul>
 * <p>
 * 优点：相比 v4 随机 UUID，v7 可按时间排序（DB 主键友好）；相比 Snowflake 无需机器 ID。
 * <p>
 * 注意：本实现模拟 UUIDv7（UUID 类在 JDK 8 还是 v3/v4/v5，要 JDK 17+ 才有 v7）。手写位运算。
 */
public class UuidV7 {

    private static final SecureRandom RANDOM = new SecureRandom();

    private UuidV7() {
    }

    /**
     * 生成 UUIDv7 字符串（36 字符，含 4 个 -）。
     */
    public static String next() {
        return format(toBytes(nextMillis(), nextRandom12(), nextRandom62()));
    }

    /**
     * 给定时间戳生成（用于测试 / 写时定）。
     */
    public static String fromMillis(long epochMillis) {
        return format(toBytes(epochMillis, nextRandom12(), nextRandom62()));
    }

    /**
     * UUID 对象形式。
     */
    public static UUID toUuid(String v7) {
        return UUID.fromString(v7);
    }

    // ===== 内部 =====

    private static long nextMillis() {
        return System.currentTimeMillis();
    }

    private static int nextRandom12() {
        return RANDOM.nextInt() & 0xFFF;   // 12 bit
    }

    private static long nextRandom62() {
        long r = RANDOM.nextLong();
        return r & 0x3FFFFFFFFFFFFFFFL;     // 62 bit
    }

    private static byte[] toBytes(long millis, int randA12, long randB62) {
        // 16 字节布局：msb(8) | lsb(8)
        // msb: [48 ms timestamp][4 ver=7][12 randA]
        // lsb: [2 var=10][62 randB]
        long msb = (millis & 0xFFFFFFFFFFFFL) << 16;       // 48 bit timestamp
        msb |= 0x7000L;                                    // version=7 in 4 bit
        msb |= (randA12 & 0xFFF);                          // 12 bit randA
        long lsb = 0x8000000000000000L;                    // variant=10 in 2 bit
        lsb |= (randB62 & 0x3FFFFFFFFFFFFFFFL);            // 62 bit randB
        return toBytes(msb, lsb);
    }

    private static byte[] toBytes(long msb, long lsb) {
        byte[] b = new byte[16];
        for (int i = 7; i >= 0; i--) {
            b[i] = (byte) (msb & 0xFF);
            msb >>>= 8;
        }
        for (int i = 15; i >= 8; i--) {
            b[i] = (byte) (lsb & 0xFF);
            lsb >>>= 8;
        }
        return b;
    }

    private static String format(byte[] b) {
        // 8-4-4-4-12
        StringBuilder sb = new StringBuilder(36);
        appendHex(sb, b, 0, 4);
        sb.append('-');
        appendHex(sb, b, 4, 2);
        sb.append('-');
        appendHex(sb, b, 6, 2);
        sb.append('-');
        appendHex(sb, b, 8, 2);
        sb.append('-');
        appendHex(sb, b, 10, 6);
        return sb.toString();
    }

    private static void appendHex(StringBuilder sb, byte[] b, int off, int len) {
        for (int i = 0; i < len; i++) {
            int v = b[off + i] & 0xFF;
            sb.append(Character.forDigit(v >>> 4, 16));
            sb.append(Character.forDigit(v & 0xF, 16));
        }
    }
}
