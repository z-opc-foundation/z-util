package com.zifang.util.http.base.pojo;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * HttpRequestHeaderTest类。
 */
public class HttpRequestHeaderTest {

    @Test
    /**
     * testDefaultConstructor方法。
     */
    public void testDefaultConstructor() {
        HttpRequestHeader header = new HttpRequestHeader();
        assertTrue(header.isEmpty());
    }

    @Test
    /**
     * testPutAndGet方法。
     */
    public void testPutAndGet() {
        HttpRequestHeader header = new HttpRequestHeader();
        header.put("Content-Type", "application/json");

        assertEquals("application/json", header.get("Content-Type"));
    }

    @Test
    /**
     * testContainsKey方法。
     */
    public void testContainsKey() {
        HttpRequestHeader header = new HttpRequestHeader();
        header.put("Accept", "text/html");

        assertTrue(header.containsKey("Accept"));
        assertFalse(header.containsKey("X-Custom-Header"));
    }

    @Test
    /**
     * testRemove方法。
     */
    public void testRemove() {
        HttpRequestHeader header = new HttpRequestHeader();
        header.put("Accept", "text/html");
        header.remove("Accept");

        assertFalse(header.containsKey("Accept"));
    }

    @Test
    /**
     * testClear方法。
     */
    public void testClear() {
        HttpRequestHeader header = new HttpRequestHeader();
        header.put("Accept", "text/html");
        header.put("Content-Type", "application/json");
        header.clear();

        assertTrue(header.isEmpty());
    }

    @Test
    /**
     * testSize方法。
     */
    public void testSize() {
        HttpRequestHeader header = new HttpRequestHeader();
        header.put("Accept", "text/html");
        header.put("Content-Type", "application/json");

        assertEquals(2, header.size());
    }

    @Test
    /**
     * testKeySet方法。
     */
    public void testKeySet() {
        HttpRequestHeader header = new HttpRequestHeader();
        header.put("Accept", "text/html");
        header.put("Content-Type", "application/json");

        assertEquals(2, header.keySet().size());
        assertTrue(header.keySet().contains("Accept"));
        assertTrue(header.keySet().contains("Content-Type"));
    }

    @Test
    /**
     * testValues方法。
     */
    public void testValues() {
        HttpRequestHeader header = new HttpRequestHeader();
        header.put("Accept", "text/html");
        header.put("Content-Type", "application/json");

        assertEquals(2, header.values().size());
        assertTrue(header.values().contains("text/html"));
        assertTrue(header.values().contains("application/json"));
    }

    @Test
    /**
     * testEntrySet方法。
     */
    public void testEntrySet() {
        HttpRequestHeader header = new HttpRequestHeader();
        header.put("Accept", "text/html");

        assertEquals(1, header.entrySet().size());
    }

    @Test
    /**
     * testPutAll方法。
     */
    public void testPutAll() {
        HttpRequestHeader header1 = new HttpRequestHeader();
        header1.put("Accept", "text/html");

        HttpRequestHeader header2 = new HttpRequestHeader();
        header2.putAll(header1);

        assertEquals("text/html", header2.get("Accept"));
    }
}
