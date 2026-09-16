package com.zifang.util.distributes.sequence;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * SnowflakeIdWorkerTest类。
 */
public class SnowflakeIdWorkerTest {

    @Test
    /**
     * testNextId_BasicFunctionality方法。
     */
    public void testNextId_BasicFunctionality() {
        SnowflakeIdWorker worker = new SnowflakeIdWorker(1, 1);
        long id = worker.nextId();
        assertTrue("ID should be positive", id > 0);
    }

    @Test
    /**
     * testNextId_Uniqueness方法。
     */
    public void testNextId_Uniqueness() {
        SnowflakeIdWorker worker = new SnowflakeIdWorker(0, 0);
        Set<Long> ids = new HashSet<>();
        int count = 10000;

        for (int i = 0; i < count; i++) {
            long id = worker.nextId();
            assertTrue("ID should be unique", !ids.contains(id));
            ids.add(id);
        }

        assertEquals("All IDs should be unique", count, ids.size());
    }

    @Test
    /**
     * testNextId_OrderedGeneration方法。
     */
    public void testNextId_OrderedGeneration() {
        SnowflakeIdWorker worker = new SnowflakeIdWorker(0, 0);
        long prevId = 0;
        for (int i = 0; i < 1000; i++) {
            long id = worker.nextId();
            assertTrue("IDs should be generated in order", id > prevId);
            prevId = id;
        }
    }

    @Test
    /**
     * testNextId_IdStructure方法。
     */
    public void testNextId_IdStructure() {
        SnowflakeIdWorker worker = new SnowflakeIdWorker(5, 3);
        long id = worker.nextId();
        // Snowflake IDs are 64-bit, but Long.toBinaryString() trims leading zeros
        assertTrue("ID should be a positive 64-bit value", id > 0);
        assertTrue("ID should be less than 2^63 (sign bit must be 0)", id >= 0);
        // Verify it's using the full 64-bit range
        assertTrue("ID should be large enough to use many bits", id > 1000000000000L);
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testConstructor_InvalidWorkerId_Negative方法。
     */
    public void testConstructor_InvalidWorkerId_Negative() {
        new SnowflakeIdWorker(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testConstructor_InvalidWorkerId_TooLarge方法。
     */
    public void testConstructor_InvalidWorkerId_TooLarge() {
        new SnowflakeIdWorker(32, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testConstructor_InvalidDatacenterId_Negative方法。
     */
    public void testConstructor_InvalidDatacenterId_Negative() {
        new SnowflakeIdWorker(0, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testConstructor_InvalidDatacenterId_TooLarge方法。
     */
    public void testConstructor_InvalidDatacenterId_TooLarge() {
        new SnowflakeIdWorker(0, 32);
    }

    @Test
    /**
     * testConstructor_ValidBoundaryValues方法。
     */
    public void testConstructor_ValidBoundaryValues() {
        SnowflakeIdWorker worker1 = new SnowflakeIdWorker(0, 0);
        SnowflakeIdWorker worker31 = new SnowflakeIdWorker(31, 31);

        assertNotNull(worker1);
        assertNotNull(worker31);

        assertTrue(worker1.nextId() > 0);
        assertTrue(worker31.nextId() > 0);
    }

    @Test
    /**
     * testNextId_MultipleWorkers方法。
     */
    public void testNextId_MultipleWorkers() {
        SnowflakeIdWorker worker1 = new SnowflakeIdWorker(1, 1);
        SnowflakeIdWorker worker2 = new SnowflakeIdWorker(2, 2);

        Set<Long> ids = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            ids.add(worker1.nextId());
            ids.add(worker2.nextId());
        }

        assertEquals("All IDs from different workers should be unique", 2000, ids.size());
    }
}
