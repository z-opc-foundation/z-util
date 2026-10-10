package com.zifang.util.http.parser.curl;

import com.zifang.util.http.base.define.RequestMethod;
import com.zifang.util.http.base.pojo.HttpRequestDefinition;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * cURL 解析器测试类
 */

/**
 * CurlParserTest类。
 */
public class CurlParserTest {

    @Test
    /**
     * testSimpleGet方法。
     */
    public void testSimpleGet() {
        String curl = "curl https://api.example.com/users";

        HttpRequestDefinition definition = CurlParser.parse(curl);

        assertNotNull(definition);
        assertNotNull(definition.getHttpRequestLine());
        assertEquals("https://api.example.com/users", definition.getHttpRequestLine().getUrl());
        assertEquals(RequestMethod.GET, definition.getHttpRequestLine().getRequestMethod());
    }

    @Test
    /**
     * testGetWithHeaders方法。
     */
    public void testGetWithHeaders() {
        String curl = "curl -H 'Accept: application/json' -H 'Authorization: Bearer token123' https://api.example.com/users";

        HttpRequestDefinition definition = CurlParser.parse(curl);

        assertNotNull(definition);
        assertNotNull(definition.getHttpRequestHeader());
        assertEquals("application/json", definition.getHttpRequestHeader().get("Accept"));
        assertEquals("Bearer token123", definition.getHttpRequestHeader().get("Authorization"));
    }

    @Test
    /**
     * testPostWithJson方法。
     */
    public void testPostWithJson() {
        String curl = "curl -X POST -H 'Content-Type: application/json' -d '{\"name\":\"张三\",\"age\":25}' https://api.example.com/users";

        HttpRequestDefinition definition = CurlParser.parse(curl);

        assertNotNull(definition);
        assertEquals(RequestMethod.POST, definition.getHttpRequestLine().getRequestMethod());
        assertEquals("application/json", definition.getHttpRequestHeader().get("Content-Type"));
        assertNotNull(definition.getHttpRequestBody());
        assertNotNull(definition.getHttpRequestBody().getBody());
    }

    @Test
    /**
     * testBasicAuth方法。
     */
    public void testBasicAuth() {
        String curl = "curl -u username:password https://api.example.com/protected";

        HttpRequestDefinition definition = CurlParser.parse(curl);

        assertNotNull(definition);
        assertNotNull(definition.getHttpRequestHeader().get("Authorization"));
        assertTrue(definition.getHttpRequestHeader().get("Authorization").startsWith("Basic "));
    }

    @Test
    /**
     * testCookies方法。
     */
    public void testCookies() {
        String curl = "curl -b 'session=abc123; user=john' https://api.example.com/users";

        HttpRequestDefinition definition = CurlParser.parse(curl);

        assertNotNull(definition);
        assertEquals("session=abc123; user=john", definition.getHttpRequestHeader().get("Cookie"));
    }

    @Test
    /**
     * testPutRequest方法。
     */
    public void testPutRequest() {
        String curl = "curl -X PUT -d 'name=updated' https://api.example.com/users/123";

        HttpRequestDefinition definition = CurlParser.parse(curl);

        assertEquals(RequestMethod.PUT, definition.getHttpRequestLine().getRequestMethod());
        assertEquals("https://api.example.com/users/123", definition.getHttpRequestLine().getUrl());
    }

    @Test
    /**
     * testDeleteRequest方法。
     */
    public void testDeleteRequest() {
        String curl = "curl -X DELETE https://api.example.com/users/123";

        HttpRequestDefinition definition = CurlParser.parse(curl);

        assertEquals(RequestMethod.DELETE, definition.getHttpRequestLine().getRequestMethod());
    }

    @Test
    /**
     * testQuotedUrl方法。
     */
    public void testQuotedUrl() {
        String curl = "curl 'https://api.example.com/users?name=John&age=25'";

        HttpRequestDefinition definition = CurlParser.parse(curl);

        assertEquals("https://api.example.com/users?name=John&age=25", definition.getHttpRequestLine().getUrl());
    }

    @Test
    /**
     * testFormData方法。
     */
    public void testFormData() {
        String curl = "curl -X POST -d 'name=John&age=25' https://api.example.com/users";

        HttpRequestDefinition definition = CurlParser.parse(curl);

        assertEquals(RequestMethod.POST, definition.getHttpRequestLine().getRequestMethod());
        assertNotNull(definition.getHttpRequestBody());
        assertEquals("application/x-www-form-urlencoded", definition.getHttpRequestHeader().get("Content-Type"));
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testEmptyCommand方法。
     */
    public void testEmptyCommand() {
        CurlParser.parse("");
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testNullCommand方法。
     */
    public void testNullCommand() {
        CurlParser.parse(null);
    }

    @Test
    /**
     * testRoundTrip方法。
     */
    public void testRoundTrip() {
        String originalCurl = "curl -X POST -H 'Content-Type: application/json' -H 'Authorization: Bearer token' -d '{\"name\":\"test\"}' https://api.example.com/users";

        // 解析
        HttpRequestDefinition definition = CurlParser.parse(originalCurl);

        // 重新构建
        String rebuiltCurl = CurlBuilder.build(definition);

        // 验证重建后的命令可以正确解析
        HttpRequestDefinition reparsed = CurlParser.parse(rebuiltCurl);

        assertEquals(definition.getHttpRequestLine().getRequestMethod(), reparsed.getHttpRequestLine().getRequestMethod());
        assertEquals(definition.getHttpRequestLine().getUrl(), reparsed.getHttpRequestLine().getUrl());
    }

}
