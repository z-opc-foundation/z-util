package com.zifang.util.http.base.pojo;

import com.zifang.util.http.base.define.RequestMethod;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * HttpRequestDefinitionTest类。
 */
public class HttpRequestDefinitionTest {

    @Test
    /**
     * testDefaultConstructor方法。
     */
    public void testDefaultConstructor() {
        HttpRequestDefinition definition = new HttpRequestDefinition();
        assertNull(definition.getHttpRequestLine());
        assertNull(definition.getHttpRequestHeader());
        assertNull(definition.getHttpRequestBody());
    }

    @Test
    /**
     * testSettersAndGetters方法。
     */
    public void testSettersAndGetters() {
        HttpRequestDefinition definition = new HttpRequestDefinition();

        HttpRequestLine requestLine = new HttpRequestLine();
        requestLine.setUrl("http://example.com");
        requestLine.setRequestMethod(RequestMethod.GET);

        HttpRequestHeader requestHeader = new HttpRequestHeader();
        requestHeader.put("Accept", "application/json");

        HttpRequestBody requestBody = new HttpRequestBody();
        requestBody.setBody("test".getBytes());

        definition.setHttpRequestLine(requestLine);
        definition.setHttpRequestHeader(requestHeader);
        definition.setHttpRequestBody(requestBody);

        assertSame(requestLine, definition.getHttpRequestLine());
        assertSame(requestHeader, definition.getHttpRequestHeader());
        assertSame(requestBody, definition.getHttpRequestBody());
    }

    @Test
    /**
     * testToString方法。
     */
    public void testToString() {
        HttpRequestDefinition definition = new HttpRequestDefinition();
        HttpRequestLine requestLine = new HttpRequestLine();
        requestLine.setUrl("http://example.com");
        definition.setHttpRequestLine(requestLine);

        String str = definition.toString();
        assertNotNull(str);
        assertTrue(str.contains("HttpRequestDefinition"));
    }

    @Test
    /**
     * testEqualsWithSameContent方法。
     */
    public void testEqualsWithSameContent() {
        HttpRequestDefinition def1 = new HttpRequestDefinition();
        HttpRequestDefinition def2 = new HttpRequestDefinition();

        HttpRequestLine requestLine = new HttpRequestLine();
        requestLine.setUrl("http://example.com");
        requestLine.setRequestMethod(RequestMethod.GET);

        def1.setHttpRequestLine(requestLine);
        def2.setHttpRequestLine(requestLine);

        assertEquals(def1, def2);
    }

    @Test
    /**
     * testEqualsWithDifferentContent方法。
     */
    public void testEqualsWithDifferentContent() {
        HttpRequestDefinition def1 = new HttpRequestDefinition();
        HttpRequestLine line1 = new HttpRequestLine();
        line1.setUrl("http://example.com");
        def1.setHttpRequestLine(line1);

        HttpRequestDefinition def2 = new HttpRequestDefinition();
        HttpRequestLine line2 = new HttpRequestLine();
        line2.setUrl("http://other.com");
        def2.setHttpRequestLine(line2);

        assertNotEquals(def1, def2);
    }

    @Test
    /**
     * testEqualsWithNull方法。
     */
    public void testEqualsWithNull() {
        HttpRequestDefinition def1 = new HttpRequestDefinition();
        assertNotEquals(null, def1);
    }

    @Test
    /**
     * testHashCode方法。
     */
    public void testHashCode() {
        HttpRequestDefinition def1 = new HttpRequestDefinition();
        HttpRequestDefinition def2 = new HttpRequestDefinition();

        assertEquals(def1.hashCode(), def2.hashCode());
    }
}
