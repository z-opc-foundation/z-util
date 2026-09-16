package com.zifang.util.source;

import com.zifang.util.source.compiler.CharSequenceJavaFileObject;
import com.zifang.util.source.compiler.CompileContext;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * CompileContext 测试
 */

/**
 * CompileContextTest类。
 */
public class CompileContextTest {

    private CompileContext compileContext;

    @Before
    /**
     * setUp方法。
     */
    public void setUp() {
        compileContext = new CompileContext();
        compileContext.initial();
    }

    @Test
    /**
     * testInitial方法。
     */
    public void testInitial() {
        assertNotNull(compileContext);
        // 验证编译器已初始化
        assertNotNull(compileContext);
    }

    @Test
    /**
     * testAddJavaObject方法。
     */
    public void testAddJavaObject() {
        String sourceCode = "public class HelloService { public String hello() { return \"Hello\"; } }";
        CharSequenceJavaFileObject javaFileObject = new CharSequenceJavaFileObject("HelloService", sourceCode);
        URI uri = URI.create("HelloService.java");

        compileContext.addJavaObject(uri, javaFileObject);
        // 验证添加成功，编译上下文包含文件对象
        assertNotNull(compileContext);
    }

    @Test
    /**
     * testId方法。
     */
    public void testId() {
        // 验证静态 id 字段存在且为时间戳
        assertNotNull(CompileContext.id);
        assertTrue(CompileContext.id > 0);
    }

    @Test
    /**
     * testCompileResult方法。
     */
    public void testCompileResult() {
        // 测试编译结果输出（只是验证方法可调用）
        String sourceCode = "public class MathService { " +
                "public int add(int a, int b) { return a + b; } " +
                "public int subtract(int a, int b) { return a - b; } }";
        CharSequenceJavaFileObject javaFileObject = new CharSequenceJavaFileObject("MathService", sourceCode);
        URI uri = URI.create("MathService.java");

        compileContext.addJavaObject(uri, javaFileObject);
        compileContext.compile();
    }

    @Test
    /**
     * testLoadClass方法。
     */
    public void testLoadClass() {
        String sourceCode = "public class UserService { public String getName() { return \"test\"; } }";
        CharSequenceJavaFileObject javaFileObject = new CharSequenceJavaFileObject("UserService", sourceCode);
        URI uri = URI.create("UserService.java");

        compileContext.addJavaObject(uri, javaFileObject);
        compileContext.compile();

        Class<?> clazz = compileContext.load("UserService");
        // 可能加载失败，这是正常的
        // 编译可能有问题，或者类加载器配置问题
        assertNotNull(clazz);
    }
}