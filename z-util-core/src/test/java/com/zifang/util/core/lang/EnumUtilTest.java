package com.zifang.util.core.lang;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * EnumUtilTest类。
 */
public class EnumUtilTest {

    /**
     * 测试用枚举。
     */
    private enum Color {
        RED(1, "红色"),
        GREEN(2, "绿色"),
        BLUE(3, "蓝色");

        private final int code;
        private final String label;

        Color(int code, String label) {
            this.code = code;
            this.label = label;
        }

        public int getCode() {
            return code;
        }

        public String getLabel() {
            return label;
        }
    }

    @Test
    /**
     * testEnumUtil_ClassExists方法。
     */
    public void testEnumUtil_ClassExists() {
        assertNotNull(EnumUtil.class);
    }

    @Test
    /**
     * testEnumUtil_IsPublicClass方法。
     */
    public void testEnumUtil_IsPublicClass() {
        assertNotNull(EnumUtil.class.getModifiers());
    }

    @Test
    /**
     * testQuery方法。
     */
    public void testQuery() {
        // 按code查校验
        assertEquals(Color.GREEN, EnumUtil.query(Color.class, Color::getCode, 2));
        assertEquals(Color.BLUE, EnumUtil.query(Color.class, Color::getCode, 3));
        // 按label查校验
        assertEquals(Color.RED, EnumUtil.query(Color.class, Color::getLabel, "红色"));
        // 不存在时返回null
        assertNull(EnumUtil.query(Color.class, Color::getCode, 99));
        assertNull(EnumUtil.query(Color.class, Color::getLabel, "黑色"));
        // value为null时返回null
        assertNull(EnumUtil.query(Color.class, Color::getCode, null));
        // 入参null时返回null
        assertNull(EnumUtil.query(null, Color::getCode, 1));
        assertNull(EnumUtil.query(Color.class, null, 1));
    }
}
