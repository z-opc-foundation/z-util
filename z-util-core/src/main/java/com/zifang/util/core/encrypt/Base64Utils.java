package com.zifang.util.core.encrypt;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64 编解码工具（JDK {@link Base64} 封装）。
 * <p>
 * {@link com.zifang.util.core.lang.StringUtil#base64Encode(String)} 只做字符串往返，
 * 本类面向密钥、盐值、密文等二进制字节数组，并补充 URL-safe 与容错解码。
 */
public final class Base64Utils {

    private Base64Utils() {
    }

    /**
     * 标准编码，不插入换行。
     *
     * @param data 待编码字节，为 null 时返回 null
     * @return Base64 字符串
     */
    public static String encode(byte[] data) {
        if (data == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * 标准解码。
     *
     * @param text Base64 字符串，为 null 或空时返回空数组
     * @return 解码后的字节
     * @throws IllegalArgumentException 含非法字符时抛出，容错场景改用 {@link #decodeLenient(String)}
     */
    public static byte[] decode(String text) {
        if (text == null || text.isEmpty()) {
            return new byte[0];
        }
        return Base64.getDecoder().decode(text);
    }

    /**
     * URL-safe 编码，不带 {@code =} 填充（用于出现在路径或查询串中的场景）。
     *
     * @param data 待编码字节，为 null 时返回 null
     * @return URL-safe Base64 字符串
     */
    public static String encodeUrlSafe(byte[] data) {
        if (data == null) {
            return null;
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    /**
     * URL-safe 解码，兼容带或不带 {@code =} 填充。
     *
     * @param text URL-safe Base64 字符串，为 null 或空时返回空数组
     * @return 解码后的字节
     */
    public static byte[] decodeUrlSafe(String text) {
        if (text == null || text.isEmpty()) {
            return new byte[0];
        }
        return Base64.getUrlDecoder().decode(text + padding(text));
    }

    /**
     * 字符串按 UTF-8 取字节后标准编码。
     *
     * @param text 待编码字符串，为 null 时返回 null
     * @return Base64 字符串
     */
    public static String encodeString(String text) {
        if (text == null) {
            return null;
        }
        return encode(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 标准解码后按 UTF-8 还原字符串。
     *
     * @param text Base64 字符串，为 null 或空时返回原值
     * @return 还原后的字符串
     */
    public static String decodeToString(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return new String(decode(text), StandardCharsets.UTF_8);
    }

    /**
     * 容错解码：剔除所有空白与换行（兼容折行存储的密钥、DB 里带 {@code \n} 的值），
     * 并把 URL-safe 字母表归一为标准字母表。
     * <p>
     * 不做"丢弃任意非法字符"的猜测式清洗——PEM 的 {@code -----BEGIN ...-----}  armor 行
     * 需由调用方先行剥除；armor 由合法字母表字符组成，未剥除时不会被识别，只会静默解出错误字节。
     *
     * @param text Base64 字符串，为 null 或空时返回空数组
     * @return 解码后的字节
     * @throws IllegalArgumentException 去空白后仍含字母表之外的字符时抛出
     */
    public static byte[] decodeLenient(String text) {
        if (text == null || text.isEmpty()) {
            return new byte[0];
        }
        StringBuilder cleaned = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (!Character.isWhitespace(c)) {
                cleaned.append(c);
            }
        }
        String value = cleaned.toString();
        if (value.indexOf('-') >= 0 || value.indexOf('_') >= 0) {
            return Base64.getUrlDecoder().decode(value + padding(value));
        }
        return Base64.getDecoder().decode(value + padding(value));
    }

    /**
     * 补齐 {@code =} 填充至 4 字节分组；已带填充时返回空串。
     */
    private static String padding(String value) {
        if (value.indexOf('=') >= 0) {
            return "";
        }
        int mod = value.length() % 4;
        if (mod == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = mod; i < 4; i++) {
            sb.append('=');
        }
        return sb.toString();
    }
}
