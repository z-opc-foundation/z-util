package com.zifang.util.http.server;

import com.zifang.util.http.base.define.RestController;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * HttpServerInvocationHandlerTest类。
 */
public class HttpServerInvocationHandlerTest {

    @Test
    /**
     * testConstructorWithInterface方法。
     */
    public void testConstructorWithInterface() {
        HttpServerInvocationHandler handler = new HttpServerInvocationHandler(TestApi.class);
        assertEquals(TestApi.class, getTargetFieldValue(handler));
    }

    @Test
    /**
     * testConstructorWithInterfaceAndContextParams方法。
     */
    public void testConstructorWithInterfaceAndContextParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("key", "value");

        HttpServerInvocationHandler handler = new HttpServerInvocationHandler(TestApi.class, params);
        assertEquals(TestApi.class, getTargetFieldValue(handler));
        assertSame(params, getContextParamsFieldValue(handler));
    }

    private Object getTargetFieldValue(HttpServerInvocationHandler handler) {
        try {
            Field field = HttpServerInvocationHandler.class.getDeclaredField("target");
            field.setAccessible(true);
            return field.get(handler);
        } catch (Exception e) {
            return null;
        }
    }

    private Object getContextParamsFieldValue(HttpServerInvocationHandler handler) {
        try {
            Field field = HttpServerInvocationHandler.class.getDeclaredField("contextParams");
            field.setAccessible(true);
            return field.get(handler);
        } catch (Exception e) {
            return null;
        }
    }

    @RestController("/test")
/**
 * TestApi接口。
 */
    public interface TestApi {
    }
}
