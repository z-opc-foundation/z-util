package com.zifang.util.proxy.bytecode.decompile.core;

import com.zifang.util.proxy.bytecode.model.ClassFile;
import com.zifang.util.proxy.bytecode.resolver.ByteCodeResolver;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 解析 → 源码生成 全链路集成测试。
 * <p>
 * 夹具 {@link testclass.DecompileFixture} 覆盖对象数组 / 基本类型数组 /
 * 泛型字段 / 静态块 / 构造方法，验证反编译输出的关键结构。
 */
public class SrcCreatorFixtureTest {

    private static String decompileFixture() {
        InputStream is = SrcCreatorFixtureTest.class.getResourceAsStream("/testclass/DecompileFixture.class");
        assertNotNull("夹具字节码资源未找到：/testclass/DecompileFixture.class", is);
        ClassFile classFile = ByteCodeResolver.parseFromStream(is);
        return SrcCreator.createJavaFileSrc(classFile);
    }

    @Test
    public void packageAndClassDeclaration() {
        String src = decompileFixture();
        assertTrue(src.startsWith("package testclass;"));
        assertTrue(src.contains("public class DecompileFixture"));
    }

    @Test
    public void scalarFieldDeclarations() {
        String src = decompileFixture();
        assertTrue("应包含 String 字段: " + src, src.contains("public String name;"));
        assertTrue("应包含 long 字段: " + src, src.contains("public long big;"));
        assertTrue("应包含 double 字段: " + src, src.contains("public double dbl;"));
        assertTrue("应包含 boolean 字段: " + src, src.contains("public boolean flag;"));
        assertTrue("应包含泛型 List<String> 字段: " + src, src.contains("public List<String> list;"));
    }

    @Test
    public void arrayFieldDeclarationsKeepDimensions() {
        String src = decompileFixture();
        assertTrue("对象数组应为 String[]: " + src, src.contains("public String[] names;"));
        assertTrue("基本类型数组应为 int[]: " + src, src.contains("public int[] nums;"));
    }

    @Test
    public void staticInitializerBlock() {
        String src = decompileFixture();
        assertTrue(src.contains("static {"));
        assertTrue("静态块应包含 counter = 7;: " + src, src.contains("counter = 7;"));
    }

    @Test
    public void constructorWithNameAssignment() {
        String src = decompileFixture();
        assertTrue(src.contains("DecompileFixture()"));
        assertTrue("构造方法应有字段赋值: " + src, src.contains("this.name = \"abc\";"));
    }

    @Test
    public void methodSignatureWithParams() {
        String src = decompileFixture();
        assertTrue("方法签名应为 int add(int, int): " + src, src.contains("int add(int, int)"));
    }
}
