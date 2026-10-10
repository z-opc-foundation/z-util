package com.zifang.util.http;

import com.zifang.util.http.net.bookdemo.EncoderTest;
import com.zifang.util.http.net.bookdemo.SafeBufferedReaderTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * 所有测试套件
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        // bookdemo 自带测试（curl/sse/server 测试已随 R1 三拆迁往 http-core / http-server 模块）
        EncoderTest.class,
        SafeBufferedReaderTest.class
})
/**
 * AllTests类。
 */
public class AllTests {
    // 测试套件入口
}
