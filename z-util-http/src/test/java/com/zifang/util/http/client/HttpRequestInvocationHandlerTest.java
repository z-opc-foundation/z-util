package com.zifang.util.http.client;

import com.zifang.util.http.base.define.RestController;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * HttpRequestInvocationHandlerTest类。
 */
public class HttpRequestInvocationHandlerTest {

    @Test
    /**
     * testConstructorWithInterface方法。
     */
    public void testConstructorWithInterface() {
        HttpRequestInvocationHandler handler = new HttpRequestInvocationHandler(TestApi.class);
        assertEquals(TestApi.class, handler.getTarget());
    }

    @Test
    /**
     * testConstructorWithInterfaceAndContextParams方法。
     */
    public void testConstructorWithInterfaceAndContextParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("key", "value");

        HttpRequestInvocationHandler handler = new HttpRequestInvocationHandler(TestApi.class, params);
        assertEquals(TestApi.class, handler.getTarget());
        assertSame(params, handler.getContextParams());
    }

    @Test
    /**
     * testSetContextParams方法。
     */
    public void testSetContextParams() {
        HttpRequestInvocationHandler handler = new HttpRequestInvocationHandler(TestApi.class);
        Map<String, Object> params = new HashMap<>();
        params.put("key", "value");

        handler.setContextParams(params);
        assertSame(params, handler.getContextParams());
    }

    @Test
    /**
     * testGetContextParams方法。
     */
    public void testGetContextParams() {
        HttpRequestInvocationHandler handler = new HttpRequestInvocationHandler(TestApi.class);
        assertNull(handler.getContextParams());
    }

    @Test
    /**
     * testToString方法。
     */
    public void testToString() {
        HttpRequestInvocationHandler handler = new HttpRequestInvocationHandler(TestApi.class);
        String str = handler.toString();
        assertNotNull(str);
        assertTrue(str.contains("HttpRequestInvocationHandler"));
    }

    @Test
    /**
     * testEqualsWithSameContent方法。
     */
    public void testEqualsWithSameContent() {
        Map<String, Object> params1 = new HashMap<>();
        Map<String, Object> params2 = new HashMap<>();

        HttpRequestInvocationHandler handler1 = new HttpRequestInvocationHandler(TestApi.class, params1);
        HttpRequestInvocationHandler handler2 = new HttpRequestInvocationHandler(TestApi.class, params2);

        assertEquals(handler1, handler2);
    }

    @Test
    /**
     * testEqualsWithDifferentParams方法。
     */
    public void testEqualsWithDifferentParams() {
        Map<String, Object> params1 = new HashMap<>();
        params1.put("key1", "value1");

        Map<String, Object> params2 = new HashMap<>();
        params2.put("key2", "value2");

        HttpRequestInvocationHandler handler1 = new HttpRequestInvocationHandler(TestApi.class, params1);
        HttpRequestInvocationHandler handler2 = new HttpRequestInvocationHandler(TestApi.class, params2);

        assertNotEquals(handler1, handler2);
    }

    @Test
    /**
     * testEqualsWithSelf方法。
     */
    public void testEqualsWithSelf() {
        HttpRequestInvocationHandler handler = new HttpRequestInvocationHandler(TestApi.class);
        assertEquals(handler, handler);
    }

    @Test
    /**
     * testEqualsWithNull方法。
     */
    public void testEqualsWithNull() {
        HttpRequestInvocationHandler handler = new HttpRequestInvocationHandler(TestApi.class);
        assertNotEquals(null, handler);
    }

    @Test
    /**
     * testHashCode方法。
     */
    public void testHashCode() {
        Map<String, Object> params = new HashMap<>();
        HttpRequestInvocationHandler handler1 = new HttpRequestInvocationHandler(TestApi.class, params);
        HttpRequestInvocationHandler handler2 = new HttpRequestInvocationHandler(TestApi.class, params);

        assertEquals(handler1.hashCode(), handler2.hashCode());
    }

    @RestController("/test")
/**
 * TestApi接口。
 */
    public interface TestApi {
    }
}
