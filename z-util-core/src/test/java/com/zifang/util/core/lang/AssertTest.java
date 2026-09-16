package com.zifang.util.core.lang;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class AssertTest {

    @Test
    public void testNotNull() {
        assertEquals("x", Assert.notNull("x", "x"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotNullFails() {
        Assert.notNull(null, "param");
    }

    @Test
    public void testNotBlank() {
        assertEquals("ok", Assert.notBlank("ok", "name"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotBlankFailsOnNull() {
        Assert.notBlank(null, "name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotBlankFailsOnEmpty() {
        Assert.notBlank("", "name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotBlankFailsOnSpaces() {
        Assert.notBlank("   ", "name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotEmptyCollection() {
        Assert.notEmpty(Collections.emptyList(), "ids");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotEmptyMap() {
        Assert.notEmpty(new HashMap<>(), "map");
    }

    @Test
    public void testPositive() {
        assertEquals(1, Assert.positive(1, "v"));
        assertEquals(100L, Assert.positive(100L, "v"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPositiveFails() {
        Assert.positive(0, "v");
    }

    @Test
    public void testInRange() {
        assertEquals(0.5, Assert.inRange(0.5, 0.0, 1.0, "ratio"), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInRangeFails() {
        Assert.inRange(2.0, 0.0, 1.0, "ratio");
    }

    @Test
    public void testIsTrue() {
        Assert.isTrue(true, "ok");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsTrueFails() {
        Assert.isTrue(false, "should be true");
    }

    @Test(expected = IllegalStateException.class)
    public void testStateFails() {
        Assert.state(false, "bad state");
    }

    @Test
    public void testLazyMessage() {
        Assert.notNull("ok", () -> "expensive: " + "computed");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLazyMessageFails() {
        Assert.notNull(null, () -> "lazy fail");
    }

    @Test
    public void testNotEmptyArray() {
        Assert.notEmpty(new String[]{"a"}, "arr");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotEmptyArrayFails() {
        Assert.notEmpty(new String[0], "arr");
    }

    @Test
    public void testNotEmptyListReturnsValue() {
        assertEquals(Arrays.asList(1, 2), Assert.notEmpty(Arrays.asList(1, 2), "ids"));
    }
}