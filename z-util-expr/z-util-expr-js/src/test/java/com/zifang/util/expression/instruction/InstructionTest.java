package com.zifang.util.expression.instruction;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Instruction 指令类完整测试
 */
public class InstructionTest {

    @Test
    public void testDefaultConstructor() {
        Instruction instruction = new Instruction();
        assertNull(instruction.getInstructionCode());
        assertNull(instruction.getParams());
    }

    @Test
    public void testSettersAndGetters() {
        Instruction instruction = new Instruction();
        instruction.setInstructionCode("ADD");
        instruction.setParams(new Object[]{"param1", 2});

        assertEquals("ADD", instruction.getInstructionCode());
        assertEquals(2, instruction.getParams().length);
        assertEquals("param1", instruction.getParams()[0]);
        assertEquals(2, instruction.getParams()[1]);
    }

    @Test
    public void testStaticFactory() {
        Instruction instruction = Instruction.of("SUB", new Object[]{"a", "b"});
        assertEquals("SUB", instruction.getInstructionCode());
        assertEquals(2, instruction.getParams().length);
    }

    @Test
    public void testToString() {
        Instruction instruction = Instruction.of("ADD", new Object[]{1, 2, 3});
        String str = instruction.toString();
        assertTrue(str.contains("ADD"));
        assertTrue(str.contains("1"));
    }

    @Test
    public void testEquals() {
        Instruction i1 = Instruction.of("ADD", new Object[]{1, 2});
        Instruction i2 = Instruction.of("ADD", new Object[]{1, 2});
        Instruction i3 = Instruction.of("SUB", new Object[]{1, 2});

        assertEquals(i1, i2);
        assertNotEquals(i1, i3);
    }

    @Test
    public void testHashCode() {
        Instruction i1 = Instruction.of("ADD", new Object[]{1, 2});
        Instruction i2 = Instruction.of("ADD", new Object[]{1, 2});

        assertEquals(i1.hashCode(), i2.hashCode());
    }

    @Test
    public void testNotEqualsWithNull() {
        Instruction i1 = Instruction.of("ADD", new Object[]{1});
        assertNotEquals(null, i1);
    }

    @Test
    public void testNotEqualsWithDifferentClass() {
        Instruction i1 = Instruction.of("ADD", new Object[]{1});
        assertNotEquals("string", i1);
    }

    @Test
    public void testParamsWithDifferentLength() {
        Instruction i1 = Instruction.of("ADD", new Object[]{1});
        Instruction i2 = Instruction.of("ADD", new Object[]{1, 2});
        assertNotEquals(i1, i2);
    }

    @Test
    public void testReflexivity() {
        Instruction i1 = Instruction.of("ADD", new Object[]{1, 2});
        assertEquals(i1, i1);
    }
}
