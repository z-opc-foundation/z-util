package com.zifang.util.core.time;

import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

/**
 * LocalDateUtilTest类。
 */
public class LocalDateUtilTest {

    @Test
    /**
     * testListDatesBetween_NormalRange方法。
     */
    public void testListDatesBetween_NormalRange() {
        LocalDate start = LocalDate.of(2026, 8, 20);
        LocalDate end = LocalDate.of(2026, 8, 24);
        List<LocalDate> dates = LocalDateUtil.listDatesBetween(start, end);
        assertEquals(5, dates.size());
        assertEquals(LocalDate.of(2026, 8, 20), dates.get(0));
        assertEquals(LocalDate.of(2026, 8, 24), dates.get(4));
    }

    @Test
    /**
     * testListDatesBetween_SameDay方法。
     */
    public void testListDatesBetween_SameDay() {
        LocalDate day = LocalDate.of(2026, 8, 20);
        List<LocalDate> dates = LocalDateUtil.listDatesBetween(day, day);
        assertEquals(1, dates.size());
        assertEquals(day, dates.get(0));
    }

    @Test
    /**
     * testListDatesBetween_CrossMonth方法。
     */
    public void testListDatesBetween_CrossMonth() {
        List<LocalDate> dates = LocalDateUtil.listDatesBetween(
                LocalDate.of(2026, 7, 30), LocalDate.of(2026, 8, 2));
        assertEquals(4, dates.size());
        assertEquals(LocalDate.of(2026, 7, 31), dates.get(1));
        assertEquals(LocalDate.of(2026, 8, 1), dates.get(2));
    }

    @Test
    /**
     * testListDatesBetween_InvalidInput方法。
     */
    public void testListDatesBetween_InvalidInput() {
        // start晚于end返回空列表
        assertTrue(LocalDateUtil.listDatesBetween(
                LocalDate.of(2026, 8, 2), LocalDate.of(2026, 8, 1)).isEmpty());
        // null返回空列表
        assertTrue(LocalDateUtil.listDatesBetween(null, LocalDate.of(2026, 8, 1)).isEmpty());
        assertTrue(LocalDateUtil.listDatesBetween(LocalDate.of(2026, 8, 1), null).isEmpty());
        assertTrue(LocalDateUtil.listDatesBetween(null, null).isEmpty());
    }

    @Test
    /**
     * testListDatesBetween_Order方法。
     */
    public void testListDatesBetween_Order() {
        List<LocalDate> dates = LocalDateUtil.listDatesBetween(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 5));
        for (int i = 1; i < dates.size(); i++) {
            assertTrue(dates.get(i).isAfter(dates.get(i - 1)));
        }
    }

    @Test
    /**
     * testAge方法。
     */
    public void testAge() {
        // 整周岁
        assertEquals(30, LocalDateUtil.age(
                LocalDate.of(1996, 1, 1), LocalDate.of(2026, 1, 1)));
        // 未到生日差一天少一岁
        assertEquals(29, LocalDateUtil.age(
                LocalDate.of(1996, 1, 2), LocalDate.of(2026, 1, 1)));
        // 生日当天
        assertEquals(30, LocalDateUtil.age(
                LocalDate.of(1996, 8, 22), LocalDate.of(2026, 8, 22)));
        // 闰年生日非周年日时少一岁，周年日当天为整岁
        assertEquals(3, LocalDateUtil.age(
                LocalDate.of(2000, 2, 29), LocalDate.of(2004, 2, 28)));
        assertEquals(4, LocalDateUtil.age(
                LocalDate.of(2000, 2, 29), LocalDate.of(2004, 2, 29)));
        // 出生同一天为0岁
        assertEquals(0, LocalDateUtil.age(
                LocalDate.of(2026, 8, 22), LocalDate.of(2026, 8, 22)));
    }

    @Test
    /**
     * testAge_InvalidInput方法。
     */
    public void testAge_InvalidInput() {
        // 任一为null返回0
        assertEquals(0, LocalDateUtil.age(null, LocalDate.of(2026, 1, 1)));
        assertEquals(0, LocalDateUtil.age(LocalDate.of(2026, 1, 1), null));
        assertEquals(0, LocalDateUtil.age((LocalDate) null));
        // 出生日期晚于参照日期返回0
        assertEquals(0, LocalDateUtil.age(
                LocalDate.of(2027, 1, 1), LocalDate.of(2026, 1, 1)));
    }

    @Test
    /**
     * testAge_UntilNow方法。
     */
    public void testAge_UntilNow() {
        LocalDate birth = LocalDate.of(2000, 1, 1);
        int expected = (int) birth.until(LocalDate.now(), java.time.temporal.ChronoUnit.YEARS);
        assertEquals(expected, LocalDateUtil.age(birth));
    }
}
