package com.zifang.util.bc.compile;

import com.zifang.util.bc.bytecode.model.ClassFile;
import com.zifang.util.bc.bytecode.resolver.ByteCodeResolver;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 从 0 制造 class 端到端测试：动态拼源码 → javax.tools 内存编译 → 自定义
 * ClassLoader 加载 → 反射执行 → 用本模块 ByteCodeResolver 解析产物 →
 * SrcCreator 反编译回源码。
 * <p>
 * 「代理就是一种制造字节码的模式」，这条链是制造能力的最直接证据。
 */
public class BytecodeManufactureTest {

    @Test
    public void manufactureCompileLoadExecute() throws Exception {
        // 1. 从 0 拼源码：类名含纳秒时间戳，证明每次运行都是新造的类
        String suffix = Long.toHexString(System.nanoTime());
        String fqn = "generated.Manufactured" + suffix;
        String javaCode = "package generated;\n"
                + "\n"
                + "public class Manufactured" + suffix + " {\n"
                + "    public int add(int a, int b) {\n"
                + "        return a + b;\n"
                + "    }\n"
                + "\n"
                + "    public String greet(String who) {\n"
                + "        return \"hello \" + who;\n"
                + "    }\n"
                + "}\n";

        // 2. 内存编译为字节码
        Map<String, BytesJavaFileObject> compiled = CFJavaCompiler.compile(fqn, javaCode);
        assertTrue("编译产物应包含 " + fqn + "，实际 keys=" + compiled.keySet(), compiled.containsKey(fqn));
        byte[] classBytes = compiled.get(fqn).getBytes();
        assertEquals("字节码应为 4 字节魔数开头", 4, classBytes.length >= 4 ? 4 : classBytes.length);
        assertEquals("魔数应为 0xCAFEBABE", 0xCA, classBytes[0] & 0xFF);
        assertEquals(0xFE, classBytes[1] & 0xFF);
        assertEquals(0xBA, classBytes[2] & 0xFF);
        assertEquals(0xBE, classBytes[3] & 0xFF);

        // 3. 自定义 ClassLoader 定义并执行
        MapClassLoader loader = new MapClassLoader(java.util.Collections.singletonMap(fqn, classBytes));
        Class<?> manufactured = loader.loadClass(fqn);
        Object instance = manufactured.getDeclaredConstructor().newInstance();
        assertEquals(5, manufactured.getDeclaredMethod("add", int.class, int.class).invoke(instance, 2, 3));
        assertEquals("hello zifang",
                manufactured.getDeclaredMethod("greet", String.class).invoke(instance, "zifang"));

        // 4. 本模块解析器消费自产字节码：类名回读一致（class 文件内部形式为斜杠分隔）
        ClassFile classFile = ByteCodeResolver.parseFromStream(new ByteArrayInputStream(classBytes));
        assertEquals(fqn.replace('.', '/'), classFile.getClassName());

        // 5. 反编译回源码：方法签名可见
        String decompiled = com.zifang.util.bc.bytecode.decompile.core.SrcCreator.createJavaFileSrc(classFile);
        assertTrue("反编译应含类声明: " + decompiled, decompiled.contains("class Manufactured" + suffix));
        assertTrue("反编译应含方法签名: " + decompiled, decompiled.contains("int add(int, int)"));
    }

    @Test
    public void compileFailureYieldsEmptyMapNotException() throws Exception {
        Map<String, BytesJavaFileObject> compiled = CFJavaCompiler.compile(
                "generated.Broken", "package generated; public class Broken { this is not java }");
        assertNotNull(compiled);
        assertTrue("编译失败应返回空 map", compiled.isEmpty());
    }
}
