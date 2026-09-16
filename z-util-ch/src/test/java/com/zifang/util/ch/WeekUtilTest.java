package com.zifang.util.ch;

import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * WeekUtilTest类。
 */
public class WeekUtilTest {

    @Test
    /**
     * testGetWeekDay方法。
     */
    public void testGetWeekDay() {
        // 2026-08-22 是周六
        assertEquals("周六", WeekUtil.getWeekDay(LocalDate.of(2026, 8, 22)));
        assertEquals("星期六", WeekUtil.getWeekDay(LocalDate.of(2026, 8, 22), true));
        // 2026-08-17 是周一
        assertEquals("周一", WeekUtil.getWeekDay(LocalDate.of(2026, 8, 17)));
        // 2026-08-23 是周日
        assertEquals("周日", WeekUtil.getWeekDay(LocalDate.of(2026, 8, 23)));
        assertEquals("星期日", WeekUtil.getWeekDay(LocalDate.of(2026, 8, 23), true));
        // null安全
        assertNull(WeekUtil.getWeekDay((LocalDate) null));
        assertNull(WeekUtil.getWeekDay((LocalDate) null, true));
    }

    @Test
    /**
     * testGetWeekDayByDate方法。
     */
    public void testGetWeekDayByDate() {
        Date date = java.sql.Date.valueOf("2026-08-22");
        assertEquals("周六", WeekUtil.getWeekDay(date));
        assertEquals("星期六", WeekUtil.getWeekDay(date, true));
        assertNull(WeekUtil.getWeekDay((Date) null));
    }

    @Test
    /**
     * testGetWeekDayOfToday方法。
     */
    public void testGetWeekDayOfToday() {
        String today = WeekUtil.getWeekDayOfToday();
        assertNotNull(today);
        // 与LocalDate版结果一致
        assertEquals(WeekUtil.getWeekDay(LocalDate.now()), today);
    }

    @Test
    /**
     * testToWeekDayName方法。
     */
    public void testToWeekDayName() {
        assertEquals("周一", WeekUtil.toWeekDayName(1));
        assertEquals("周五", WeekUtil.toWeekDayName(5));
        assertEquals("周日", WeekUtil.toWeekDayName(7));
        assertEquals("星期三", WeekUtil.toWeekDayName(3, true));
        // 越界返回null
        assertNull(WeekUtil.toWeekDayName(0));
        assertNull(WeekUtil.toWeekDayName(8));
        assertNull(WeekUtil.toWeekDayName(-1, true));
    }

    @Test
    /**
     * testToWeekDayValue方法。
     */
    public void testToWeekDayValue() {
        // 兼容三种前缀写法
        assertEquals(1, WeekUtil.toWeekDayValue("周一"));
        assertEquals(1, WeekUtil.toWeekDayValue("星期一"));
        assertEquals(1, WeekUtil.toWeekDayValue("礼拜一"));
        assertEquals(6, WeekUtil.toWeekDayValue("周六"));
        assertEquals(7, WeekUtil.toWeekDayValue("星期日"));
        // 名称与序号互转一致
        for (int i = 1; i <= 7; i++) {
            assertEquals(i, WeekUtil.toWeekDayValue(WeekUtil.toWeekDayName(i)));
        }
        // 无法识别
        assertEquals(WeekUtil.UNRECOGNIZED, WeekUtil.toWeekDayValue("周八"));
        assertEquals(WeekUtil.UNRECOGNIZED, WeekUtil.toWeekDayValue("周"));
        assertEquals(WeekUtil.UNRECOGNIZED, WeekUtil.toWeekDayValue(null));
        assertEquals(WeekUtil.UNRECOGNIZED, WeekUtil.toWeekDayValue("Monday"));
    }

    @Test
    /**
     * testToDayOfWeek方法。
     */
    public void testToDayOfWeek() {
        assertEquals(DayOfWeek.MONDAY, WeekUtil.toDayOfWeek("周一"));
        assertEquals(DayOfWeek.SUNDAY, WeekUtil.toDayOfWeek("星期日"));
        assertNull(WeekUtil.toDayOfWeek("周八"));
        assertNull(WeekUtil.toDayOfWeek(null));
    }
}
