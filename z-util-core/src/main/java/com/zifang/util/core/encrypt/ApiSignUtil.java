package com.zifang.util.core.encrypt;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

/**
 * API参数签名工具类
 * <p>
 * 面向开放接口调用签名场景，将请求参数按参数名字典序排序后拼接为查询串，
 * 再对「查询串 + 密钥」做摘要或HMAC运算得到签名值。
 * <p>
 * 使用示例：
 * <pre>
 * Map&lt;String, Object&gt; params = new HashMap&lt;&gt;();
 * params.put("app_id", "1000");
 * params.put("timestamp", "1719300000");
 * params.put("nonce", "a1b2c3");
 *
 * // MD5签名（大写十六进制）
 * String sign = ApiSignUtil.signWithMd5(params, secret);
 *
 * // SHA-256签名（小写十六进制）
 * String sign = ApiSignUtil.signWithSha256(params, secret);
 *
 * // HMAC-SHA256签名
 * String sign = ApiSignUtil.signWithHmacSha256(params, secret);
 *
 * // 验签
 * boolean ok = ApiSignUtil.verifyMd5(params, secret, receivedSign);
 * </pre>
 * <p>
 * 签名规则：
 * <ul>
 *   <li>参数按参数名自然字典序（区分大小写）排序</li>
 *   <li>拼接格式为 key1=value1&amp;key2=value2...</li>
 *   <li>默认跳过值为null的参数；可选跳过空字符串</li>
 *   <li>密钥直接拼接在查询串末尾（MD5/SHA-256）或作为HMAC密钥使用</li>
 * </ul>
 *
 * @author zifang
 * @since 1.0
 */
public class ApiSignUtil {

    /**
     * MD5 算法名称
     */
    private static final String ALGORITHM_MD5 = "MD5";

    /**
     * SHA-256 算法名称
     */
    private static final String ALGORITHM_SHA256 = "SHA-256";

    /**
     * HMAC-SHA256 算法名称
     */
    private static final String ALGORITHM_HMAC_SHA256 = "HmacSHA256";

    /**
     * 查询串键值对之间的分隔符
     */
    private static final String PAIR_SEPARATOR = "&";

    /**
     * 键与值之间的连接符
     */
    private static final String KEY_VALUE_SEPARATOR = "=";

    private ApiSignUtil() {
        // 工具类禁止实例化
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 构建按参数名字典序排序的查询串
     * <p>
     * 跳过值为null的参数，其余参数以key=value形式用&amp;连接。
     *
     * @param params 请求参数
     * @return 排序后的查询串；参数为null或为空时返回空字符串
     */
    public static String buildSortedQueryString(Map<String, ?> params) {
        return buildSortedQueryString(params, false);
    }

    /**
     * 构建按参数名字典序排序的查询串
     *
     * @param params          请求参数
     * @param skipEmptyValues 是否同时跳过空字符串值
     * @return 排序后的查询串；参数为null或为空时返回空字符串
     */
    public static String buildSortedQueryString(Map<String, ?> params, boolean skipEmptyValues) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        // TreeMap天然按key自然顺序排序
        Map<String, ?> sorted = new TreeMap<>(params);
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, ?> entry : sorted.entrySet()) {
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            String valueStr = String.valueOf(value);
            if (skipEmptyValues && valueStr.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(PAIR_SEPARATOR);
            }
            builder.append(entry.getKey()).append(KEY_VALUE_SEPARATOR).append(valueStr);
        }
        return builder.toString();
    }

    /**
     * MD5签名：对「查询串 + 密钥」做MD5摘要，返回大写十六进制
     *
     * @param params 请求参数
     * @param secret 签名密钥
     * @return 签名值（32位大写十六进制）
     */
    public static String signWithMd5(Map<String, ?> params, String secret) {
        return digestToHex(ALGORITHM_MD5, buildSortedQueryString(params) + nullToEmpty(secret), true);
    }

    /**
     * MD5签名：对「查询串 + 密钥」做MD5摘要
     *
     * @param queryString 已排序的查询串
     * @param secret      签名密钥
     * @return 签名值（32位大写十六进制）
     */
    public static String signWithMd5(String queryString, String secret) {
        return digestToHex(ALGORITHM_MD5, nullToEmpty(queryString) + nullToEmpty(secret), true);
    }

    /**
     * SHA-256签名：对「查询串 + 密钥」做SHA-256摘要，返回小写十六进制
     *
     * @param params 请求参数
     * @param secret 签名密钥
     * @return 签名值（64位小写十六进制）
     */
    public static String signWithSha256(Map<String, ?> params, String secret) {
        return digestToHex(ALGORITHM_SHA256, buildSortedQueryString(params) + nullToEmpty(secret), false);
    }

    /**
     * SHA-256签名：对「查询串 + 密钥」做SHA-256摘要
     *
     * @param queryString 已排序的查询串
     * @param secret      签名密钥
     * @return 签名值（64位小写十六进制）
     */
    public static String signWithSha256(String queryString, String secret) {
        return digestToHex(ALGORITHM_SHA256, nullToEmpty(queryString) + nullToEmpty(secret), false);
    }

    /**
     * HMAC-SHA256签名：以密钥对查询串做HMAC-SHA256运算，返回小写十六进制
     *
     * @param params 请求参数
     * @param key    HMAC密钥
     * @return 签名值（64位小写十六进制）
     */
    public static String signWithHmacSha256(Map<String, ?> params, String key) {
        return hmacSha256ToHex(buildSortedQueryString(params), nullToEmpty(key));
    }

    /**
     * HMAC-SHA256签名：以密钥对查询串做HMAC-SHA256运算
     *
     * @param queryString 已排序的查询串
     * @param key         HMAC密钥
     * @return 签名值（64位小写十六进制）
     */
    public static String signWithHmacSha256(String queryString, String key) {
        return hmacSha256ToHex(nullToEmpty(queryString), nullToEmpty(key));
    }

    /**
     * 验证MD5签名：忽略大小写比较
     *
     * @param params 请求参数
     * @param secret 签名密钥
     * @param sign   待验证的签名值
     * @return 一致返回true，否则返回false
     */
    public static boolean verifyMd5(Map<String, ?> params, String secret, String sign) {
        return verify(signWithMd5(params, secret), sign);
    }

    /**
     * 验证SHA-256签名：忽略大小写比较
     *
     * @param params 请求参数
     * @param secret 签名密钥
     * @param sign   待验证的签名值
     * @return 一致返回true，否则返回false
     */
    public static boolean verifySha256(Map<String, ?> params, String secret, String sign) {
        return verify(signWithSha256(params, secret), sign);
    }

    /**
     * 验证HMAC-SHA256签名：忽略大小写比较
     *
     * @param params 请求参数
     * @param key    HMAC密钥
     * @param sign   待验证的签名值
     * @return 一致返回true，否则返回false
     */
    public static boolean verifyHmacSha256(Map<String, ?> params, String key, String sign) {
        return verify(signWithHmacSha256(params, key), sign);
    }

    /**
     * 忽略大小写比较两个签名值是否一致
     *
     * @param expected 期望的签名值
     * @param actual   实际的签名值
     * @return 一致返回true；任一为null或内容不同返回false
     */
    private static boolean verify(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return expected.equalsIgnoreCase(actual);
    }

    /**
     * 计算消息摘要并转为十六进制字符串
     *
     * @param algorithm 摘要算法名称
     * @param content   待摘要内容
     * @param upperCase 结果是否转为大写
     * @return 十六进制摘要字符串
     */
    private static String digestToHex(String algorithm, String content, boolean upperCase) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] bytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            String hex = toHex(bytes);
            return upperCase ? hex.toUpperCase() : hex;
        } catch (NoSuchAlgorithmException e) {
            // JDK内置算法，理论上不会缺失
            throw new IllegalStateException("Digest algorithm not available: " + algorithm, e);
        }
    }

    /**
     * 计算HMAC-SHA256并转为小写十六进制字符串
     *
     * @param content 待签名内容
     * @param key     HMAC密钥
     * @return 十六进制签名字符串
     */
    private static String hmacSha256ToHex(String content, String key) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM_HMAC_SHA256);
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM_HMAC_SHA256);
            mac.init(keySpec);
            byte[] bytes = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return toHex(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("HMAC-SHA256 signing failed", e);
        }
    }

    /**
     * 字节数组转小写十六进制字符串
     *
     * @param bytes 字节数组
     * @return 小写十六进制字符串
     */
    private static String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() < 2) {
                builder.append('0');
            }
            builder.append(hex);
        }
        return builder.toString();
    }

    /**
     * null转空字符串
     *
     * @param value 原字符串
     * @return 原字符串或空字符串
     */
    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
