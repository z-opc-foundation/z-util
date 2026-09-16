package com.zifang.util.core.lang;

import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

/**
 * ObjectUtilTest类。
 */
public class ObjectUtilTest {

    // --- deepCloneObject ---

    @Test
    /**
     * testDeepCloneObject_WithSerializableObject方法。
     */
    public void testDeepCloneObject_WithSerializableObject() {
        TestSerializable original = new TestSerializable();
        original.setValue("test");
        original.setCount(42);

        TestSerializable cloned = ObjectUtil.deepCloneObject(original);

        assertNotNull(cloned);
        assertEquals(original.getValue(), cloned.getValue());
        assertEquals(original.getCount(), cloned.getCount());
        assertNotSame(original, cloned);
    }

    @Test
    /**
     * testDeepCloneObject_ValuesAreIndependent方法。
     */
    public void testDeepCloneObject_ValuesAreIndependent() {
        TestSerializable original = new TestSerializable();
        original.setValue("original");

        TestSerializable cloned = ObjectUtil.deepCloneObject(original);
        cloned.setValue("modified");

        assertEquals("original", original.getValue());
        assertEquals("modified", cloned.getValue());
    }

    @Test
    /**
     * testDeepCloneObject_WithNull方法。
     */
    public void testDeepCloneObject_WithNull() {
        String result = ObjectUtil.deepCloneObject(null);
        assertNull(result);
    }

    @Test
    /**
     * testDeepCloneObject_WithString方法。
     */
    public void testDeepCloneObject_WithString() {
        String original = "test";
        String cloned = ObjectUtil.deepCloneObject(original);
        assertEquals(original, cloned);
        assertNotSame(original, cloned);
    }

    // --- deepCloneCollection ---

    @Test
    /**
     * testDeepCloneCollection_WithList方法。
     */
    public void testDeepCloneCollection_WithList() throws Exception {
        List<String> original = new ArrayList<>();
        original.add("a");
        original.add("b");
        original.add("c");

        Collection<String> cloned = ObjectUtil.deepCloneCollection(original);

        assertNotNull(cloned);
        assertEquals(original.size(), cloned.size());
        assertEquals(original, cloned);
        assertNotSame(original, cloned);
    }

    @Test
    /**
     * testDeepCloneCollection_ValuesAreIndependent方法。
     */
    public void testDeepCloneCollection_ValuesAreIndependent() throws Exception {
        List<String> original = new ArrayList<>();
        original.add("original");

        Collection<String> cloned = ObjectUtil.deepCloneCollection(original);
        List<String> clonedList = new ArrayList<>(cloned);
        clonedList.set(0, "modified");

        assertEquals("original", original.get(0));
        assertEquals("modified", clonedList.get(0));
    }

    @Test
    /**
     * testDeepCloneCollection_WithEmptyList方法。
     */
    public void testDeepCloneCollection_WithEmptyList() throws Exception {
        List<String> original = new ArrayList<>();
        Collection<String> cloned = ObjectUtil.deepCloneCollection(original);

        assertNotNull(cloned);
        assertTrue(cloned.isEmpty());
        assertNotSame(original, cloned);
    }

    @Test
    /**
     * testDeepCloneCollection_WithArraysAsList方法。
     */
    public void testDeepCloneCollection_WithArraysAsList() throws Exception {
        List<Integer> original = Arrays.asList(1, 2, 3);
        Collection<Integer> cloned = ObjectUtil.deepCloneCollection(original);

        assertNotNull(cloned);
        assertEquals(original, cloned);
    }

    // --- isEmpty / isNotEmpty ---

    @Test
    /**
     * testIsEmpty_WithVariousTypes方法。
     */
    public void testIsEmpty_WithVariousTypes() {
        assertTrue(ObjectUtil.isEmpty(null));
        assertTrue(ObjectUtil.isEmpty(""));
        assertTrue(ObjectUtil.isEmpty(new ArrayList<String>()));
        assertTrue(ObjectUtil.isEmpty(new java.util.HashMap<String, String>()));
        assertTrue(ObjectUtil.isEmpty(new int[0]));
        // 非空输入
        assertFalse(ObjectUtil.isEmpty("a"));
        assertFalse(ObjectUtil.isEmpty(Arrays.asList(1)));
        assertFalse(ObjectUtil.isEmpty(new int[]{1}));
        // 非集合类型仅判断null
        assertFalse(ObjectUtil.isEmpty(0));
        // isNotEmpty取反
        assertTrue(ObjectUtil.isNotEmpty("a"));
        assertFalse(ObjectUtil.isNotEmpty(""));
    }

    @Test
    /**
     * testIsAllEmpty_WithVariousArgs方法。
     */
    public void testIsAllEmpty_WithVariousArgs() {
        assertTrue(ObjectUtil.isAllEmpty());
        assertTrue(ObjectUtil.isAllEmpty(null, "", new ArrayList<String>()));
        assertFalse(ObjectUtil.isAllEmpty(null, "a"));
        assertFalse(ObjectUtil.isAllEmpty("a", "b"));
    }

    @Test
    /**
     * testIsAllNotEmpty_WithVariousArgs方法。
     */
    public void testIsAllNotEmpty_WithVariousArgs() {
        assertFalse(ObjectUtil.isAllNotEmpty());
        assertTrue(ObjectUtil.isAllNotEmpty("a", Arrays.asList(1), new int[]{1}));
        assertFalse(ObjectUtil.isAllNotEmpty("a", null));
        assertFalse(ObjectUtil.isAllNotEmpty("a", ""));
    }

    @Test
    /**
     * testEqualsAny方法。
     */
    public void testEqualsAny() {
        // 匹配任一候选
        assertTrue(ObjectUtil.equalsAny(2, 1, 2, 3));
        // 不匹配
        assertFalse(ObjectUtil.equalsAny(5, 1, 2, 3));
        // null与null相等
        assertTrue(ObjectUtil.equalsAny(null, null, 1));
        // 目标null不匹配非null候选
        assertFalse(ObjectUtil.equalsAny(null, 1, 2));
        // 候选为null或空
        assertFalse(ObjectUtil.equalsAny(1));
        assertFalse(ObjectUtil.equalsAny(1, (Integer[]) null));
        // 字符串
        assertTrue(ObjectUtil.equalsAny("b", "a", "b"));
    }

    @Test
    /**
     * testGetIfNotNull方法。
     */
    public void testGetIfNotNull() {
        // 对象非null时执行取值函数
        assertEquals(Integer.valueOf(3), ObjectUtil.getIfNotNull("abc", String::length));
        // 对象为null时返回null
        assertNull(ObjectUtil.getIfNotNull((String) null, String::length));
        // 对象为null时返回默认值
        assertEquals(Integer.valueOf(0), ObjectUtil.getIfNotNull((String) null, String::length, 0));
        // 对象非null时默认值不生效
        assertEquals(Integer.valueOf(3), ObjectUtil.getIfNotNull("abc", String::length, 0));
    }

    // Helper class for testing
    static class TestSerializable implements Serializable {
        private static final long serialVersionUID = 1L;
        private String value;
        private int count;

        /**
         * getValue方法。
         *
         * @return String类型返回值
         */
        public String getValue() {
            return value;
        }

        /**
         * setValue方法。
         * * @param value String类型参数
         */
        public void setValue(String value) {
            this.value = value;
        }

        /**
         * getCount方法。
         *
         * @return int类型返回值
         */
        public int getCount() {
            return count;
        }

        /**
         * setCount方法。
         * * @param count int类型参数
         */
        public void setCount(int count) {
            this.count = count;
        }
    }
}
