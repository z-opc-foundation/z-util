package com.zifang.util.core.time;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.*;

/**
 * LocalDateTimeUtilTest类。
 */
public class LocalDateTimeUtilTest {

    @Test
    /**
     * testDayStart方法。
     */
    public void testDayStart() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 8, 22, 15, 30, 45, 123);
        assertEquals(LocalDateTime.of(2026, 8, 22, 0, 0, 0),
                LocalDateTimeUtil.dayStart(dateTime));
        // 已经是0点时原样返回
        assertEquals(LocalDateTime.of(2026, 8, 22, 0, 0, 0),
                LocalDateTimeUtil.dayStart(LocalDateTime.of(2026, 8, 22, 0, 0, 0)));
        // null返回null
        assertNull(LocalDateTimeUtil.dayStart(null));
    }

    @Test
    /**
     * testDayEnd方法。
     */
    public void testDayEnd() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 8, 22, 15, 30, 45, 123);
        assertEquals(LocalDateTime.of(2026, 8, 22, 23, 59, 59, 999999999),
                LocalDateTimeUtil.dayEnd(dateTime));
        // null返回null
        assertNull(LocalDateTimeUtil.dayEnd(null));
    }

    @Test
    /**
     * testMinuteStart方法。
     */
    public void testMinuteStart() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 8, 22, 15, 30, 45, 123);
        assertEquals(LocalDateTime.of(2026, 8, 22, 15, 30, 0),
                LocalDateTimeUtil.minuteStart(dateTime));
        // null返回null
        assertNull(LocalDateTimeUtil.minuteStart(null));
    }

    @Test
    /**
     * testMinuteEnd方法。
     */
    public void testMinuteEnd() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 8, 22, 15, 30, 45, 123);
        // 秒置为59，纳秒归零
        assertEquals(LocalDateTime.of(2026, 8, 22, 15, 30, 59, 0),
                LocalDateTimeUtil.minuteEnd(dateTime));
        // null返回null
        assertNull(LocalDateTimeUtil.minuteEnd(null));
    }

    @Test
    /**
     * testQuarterStart方法。
     */
    public void testQuarterStart() {
        // Q1
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0, 0),
                LocalDateTimeUtil.quarterStart(LocalDateTime.of(2026, 2, 15, 10, 30)));
        // Q3
        assertEquals(LocalDateTime.of(2026, 7, 1, 0, 0, 0),
                LocalDateTimeUtil.quarterStart(LocalDateTime.of(2026, 8, 22, 10, 30)));
        // Q4边界
        assertEquals(LocalDateTime.of(2026, 10, 1, 0, 0, 0),
                LocalDateTimeUtil.quarterStart(LocalDateTime.of(2026, 12, 31, 23, 59)));
        // null返回null
        assertNull(LocalDateTimeUtil.quarterStart(null));
    }

    @Test
    /**
     * testQuarterEnd方法。
     */
    public void testQuarterEnd() {
        // Q1结束3月31日
        assertEquals(LocalDateTime.of(2026, 3, 31, 23, 59, 59, 999999999),
                LocalDateTimeUtil.quarterEnd(LocalDateTime.of(2026, 2, 15, 10, 30)));
        // Q3结束9月30日
        assertEquals(LocalDateTime.of(2026, 9, 30, 23, 59, 59, 999999999),
                LocalDateTimeUtil.quarterEnd(LocalDateTime.of(2026, 8, 22, 10, 30)));
        // Q4结束12月31日
        assertEquals(LocalDateTime.of(2026, 12, 31, 23, 59, 59, 999999999),
                LocalDateTimeUtil.quarterEnd(LocalDateTime.of(2026, 12, 31, 23, 59)));
        // null返回null
        assertNull(LocalDateTimeUtil.quarterEnd(null));
    }

    @Test
    /**
     * testHalfYearStart方法。
     */
    public void testHalfYearStart() {
        // 上半年
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0, 0),
                LocalDateTimeUtil.halfYearStart(LocalDateTime.of(2026, 3, 15, 10, 30)));
        // 下半年（7月及以后）
        assertEquals(LocalDateTime.of(2026, 7, 1, 0, 0, 0),
                LocalDateTimeUtil.halfYearStart(LocalDateTime.of(2026, 7, 1, 0, 0)));
        // 6月30日仍属上半年
        assertEquals(LocalDateTime.of(2026, 1, 1, 0, 0, 0),
                LocalDateTimeUtil.halfYearStart(LocalDateTime.of(2026, 6, 30, 23, 59)));
        // null返回null
        assertNull(LocalDateTimeUtil.halfYearStart(null));
    }

    @Test
    /**
     * testHalfYearEnd方法。
     */
    public void testHalfYearEnd() {
        // 上半年结束6月30日
        assertEquals(LocalDateTime.of(2026, 6, 30, 23, 59, 59, 999999999),
                LocalDateTimeUtil.halfYearEnd(LocalDateTime.of(2026, 3, 15, 10, 30)));
        // 下半年结束12月31日
        assertEquals(LocalDateTime.of(2026, 12, 31, 23, 59, 59, 999999999),
                LocalDateTimeUtil.halfYearEnd(LocalDateTime.of(2026, 8, 22, 10, 30)));
        // 闰年上半年结束仍为6月30日
        assertEquals(LocalDateTime.of(2024, 6, 30, 23, 59, 59, 999999999),
                LocalDateTimeUtil.halfYearEnd(LocalDateTime.of(2024, 2, 29, 10, 30)));
        // null返回null
        assertNull(LocalDateTimeUtil.halfYearEnd(null));
    }

    @Test
    /**
     * testListDateTimesBetween方法。
     */
    public void testListDateTimesBetween() {
        LocalDateTime start = LocalDateTime.of(2026, 8, 20, 10, 30);
        LocalDateTime end = LocalDateTime.of(2026, 8, 23, 10, 30);
        List<LocalDateTime> result = LocalDateTimeUtil.listDateTimesBetween(start, end);
        // 按天步进含两端：4个时间点，每天同一时刻
        assertEquals(4, result.size());
        assertEquals(LocalDateTime.of(2026, 8, 20, 10, 30), result.get(0));
        assertEquals(LocalDateTime.of(2026, 8, 21, 10, 30), result.get(1));
        assertEquals(LocalDateTime.of(2026, 8, 23, 10, 30), result.get(3));
        // start等于end时返回单个元素
        List<LocalDateTime> single = LocalDateTimeUtil.listDateTimesBetween(start, start);
        assertEquals(1, single.size());
        assertEquals(start, single.get(0));
    }

    @Test
    /**
     * testListDateTimesBetween_InvalidInput方法。
     */
    public void testListDateTimesBetween_InvalidInput() {
        LocalDateTime start = LocalDateTime.of(2026, 8, 22, 10, 30);
        LocalDateTime end = LocalDateTime.of(2026, 8, 23, 10, 30);
        // start晚于end返回空列表
        assertTrue(LocalDateTimeUtil.listDateTimesBetween(end, start).isEmpty());
        // null返回空列表
        assertTrue(LocalDateTimeUtil.listDateTimesBetween(null, end).isEmpty());
        assertTrue(LocalDateTimeUtil.listDateTimesBetween(start, null).isEmpty());
        assertTrue(LocalDateTimeUtil.listDateTimesBetween(null, null).isEmpty());
    }
}
