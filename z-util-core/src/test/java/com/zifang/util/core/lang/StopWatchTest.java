package com.zifang.util.core.lang;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * StopWatch 单元测试。
 */
public class StopWatchTest {

    @Test
    public void testStartStopSingleTask() throws InterruptedException {
        StopWatch sw = new StopWatch("test");
        sw.start("task1");
        Thread.sleep(50);
        sw.stop();

        assertEquals(1, sw.getTaskInfo().length);
        assertEquals("task1", sw.getTaskInfo()[0].getTaskName());
        assertTrue(sw.getTaskInfo()[0].getTimeMillis() >= 50);
        assertFalse(sw.isRunning());
    }

    @Test
    public void testMultipleTasks() throws InterruptedException {
        StopWatch sw = new StopWatch();
        sw.start("a");
        Thread.sleep(20);
        sw.stop();
        sw.start("b");
        Thread.sleep(30);
        sw.stop();

        assertEquals(2, sw.getTaskInfo().length);
        assertTrue(sw.getTotalTimeMillis() >= 50);
    }

    @Test(expected = IllegalStateException.class)
    public void testDoubleStartThrows() {
        StopWatch sw = new StopWatch();
        sw.start("a");
        sw.start("b");
    }

    @Test(expected = IllegalStateException.class)
    public void testStopWithoutStartThrows() {
        new StopWatch().stop();
    }

    @Test
    public void testPrettyPrint() throws InterruptedException {
        StopWatch sw = new StopWatch("id");
        sw.start("x");
        Thread.sleep(10);
        sw.stop();
        String out = sw.prettyPrint();
        assertNotNull(out);
        assertTrue(out.contains("x"));
        assertTrue(out.contains("id"));
    }
}