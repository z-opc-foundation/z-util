package com.zifang.util.core.net;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * UrlUtil 单元测试类
 */

/**
 * UrlUtilTest类。
 */
public class UrlUtilTest {

    // ========== encode ==========

    @Test
    /**
     * testEncode_Null方法。
     */
    public void testEncode_Null() {
        assertNull(UrlUtil.encode(null));
    }

    @Test
    /**
     * testEncode_NormalString方法。
     */
    public void testEncode_NormalString() {
        assertEquals("hello%20world", UrlUtil.encode("hello world"));
    }

    @Test
    /**
     * testEncode_SpecialChars方法。
     */
    public void testEncode_SpecialChars() {
        String result = UrlUtil.encode("a=b&c=d");
        assertTrue(result.contains("%3D"));
        assertTrue(result.contains("%26"));
    }

    @Test
    /**
     * testEncode_Chinese方法。
     */
    public void testEncode_Chinese() {
        String result = UrlUtil.encode("测试");
        assertTrue(result.contains("%E"));
    }

    @Test
    /**
     * testEncode_CustomEncoding方法。
     */
    public void testEncode_CustomEncoding() {
        assertEquals("hello%20world", UrlUtil.encode("hello world", "UTF-8"));
    }

    @Test
    /**
     * testEncode_Empty方法。
     */
    public void testEncode_Empty() {
        assertEquals("", UrlUtil.encode(""));
    }

    // ========== decode ==========

    @Test
    /**
     * testDecode_Null方法。
     */
    public void testDecode_Null() {
        assertNull(UrlUtil.decode(null));
    }

    @Test
    /**
     * testDecode_EncodedString方法。
     */
    public void testDecode_EncodedString() {
        assertEquals("hello world", UrlUtil.decode("hello%20world"));
    }

    @Test
    /**
     * testDecode_CustomEncoding方法。
     */
    public void testDecode_CustomEncoding() {
        assertEquals("hello world", UrlUtil.decode("hello%20world", "UTF-8"));
    }

    @Test
    /**
     * testDecode_Chinese方法。
     */
    public void testDecode_Chinese() {
        assertEquals("测试", UrlUtil.decode("%E6%B5%8B%E8%AF%95"));
    }

    // ========== setParam ==========

    @Test
    /**
     * testSetParam_NullUrl方法。
     */
    public void testSetParam_NullUrl() {
        assertNull(UrlUtil.setParam(null, "name", "value"));
    }

    @Test
    /**
     * testSetParam_NullParamName方法。
     */
    public void testSetParam_NullParamName() {
        assertNull(UrlUtil.setParam("http://example.com", null, "value"));
    }

    @Test
    /**
     * testSetParam_WithoutExistingParams方法。
     */
    public void testSetParam_WithoutExistingParams() {
        String result = UrlUtil.setParam("http://example.com", "name", "value");
        assertEquals("http://example.com?name=value", result);
    }

    @Test
    /**
     * testSetParam_WithExistingParams方法。
     */
    public void testSetParam_WithExistingParams() {
        String result = UrlUtil.setParam("http://example.com?a=1", "b", "2");
        assertEquals("http://example.com?a=1&b=2", result);
    }

    @Test
    /**
     * testSetParam_ReplaceExistingParam方法。
     */
    public void testSetParam_ReplaceExistingParam() {
        String result = UrlUtil.setParam("http://example.com?name=old", "name", "new");
        assertEquals("http://example.com?name=new", result);
    }

    @Test
    /**
     * testSetParam_ReplaceMiddleParam方法。
     */
    public void testSetParam_ReplaceMiddleParam() {
        String result = UrlUtil.setParam("http://example.com?a=1&b=2&c=3", "b", "new");
        assertEquals("http://example.com?a=1&b=new&c=3", result);
    }

    @Test
    /**
     * testSetParam_ValueAutoEncoded方法。
     */
    public void testSetParam_ValueAutoEncoded() {
        String result = UrlUtil.setParam("http://example.com", "name", "hello world");
        assertEquals("http://example.com?name=hello%20world", result);
    }

    // ========== getParamValue ==========

    @Test
    /**
     * testGetParamValue_NullUrl方法。
     */
    public void testGetParamValue_NullUrl() {
        assertNull(UrlUtil.getParamValue(null, "name"));
    }

    @Test
    /**
     * testGetParamValue_NullParamName方法。
     */
    public void testGetParamValue_NullParamName() {
        assertNull(UrlUtil.getParamValue("http://example.com?name=test", null));
    }

    @Test
    /**
     * testGetParamValue_WithExistingParam方法。
     */
    public void testGetParamValue_WithExistingParam() {
        assertEquals("test", UrlUtil.getParamValue("http://example.com?name=test", "name"));
    }

    @Test
    /**
     * testGetParamValue_WithMultipleParams方法。
     */
    public void testGetParamValue_WithMultipleParams() {
        assertEquals("2", UrlUtil.getParamValue("http://example.com?a=1&b=2&c=3", "b"));
    }

    @Test
    /**
     * testGetParamValue_FirstParam方法。
     */
    public void testGetParamValue_FirstParam() {
        assertEquals("1", UrlUtil.getParamValue("http://example.com?a=1&b=2", "a"));
    }

    @Test
    /**
     * testGetParamValue_LastParam方法。
     */
    public void testGetParamValue_LastParam() {
        assertEquals("3", UrlUtil.getParamValue("http://example.com?a=1&b=2&c=3", "c"));
    }

    @Test
    /**
     * testGetParamValue_WithNonExistentParam方法。
     */
    public void testGetParamValue_WithNonExistentParam() {
        assertNull(UrlUtil.getParamValue("http://example.com?name=test", "nonExistent"));
    }

    @Test
    /**
     * testGetParamValue_WithoutParams方法。
     */
    public void testGetParamValue_WithoutParams() {
        assertNull(UrlUtil.getParamValue("http://example.com", "name"));
    }

    @Test
    /**
     * testGetParamValue_ValueAutoDecoded方法。
     */
    public void testGetParamValue_ValueAutoDecoded() {
        assertEquals("hello world", UrlUtil.getParamValue("http://example.com?name=hello%20world", "name"));
    }

    // ========== removeParam ==========

    @Test
    /**
     * testRemoveParam_NullUrl方法。
     */
    public void testRemoveParam_NullUrl() {
        assertNull(UrlUtil.removeParam(null, "name"));
    }

    @Test
    /**
     * testRemoveParam_NullParamNames方法。
     */
    public void testRemoveParam_NullParamNames() {
        assertEquals("http://example.com?name=test", UrlUtil.removeParam("http://example.com?name=test", (String[]) null));
    }

    @Test
    /**
     * testRemoveParam_SingleParam方法。
     */
    public void testRemoveParam_SingleParam() {
        String result = UrlUtil.removeParam("http://example.com?name=test", "name");
        assertEquals("http://example.com", result);
    }

    @Test
    /**
     * testRemoveParam_MultipleParams方法。
     */
    public void testRemoveParam_MultipleParams() {
        String result = UrlUtil.removeParam("http://example.com?a=1&b=2&c=3", "b");
        assertTrue(result.contains("a=1"));
        assertFalse(result.contains("b=2"));
        assertTrue(result.contains("c=3"));
    }

    @Test
    /**
     * testRemoveParam_MultipleParamNames方法。
     */
    public void testRemoveParam_MultipleParamNames() {
        String result = UrlUtil.removeParam("http://example.com?a=1&b=2&c=3", "a", "c");
        assertFalse(result.contains("a=1"));
        assertTrue(result.contains("b=2"));
        assertFalse(result.contains("c=3"));
    }

    @Test
    /**
     * testRemoveParam_NonExistentParam方法。
     */
    public void testRemoveParam_NonExistentParam() {
        assertEquals("http://example.com?name=test", UrlUtil.removeParam("http://example.com?name=test", "nonExistent"));
    }

    @Test
    /**
     * testRemoveParam_LastParam方法。
     */
    public void testRemoveParam_LastParam() {
        assertEquals("http://example.com?a=1&b=2", UrlUtil.removeParam("http://example.com?a=1&b=2&c=3", "c"));
    }

    @Test
    /**
     * testRemoveParam_OnlyParam方法。
     */
    public void testRemoveParam_OnlyParam() {
        assertEquals("http://example.com", UrlUtil.removeParam("http://example.com?name=test", "name"));
    }

    // ========== urlJoin ==========

    @Test
    /**
     * testUrlJoin_NullBaseUrl方法。
     */
    public void testUrlJoin_NullBaseUrl() {
        assertEquals("http://other.com/new", UrlUtil.urlJoin(null, "http://other.com/new"));
    }

    @Test
    /**
     * testUrlJoin_NullLocation方法。
     */
    public void testUrlJoin_NullLocation() throws MalformedURLException {
        assertEquals("http://example.com/path", UrlUtil.urlJoin(new URL("http://example.com/path"), null));
    }

    @Test
    /**
     * testUrlJoin_AbsoluteLocation方法。
     */
    public void testUrlJoin_AbsoluteLocation() throws MalformedURLException {
        URL url = new URL("http://example.com/path");
        assertEquals("http://other.com/new", UrlUtil.urlJoin(url, "http://other.com/new"));
    }

    @Test
    /**
     * testUrlJoin_RelativeLocation方法。
     */
    public void testUrlJoin_RelativeLocation() throws MalformedURLException {
        URL url = new URL("http://example.com/path");
        String result = UrlUtil.urlJoin(url, "/new");
        assertEquals("http://example.com/new", result);
    }

    @Test
    /**
     * testUrlJoin_RelativeLocationWithSlash方法。
     */
    public void testUrlJoin_RelativeLocationWithSlash() throws MalformedURLException {
        URL url = new URL("http://example.com/moved_perm");
        String result = UrlUtil.urlJoin(url, "/");
        assertEquals("http://example.com/", result);
    }

    @Test
    /**
     * testUrlJoin_RelativeLocationWithPath方法。
     */
    public void testUrlJoin_RelativeLocationWithPath() throws MalformedURLException {
        URL url = new URL("http://example.com/path");
        String result = UrlUtil.urlJoin(url, "subpath");
        assertEquals("http://example.com/subpath", result);
    }

    // ========== getRequestParams ==========

    @Test
    /**
     * testGetRequestParams_Null方法。
     */
    public void testGetRequestParams_Null() {
        Map<String, String> result = UrlUtil.getRequestParams(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== parseQuery ==========

    @Test
    /**
     * testParseQuery_Null方法。
     */
    public void testParseQuery_Null() {
        Map<String, String> result = UrlUtil.parseQuery(null, '&', '=', null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    /**
     * testParseQuery_Empty方法。
     */
    public void testParseQuery_Empty() {
        Map<String, String> result = UrlUtil.parseQuery("", '&', '=', null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    /**
     * testParseQuery_Simple方法。
     */
    public void testParseQuery_Simple() {
        Map<String, String> result = UrlUtil.parseQuery("a=1&b=2", '&', '=', null);
        assertEquals("1", result.get("a"));
        assertEquals("2", result.get("b"));
    }

    @Test
    /**
     * testParseQuery_DuplicateParams方法。
     */
    public void testParseQuery_DuplicateParams() {
        Map<String, String> result = UrlUtil.parseQuery("a=1&a=2", '&', '=', null);
        assertEquals("2", result.get("a"));
    }

    @Test
    /**
     * testParseQuery_DuplicateParamsWithDupLink方法。
     */
    public void testParseQuery_DuplicateParamsWithDupLink() {
        Map<String, String> result = UrlUtil.parseQuery("a=1&a=2", '&', '=', ",");
        assertEquals("2,1", result.get("a"));
    }

    @Test
    /**
     * testParseQuery_NoSeparator方法。
     */
    public void testParseQuery_NoSeparator() {
        Map<String, String> result = UrlUtil.parseQuery("a", '&', '=', null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== httpParseQuery ==========

    @Test
    /**
     * testHttpParseQuery_Valid方法。
     */
    public void testHttpParseQuery_Valid() {
        Map<String, String> result = UrlUtil.httpParseQuery("name=test&value=123");
        assertEquals("test", result.get("name"));
        assertEquals("123", result.get("value"));
    }

    @Test
    /**
     * testHttpParseQuery_Null方法。
     */
    public void testHttpParseQuery_Null() {
        Map<String, String> result = UrlUtil.httpParseQuery(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    /**
     * testHttpParseQuery_Empty方法。
     */
    public void testHttpParseQuery_Empty() {
        Map<String, String> result = UrlUtil.httpParseQuery("");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== decodeQuery ==========

    @Test
    /**
     * testDecodeQuery_Null方法。
     */
    public void testDecodeQuery_Null() {
        assertNull(UrlUtil.decodeQuery(null));
    }

    @Test
    /**
     * testDecodeQuery_EncodedString方法。
     */
    public void testDecodeQuery_EncodedString() {
        assertEquals("hello world", UrlUtil.decodeQuery("hello%20world"));
    }

    @Test
    /**
     * testDecodeQuery_PlusAsSpace方法。
     */
    public void testDecodeQuery_PlusAsSpace() {
        assertEquals("hello world", UrlUtil.decodeQuery("hello+world"));
    }

    @Test
    /**
     * testDecodeQuery_Chinese方法。
     */
    public void testDecodeQuery_Chinese() {
        assertEquals("测试", UrlUtil.decodeQuery("%E6%B5%8B%E8%AF%95"));
    }

    @Test
    /**
     * testDecodeQuery_InvalidSequence方法。
     */
    public void testDecodeQuery_InvalidSequence() {
        // 无效序列不抛异常，返回原字符串
        String result = UrlUtil.decodeQuery("%ZZ");
        assertEquals("%ZZ", result);
    }

    @Test
    /**
     * testDecodeQuery_IncompleteSequence方法。
     */
    public void testDecodeQuery_IncompleteSequence() {
        // 不完整的序列不抛异常，返回原字符串
        String result = UrlUtil.decodeQuery("%ZZ");
        assertNotNull(result);
    }

    // ========== Builder ==========

    @Test
    /**
     * testBuilder_Basic方法。
     */
    public void testBuilder_Basic() {
        String result = new UrlUtil.Builder("/api/user").toString();
        assertEquals("/api/user", result);
    }

    @Test
    /**
     * testBuilder_WithQueryParams方法。
     */
    public void testBuilder_WithQueryParams() {
        String result = new UrlUtil.Builder("/api/user")
                .queryParam("name", "张三")
                .queryParam("age", "20")
                .toString();
        assertTrue(result.startsWith("/api/user"));
        assertTrue(result.contains("name="));
        assertTrue(result.contains("age="));
    }

    @Test
    /**
     * testBuilder_QueryParamWithNullName方法。
     */
    public void testBuilder_QueryParamWithNullName() {
        String result = new UrlUtil.Builder("/api/user")
                .queryParam(null, "value")
                .toString();
        assertEquals("/api/user", result);
    }

    @Test
    /**
     * testBuilder_ChainMultipleQueryParams方法。
     */
    public void testBuilder_ChainMultipleQueryParams() {
        String result = new UrlUtil.Builder("/search")
                .queryParam("q", "test")
                .queryParam("page", "1")
                .queryParam("size", "10")
                .toString();
        assertTrue(result.contains("q="));
        assertTrue(result.contains("page="));
        assertTrue(result.contains("size="));
    }

    @Test
    /**
     * testBuilder_PathSegment方法。
     */
    public void testBuilder_PathSegment() {
        String result = new UrlUtil.Builder("/api")
                .pathSegment("user")
                .pathSegment("123")
                .toString();
        assertEquals("/api/user/123", result);
    }

    @Test
    /**
     * testBuilder_QueryParamsMap方法。
     */
    public void testBuilder_QueryParamsMap() {
        Map<String, String> params = new java.util.HashMap<>();
        params.put("name", "test");
        params.put("value", "123");
        String result = new UrlUtil.Builder("/api/user")
                .queryParams(params)
                .toString();
        assertTrue(result.contains("name="));
        assertTrue(result.contains("value="));
    }

    @Test
    /**
     * testBuilder_Build方法。
     */
    public void testBuilder_Build() {
        String result = new UrlUtil.Builder("/api/user")
                .queryParam("name", "test")
                .build();
        assertTrue(result.contains("name="));
    }

    @Test
    /**
     * testBuilder_ToString方法。
     */
    public void testBuilder_ToString() {
        UrlUtil.Builder builder = new UrlUtil.Builder("/api/user");
        assertEquals(builder.toString(), builder.build());
    }
}
