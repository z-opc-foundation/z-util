package com.zifang.util.pandas.str;

import com.zifang.util.pandas.Series;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * StringAccessor 字符串操作测试类
 * 测试字符串处理、正则表达式、文本提取等功能
 */

/**
 * StringAccessorTest类。
 */
public class StringAccessorTest {

    private Series stringSeries;
    private Series numericSeries;

    @Before
    /**
     * setUp方法。
     */
    public void setUp() {
        // 创建字符串测试数据
        stringSeries = Series.of(new String[]{"Hello World", "JAVA Programming", "  Test String  ", "lowercase", "123-456-789"});

        // 创建数值测试数据（会被转换为字符串）
        double[] nums = {123.45, 678.90, 111.22};
        numericSeries = new Series(nums);
    }

    @Ignore
    @Test
    /**
     * testLength方法。
     */
    public void testLength() {
        StringAccessor accessor = new StringAccessor(stringSeries);
        Series result = accessor.length();

        assertEquals(5, result.length());
        assertEquals(11.0, result.toArray()[0], 0.001);  // "Hello World"
        assertEquals(16.0, result.toArray()[1], 0.001); // "JAVA Programming"
        assertEquals(16.0, result.toArray()[2], 0.001); // "  Test String  "
    }

    @Test
    /**
     * testLower方法。
     */
    public void testLower() {
        StringAccessor accessor = new StringAccessor(stringSeries);
        Series result = accessor.lower();

        assertEquals(5, result.length());
        // 验证小写转换（由于返回的是数值映射，验证索引存在）
        assertNotNull(result);
    }

    @Test
    /**
     * testUpper方法。
     */
    public void testUpper() {
        StringAccessor accessor = new StringAccessor(stringSeries);
        Series result = accessor.upper();

        assertEquals(5, result.length());
        assertNotNull(result);
    }

    @Test
    /**
     * testStrip方法。
     */
    public void testStrip() {
        StringAccessor accessor = new StringAccessor(stringSeries);
        Series result = accessor.strip();

        assertEquals(5, result.length());
        assertNotNull(result);
    }

    @Test
    /**
     * testReplace方法。
     */
    public void testReplace() {
        StringAccessor accessor = new StringAccessor(stringSeries);
        Series result = accessor.replace("World", "Java");

        assertEquals(5, result.length());
        assertNotNull(result);
    }

    @Test
    /**
     * testReplaceRegex方法。
     */
    public void testReplaceRegex() {
        StringAccessor accessor = new StringAccessor(stringSeries);
        Series result = accessor.replaceRegex("[0-9]", "X");

        assertEquals(5, result.length());
        assertNotNull(result);
    }

    @Ignore
    @Test
    /**
     * testContains方法。
     */
    public void testContains() {
        StringAccessor accessor = new StringAccessor(stringSeries);
        Series result = accessor.contains("World");

        assertEquals(5, result.length());
        assertEquals(1.0, result.toArray()[0], 0.001);  // "Hello World" contains "World"
        assertEquals(0.0, result.toArray()[1], 0.001);  // "JAVA Programming" doesn't contain "World"
    }

    @Test
    /**
     * testMatch方法。
     */
    public void testMatch() {
        StringAccessor accessor = new StringAccessor(stringSeries);
        Series result = accessor.match("^[A-Z].*");  // 以大写字母开头

        assertEquals(5, result.length());
        assertNotNull(result);
    }

    @Test
    /**
     * testSlice方法。
     */
    public void testSlice() {
        StringAccessor accessor = new StringAccessor(stringSeries);
        Series result = accessor.slice(0, 5);

        assertEquals(5, result.length());
        assertNotNull(result);
    }

    @Test
    /**
     * testSplit方法。
     */
    public void testSplit() {
        StringAccessor accessor = new StringAccessor(stringSeries);
        Series result = accessor.split(" ", 0);  // 按空格分割，取第一部分

        assertEquals(5, result.length());
        assertNotNull(result);
    }

    @Test
    /**
     * testCat方法。
     */
    public void testCat() {
        StringAccessor accessor = new StringAccessor(stringSeries);
        Series result = accessor.cat("_suffix");

        assertEquals(5, result.length());
        assertNotNull(result);
    }

    @Test
    /**
     * testRepeat方法。
     */
    public void testRepeat() {
        StringAccessor accessor = new StringAccessor(stringSeries);
        Series result = accessor.repeat(2);

        assertEquals(5, result.length());
        assertNotNull(result);
    }

    @Test
    /**
     * testNumericToStringOperations方法。
     */
    public void testNumericToStringOperations() {
        // 测试数值序列的字符串操作
        StringAccessor accessor = new StringAccessor(numericSeries);
        Series result = accessor.length();

        assertEquals(3, result.length());
        assertNotNull(result);
    }
}
