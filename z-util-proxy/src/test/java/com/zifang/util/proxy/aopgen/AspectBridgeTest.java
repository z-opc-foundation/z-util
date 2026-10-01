package com.zifang.util.proxy.aopgen;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

/**
 * 把既有的 {@link com.zifang.util.proxy.aspects.Aspect} 抽象接上
 * SourceProxyFactory 的字节码代理链路——端到端验证 "aop 模块的切面可以直接复用
 * 我们造的字节码代理"。
 */
public class AspectBridgeTest {

    @Test
    public void realAspectFiresOnEveryInvocation() {
        EchoService target = new EchoService();
        CountingAspect aspect = new CountingAspect();
        EchoService proxy = SourceProxyFactory.proxy(target, new AspectToHookAdapter(aspect, target));

        assertNotNull(proxy);
        assertSame("代理应能转型回目标类型", true, proxy instanceof EchoService);

        assertEquals("echo:hi", proxy.echo("hi"));
        assertEquals(3, proxy.len("abc"));
        proxy.run();

        assertEquals(Arrays.asList("echo", "len", "run"), aspect.beforeLog);
        assertEquals(Arrays.asList("echo=echo:hi", "len=3", "run=null"), aspect.afterLog);
        assertEquals(0, aspect.afterExceptionLog.size());
    }

    @Test
    public void bridgePropagatesExceptionAndFiresAfterException() {
        CountingAspect aspect = new CountingAspect();
        EchoService proxy = SourceProxyFactory.proxy(new EchoService(), new AspectToHookAdapter(aspect, new EchoService()));
        try {
            proxy.explode();
            throw new AssertionError("期望 explode 抛 IllegalStateException");
        } catch (IllegalStateException expected) {
            assertEquals("boom", expected.getMessage());
        }
        // 异常路径：before + afterException 触发，after 不触发
        assertEquals(java.util.Arrays.asList("explode"), aspect.beforeLog);
        assertEquals(0, aspect.afterLog.size());
        assertEquals(java.util.Arrays.asList("explode:boom"), aspect.afterExceptionLog);
    }
}