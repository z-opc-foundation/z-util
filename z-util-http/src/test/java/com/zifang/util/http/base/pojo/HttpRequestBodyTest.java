package com.zifang.util.http.base.pojo;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * HttpRequestBodyTest类。
 */
public class HttpRequestBodyTest {

    @Test
    /**
     * testDefaultConstructor方法。
     */
    public void testDefaultConstructor() {
        HttpRequestBody httpRequestBody = new HttpRequestBody();
        assertNull(httpRequestBody.getBody());
    }

    @Test
    /**
     * testSettersAndGetters方法。
     */
    public void testSettersAndGetters() {
        HttpRequestBody httpRequestBody = new HttpRequestBody();
        byte[] body = "test content".getBytes();
        httpRequestBody.setBody(body);

        assertArrayEquals(body, httpRequestBody.getBody());
    }

    @Test
    /**
     * testToString方法。
     */
    public void testToString() {
        HttpRequestBody httpRequestBody = new HttpRequestBody();
        byte[] body = "test".getBytes();
        httpRequestBody.setBody(body);

        String str = httpRequestBody.toString();
        assertTrue(str.contains("byte[4]"));
    }

    @Test
    /**
     * testToStringWithNullBody方法。
     */
    public void testToStringWithNullBody() {
        HttpRequestBody httpRequestBody = new HttpRequestBody();
        String str = httpRequestBody.toString();
        assertTrue(str.contains("null"));
    }

    @Test
    /**
     * testEqualsWithSameBody方法。
     */
    public void testEqualsWithSameBody() {
        HttpRequestBody body1 = new HttpRequestBody();
        body1.setBody("test".getBytes());

        HttpRequestBody body2 = new HttpRequestBody();
        body2.setBody("test".getBytes());

        assertEquals(body1, body2);
    }

    @Test
    /**
     * testEqualsWithDifferentBody方法。
     */
    public void testEqualsWithDifferentBody() {
        HttpRequestBody body1 = new HttpRequestBody();
        body1.setBody("test1".getBytes());

        HttpRequestBody body2 = new HttpRequestBody();
        body2.setBody("test2".getBytes());

        assertNotEquals(body1, body2);
    }

    @Test
    /**
     * testEqualsWithNullBodies方法。
     */
    public void testEqualsWithNullBodies() {
        HttpRequestBody body1 = new HttpRequestBody();
        HttpRequestBody body2 = new HttpRequestBody();

        assertEquals(body1, body2);
    }

    @Test
    /**
     * testEqualsWithOneNullBody方法。
     */
    public void testEqualsWithOneNullBody() {
        HttpRequestBody body1 = new HttpRequestBody();
        body1.setBody("test".getBytes());

        HttpRequestBody body2 = new HttpRequestBody();

        assertNotEquals(body1, body2);
    }

    @Test
    /**
     * testHashCode方法。
     */
    public void testHashCode() {
        HttpRequestBody body1 = new HttpRequestBody();
        body1.setBody("test".getBytes());

        HttpRequestBody body2 = new HttpRequestBody();
        body2.setBody("test".getBytes());

        assertEquals(body1.hashCode(), body2.hashCode());
    }

    @Test
    /**
     * testHashCodeWithNullBody方法。
     */
    public void testHashCodeWithNullBody() {
        HttpRequestBody body = new HttpRequestBody();
        int hash = body.hashCode();
        assertEquals(1, hash);
    }
}
