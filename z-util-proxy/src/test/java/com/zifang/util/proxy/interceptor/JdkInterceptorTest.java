package com.zifang.util.proxy.interceptor;

import com.zifang.util.proxy.aspects.Aspect;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * JdkInterceptorTest类。
 */
public class JdkInterceptorTest {

    @Test
    /**
     * testGetTarget方法。
     */
    public void testGetTarget() {
        TestTarget target = new TestTarget();
        TestAspect aspect = new TestAspect();
        JdkInterceptor interceptor = new JdkInterceptor(target, aspect);

        assertSame(target, interceptor.getTarget());
    }

    @Test
    /**
     * testBeforeReturningTrue方法。
     */
    public void testBeforeReturningTrue() throws Throwable {
        TestTarget target = new TestTarget();
        TestAspect aspect = new TestAspect();
        JdkInterceptor interceptor = new JdkInterceptor(target, aspect);

        Method method = TestTarget.class.getMethod("sayHello");
        Object result = interceptor.invoke(null, method, new Object[]{});

        assertTrue(aspect.isBeforeCalled());
        assertTrue(aspect.isAfterCalled());
    }

    @Test
    /**
     * testBeforeReturningFalse方法。
     */
    public void testBeforeReturningFalse() throws Throwable {
        TestTarget target = new TestTarget();
        BlockingAspect aspect = new BlockingAspect();
        JdkInterceptor interceptor = new JdkInterceptor(target, aspect);

        Method method = TestTarget.class.getMethod("sayHello");
        Object result = interceptor.invoke(null, method, new Object[]{});

        assertTrue(aspect.isBeforeCalled());
        assertFalse(aspect.isAfterCalled());
        assertNull(result);
    }

    @Test
    /**
     * testAfterException方法。
     */
    public void testAfterException() throws Throwable {
        TestTarget target = new TestTarget();
        ExceptionAspect aspect = new ExceptionAspect();
        JdkInterceptor interceptor = new JdkInterceptor(target, aspect);

        Method method = TestTarget.class.getMethod("throwException");
        try {
            interceptor.invoke(null, method, new Object[]{});
            fail("Expected exception");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof RuntimeException);
        }

        assertTrue(aspect.isBeforeCalled());
        assertTrue(aspect.isAfterExceptionCalled());
    }

    @Test
    /**
     * testAfterExceptionRethrow方法。
     */
    public void testAfterExceptionRethrow() throws Throwable {
        TestTarget target = new TestTarget();
        RethrowAspect aspect = new RethrowAspect();
        JdkInterceptor interceptor = new JdkInterceptor(target, aspect);

        Method method = TestTarget.class.getMethod("throwException");
        try {
            interceptor.invoke(null, method, new Object[]{});
            fail("Expected RuntimeException");
        } catch (Exception e) {
            assertTrue(e.getCause() instanceof RuntimeException);
        }
    }

    public static class TestTarget {
        /**
         * sayHello方法。
         *
         * @return String类型返回值
         */
        public String sayHello() {
            return "Hello";
        }

        /**
         * throwException方法。
         */
        public void throwException() {
            throw new RuntimeException("Test exception");
        }
    }

    public static class TestAspect implements Aspect {
        private boolean beforeCalled = false;
        private boolean afterCalled = false;

        @Override
        /**
         * before方法。
         *      * @param target Object类型参数
         * @param method Method类型参数
         * @param args Object[]类型参数
         * @return boolean类型返回值
         */
        public boolean before(Object target, Method method, Object[] args) {
            beforeCalled = true;
            return true;
        }

        @Override
        /**
         * after方法。
         *      * @param target Object类型参数
         * @param method Method类型参数
         * @param args Object[]类型参数
         * @param returnVal Object类型参数
         * @return boolean类型返回值
         */
        public boolean after(Object target, Method method, Object[] args, Object returnVal) {
            afterCalled = true;
            return true;
        }

        @Override
        /**
         * afterException方法。
         *      * @param target Object类型参数
         * @param method Method类型参数
         * @param args Object[]类型参数
         * @param e Throwable类型参数
         * @return boolean类型返回值
         */
        public boolean afterException(Object target, Method method, Object[] args, Throwable e) {
            return true;
        }

        /**
         * isBeforeCalled方法。
         *
         * @return boolean类型返回值
         */
        public boolean isBeforeCalled() {
            return beforeCalled;
        }

        /**
         * isAfterCalled方法。
         *
         * @return boolean类型返回值
         */
        public boolean isAfterCalled() {
            return afterCalled;
        }
    }

    public static class BlockingAspect implements Aspect {
        private boolean beforeCalled = false;
        private boolean afterCalled = false;

        @Override
        /**
         * before方法。
         *      * @param target Object类型参数
         * @param method Method类型参数
         * @param args Object[]类型参数
         * @return boolean类型返回值
         */
        public boolean before(Object target, Method method, Object[] args) {
            beforeCalled = true;
            return false;
        }

        @Override
        /**
         * after方法。
         *      * @param target Object类型参数
         * @param method Method类型参数
         * @param args Object[]类型参数
         * @param returnVal Object类型参数
         * @return boolean类型返回值
         */
        public boolean after(Object target, Method method, Object[] args, Object returnVal) {
            afterCalled = true;
            return true;
        }

        @Override
        /**
         * afterException方法。
         *      * @param target Object类型参数
         * @param method Method类型参数
         * @param args Object[]类型参数
         * @param e Throwable类型参数
         * @return boolean类型返回值
         */
        public boolean afterException(Object target, Method method, Object[] args, Throwable e) {
            return true;
        }

        /**
         * isBeforeCalled方法。
         *
         * @return boolean类型返回值
         */
        public boolean isBeforeCalled() {
            return beforeCalled;
        }

        /**
         * isAfterCalled方法。
         *
         * @return boolean类型返回值
         */
        public boolean isAfterCalled() {
            return afterCalled;
        }
    }

    public static class ExceptionAspect implements Aspect {
        private boolean beforeCalled = false;
        private boolean afterExceptionCalled = false;

        @Override
        /**
         * before方法。
         *      * @param target Object类型参数
         * @param method Method类型参数
         * @param args Object[]类型参数
         * @return boolean类型返回值
         */
        public boolean before(Object target, Method method, Object[] args) {
            beforeCalled = true;
            return true;
        }

        @Override
        /**
         * after方法。
         *      * @param target Object类型参数
         * @param method Method类型参数
         * @param args Object[]类型参数
         * @param returnVal Object类型参数
         * @return boolean类型返回值
         */
        public boolean after(Object target, Method method, Object[] args, Object returnVal) {
            return false;
        }

        @Override
        /**
         * afterException方法。
         *      * @param target Object类型参数
         * @param method Method类型参数
         * @param args Object[]类型参数
         * @param e Throwable类型参数
         * @return boolean类型返回值
         */
        public boolean afterException(Object target, Method method, Object[] args, Throwable e) {
            afterExceptionCalled = true;
            return true;
        }

        /**
         * isBeforeCalled方法。
         *
         * @return boolean类型返回值
         */
        public boolean isBeforeCalled() {
            return beforeCalled;
        }

        /**
         * isAfterExceptionCalled方法。
         *
         * @return boolean类型返回值
         */
        public boolean isAfterExceptionCalled() {
            return afterExceptionCalled;
        }
    }

    public static class RethrowAspect implements Aspect {
        @Override
        /**
         * before方法。
         *      * @param target Object类型参数
         * @param method Method类型参数
         * @param args Object[]类型参数
         * @return boolean类型返回值
         */
        public boolean before(Object target, Method method, Object[] args) {
            return true;
        }

        @Override
        /**
         * after方法。
         *      * @param target Object类型参数
         * @param method Method类型参数
         * @param args Object[]类型参数
         * @param returnVal Object类型参数
         * @return boolean类型返回值
         */
        public boolean after(Object target, Method method, Object[] args, Object returnVal) {
            return true;
        }

        @Override
        /**
         * afterException方法。
         *      * @param target Object类型参数
         * @param method Method类型参数
         * @param args Object[]类型参数
         * @param e Throwable类型参数
         * @return boolean类型返回值
         */
        public boolean afterException(Object target, Method method, Object[] args, Throwable e) {
            return false;
        }
    }
}
