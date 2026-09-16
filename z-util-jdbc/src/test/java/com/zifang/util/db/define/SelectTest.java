package com.zifang.util.db.define;

import org.junit.Test;

import java.lang.annotation.*;

import static org.junit.Assert.*;

/**
 * Select 注解测试
 */

/**
 * SelectTest类。
 */
public class SelectTest {

    @Test
    /**
     * testSelectIsAnnotation方法。
     */
    public void testSelectIsAnnotation() {
        assertTrue(Select.class.isAnnotation());
    }

    @Test
    /**
     * testSelectTargetMethod方法。
     */
    public void testSelectTargetMethod() {
        assertTrue(Select.class.isAnnotationPresent(Target.class));
        Target target = Select.class.getAnnotation(Target.class);
        assertEquals(1, target.value().length);
        assertEquals(ElementType.METHOD, target.value()[0]);
    }

    @Test
    /**
     * testSelectRetentionRuntime方法。
     */
    public void testSelectRetentionRuntime() {
        assertTrue(Select.class.isAnnotationPresent(Retention.class));
        Retention retention = Select.class.getAnnotation(Retention.class);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    /**
     * testSelectDocumented方法。
     */
    public void testSelectDocumented() {
        assertTrue(Select.class.isAnnotationPresent(Documented.class));
    }

    @Test
    /**
     * testSelectHasValueMethod方法。
     */
    public void testSelectHasValueMethod() {
        try {
            java.lang.reflect.Method valueMethod = Select.class.getMethod("value");
            assertNotNull(valueMethod);
            assertEquals(String.class, valueMethod.getReturnType());
        } catch (NoSuchMethodException e) {
            fail("value method should exist");
        }
    }

    @Test
    /**
     * testSelectHasOneMethod方法。
     */
    public void testSelectHasOneMethod() {
        java.lang.reflect.Method[] methods = Select.class.getDeclaredMethods();
        assertEquals(1, methods.length);
        assertEquals("value", methods[0].getName());
    }
}