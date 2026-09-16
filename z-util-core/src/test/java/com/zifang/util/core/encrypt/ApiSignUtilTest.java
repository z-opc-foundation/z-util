package com.zifang.util.core.encrypt;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * ApiSignUtilTest类。
 */
public class ApiSignUtilTest {

    @Test
    /**
     * testBuildSortedQueryString方法。
     */
    public void testBuildSortedQueryString() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "abc");
        params.put("app_id", "1000");
        params.put("timestamp", 1719300000L);

        // 按key字典序排序：app_id -> name -> timestamp
        assertEquals("app_id=1000&name=abc&timestamp=1719300000",
                ApiSignUtil.buildSortedQueryString(params));
    }

    @Test
    /**
     * testBuildSortedQueryString_skipNullAndEmpty方法。
     */
    public void testBuildSortedQueryString_skipNullAndEmpty() {
        Map<String, Object> params = new HashMap<>();
        params.put("b", null);
        params.put("a", "1");
        params.put("c", "");

        // 默认仅跳过null
        assertEquals("a=1&c=", ApiSignUtil.buildSortedQueryString(params));
        // 可选同时跳过空字符串
        assertEquals("a=1", ApiSignUtil.buildSortedQueryString(params, true));
    }

    @Test
    /**
     * testBuildSortedQueryString_empty方法。
     */
    public void testBuildSortedQueryString_empty() {
        assertEquals("", ApiSignUtil.buildSortedQueryString(null));
        assertEquals("", ApiSignUtil.buildSortedQueryString(new HashMap<String, Object>()));
        // 全部为null值时结果为空串
        Map<String, Object> params = new HashMap<>();
        params.put("a", null);
        assertEquals("", ApiSignUtil.buildSortedQueryString(params));
    }

    @Test
    /**
     * testSignWithMd5方法。
     * <p>
     * 用固定输入验证签名结果与独立计算的MD5值一致。
     */
    public void testSignWithMd5() {
        Map<String, Object> params = new HashMap<>();
        params.put("app_id", "1000");
        params.put("nonce", "a1b2c3");

        String sign = ApiSignUtil.signWithMd5(params, "secret1");
        // 查询串为 app_id=1000&nonce=a1b2c3，拼接密钥后为 app_id=1000&nonce=a1b2c3secret1
        String expected = ApiSignUtil.signWithMd5("app_id=1000&nonce=a1b2c3", "secret1");
        assertEquals(expected, sign);
        // MD5输出为32位大写十六进制
        assertEquals(32, sign.length());
        assertEquals(sign.toUpperCase(), sign);

        // 与MD5Utils独立计算交叉验证
        assertEquals(MD5Utils.encrypt("app_id=1000&nonce=a1b2c3secret1").toUpperCase(), sign);
    }

    @Test
    /**
     * testSignWithSha256方法。
     */
    public void testSignWithSha256() {
        Map<String, Object> params = new HashMap<>();
        params.put("b", "2");
        params.put("a", "1");

        String sign = ApiSignUtil.signWithSha256(params, "key");
        // SHA-256输出为64位小写十六进制
        assertEquals(64, sign.length());
        assertEquals(sign.toLowerCase(), sign);

        // 乱序放入结果一致（内部排序）
        Map<String, Object> params2 = new HashMap<>();
        params2.put("a", "1");
        params2.put("b", "2");
        assertEquals(sign, ApiSignUtil.signWithSha256(params2, "key"));
    }

    @Test
    /**
     * testSignWithHmacSha256方法。
     */
    public void testSignWithHmacSha256() {
        Map<String, Object> params = new HashMap<>();
        params.put("x", "1");
        params.put("y", "2");

        String sign = ApiSignUtil.signWithHmacSha256(params, "hmac-key");
        // HMAC-SHA256输出为64位小写十六进制
        assertEquals(64, sign.length());
        // 不同密钥产生不同签名
        assertNotEquals(sign, ApiSignUtil.signWithHmacSha256(params, "other-key"));
        // 查询串入口一致
        assertEquals(sign, ApiSignUtil.signWithHmacSha256("x=1&y=2", "hmac-key"));
    }

    @Test
    /**
     * testVerify方法。
     */
    public void testVerify() {
        Map<String, Object> params = new HashMap<>();
        params.put("a", "1");

        String md5Sign = ApiSignUtil.signWithMd5(params, "s");
        assertTrue(ApiSignUtil.verifyMd5(params, "s", md5Sign));
        // 忽略大小写
        assertTrue(ApiSignUtil.verifyMd5(params, "s", md5Sign.toLowerCase()));
        // 密钥不同验签失败
        assertFalse(ApiSignUtil.verifyMd5(params, "s2", md5Sign));

        String shaSign = ApiSignUtil.signWithSha256(params, "s");
        assertTrue(ApiSignUtil.verifySha256(params, "s", shaSign));
        assertFalse(ApiSignUtil.verifySha256(params, "s", shaSign + "0"));

        String hmacSign = ApiSignUtil.signWithHmacSha256(params, "s");
        assertTrue(ApiSignUtil.verifyHmacSha256(params, "s", hmacSign));
        assertFalse(ApiSignUtil.verifyHmacSha256(params, "s", null));
    }

    @Test
    /**
     * testSign_stable方法。
     * <p>
     * 相同参数多次签名结果稳定。
     */
    public void testSign_stable() {
        Map<String, Object> params = new HashMap<>();
        params.put("k1", "v1");
        params.put("k2", "v2");
        params.put("k3", null);

        String first = ApiSignUtil.signWithMd5(params, "secret");
        String second = ApiSignUtil.signWithMd5(params, "secret");
        assertEquals(first, second);
    }
}
