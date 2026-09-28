package com.zifang.util.core.encrypt;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Base62 编解码与短码生成。
 * <p>
 * 字母表为 {@code 0-9A-Za-z}，不含 {@code +/=/_} 等需转义字符，适合短链码、API Key、
 * 邀请码这类需要出现在 URL 或人工输入中的标识。字节编码保留前导零。
 */
public final class Base62 {

    private static final char[] ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    private static final int[] REVERSE = new int[128];

    private static final BigInteger BASE = BigInteger.valueOf(62);

    static {
        Arrays.fill(REVERSE, -1);
        for (int i = 0; i < ALPHABET.length; i++) {
            REVERSE[ALPHABET[i]] = i;
        }
    }

    private static final SecureRandom DEFAULT_RANDOM = new SecureRandom();

    private Base62() {
    }

    /**
     * 字节数组编码为 Base62 字符串。
     *
     * @param data 待编码字节，null 或空时返回空串
     * @return Base62 字符串，前导零字节以等量 '0' 保留
     */
    public static String encode(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        int leadingZeros = 0;
        while (leadingZeros < data.length && data[leadingZeros] == 0) {
            leadingZeros++;
        }
        StringBuilder sb = new StringBuilder();
        if (leadingZeros == data.length) {
            for (int i = 0; i < leadingZeros; i++) {
                sb.append(ALPHABET[0]);
            }
            return sb.toString();
        }
        BigInteger value = new BigInteger(1, Arrays.copyOfRange(data, leadingZeros, data.length));
        BigInteger[] quotientAndRemainder = value.divideAndRemainder(BASE);
        int[] digits = new int[value.bitLength() / 5 + 3];
        int index = digits.length;
        while (quotientAndRemainder[1].signum() != 0 || quotientAndRemainder[0].signum() != 0) {
            digits[--index] = quotientAndRemainder[1].intValue();
            quotientAndRemainder = quotientAndRemainder[0].divideAndRemainder(BASE);
        }
        for (int i = index; i < digits.length; i++) {
            sb.append(ALPHABET[digits[i]]);
        }
        for (int i = 0; i < leadingZeros; i++) {
            sb.insert(0, ALPHABET[0]);
        }
        return sb.toString();
    }

    /**
     * Base62 字符串解码为字节数组。
     *
     * @param text Base62 字符串，null 或空时返回空数组
     * @return 解码后的字节
     * @throws IllegalArgumentException 含字母表之外的字符时抛出
     */
    public static byte[] decode(String text) {
        if (text == null || text.isEmpty()) {
            return new byte[0];
        }
        int leadingZeroChars = 0;
        while (leadingZeroChars < text.length() && text.charAt(leadingZeroChars) == ALPHABET[0]) {
            leadingZeroChars++;
        }
        BigInteger value = BigInteger.ZERO;
        for (int i = leadingZeroChars; i < text.length(); i++) {
            value = value.multiply(BASE).add(BigInteger.valueOf(indexOf(text.charAt(i))));
        }
        if (value.signum() == 0) {
            return new byte[leadingZeroChars];
        }
        byte[] magnitude = value.toByteArray();
        if (magnitude.length > 1 && magnitude[0] == 0) {
            magnitude = Arrays.copyOfRange(magnitude, 1, magnitude.length);
        }
        byte[] result = new byte[magnitude.length + leadingZeroChars];
        System.arraycopy(magnitude, 0, result, leadingZeroChars, magnitude.length);
        return result;
    }

    /**
     * 生成指定长度的 Base62 随机串（默认 {@link SecureRandom}）。
     *
     * @param length 目标长度，必须大于 0
     * @return 随机短码
     */
    public static String random(int length) {
        return random(length, DEFAULT_RANDOM);
    }

    /**
     * 生成指定长度的 Base62 随机串。
     * <p>
     * 每个字符独立取字母表，长度即为结果长度，便于直接用作定长短码。
     *
     * @param length 目标长度，必须大于 0
     * @param random 随机源，为 null 时使用默认 {@link SecureRandom}
     * @return 随机短码
     */
    public static String random(int length, SecureRandom random) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be positive: " + length);
        }
        SecureRandom source = random == null ? DEFAULT_RANDOM : random;
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET[source.nextInt(ALPHABET.length)]);
        }
        return sb.toString();
    }

    /**
     * 单个 Base62 字符转数值。
     *
     * @param c 字符
     * @return 数值 0-61
     * @throws IllegalArgumentException 非法字符时抛出
     */
    private static int indexOf(char c) {
        if (c >= REVERSE.length || REVERSE[c] < 0) {
            throw new IllegalArgumentException("invalid base62 char: " + c);
        }
        return REVERSE[c];
    }
}
