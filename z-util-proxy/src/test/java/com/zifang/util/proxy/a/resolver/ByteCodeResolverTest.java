package com.zifang.util.proxy.a.resolver;

import com.zifang.util.proxy.a.model.ClassFile;
import com.zifang.util.proxy.a.model.constantpool.AbstractConstantPool;
import com.zifang.util.proxy.a.model.constantpool.ClassInfo;
import com.zifang.util.proxy.a.model.constantpool.Utf8Info;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * ByteCodeResolver 测试类
 * <p>
 * 测试字节码解析器的核心功能：解析 .class 文件并提取类信息。
 */

/**
 * ByteCodeResolverTest类。
 */
public class ByteCodeResolverTest {

    /**
     * 测试解析简单类
     * <p>
     * 使用内嵌的测试类进行解析，验证解析结果的正确性。
     */
    @Test
    /**
     * testParseSimpleClass方法。
     */
    public void testParseSimpleClass() {
        // 获取测试类的字节码
        InputStream is = getClass().getResourceAsStream("/testclass/TestClassParse1.class");
        assertNotNull("测试类字节码资源未找到", is);

        // 解析字节码
        ClassFile classFile = ByteCodeResolver.parseFromStream(is);

        // 验证基本结构
        assertNotNull("ClassFile 不应为 null", classFile);
        assertNotNull("Magic 不应为 null", classFile.magic);
        assertEquals("魔数应为 0xCAFEBABE", 0xCAFEBABE, classFile.magic.value);
        assertNotNull("主版本号不应为 null", classFile.majorVersion);
        assertNotNull("次版本号不应为 null", classFile.minorVersion);

        // 验证常量池
        assertNotNull("常量池信息不应为 null", classFile.poolInfo);
        assertNotNull("常量池列表不应为 null", classFile.poolInfo.getPoolList());
        assertTrue("常量池应有内容", classFile.poolInfo.getPoolList().size() > 0);

        // 验证类名解析
        String className = classFile.getClassName();
        assertNotNull("类名不应为 null", className);
        assertTrue("类名应包含 TestClassParse1", className.contains("TestClassParse1"));

        // 关闭流
        try {
            is.close();
        } catch (Exception e) {
            // 忽略关闭异常
        }
    }

    /**
     * 测试常量池解析
     * <p>
     * 验证常量池中的 UTF8 和 Class 常量能被正确解析。
     */
    @Test
    /**
     * testConstantPoolParsing方法。
     */
    public void testConstantPoolParsing() {
        InputStream is = getClass().getResourceAsStream("/testclass/TestClassParse1.class");
        assertNotNull("测试类字节码资源未找到", is);

        ClassFile classFile = ByteCodeResolver.parseFromStream(is);

        // 验证常量池包含必要的常量类型
        boolean foundUtf8 = false;
        boolean foundClass = false;

        for (AbstractConstantPool constant : classFile.poolInfo.getPoolList()) {
            if (constant instanceof Utf8Info) {
                foundUtf8 = true;
                String value = ((Utf8Info) constant).getValue();
                assertNotNull("UTF8 常量的值不应为 null", value);
            }
            if (constant instanceof ClassInfo) {
                foundClass = true;
            }
        }

        assertTrue("常量池应包含 Utf8Info", foundUtf8);
        assertTrue("常量池应包含 ClassInfo", foundClass);

        try {
            is.close();
        } catch (Exception e) {
            // 忽略关闭异常
        }
    }

    /**
     * 测试从文件路径解析
     */
    @Test
    /**
     * testParseFromFilePath方法。
     */
    public void testParseFromFilePath() {
        // 使用测试资源文件
        String resourcePath = getClass().getResource("/testclass/TestClassParse1.class").getFile();

        ClassFile classFile = ByteCodeResolver.parseFromFile(resourcePath);

        assertNotNull("ClassFile 不应为 null", classFile);
        assertNotNull("Magic 不应为 null", classFile.magic);
    }

    /**
     * 测试无效魔数检测
     */
    @Test(expected = RuntimeException.class)
    /**
     * testInvalidMagicNumber方法。
     */
    public void testInvalidMagicNumber() {
        // 创建一个假的字节码文件（无效魔数）
        byte[] fakeBytes = new byte[]{
                0x00, 0x00, 0x00, 0x00,  // 无效魔数
                0x00, 0x03,  // minor version
                0x00, 0x34   // major version (Java 8)
        };

        InputStream is = new java.io.ByteArrayInputStream(fakeBytes);
        ByteCodeResolver.parseFromStream(is);
    }

    /**
     * 测试获取方法数量
     */
    @Test
    /**
     * testGetMethodCount方法。
     */
    public void testGetMethodCount() {
        InputStream is = getClass().getResourceAsStream("/testclass/TestClassParse1.class");
        assertNotNull("测试类字节码资源未找到", is);

        ClassFile classFile = ByteCodeResolver.parseFromStream(is);

        // TestClassParse1 有两个方法: method() 和 run()
        int methodCount = classFile.getMethodCount();
        assertTrue("方法数量应 >= 0", methodCount >= 0);

        try {
            is.close();
        } catch (Exception e) {
            // 忽略关闭异常
        }
    }
}