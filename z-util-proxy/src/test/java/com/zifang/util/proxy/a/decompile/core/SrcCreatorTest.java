package com.zifang.util.proxy.a.decompile.core;

import com.zifang.util.proxy.a.model.ClassFile;
import com.zifang.util.proxy.a.resolver.ByteCodeResolver;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * SrcCreator 反编译测试
 */

/**
 * SrcCreatorTest类。
 */
public class SrcCreatorTest {

    /**
     * 测试简单类的反编译
     */
    @Test
    /**
     * testSimpleClassDecompile方法。
     */
    public void testSimpleClassDecompile() {
        InputStream is = getClass().getResourceAsStream("/testclass/TestClassParse1.class");
        ClassFile classFile = ByteCodeResolver.parseFromStream(is);
        String src = SrcCreator.createJavaFileSrc(classFile);

        // 验证基本结构
        assertNotNull(src);
        assertTrue(src.contains("package testclass;"));
        assertTrue(src.contains("public class TestClassParse1"));

        // 验证字段
        assertTrue(src.contains("public static int i;"));
        assertTrue(src.contains("public int n;"));
        assertTrue(src.contains("public static final int m;"));

        // 验证构造方法中的字段赋值
        assertTrue(src.contains("this.n = 1;"));

        // 验证方法体
        assertTrue(src.contains("j = 1;"));
        assertTrue(src.contains("return;"));
    }

    /**
     * 测试方法体生成 - 局部变量赋值
     */
    @Test
    /**
     * testLocalVariableAssignment方法。
     */
    public void testLocalVariableAssignment() {
        InputStream is = getClass().getResourceAsStream("/testclass/TestClassParse1.class");
        ClassFile classFile = ByteCodeResolver.parseFromStream(is);
        String src = SrcCreator.createJavaFileSrc(classFile);

        // 验证 method() 方法中的 j = 1
        assertTrue(src.contains("j = 1;"));
    }

    /**
     * 测试方法体生成 - 字段赋值
     */
    @Test
    /**
     * testFieldAssignment方法。
     */
    public void testFieldAssignment() {
        InputStream is = getClass().getResourceAsStream("/testclass/TestClassParse1.class");
        ClassFile classFile = ByteCodeResolver.parseFromStream(is);
        String src = SrcCreator.createJavaFileSrc(classFile);

        // 验证构造方法中的 this.n = 1
        assertTrue(src.contains("this.n = 1;"));
    }

    /**
     * 测试方法体生成 - return 语句
     */
    @Test
    /**
     * testReturnStatement方法。
     */
    public void testReturnStatement() {
        InputStream is = getClass().getResourceAsStream("/testclass/TestClassParse1.class");
        ClassFile classFile = ByteCodeResolver.parseFromStream(is);
        String src = SrcCreator.createJavaFileSrc(classFile);

        // 验证所有方法都有 return
        assertNotNull(src);
        // method() 有 return
        assertTrue(src.contains("return;"));
    }

    /**
     * 测试方法体生成 - 空方法
     */
    @Test
    /**
     * testEmptyMethod方法。
     */
    public void testEmptyMethod() {
        InputStream is = getClass().getResourceAsStream("/testclass/TestClassParse1.class");
        ClassFile classFile = ByteCodeResolver.parseFromStream(is);
        String src = SrcCreator.createJavaFileSrc(classFile);

        // 验证 run() 方法存在（即使它是空的）
        assertTrue(src.contains("public void run()"));
    }

    /**
     * 测试方法体生成 - 静态初始化块
     */
    @Test
    /**
     * testStaticInitializer方法。
     */
    public void testStaticInitializer() {
        InputStream is = getClass().getResourceAsStream("/testclass/TestClassParse1.class");
        ClassFile classFile = ByteCodeResolver.parseFromStream(is);
        String src = SrcCreator.createJavaFileSrc(classFile);

        // 验证静态初始化块存在且包含 i = 0
        assertTrue(src.contains("static {"));
        assertTrue(src.contains("i = 0"));
    }

    /**
     * 测试字节码解析和反编译的集成
     */
    @Test
    /**
     * testBytecodeToSource方法。
     */
    public void testBytecodeToSource() {
        InputStream is = getClass().getResourceAsStream("/testclass/TestClassParse1.class");
        ClassFile classFile = ByteCodeResolver.parseFromStream(is);
        String src = SrcCreator.createJavaFileSrc(classFile);

        // 输出便于调试
        System.out.println("=== 生成的源码 ===");
        System.out.println(src);

        // 基本验证
        assertNotNull(src);
        assertTrue(src.length() > 0);
        assertTrue(src.contains("class TestClassParse1"));
    }
}