package com.zifang.util.core.lang.converter;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * ConvertersTest类。
 */
public class ConvertersTest {

    @Test
    /**
     * testCastList方法。
     */
    public void testCastList() {
        List<Object> raw = new ArrayList<>();
        raw.add("a");
        raw.add("b");
        List<String> result = Converters.castList(raw, String.class);
        // 逐元素转换保持顺序
        assertEquals(2, result.size());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
    }

    @Test
    /**
     * testCastList_NotListOrEmpty方法。
     */
    public void testCastList_NotListOrEmpty() {
        // 非List对象返回空列表
        assertTrue(Converters.castList("not a list", String.class).isEmpty());
        assertTrue(Converters.castList(null, String.class).isEmpty());
        // 空列表返回空列表
        assertTrue(Converters.castList(new ArrayList<>(), String.class).isEmpty());
    }

    @Test
    /**
     * testCastList_IncompatibleElement方法。
     */
    public void testCastList_IncompatibleElement() {
        List<Object> raw = new ArrayList<>();
        raw.add("a");
        raw.add(123);
        boolean thrown = false;
        try {
            Converters.castList(raw, String.class);
        } catch (ClassCastException e) {
            thrown = true;
        }
        // 元素类型不兼容时抛出ClassCastException
        assertTrue(thrown);
    }
}
