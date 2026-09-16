package com.zifang.util.source;

import com.zifang.util.source.compiler.CustomerCompileJavaFileManager;
import org.junit.Test;

import javax.tools.StandardLocation;
import java.net.URI;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * CustomerCompileJavaFileManager 测试
 */

/**
 * CustomerCompileJavaFileManagerTest类。
 */
public class CustomerCompileJavaFileManagerTest {

    @Test
    /**
     * testFromLocationClassPath方法。
     */
    public void testFromLocationClassPath() throws Exception {
        URI uri = CustomerCompileJavaFileManager.fromLocation(
                StandardLocation.CLASS_PATH,
                "com.example",
                "Test.class"
        );

        assertNotNull(uri);
        assertTrue(uri.toString().contains("CLASS_PATH"));
        assertTrue(uri.toString().contains("com.example"));
        assertTrue(uri.toString().contains("Test.class"));
    }

    @Test
    /**
     * testFromLocationSourcePath方法。
     */
    public void testFromLocationSourcePath() throws Exception {
        URI uri = CustomerCompileJavaFileManager.fromLocation(
                StandardLocation.SOURCE_PATH,
                "com.example",
                "Test.java"
        );

        assertNotNull(uri);
        assertTrue(uri.toString().contains("SOURCE_PATH"));
    }
}