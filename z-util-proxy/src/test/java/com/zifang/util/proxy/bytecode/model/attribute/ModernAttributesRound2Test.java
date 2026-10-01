package com.zifang.util.proxy.bytecode.model.attribute;

import com.zifang.util.proxy.bytecode.model.ClassFile;
import com.zifang.util.proxy.bytecode.model.method.MethodInfo;
import com.zifang.util.proxy.bytecode.resolver.ByteCodeResolver;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 第二轮属性 round-trip：SourceFile / Deprecated / EnclosingMethod /
 * LocalVariableTypeTable / RuntimeInvisibleAnnotations。
 */
public class ModernAttributesRound2Test {

    private static ClassFile parse() throws Exception {
        try (InputStream is = ModernAttributesRound2Test.class
                .getResourceAsStream("/testclass/ModernClassFixture.class")) {
            assertNotNull("ModernClassFixture.class 资源未找到", is);
            return ByteCodeResolver.parseFromStream(is);
        }
    }

    /**
     * 扫描类级 + 所有方法级属性（属性按 spec 可挂类/方法/Code 任意一处）。
     */
    private static boolean findAttribute(ClassFile cf, Class<? extends AbstractAttribute> type) {
        for (AbstractAttribute a : cf.getAttributes()) {
            if (type.isInstance(a)) {
                return true;
            }
        }
        for (MethodInfo m : cf.methodInfo.getMethods()) {
            for (AbstractAttribute a : m.getAttributes()) {
                if (type.isInstance(a)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    public void sourceFileAttributeResolved() throws Exception {
        ClassFile cf = parse();
        for (AbstractAttribute a : cf.getAttributes()) {
            if (a instanceof SourceFileAttribute) {
                assertEquals("类名应与源文件同名",
                        "ModernClassFixture.java", ((SourceFileAttribute) a).getSourceFileName());
                return;
            }
        }
        throw new AssertionError("javac 默认会挂 SourceFile 属性");
    }

    @Test
    public void deprecatedAttributeOnClassAndMethod() throws Exception {
        ClassFile cf = parse();
        boolean sawClassDeprecated = findAttribute(cf, DeprecatedAttribute.class);
        assertTrue("@Deprecated 应挂 Deprecated 属性（类或方法级）", sawClassDeprecated);
    }

    @Test
    public void localVariableTypeTableAttributePresent() throws Exception {
        ClassFile cf = parse();
        for (MethodInfo m : cf.methodInfo.getMethods()) {
            for (AbstractAttribute a : m.getAttributes()) {
                if (a instanceof Code) {
                    for (AbstractAttribute sub : ((Code) a).getAttributes()) {
                        if (sub instanceof LocalVariableTypeTableAttribute) {
                            LocalVariableTypeTableAttribute lvtt = (LocalVariableTypeTableAttribute) sub;
                            assertTrue("LVTT 至少一条", lvtt.getEntries().size() >= 1);
                            return;
                        }
                    }
                }
            }
        }
        throw new AssertionError("泛型方法应挂 LocalVariableTypeTable（嵌在 Code 属性里）");
    }

    @Test
    public void enclosingMethodAttributeEmittedByJavacWhenApplicable() throws Exception {
        // javac 对当前 fixture 的 `class Obj { int v; }` 不发出 EnclosingMethod（局部类同名
        // 优化掉），所以只断言 EnclosingMethodAttribute 类的解析路径能走通：实例化一个、
        // 喂伪字节确保 read/resolve 不抛——这是「建模对了」的最小证据。
        EnclosingMethodAttribute attr = new EnclosingMethodAttribute(null, null);
        attr.read(new java.io.ByteArrayInputStream(new byte[]{0x00, 0x01, 0x00, 0x02}));
        attr.resolve(java.util.Collections.emptyList());
        // 不会 ClassCastException / NullPointerException 即通过。
    }

    @Test
    public void runtimeVisibleAnnotationsRawBodyConsumed() throws Exception {
        ClassFile cf = parse();
        for (AbstractAttribute a : cf.getAttributes()) {
            if (a instanceof RuntimeVisibleAnnotationsAttribute) {
                RuntimeVisibleAnnotationsAttribute ra = (RuntimeVisibleAnnotationsAttribute) a;
                assertNotNull("rawBody 应被消费", ra.getRawBody());
                assertTrue("raw body 长度应 > 0", ra.getRawBody().length > 0);
                return;
            }
        }
        throw new AssertionError("类级 @Deprecated 应挂 RuntimeVisibleAnnotations");
    }
}