package com.zifang.util.proxy.bytecode.decompile.core;

import com.zifang.util.proxy.bytecode.model.ClassFile;
import com.zifang.util.proxy.bytecode.resolver.ByteCodeResolver;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * try/catch 反编译：exception_table 整段覆盖 0..codeLength 时套上 try { ... }
 * catch (Type e) { ... } 框架。catch 体放注释占位，未展开字节码 handler。
 */
public class SrcCreatorControlFlowTest {

    private static String decompile() {
        InputStream is = SrcCreatorControlFlowTest.class
                .getResourceAsStream("/testclass/ControlFlowFixture.class");
        assertNotNull("夹具字节码资源未找到", is);
        ClassFile classFile = ByteCodeResolver.parseFromStream(is);
        return SrcCreator.createJavaFileSrc(classFile);
    }

    @Test
    public void singleTryCatchEmitted() {
        String src = decompile();
        assertTrue("单 try 应被识别: " + src, src.contains("try {"));
        assertTrue("NumberFormatException 应作为 catch 类型: " + src,
                src.contains("catch (java.lang.NumberFormatException"));
        assertTrue("safeParse 方法应保留签名: " + src, src.contains("int safeParse(") && src.contains("String"));
    }

    @Test
    public void multipleCatchesEmitted() {
        String src = decompile();
        assertTrue("NPE catch: " + src, src.contains("catch (java.lang.NullPointerException"));
        assertTrue("RuntimeException catch: " + src, src.contains("catch (java.lang.RuntimeException"));
    }

    @Test
    public void finallyEmittedAsThrowableCatch() {
        String src = decompile();
        assertTrue("finally 经 catch_type=0 渲染成 Throwable: " + src,
                src.contains("catch (java.lang.Throwable"));
    }
}