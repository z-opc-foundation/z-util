package com.zifang.util.http.server;

import com.zifang.util.http.base.define.RestController;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * HttpServerProxyTest类。
 */
public class HttpServerProxyTest {

    @Test
    /**
     * testProxyReturnsNonNull方法。
     */
    public void testProxyReturnsNonNull() {
        TestApi proxy = HttpServerProxy.proxy(TestApi.class);
        assertNotNull(proxy);
    }

    @Test
    /**
     * testProxyReturnsCorrectType方法。
     */
    public void testProxyReturnsCorrectType() {
        TestApi proxy = HttpServerProxy.proxy(TestApi.class);
        assertTrue(proxy instanceof TestApi);
    }

    @RestController("/test")
/**
 * TestApi接口。
 */
    public interface TestApi {
    }
}
