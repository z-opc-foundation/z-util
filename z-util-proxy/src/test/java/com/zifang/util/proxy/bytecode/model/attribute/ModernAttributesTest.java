package com.zifang.util.proxy.bytecode.model.attribute;

import com.zifang.util.proxy.bytecode.model.ClassFile;
import com.zifang.util.proxy.bytecode.resolver.ByteCodeResolver;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 现代类属性 round-trip：InnerClasses / BootstrapMethods / RuntimeVisibleAnnotations /
 * NestMembers 的解析不应破坏流、且能从 fixture 字节里读出至少一条记录。
 */
public class ModernAttributesTest {

    private static ClassFile parse() throws Exception {
        try (InputStream is = ModernAttributesTest.class.getResourceAsStream("/testclass/ModernClassFixture.class")) {
            assertNotNull("ModernClassFixture.class 资源未找到", is);
            return ByteCodeResolver.parseFromStream(is);
        }
    }

    @Test
    public void innerClassesParsedWithoutDesync() throws Exception {
        ClassFile cf = parse();
        boolean saw = false;
        for (AbstractAttribute a : cf.getAttributes()) {
            if (a instanceof InnerClassesAttribute) {
                InnerClassesAttribute ic = (InnerClassesAttribute) a;
                assertTrue("InnerClasses 至少 1 条", ic.getEntries().size() >= 1);
                saw = true;
            }
        }
        assertTrue("ModernClassFixture 应挂 InnerClasses 属性", saw);
    }

    @Test
    public void bootstrapMethodsParsedWithoutDesync() throws Exception {
        ClassFile cf = parse();
        boolean saw = false;
        for (AbstractAttribute a : cf.getAttributes()) {
            if (a instanceof BootstrapMethodsAttribute) {
                BootstrapMethodsAttribute bm = (BootstrapMethodsAttribute) a;
                assertTrue("bootstrapMethods 至少 1 条", bm.getEntries().size() >= 1);
                saw = true;
            }
        }
        assertTrue("lambda 字段应挂 BootstrapMethods", saw);
    }

    @Test
    public void runtimeVisibleAnnotationsParsedWithoutDesync() throws Exception {
        ClassFile cf = parse();
        boolean saw = false;
        for (AbstractAttribute a : cf.getAttributes()) {
            if (a instanceof RuntimeVisibleAnnotationsAttribute) {
                RuntimeVisibleAnnotationsAttribute ra = (RuntimeVisibleAnnotationsAttribute) a;
                assertNotNull("rawBody 应被消费", ra.getRawBody());
                saw = true;
            }
        }
        assertTrue("@Deprecated 应挂 RuntimeVisibleAnnotations", saw);
    }

    @Test
    public void codeAttributesIncludeStackMapTable() throws Exception {
        ClassFile cf = parse();
        boolean saw = false;
        for (com.zifang.util.proxy.bytecode.model.method.MethodInfo mi : cf.methodInfo.getMethods()) {
            com.zifang.util.proxy.bytecode.model.attribute.Code code = findCode(mi);
            if (code == null) {
                continue;
            }
            for (AbstractAttribute a : code.getAttributes()) {
                if (a instanceof StackMapTableAttribute) {
                    assertNotNull("StackMapTable 字节应被消费", ((StackMapTableAttribute) a).getRawBody());
                    saw = true;
                }
            }
        }
        assertTrue("Code 属性应挂 StackMapTable", saw);
    }

    private static com.zifang.util.proxy.bytecode.model.attribute.Code findCode(com.zifang.util.proxy.bytecode.model.method.MethodInfo mi) {
        for (AbstractAttribute a : mi.getAttributes()) {
            if (a instanceof com.zifang.util.proxy.bytecode.model.attribute.Code) {
                return (com.zifang.util.proxy.bytecode.model.attribute.Code) a;
            }
        }
        return null;
    }
}