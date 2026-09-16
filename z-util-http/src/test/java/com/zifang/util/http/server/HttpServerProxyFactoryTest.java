package com.zifang.util.http.server;

import com.zifang.util.http.base.define.GetMapping;
import com.zifang.util.http.base.define.RestController;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * HttpServerProxyFactoryTest类。
 */
public class HttpServerProxyFactoryTest {

    @Test
    /**
     * testGetInterfaceClass方法。
     */
    public void testGetInterfaceClass() {
        HttpServerProxyFactory<TestApi> factory = new HttpServerProxyFactory<>(TestApi.class, new TestApiImpl());
        assertEquals(TestApi.class, factory.getInterfaceClass());
    }

    @Test
    /**
     * testGetTarget方法。
     */
    public void testGetTarget() {
        TestApiImpl target = new TestApiImpl();
        HttpServerProxyFactory<TestApi> factory = new HttpServerProxyFactory<>(TestApi.class, target);
        assertSame(target, factory.getTarget());
    }

    @RestController("/api")
/**
 * TestApi接口。
 */
    public interface TestApi {
        @GetMapping("/test")
        String test();
    }

    public static class TestApiImpl implements TestApi {
        @Override
        /**
         * test方法。
         * @return String类型返回值
         */
        public String test() {
            return "test";
        }
    }
}
