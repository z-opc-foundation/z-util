package com.zifang.util.source;

import com.zifang.util.source.generator.info.FieldInfo;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * FieldInfo 模型测试
 */

/**
 * FieldInfoTest类。
 */
public class FieldInfoTest {

    @Test
    /**
     * testDefaultConstructor方法。
     */
    public void testDefaultConstructor() {
        FieldInfo fieldInfo = new FieldInfo();
        assertNull(fieldInfo.getType());
        assertNull(fieldInfo.getValue());
    }

    @Test
    /**
     * testTwoParamConstructor方法。
     */
    public void testTwoParamConstructor() {
        FieldInfo fieldInfo = new FieldInfo("String", "name");
        assertEquals("String", fieldInfo.getType());
        assertEquals("name", fieldInfo.getValue());
        assertEquals("null", fieldInfo.getInitializer());
    }

    @Test
    /**
     * testFourParamConstructor方法。
     */
    public void testFourParamConstructor() {
        FieldInfo fieldInfo = new FieldInfo(
                "int", "age",
                new int[]{java.lang.reflect.Modifier.PRIVATE},
                "18"
        );
        assertEquals("int", fieldInfo.getType());
        assertEquals("age", fieldInfo.getValue());
        assertEquals("18", fieldInfo.getInitializer());
        assertEquals(1, fieldInfo.getModifiers().length);
    }

    @Test
    /**
     * testEquals方法。
     */
    public void testEquals() {
        FieldInfo f1 = new FieldInfo("String", "name");
        FieldInfo f2 = new FieldInfo("int", "name");
        FieldInfo f3 = new FieldInfo("String", "name");

        // FieldInfo.equals 只比较字段名称，不比较类型
        assertTrue(f1.equals(f2)); // same name
        assertTrue(f1.equals(f3)); // same type and name
    }

    @Test
    /**
     * testHashCode方法。
     */
    public void testHashCode() {
        FieldInfo f1 = new FieldInfo("String", "name");
        FieldInfo f2 = new FieldInfo("String", "name");

        assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test
    /**
     * testToString方法。
     */
    public void testToString() {
        FieldInfo fieldInfo = new FieldInfo("String", "name", new int[]{}, "\"default\"");
        assertEquals("String name = \"default\";", fieldInfo.toString());
    }

    @Test
    /**
     * testToStringWithoutInitializer方法。
     */
    public void testToStringWithoutInitializer() {
        FieldInfo fieldInfo = new FieldInfo("String", "name");
        assertEquals("String name = null;", fieldInfo.toString());
    }

    @Test
    /**
     * testModifiers方法。
     */
    public void testModifiers() {
        FieldInfo fieldInfo = new FieldInfo();
        fieldInfo.setModifiers(new int[]{
                java.lang.reflect.Modifier.PRIVATE,
                java.lang.reflect.Modifier.STATIC,
                java.lang.reflect.Modifier.FINAL
        });

        int[] mods = fieldInfo.getModifiers();
        assertEquals(3, mods.length);
    }

    @Test
    /**
     * testSetModifier方法。
     */
    public void testSetModifier() {
        FieldInfo fieldInfo = new FieldInfo();
        fieldInfo.setModifier(java.lang.reflect.Modifier.PUBLIC);

        assertEquals(1, fieldInfo.getModifiers().length);
        assertEquals(java.lang.reflect.Modifier.PUBLIC, fieldInfo.getModifiers()[0]);
    }

    @Test
    /**
     * testAnnotations方法。
     */
    public void testAnnotations() {
        FieldInfo fieldInfo = new FieldInfo("String", "name");
        com.zifang.util.source.generator.info.AnnotationInfo annot =
                new com.zifang.util.source.generator.info.AnnotationInfo("Nullable");

        fieldInfo.getAnnotations().add(annot);

        assertEquals(1, fieldInfo.getAnnotations().size());
        assertEquals("Nullable", fieldInfo.getAnnotations().get(0).getType());
    }
}