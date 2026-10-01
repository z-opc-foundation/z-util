package com.zifang.util.proxy.weave;

import com.zifang.util.proxy.aopgen.EchoService;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * ASM 字节级织入端到端：读既有 class → 织入入口钩子 → 定义 → 执行。
 */
public class AsmEntryWeaveTest {

    private byte[] wovenBytes;

    @Before
    public void weaveEchoService() throws Exception {
        byte[] original;
        try (InputStream is = EchoService.class.getResourceAsStream("EchoService.class")) {
            assertTrue("EchoService.class 资源未找到", is != null);
            original = readAll(is);
        }
        wovenBytes = EntryWeaver.weave(original, WeaveRecorder.class, "record");
        WeaveRecorder.clear();
    }

    private static String wovenClass() {
        // ByteCodeResolver 写入的 owner 是 class 内部名（斜杠分隔），与 Class#getName 的
        // 外部形式不同；测试断言必须按同一口径才能钉住织入是否真的发生。
        return EchoService.class.getName().replace('.', '/');
    }

    @Test
    public void wovenClassExecutesWithEntryHooks() throws Exception {
        ClassLoader childFirst = new ChildFirstClassLoader(
                Collections.singletonMap(EchoService.class.getName(), wovenBytes),
                getClass().getClassLoader());
        Class<?> woven = childFirst.loadClass(EchoService.class.getName());
        Object instance = woven.getDeclaredConstructor().newInstance();

        // 业务语义不变
        assertEquals("echo:hi", woven.getMethod("echo", String.class).invoke(instance, "hi"));
        assertEquals(3, woven.getMethod("len", String.class).invoke(instance, "abc"));

        // 入口钩子已记录（不含构造器）
        assertTrue(WeaveRecorder.ENTRIES.contains(wovenClass() + "#echo"));
        assertTrue(WeaveRecorder.ENTRIES.contains(wovenClass() + "#len"));
        for (String entry : WeaveRecorder.ENTRIES) {
            assertFalse("构造器不应织入: " + entry, entry.contains("#<init>"));
        }
    }

    @Test
    public void finalMethodIsWeavableAtByteLevel() throws Exception {
        // final 方法在字节级照样可以织入口（源码级代理织不进，这正是两条路线的分界）
        ClassLoader childFirst = new ChildFirstClassLoader(
                Collections.singletonMap(EchoService.class.getName(), wovenBytes),
                getClass().getClassLoader());
        Class<?> woven = childFirst.loadClass(EchoService.class.getName());
        Object instance = woven.getDeclaredConstructor().newInstance();
        assertEquals("stable", woven.getMethod("stable").invoke(instance));
        assertTrue(WeaveRecorder.ENTRIES.contains(wovenClass() + "#stable"));
    }

    private static byte[] readAll(InputStream is) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) != -1) {
            bos.write(buf, 0, n);
        }
        return bos.toByteArray();
    }
}
