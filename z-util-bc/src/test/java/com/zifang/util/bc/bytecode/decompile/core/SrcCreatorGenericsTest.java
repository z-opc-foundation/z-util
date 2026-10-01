package com.zifang.util.bc.bytecode.decompile.core;

import com.zifang.util.bc.bytecode.model.ClassFile;
import com.zifang.util.bc.bytecode.model.attribute.AbstractAttribute;
import com.zifang.util.bc.bytecode.model.attribute.ExceptionsAttribute;
import com.zifang.util.bc.bytecode.model.method.MethodInfo;
import com.zifang.util.bc.bytecode.resolver.ByteCodeResolver;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Signature（字段/方法泛型签名）与 Exceptions（throws 列表）属性 → 源码形态 round-trip。
 */
public class SrcCreatorGenericsTest {

    private static String decompile() {
        InputStream is = SrcCreatorGenericsTest.class.getResourceAsStream("/testclass/GenericsFixture.class");
        assertNotNull("夹具字节码资源未找到", is);
        ClassFile classFile = ByteCodeResolver.parseFromStream(is);
        return SrcCreator.createJavaFileSrc(classFile);
    }

    @Test
    public void genericFieldMapRendersWithArguments() {
        String src = decompile();
        assertTrue("泛型 Map<String,Integer> 字段: " + src, src.contains("public Map<String, Integer> counters;"));
    }

    @Test
    public void genericListFieldRendersWithArguments() {
        String src = decompile();
        assertTrue("List<String> 字段: " + src, src.contains("public List<String> names;"));
    }

    @Test
    public void throwsClauseRenderedFromExceptionsAttribute() {
        InputStream is = SrcCreatorGenericsTest.class.getResourceAsStream("/testclass/GenericsFixture.class");
        assertNotNull("夹具字节码资源未找到", is);
        ClassFile classFile = ByteCodeResolver.parseFromStream(is);

        // 排错：safeDiv 上应该挂着 ExceptionsAttribute（不应为空）
        boolean saw = false;
        for (MethodInfo mi : classFile.methodInfo.getMethods()) {
            for (AbstractAttribute a : mi.getAttributes()) {
                if (a instanceof ExceptionsAttribute) {
                    ExceptionsAttribute e = (ExceptionsAttribute) a;
                    assertSame("expected one entry in safeDiv's Exceptions attribute",
                            1, e.getExceptionClassNames().size());
                    assertEquals("java.lang.ArithmeticException", e.getExceptionClassNames().get(0));
                    saw = true;
                }
            }
        }
        assertTrue("safeDiv 应含 ExceptionsAttribute", saw);

        String src = SrcCreator.createJavaFileSrc(classFile);
        assertTrue("应含 throws 子句: " + src, src.contains("throws java.lang.ArithmeticException"));
        assertTrue("throws 应在 int safeDiv 方法上: " + src,
                src.contains("int safeDiv(int, int) throws java.lang.ArithmeticException"));
    }
}