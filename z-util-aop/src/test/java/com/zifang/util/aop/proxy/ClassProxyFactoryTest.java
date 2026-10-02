package com.zifang.util.aop.proxy;

import com.zifang.util.aop.aspects.Aspect;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ClassProxyFactoryTest：自研子类代理（SourceProxyFactory 内存编译链路）的行为面，
 * 覆盖旧 CglibInterceptorTest 仍适用的场景。
 * <p>
 * 与旧 cglib 契约的差异：Aspect 的 boolean 返回值在新链路中被忽略
 * （MethodHook.before 为 void，无拦截语义），故旧 BlockingAspect 场景不再适用。
 */
public class ClassProxyFactoryTest {

    @Test
    /**
     * 正常路径：before/after 依次触发，返回值原样透传。
     */
    public void testProxyBeforeAfterCalled() {
        TestTarget target = new TestTarget();
        RecordingAspect aspect = new RecordingAspect();

        TestTarget proxy = new ClassProxyFactory().proxy(target, aspect);

        assertEquals("Hello", proxy.sayHello());
        assertTrue(aspect.beforeCalled);
        assertTrue(aspect.afterCalled);
        assertFalse(aspect.exceptionCalled);
    }

    @Test
    /**
     * 异常路径：afterException 触发、after 不触发，原始异常向调用方重抛。
     */
    public void testProxyAfterExceptionCalledAndRethrown() {
        TestTarget target = new TestTarget();
        RecordingAspect aspect = new RecordingAspect();

        TestTarget proxy = new ClassProxyFactory().proxy(target, aspect);

        RuntimeException thrown = assertThrows(RuntimeException.class, proxy::throwException);
        assertEquals("test", thrown.getMessage());
        assertTrue(aspect.beforeCalled);
        assertTrue(aspect.exceptionCalled);
        assertFalse(aspect.afterCalled);
    }

    @Test
    /**
     * 参数透传：切面拿到与方法实参一致的 Object[]。
     */
    public void testProxyPassesArgsThrough() {
        TestTarget target = new TestTarget();
        RecordingAspect aspect = new RecordingAspect();

        TestTarget proxy = new ClassProxyFactory().proxy(target, aspect);

        assertEquals("hi, z-util", proxy.greet("z-util"));
        assertArrayEquals(new Object[]{"z-util"}, aspect.lastArgs);
    }

    // --- Test Fixtures ---

    public static class TestTarget {
        public String sayHello() {
            return "Hello";
        }

        public String greet(String who) {
            return "hi, " + who;
        }

        public void throwException() {
            throw new RuntimeException("test");
        }
    }

    public static class RecordingAspect implements Aspect {
        private boolean beforeCalled = false;
        private boolean afterCalled = false;
        private boolean exceptionCalled = false;
        private Object[] lastArgs;

        @Override
        public boolean before(Object target, Method method, Object[] args) {
            beforeCalled = true;
            lastArgs = args;
            return true;
        }

        @Override
        public boolean after(Object target, Method method, Object[] args, Object returnVal) {
            afterCalled = true;
            return true;
        }

        @Override
        public boolean afterException(Object target, Method method, Object[] args, Throwable e) {
            exceptionCalled = true;
            return true;
        }
    }
}
