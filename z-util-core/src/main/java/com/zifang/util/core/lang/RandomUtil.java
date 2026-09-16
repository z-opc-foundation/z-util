package com.zifang.util.core.lang;

import java.util.Base64;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

/**
 * 随机数工具类
 */
public class RandomUtil {
    public static final String LETTERCHAR = "abcdefghijkllmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String NUMBERCHAR = "0123456789";
    private static final String ALL_CHAR = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LETTER_CHAR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBER_CHAR = "0123456789";

    /**
     * 生成制定范围内的随机数
     */
    public static int integer(int scopeMin, int scoeMax) {
        Random random = new Random();
        return (random.nextInt(scoeMax) % (scoeMax - scopeMin + 1) + scopeMin);
    }

    /**
     * 返回固定长度的数字
     */
    public static String number(int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(NUMBERCHAR.charAt(random.nextInt(NUMBERCHAR.length())));
        }
        return sb.toString();
    }

    /**
     * 返回一个定长的随机字符串(只包含大小写字母、数字)
     */
    public static String string(int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(ALL_CHAR.charAt(random.nextInt(ALL_CHAR.length())));
        }
        return sb.toString();
    }

    /**
     * 返回一个定长的随机纯字母字符串(只包含大小写字母)
     */
    public static String mixString(int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(ALL_CHAR.charAt(random.nextInt(LETTERCHAR.length())));
        }
        return sb.toString();
    }

    /**
     * 返回一个定长的随机纯大写字母字符串(只包含大小写字母)
     */
    public static String lowerString(int length) {
        return mixString(length).toLowerCase();
    }

    /**
     * 返回一个定长的随机纯小写字母字符串(只包含大小写字母)
     */
    public static String upperString(int length) {
        return mixString(length).toUpperCase();
    }

    /**
     * 生成一个定长的纯0字符串
     */
    public static String zeroString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append('0');
        }
        return sb.toString();
    }

    /**
     * 根据数字生成一个定长的字符串，长度不够前面补0
     */
    public static String toFixdLengthString(long num, int fixdlenth) {
        StringBuilder sb = new StringBuilder();
        String strNum = String.valueOf(num);
        if (fixdlenth - strNum.length() >= 0) {
            sb.append(zeroString(fixdlenth - strNum.length()));
        } else {
            throw new RuntimeException("将数字" + num + "转化为长度为" + fixdlenth + "的字符串发生异常！");
        }
        sb.append(strNum);
        return sb.toString();
    }

    /**
     * 根据数字生成一个定长的字符串，长度不够前面补0
     *
     * @param num       数字
     * @param fixdlenth 字符串长度
     * @return 定长的字符串
     */
    public static String toFixdLengthString(int num, int fixdlenth) {
        StringBuilder sb = new StringBuilder();
        String strNum = String.valueOf(num);
        if (fixdlenth - strNum.length() >= 0) {
            sb.append(zeroString(fixdlenth - strNum.length()));
        } else {
            throw new RuntimeException("将数字" +
                    num + "转化为长度为" + fixdlenth + "的字符串发生异常！");
        }
        sb.append(strNum);
        return sb.toString();
    }

    /**
     * 每次生成的len位数都不相同
     *
     * @param param
     * @return 定长的数字
     */
    public static int getNotSimple(int[] param, int len) {
        Random rand = new Random();
        for (int i = param.length; i > 1; i--) {
            int index = rand.nextInt(i);
            int tmp = param[index];
            param[index] = param[i - 1];
            param[i - 1] = tmp;
        }
        int result = 0;
        for (int i = 0; i < len; i++) {
            result = result * 10 + param[i];
        }
        return result;
    }

    /**
     * 从指定的数组中随机数组中的某个元素
     */
    public static <T> T randomItem(T[] param) {
        int index = integer(0, param.length);
        return param[index];
    }

    /**
     * uuid16方法。
     *
     * @return static String类型返回值
     */
    public static String uuid16() {
        String uuid = UUID.randomUUID().toString();
        char[] cs = new char[32];
        char c = 0;
        for (int i = uuid.length() / 2, j = 1; i-- > 0; ) {
            if ((c = uuid.charAt(i)) != '-') {
                cs[j++] = c;
            }
        }
        return String.valueOf(cs);
    }

    /**
     * 返回一个UUID
     *
     * @return 小写的UUID
     */
    public static String uuid() {
        UUID uuid = UUID.randomUUID();
        String s = uuid.toString();
        return s.substring(0, 8) + s.substring(9, 13) +
                s.substring(14, 18) + s.substring(19, 23) + s.substring(24);
    }

    /**
     * 返回一个UUID
     *
     * @return 大写的UUID
     */
    public static String UUID() {
        UUID uuid = UUID.randomUUID();
        String s = uuid.toString();
        String temp = s.substring(0, 8) + s.substring(9, 13) +
                s.substring(14, 18) + s.substring(19, 23) + s.substring(24);
        return temp.toUpperCase();
    }

    /**
     * 返回一个有序列的uuid编码
     * 前11位为时间(毫秒)
     * 中间4位为主机特征码
     * 剩下的保证其唯一性
     *
     * @param hostFeature 主机特征码建议设置4位
     * @return
     */
    public static String squid(String hostFeature) {
        Long date = new Date().getTime();
        String s = UUID.randomUUID().toString();
        String str = Long.toHexString(date);
        String result = str + hostFeature
                + s.substring(17, 18) + s.substring(19, 23) + s.substring(24);
        return result.toUpperCase();
    }

    /**
     * 生成 32 位紧凑 UUID（去除横线）。
     * <p>
     * 等价于 {@code UUID.randomUUID().toString().replace("-", "")}。
     * <p>
     * 示例：{@code uuidCompact() -> "550e8400e29b41d4a716446655440000"}
     *
     * @return 32 位十六进制字符串（无横线）
     */
    public static String uuidCompact() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成 Base64 编码的紧凑 UUID。
     * <p>
     * 将随机 UUID 的 128 位按大端序拆为 16 字节后整体进行 Base64 编码，
     * 得到 24 个字符（尾部含 {@code "=="} 补位符），比 32 位十六进制表示更短，
     * 适合用于对长度敏感的随机标识场景。
     * <p>
     * 示例：{@code uuidBase64() -> "aGVsbG8gd29ybGQhMTIzNA=="}
     *
     * @return 24 位 Base64 字符串
     */
    public static String uuidBase64() {
        UUID uuid = UUID.randomUUID();
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        byte[] buffer = new byte[16];
        for (int i = 0; i < 8; i++) {
            buffer[i] = (byte) ((msb >>> 8 * (7 - i)) & 0xFF);
            buffer[i + 8] = (byte) ((lsb >>> 8 * (7 - i)) & 0xFF);
        }
        return Base64.getEncoder().encodeToString(buffer);
    }

    /**
     * 生成指定长度的 UUID 短码（十六进制字符）。
     * <p>
     * 等价于 {@code UUID.randomUUID().toString().replace("-", "").substring(0, len)}。
     * 当 len &gt; 32 时，返回完整 32 位紧凑 UUID。
     *
     * @param len 期望返回的字符长度，范围 [1, 32]
     * @return 指定长度的十六进制字符串
     * @throws IllegalArgumentException 当 len &lt; 1 时
     */
    public static String uuidShort(int len) {
        if (len < 1) {
            throw new IllegalArgumentException("uuidShort length must be >= 1, got " + len);
        }
        String compact = uuidCompact();
        return len >= compact.length() ? compact : compact.substring(0, len);
    }

    /**
     * 生成带前缀的 UUID 短码。
     * <p>
     * 格式：{@code "{prefix}-{shortUuid}"}，例如 {@code "agent-550e8400"}。
     * 当 prefix 为 null 或空时，返回纯短码（等价于 {@link #uuidShort(int)}）。
     *
     * @param prefix 业务前缀，例如 {@code "agent"}、{@code "user"}；可为 null
     * @param len    短码部分长度，范围 [1, 32]
     * @return 带前缀的短码字符串
     * @throws IllegalArgumentException 当 len &lt; 1 时
     */
    public static String uuidWithPrefix(String prefix, int len) {
        if (len < 1) {
            throw new IllegalArgumentException("uuidWithPrefix length must be >= 1, got " + len);
        }
        String shortUuid = uuidShort(len);
        if (prefix == null || prefix.isEmpty()) {
            return shortUuid;
        }
        return prefix + "-" + shortUuid;
    }


    /**
     * 从指定的数组中按照指定比例返回指定的随机元素
     *
     * @param param     随机数组
     * @param percentum 比例
     * @param <T>
     * @return
     */
//    public static <T> T randomItem(T[] param, double[] percentum) {
//        int length = percentum.length;
//        Integer[] ints = ArrayUtil.doubleBitCount(percentum);
//        int max = Collections.max(Arrays.asList(ints));
//
//        int sum = 0;
//        Map map = new HashMap(length);
//        StringBuffer buffer = new StringBuffer();
//        for (int i = 0; i < max; i++) {
//            buffer.append("0");
//        }
//        int multiple = Integer.parseInt("1" + buffer.toString());
//        for (int i = 0; i < length; i++) {
//            int temp = (int) (percentum[i] * multiple);
//
//            if (i == 0) {
//                map.put(i, new int[]{1, temp});
//            } else {
//                map.put(i, new int[]{sum, sum + temp});
//            }
//            sum += temp;
//        }
//        int indexSum = integer(1, sum);
//        int index = -1;
//        for (int i = 0; i < length; i++) {
//            int[] scope = (int[]) map.get(i);
//            if (indexSum == 1) {
//                index = 0;
//                break;
//            }
//            if (indexSum > scope[0] && indexSum <= scope[1]) {
//                index = i;
//                break;
//            }
//        }
//        if (index == -1) {
//            throw new RuntimeException("随机失败");
//        } else {
//            return param[index];
//        }
//    }


    /**
     * 获取定长的随机数，包含大小写、数字
     *
     * @param length 随机数长度
     * @return
     */
    public static String generateString(int length) {
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(ALL_CHAR.charAt(random.nextInt(ALL_CHAR.length())));
        }
        return sb.toString();
    }

    /**
     * 获取定长的随机数，包含大小写字母
     *
     * @param length 随机数长度
     * @return
     */
    public static String generateMixString(int length) {
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(LETTER_CHAR.charAt(random.nextInt(LETTER_CHAR.length())));
        }
        return sb.toString();
    }

    /**
     * 获取定长的随机数，只包含小写字母
     *
     * @param length 随机数长度
     * @return
     */
    public static String generateLowerString(int length) {
        return generateMixString(length).toLowerCase();
    }

    /**
     * 获取定长的随机数，只包含大写字母
     */
    public static String generateUpperString(int length) {
        return generateMixString(length).toUpperCase();
    }

    /**
     * 获取定长的随机数，只包含数字
     *
     * @param length 随机数长度
     * @return
     */
    public static String generateNumberString(int length) {
        StringBuffer sb = new StringBuffer();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(NUMBER_CHAR.charAt(random.nextInt(NUMBER_CHAR.length())));
        }
        return sb.toString();
    }

}
