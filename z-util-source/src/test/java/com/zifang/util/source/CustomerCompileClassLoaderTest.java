package com.zifang.util.source;

import com.zifang.util.source.compiler.CustomerCompileClassLoader;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * CustomerCompileClassLoader 测试
 */

/**
 * CustomerCompileClassLoaderTest类。
 */
public class CustomerCompileClassLoaderTest {

    @Test
    /**
     * testClassExtension方法。
     */
    public void testClassExtension() {
        // 验证类文件扩展名
        assertEquals(".class", CustomerCompileClassLoader.CLASS_EXTENSION);
    }

    @Test
    /**
     * testConstructor方法。
     */
    public void testConstructor() {
        CustomerCompileClassLoader classLoader = new CustomerCompileClassLoader(Thread.currentThread().getContextClassLoader());
        assertNotNull(classLoader);
    }

    @Test
    /**
     * testGetResourceAsStream方法。
     */
    public void testGetResourceAsStream() {
        CustomerCompileClassLoader classLoader = new CustomerCompileClassLoader(Thread.currentThread().getContextClassLoader());

        // 尝试获取一个系统类作为测试
        InputStream is = classLoader.getResourceAsStream("java/lang/String.class");
        assertNotNull(is);
    }
}