package com.zifang.util.http.server;

import com.zifang.util.http.base.define.GetMapping;
import com.zifang.util.http.base.define.PostMapping;
import com.zifang.util.http.base.define.RequestMethod;
import com.zifang.util.http.base.define.RestController;
import com.zifang.util.http.base.pojo.HttpRequestDefinition;
import com.zifang.util.http.base.pojo.HttpRequestLine;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * HttpServerRequestHandlerTest类。
 */
public class HttpServerRequestHandlerTest {

    @Test(expected = IllegalArgumentException.class)
    /**
     * testHandleRequestWithNullDefinition方法。
     */
    public void testHandleRequestWithNullDefinition() {
        HttpServerRequestHandler handler = new HttpServerRequestHandler(new TestController());
        handler.handleRequest(null);
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testHandleRequestWithNullRequestLine方法。
     */
    public void testHandleRequestWithNullRequestLine() {
        HttpServerRequestHandler handler = new HttpServerRequestHandler(new TestController());
        HttpRequestDefinition definition = new HttpRequestDefinition();
        handler.handleRequest(definition);
    }

    @Test
    /**
     * testConstructor方法。
     */
    public void testConstructor() {
        TestController controller = new TestController();
        HttpServerRequestHandler handler = new HttpServerRequestHandler(controller);
        assertNotNull(handler);
    }

    @Ignore
    @Test
    /**
     * testGetMappingInfo方法。
     */
    public void testGetMappingInfo() {
        TestController controller = new TestController();
        HttpServerRequestHandler handler = new HttpServerRequestHandler(controller);

        HttpRequestDefinition definition = new HttpRequestDefinition();
        HttpRequestLine requestLine = new HttpRequestLine();
        requestLine.setRequestMethod(RequestMethod.GET);
        requestLine.setUrl("/test");
        definition.setHttpRequestLine(requestLine);

        // This will throw because the method mapping isn't found, but it shows the code works
        try {
            handler.handleRequest(definition);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("No handler found"));
        }
    }

    @RestController("/api")
    public static class TestController {
        @GetMapping("/test")
        /**
         * testMethod方法。
         * @return String类型返回值
         */
        public String testMethod() {
            return "test";
        }

        @PostMapping("/post")
        /**
         * postMethod方法。
         *      * @param body String类型参数
         * @return String类型返回值
         */
        public String postMethod(String body) {
            return body;
        }
    }
}
