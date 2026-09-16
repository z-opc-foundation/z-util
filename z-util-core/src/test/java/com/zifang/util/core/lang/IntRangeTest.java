package com.zifang.util.core.lang;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * IntRangeTest类。
 * 整数区间校验与描述测试。
 */
public class IntRangeTest {

    /**
     * testContainsClosedInterval方法：闭区间含边界值。
     */
    @Test
    public void testContainsClosedInterval() {
        IntRange range = IntRange.of(1, 5);
        assertTrue(range.contains(1));
        assertTrue(range.contains(3));
        assertTrue(range.contains(5));
        assertFalse(range.contains(0));
        assertFalse(range.contains(6));
    }

    /**
     * testContainsOpenBoundaries方法：null 边界表示该侧不限制。
     */
    @Test
    public void testContainsOpenBoundaries() {
        IntRange upperOnly = IntRange.of(null, 10);
        assertTrue(upperOnly.contains(-100));
        assertTrue(upperOnly.contains(10));
        assertFalse(upperOnly.contains(11));

        IntRange lowerOnly = IntRange.of(5, null);
        assertTrue(lowerOnly.contains(5));
        assertTrue(lowerOnly.contains(Integer.MAX_VALUE));
        assertFalse(lowerOnly.contains(4));

        IntRange unbounded = IntRange.of(null, null);
        assertTrue(unbounded.contains(Integer.MIN_VALUE));
        assertTrue(unbounded.contains(0));
        assertTrue(unbounded.contains(Integer.MAX_VALUE));
    }

    /**
     * testContainsInvalidRange方法：下限大于上限的区间对任何值都不命中。
     */
    @Test
    public void testContainsInvalidRange() {
        IntRange range = IntRange.of(10, 1);
        assertFalse(range.contains(1));
        assertFalse(range.contains(5));
        assertFalse(range.contains(10));
    }

    /**
     * testDescribe方法：单段描述各形态。
     */
    @Test
    public void testDescribe() {
        assertEquals("20", IntRange.of(20, 20).describe());
        assertEquals("45-55", IntRange.of(45, 55).describe());
        assertEquals("45-", IntRange.of(45, null).describe());
        assertEquals("-55", IntRange.of(null, 55).describe());
        assertEquals("", IntRange.of(null, null).describe());
    }

    /**
     * testMatchesAny方法：多段区间任一命中即通过，空配置不校验，null 值不通过。
     */
    @Test
    public void testMatchesAny() {
        List<IntRange> ranges = Arrays.asList(
                IntRange.of(20, 20),
                IntRange.of(45, 55),
                null);

        assertTrue(IntRange.matchesAny(null, 99));
        assertTrue(IntRange.matchesAny(Collections.<IntRange>emptyList(), 99));
        assertTrue(IntRange.matchesAny(ranges, 20));
        assertTrue(IntRange.matchesAny(ranges, 45));
        assertTrue(IntRange.matchesAny(ranges, 55));
        assertFalse(IntRange.matchesAny(ranges, 30));
        assertFalse(IntRange.matchesAny(ranges, 56));
        assertFalse(IntRange.matchesAny(ranges, null));
    }

    /**
     * testDescribeAll方法：多段描述以 " / " 拼接，跳过空描述段。
     */
    @Test
    public void testDescribeAll() {
        List<IntRange> ranges = Arrays.asList(
                IntRange.of(20, 20),
                IntRange.of(45, 55),
                IntRange.of(45, null),
                IntRange.of(null, 55),
                IntRange.of(null, null));
        assertEquals("20 / 45-55 / 45- / -55", IntRange.describeAll(ranges));
        assertEquals("", IntRange.describeAll(null));
        assertEquals("", IntRange.describeAll(Collections.<IntRange>emptyList()));
    }

    /**
     * testToStringAndGetters方法：toString 返回单段描述，getter 返回边界。
     */
    @Test
    public void testToStringAndGetters() {
        IntRange range = IntRange.of(45, 55);
        assertEquals("45-55", range.toString());
        assertEquals(Integer.valueOf(45), range.getMin());
        assertEquals(Integer.valueOf(55), range.getMax());
        assertEquals(Integer.valueOf(45), IntRange.of(45, null).getMin());
        assertEquals(null, IntRange.of(45, null).getMax());
    }
}
