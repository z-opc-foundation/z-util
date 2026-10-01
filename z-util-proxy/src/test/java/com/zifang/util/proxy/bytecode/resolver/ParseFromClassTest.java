package com.zifang.util.proxy.bytecode.resolver;

import com.zifang.util.proxy.bytecode.model.ClassFile;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * parseFromClass 桥测试：从 JVM 已加载的类直接拿到其 class 结构。
 */
public class ParseFromClassTest {

    private InputStream fixtureResourceStream() {
        return getClass().getResourceAsStream("/testclass/DecompileFixture.class");
    }

    @Test
    public void parseLiveTestClassMatchesResourceParsing() throws Exception {
        ClassFile fromLive = ByteCodeResolver.parseFromClass(testclass.DecompileFixture.class);
        ClassFile fromResource;
        try (InputStream is = fixtureResourceStream()) {
            fromResource = ByteCodeResolver.parseFromStream(is);
        }

        assertEquals(fromResource.getClassName(), fromLive.getClassName());
        assertEquals(fromResource.getMethodCount(), fromLive.getMethodCount());
        assertEquals(fromResource.getConstantPoolSize().value, fromLive.getConstantPoolSize().value);
        assertEquals(fromResource.getMagic().value, fromLive.getMagic().value);
    }

    @Test
    public void parseBootstrapClassViaJrt() {
        // bootstrap 类（ClassLoader 为 null）经 jrt 一样能拿到字节码
        ClassFile stringFile = ByteCodeResolver.parseFromClass(String.class);
        assertEquals("java/lang/String", stringFile.getClassName());
        assertTrue("String 应有大量方法", stringFile.getMethodCount() > 50);
    }

    @Test
    public void primitiveRejected() {
        try {
            ByteCodeResolver.parseFromClass(int.class);
            throw new AssertionError("应拒绝基本类型");
        } catch (RuntimeException expected) {
            assertNotNull(expected.getMessage());
        }
    }
}
