package com.zifang.util.bc.aopgen;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 源码级 AOP 代理端到端：织入 → 编译 → 定义 → 执行。
 */
public class SourceProxyFactoryTest {

    @Test
    public void proxyInterceptsBusinessMethodsAndKeepsSemantics() {
        RecordingHook hook = new RecordingHook();
        EchoService proxy = SourceProxyFactory.proxy(new EchoService(), hook);

        assertTrue("代理应为目标类型", proxy instanceof EchoService);

        assertEquals("echo:hi", proxy.echo("hi"));
        assertEquals(3, proxy.len("abc"));
        proxy.run();

        List<String> expected = Arrays.asList(
                "before:echo:[hi]", "after:echo:echo:hi",
                "before:len:[abc]", "after:len:3",
                "before:run:[]", "after:run:null");
        assertEquals(expected, hook.events);
    }

    @Test
    public void finalAndStaticMethodsStayUnhooked() {
        RecordingHook hook = new RecordingHook();
        EchoService proxy = SourceProxyFactory.proxy(new EchoService(), hook);

        // final / static 不可覆写，走继承，钩子不应出现
        assertEquals("stable", proxy.stable());
        assertEquals("stat", EchoService.stat());
        for (String e : hook.events) {
            assertFalse(e.startsWith("before:stable"));
            assertFalse(e.startsWith("before:stat"));
        }
        assertTrue(hook.events.isEmpty());
    }

    @Test
    public void generatedSourceIsHumanReadable() {
        String source = SourceProxyFactory.buildSource(EchoService.class, "EchoService$SourceProxy1");
        assertTrue(source.contains("public class EchoService$SourceProxy1 extends com.zifang.util.bc.aopgen.EchoService"));
        assertTrue(source.contains("public java.lang.String echo(java.lang.String p0)"));
        assertTrue(source.contains("hook.before(\"echo\", new Object[]{p0});"));
        assertTrue(source.contains("hook.after(\"run\", null);"));
        assertFalse(source.contains("stable("));
    }
}
