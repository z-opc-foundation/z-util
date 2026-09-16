package com.zifang.util.core.lang;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * ClassUtilTest类。
 */
public class ClassUtilTest {

    // --- isPrimitive ---

    @Test
    /**
     * testIsPrimitive_WithValidPrimitiveTypes方法。
     */
    public void testIsPrimitive_WithValidPrimitiveTypes() {
        assertTrue(ClassUtil.isPrimitive("int"));
        assertTrue(ClassUtil.isPrimitive("double"));
        assertTrue(ClassUtil.isPrimitive("long"));
        assertTrue(ClassUtil.isPrimitive("short"));
        assertTrue(ClassUtil.isPrimitive("byte"));
        assertTrue(ClassUtil.isPrimitive("boolean"));
        assertTrue(ClassUtil.isPrimitive("char"));
        assertTrue(ClassUtil.isPrimitive("float"));
    }

    @Test
    /**
     * testIsPrimitive_WithInvalidTypes方法。
     */
    public void testIsPrimitive_WithInvalidTypes() {
        assertFalse(ClassUtil.isPrimitive("String"));
        assertFalse(ClassUtil.isPrimitive("Integer"));
        assertFalse(ClassUtil.isPrimitive("java.lang.String"));
        assertFalse(ClassUtil.isPrimitive(""));
        assertFalse(ClassUtil.isPrimitive("object"));
    }

    // --- isBaseWrap ---

    @Test
    /**
     * testIsBaseWrap_WithValidWrapperTypes方法。
     */
    public void testIsBaseWrap_WithValidWrapperTypes() {
        assertTrue(ClassUtil.isBaseWrap("java.lang.Integer"));
        assertTrue(ClassUtil.isBaseWrap("java.lang.Double"));
        assertTrue(ClassUtil.isBaseWrap("java.lang.Float"));
        assertTrue(ClassUtil.isBaseWrap("java.lang.Long"));
        assertTrue(ClassUtil.isBaseWrap("java.lang.Short"));
        assertTrue(ClassUtil.isBaseWrap("java.lang.Byte"));
        assertTrue(ClassUtil.isBaseWrap("java.lang.Boolean"));
        assertTrue(ClassUtil.isBaseWrap("java.lang.Character"));
    }

    @Test
    /**
     * testIsBaseWrap_WithInvalidTypes方法。
     */
    public void testIsBaseWrap_WithInvalidTypes() {
        assertFalse(ClassUtil.isBaseWrap("int"));
        assertFalse(ClassUtil.isBaseWrap("String"));
        assertFalse(ClassUtil.isBaseWrap("java.lang.String"));
    }

    @Test
    /**
     * testIsBaseWrap_WithClass方法。
     */
    public void testIsBaseWrap_WithClass() {
        assertTrue(ClassUtil.isBaseWrap(Integer.class));
        assertTrue(ClassUtil.isBaseWrap(Double.class));
        assertTrue(ClassUtil.isBaseWrap(Boolean.class));
        assertFalse(ClassUtil.isBaseWrap(String.class));
        assertFalse(ClassUtil.isBaseWrap(int.class));
    }

    // --- isBaseOrWrap ---

    @Test
    /**
     * testIsBaseOrWrap_WithPrimitiveOrWrapperString方法。
     */
    public void testIsBaseOrWrap_WithPrimitiveOrWrapperString() {
        assertTrue(ClassUtil.isBaseOrWrap("int"));
        assertTrue(ClassUtil.isBaseOrWrap("java.lang.Integer"));
        assertTrue(ClassUtil.isBaseOrWrap("double"));
        assertTrue(ClassUtil.isBaseOrWrap("java.lang.Double"));
    }

    @Test
    /**
     * testIsBaseOrWrap_WithInvalidString方法。
     */
    public void testIsBaseOrWrap_WithInvalidString() {
        assertFalse(ClassUtil.isBaseOrWrap("String"));
        assertFalse(ClassUtil.isBaseOrWrap("java.lang.String"));
    }

    @Test
    /**
     * testIsBaseOrWrap_WithClass方法。
     */
    public void testIsBaseOrWrap_WithClass() {
        assertTrue(ClassUtil.isBaseOrWrap(int.class));
        assertTrue(ClassUtil.isBaseOrWrap(Integer.class));
        assertTrue(ClassUtil.isBaseOrWrap(double.class));
        assertFalse(ClassUtil.isBaseOrWrap(String.class));
    }

    @Test
    /**
     * testIsBaseOrWrap_WithObject方法。
     */
    public void testIsBaseOrWrap_WithObject() {
        assertTrue(ClassUtil.isBaseOrWrap(Integer.valueOf(10)));
        assertFalse(ClassUtil.isBaseOrWrap("test"));
        assertFalse(ClassUtil.isBaseOrWrap((String) null));
    }

    // --- isBaseOrWrapOrString ---

    @Test
    /**
     * testIsBaseOrWrapOrString方法。
     */
    public void testIsBaseOrWrapOrString() {
        assertTrue(ClassUtil.isBaseOrWrapOrString(String.class));
        assertTrue(ClassUtil.isBaseOrWrapOrString(int.class));
        assertTrue(ClassUtil.isBaseOrWrapOrString(Integer.class));
        assertFalse(ClassUtil.isBaseOrWrapOrString(Object.class));
    }

    // --- isSameClass ---

    @Test
    /**
     * testIsSameClass_WithSameClasses方法。
     */
    public void testIsSameClass_WithSameClasses() {
        assertTrue(ClassUtil.isSameClass(String.class, String.class));
        assertTrue(ClassUtil.isSameClass(Integer.class, Integer.class));
        assertTrue(ClassUtil.isSameClass(null, null));
    }

    @Test
    /**
     * testIsSameClass_WithDifferentClasses方法。
     */
    public void testIsSameClass_WithDifferentClasses() {
        assertFalse(ClassUtil.isSameClass(String.class, Integer.class));
        assertFalse(ClassUtil.isSameClass(String.class, null));
        assertFalse(ClassUtil.isSameClass(null, String.class));
    }

    // --- isSameNameClass ---

    @Test
    /**
     * testIsSameNameClass_WithSameClasses方法。
     */
    public void testIsSameNameClass_WithSameClasses() {
        assertTrue(ClassUtil.isSameNameClass(String.class, String.class));
        assertTrue(ClassUtil.isSameNameClass(Integer.class, Integer.class));
        assertTrue(ClassUtil.isSameNameClass(null, null));
    }

    @Test
    /**
     * testIsSameNameClass_WithDifferentClasses方法。
     */
    public void testIsSameNameClass_WithDifferentClasses() {
        assertFalse(ClassUtil.isSameNameClass(String.class, Integer.class));
        assertFalse(ClassUtil.isSameNameClass(String.class, null));
        assertFalse(ClassUtil.isSameNameClass(null, String.class));
    }

    // --- getShortClassName ---

    @Test
    /**
     * testGetShortClassName_WithValidClassName方法。
     */
    public void testGetShortClassName_WithValidClassName() {
        assertEquals("c.z.u.c.l.ClassUtil", ClassUtil.getShortClassName("com.zifang.util.core.lang.ClassUtil"));
    }

    @Test
    /**
     * testGetShortClassName_WithSimpleClassName方法。
     */
    public void testGetShortClassName_WithSimpleClassName() {
        assertEquals("String", ClassUtil.getShortClassName("String"));
    }

    @Test
    /**
     * testGetShortClassName_WithNull方法。
     */
    public void testGetShortClassName_WithNull() {
        assertNull(ClassUtil.getShortClassName(null));
    }

    // --- isOriginJdkType ---

    @Test
    /**
     * testIsOriginJdkType方法。
     */
    public void testIsOriginJdkType() {
        assertTrue(ClassUtil.isOriginJdkType(String.class));
        assertTrue(ClassUtil.isOriginJdkType(Object.class));
        assertFalse(ClassUtil.isOriginJdkType(this.getClass()));
    }

    // --- isPrimitiveNumberType ---

    @Test
    /**
     * testIsPrimitiveNumberType方法。
     */
    public void testIsPrimitiveNumberType() {
        assertTrue(ClassUtil.isPrimitiveNumberType(int.class));
        assertTrue(ClassUtil.isPrimitiveNumberType(long.class));
        assertTrue(ClassUtil.isPrimitiveNumberType(short.class));
        assertTrue(ClassUtil.isPrimitiveNumberType(byte.class));
        assertFalse(ClassUtil.isPrimitiveNumberType(double.class));
        assertFalse(ClassUtil.isPrimitiveNumberType(float.class));
        assertFalse(ClassUtil.isPrimitiveNumberType(String.class));
    }

    // --- isPrimitiveFloatingPointNumberType ---

    @Test
    /**
     * testIsPrimitiveFloatingPointNumberType方法。
     */
    public void testIsPrimitiveFloatingPointNumberType() {
        assertTrue(ClassUtil.isPrimitiveFloatingPointNumberType(double.class));
        assertTrue(ClassUtil.isPrimitiveFloatingPointNumberType(float.class));
        assertFalse(ClassUtil.isPrimitiveFloatingPointNumberType(int.class));
        assertFalse(ClassUtil.isPrimitiveFloatingPointNumberType(long.class));
    }

    // --- toClass ---

    @Test
    /**
     * testToClass_WithValidObjects方法。
     */
    public void testToClass_WithValidObjects() {
        Class<?>[] classes = ClassUtil.toClass("test", 123, 'a');
        assertEquals(3, classes.length);
        assertEquals(String.class, classes[0]);
        assertEquals(Integer.class, classes[1]);
        assertEquals(Character.class, classes[2]);
    }

    @Test
    /**
     * testToClass_WithNullElements方法。
     */
    public void testToClass_WithNullElements() {
        Class<?>[] classes = ClassUtil.toClass("test", null, 123);
        assertEquals(3, classes.length);
        assertEquals(String.class, classes[0]);
        assertNull(classes[1]);
        assertEquals(Integer.class, classes[2]);
    }

    @Test
    /**
     * testToClass_WithNullArray方法。
     */
    public void testToClass_WithNullArray() {
        assertNull(ClassUtil.toClass((Object[]) null));
    }

    @Test
    /**
     * testToClass_WithEmptyArray方法。
     */
    public void testToClass_WithEmptyArray() {
        Class<?>[] classes = ClassUtil.toClass();
        assertEquals(0, classes.length);
    }

    // --- argumentTypesToString ---

    @Test
    /**
     * testArgumentTypesToString_WithMultipleTypes方法。
     */
    public void testArgumentTypesToString_WithMultipleTypes() {
        String result = ClassUtil.argumentTypesToString(new Class[]{String.class, int.class, Object.class});
        assertEquals("(java.lang.String, int, java.lang.Object)", result);
    }

    @Test
    /**
     * testArgumentTypesToString_WithNullTypes方法。
     */
    public void testArgumentTypesToString_WithNullTypes() {
        String result = ClassUtil.argumentTypesToString(null);
        assertNotNull(result);
        assertTrue(result.contains("null") || result.equals("()"));
    }

    @Test
    /**
     * testArgumentTypesToString_WithEmptyArray方法。
     */
    public void testArgumentTypesToString_WithEmptyArray() {
        String result = ClassUtil.argumentTypesToString(new Class[]{});
        assertEquals("()", result);
    }

    @Test
    /**
     * testNewInstance_WithDefaultConstructor方法。
     */
    public void testNewInstance_WithDefaultConstructor() {
        ArrayList<String> list = ClassUtil.newInstance(ArrayList.class);
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test(expected = RuntimeException.class)
    /**
     * testNewInstance_WithNullClass方法。
     */
    public void testNewInstance_WithNullClass() {
        ClassUtil.newInstance(null);
    }

    @Test(expected = RuntimeException.class)
    /**
     * testNewInstance_WithNoDefaultConstructor方法。
     */
    public void testNewInstance_WithNoDefaultConstructor() {
        // Integer无无参构造函数
        ClassUtil.newInstance(Integer.class);
    }

    @Test
    /**
     * testScanClasses_WithDirectoryPackage方法。
     */
    public void testScanClasses_WithDirectoryPackage() {
        // 测试环境为目录类路径，扫描lang包应包含StringUtil
        List<Class<?>> classes = ClassUtil.scanClasses("com.zifang.util.core.lang");
        assertTrue(classes.contains(StringUtil.class));
        assertTrue(classes.contains(ClassUtil.class));
    }

    @Test
    /**
     * testScanClasses_WithInvalidInput方法。
     */
    public void testScanClasses_WithInvalidInput() {
        // 不存在的包返回空列表
        assertTrue(ClassUtil.scanClasses("com.zifang.util.core.notexist").isEmpty());
        // null/空包名返回空列表
        assertTrue(ClassUtil.scanClasses(null).isEmpty());
        assertTrue(ClassUtil.scanClasses("").isEmpty());
    }
}
