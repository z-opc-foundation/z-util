package com.zifang.util.monitor.thread;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * StatusLevelTest类。
 */
public class StatusLevelTest {

    @Test
    /**
     * testStatusLevelValues方法。
     */
    public void testStatusLevelValues() {
        assertNotNull(StatusLevel.OK);
        assertNotNull(StatusLevel.ERROR);
    }

    @Test
    /**
     * testStatusLevelCount方法。
     */
    public void testStatusLevelCount() {
        assertEquals(2, StatusLevel.values().length);
    }

    @Test
    /**
     * testStatusLevelOrdinal方法。
     */
    public void testStatusLevelOrdinal() {
        assertEquals(0, StatusLevel.OK.ordinal());
        assertEquals(1, StatusLevel.ERROR.ordinal());
    }
}
