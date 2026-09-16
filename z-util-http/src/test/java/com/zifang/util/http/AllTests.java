package com.zifang.util.http;

import com.zifang.util.http.parser.curl.CurlParserTest;
import com.zifang.util.http.server.HttpServerProxyTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * 所有测试套件
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        // Parser 测试
        CurlParserTest.class,

        // Server 测试
        HttpServerProxyTest.class
})
/**
 * AllTests类。
 */
public class AllTests {
    // 测试套件入口
}
