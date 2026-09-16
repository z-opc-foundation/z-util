package com.zifang.util.monitor.thread;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * StatusTest类。
 */
public class StatusTest {

    private Status status;

    @Before
    /**
     * setUp方法。
     */
    public void setUp() {
        status = new Status();
    }

    @Test
    /**
     * testDefaultStatus方法。
     */
    public void testDefaultStatus() {
        assertNull(status.getStatus());
        assertNull(status.getLevel());
    }

    @Test
    /**
     * testSetAndGetStatus方法。
     */
    public void testSetAndGetStatus() {
        String expectedStatus = "OK";
        status.setStatus(expectedStatus);
        assertEquals(expectedStatus, status.getStatus());
    }

    @Test
    /**
     * testSetAndGetLevel方法。
     */
    public void testSetAndGetLevel() {
        status.setLevel(StatusLevel.OK);
        assertEquals(StatusLevel.OK, status.getLevel());

        status.setLevel(StatusLevel.ERROR);
        assertEquals(StatusLevel.ERROR, status.getLevel());
    }

    @Test
    /**
     * testToString方法。
     */
    public void testToString() {
        status.setStatus("TestStatus");
        status.setLevel(StatusLevel.OK);
        String result = status.toString();
        assertTrue(result.contains("TestStatus"));
        assertTrue(result.contains("OK"));
    }
}
