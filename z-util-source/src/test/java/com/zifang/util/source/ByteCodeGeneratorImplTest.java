package com.zifang.util.source;

import com.zifang.util.source.generator.ByteCodeGeneratorImpl;
import com.zifang.util.source.generator.info.ClassInfo;
import org.junit.Test;

/**
 * ByteCodeGeneratorImpl 测试
 */

/**
 * ByteCodeGeneratorImplTest类。
 */
public class ByteCodeGeneratorImplTest {

    @Test(expected = IllegalArgumentException.class)
    /**
     * testGenerateNullClassInfo方法。
     */
    public void testGenerateNullClassInfo() {
        ByteCodeGeneratorImpl generator = new ByteCodeGeneratorImpl();
        generator.generate(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    /**
     * testGenerateThrowsUnsupported方法。
     */
    public void testGenerateThrowsUnsupported() {
        ByteCodeGeneratorImpl generator = new ByteCodeGeneratorImpl();
        ClassInfo classInfo = new ClassInfo();
        classInfo.setSimpleClassName("Test");
        generator.generate(classInfo);
    }
}