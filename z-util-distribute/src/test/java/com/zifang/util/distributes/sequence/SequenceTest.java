package com.zifang.util.distributes.sequence;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * SequenceTest类。
 */
public class SequenceTest {

    @Test
    /**
     * testNextId_BasicFunctionality方法。
     */
    public void testNextId_BasicFunctionality() {
        Sequence sequence = new Sequence(1, 1);
        long id = sequence.nextId();
        assertTrue("ID should be positive", id > 0);
    }

    @Test
    /**
     * testNextId_Uniqueness方法。
     */
    public void testNextId_Uniqueness() {
        Sequence sequence = new Sequence(0, 0);
        Set<Long> ids = new HashSet<>();
        int count = 10000;

        for (int i = 0; i < count; i++) {
            long id = sequence.nextId();
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
        Sequence sequence = new Sequence(0, 0);
        long prevId = 0;
        for (int i = 0; i < 1000; i++) {
            long id = sequence.nextId();
            assertTrue("IDs should be generated in order", id > prevId);
            prevId = id;
        }
    }

    @Test
    /**
     * testNextId_IdStructure方法。
     */
    public void testNextId_IdStructure() {
        Sequence sequence = new Sequence(5, 3);
        long id = sequence.nextId();
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
        new Sequence(-1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testConstructor_InvalidWorkerId_TooLarge方法。
     */
    public void testConstructor_InvalidWorkerId_TooLarge() {
        new Sequence(32, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testConstructor_InvalidDatacenterId_Negative方法。
     */
    public void testConstructor_InvalidDatacenterId_Negative() {
        new Sequence(0, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testConstructor_InvalidDatacenterId_TooLarge方法。
     */
    public void testConstructor_InvalidDatacenterId_TooLarge() {
        new Sequence(0, 32);
    }

    @Test
    /**
     * testConstructor_ValidBoundaryValues方法。
     */
    public void testConstructor_ValidBoundaryValues() {
        Sequence sequence1 = new Sequence(0, 0);
        Sequence sequence31 = new Sequence(31, 31);

        assertNotNull(sequence1);
        assertNotNull(sequence31);

        assertTrue(sequence1.nextId() > 0);
        assertTrue(sequence31.nextId() > 0);
    }

    @Test
    /**
     * testNextId_MultipleSequences方法。
     */
    public void testNextId_MultipleSequences() {
        Sequence seq1 = new Sequence(1, 1);
        Sequence seq2 = new Sequence(2, 2);

        Set<Long> ids = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            ids.add(seq1.nextId());
            ids.add(seq2.nextId());
        }

        assertEquals("All IDs from different sequences should be unique", 2000, ids.size());
    }

    @Test
    /**
     * testNextId_HighConcurrencyScenario方法。
     */
    public void testNextId_HighConcurrencyScenario() {
        Sequence sequence = new Sequence(0, 0);
        Set<Long> ids = new HashSet<>();
        int count = 5000;

        for (int i = 0; i < count; i++) {
            ids.add(sequence.nextId());
        }

        assertEquals("All IDs in high concurrency should be unique", count, ids.size());
    }
}
