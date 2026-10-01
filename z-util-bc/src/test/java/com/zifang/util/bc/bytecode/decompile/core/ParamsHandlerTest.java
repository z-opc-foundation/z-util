package com.zifang.util.bc.bytecode.decompile.core;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * ParamsHandler.getParams 测试：方法描述符参数段 → 已转换的类型名列表。
 */
public class ParamsHandlerTest {

    private static List<String> parse(String src) {
        List<String> params = new ArrayList<>();
        ParamsHandler.getParams(src, params);
        return params;
    }

    @Test
    public void emptyDescriptorYieldsNoParams() {
        assertEquals(0, parse("").size());
    }

    @Test
    public void purePrimitiveSequence() {
        assertEquals(java.util.Arrays.asList("int", "long"), parse("IJ"));
        assertEquals(java.util.Arrays.asList("byte", "char", "boolean"), parse("BCZ"));
    }

    @Test
    public void primitiveArraysInBasicSequence() {
        assertEquals(java.util.Arrays.asList("int[]", "long[][]"), parse("[I[[J"));
    }

    @Test
    public void referenceTypeThenPrimitive() {
        assertEquals(java.util.Arrays.asList("String", "int"), parse("Ljava/lang/String;I"));
    }

    @Test
    public void genericMapType() {
        assertEquals(java.util.Arrays.asList("Map<String, Integer>", "long"),
                parse("Ljava/util/Map<Ljava/lang/String;Ljava/lang/Integer;>;J"));
    }

    @Test
    public void objectArrayLosesNoDimensions() {
        assertEquals(java.util.Arrays.asList("String[]", "int"), parse("[Ljava/lang/String;I"));
        assertEquals(java.util.Arrays.asList("String[][]"), parse("[[Ljava/lang/String;"));
    }

    @Test
    public void twoReferencesAndPrimitive() {
        assertEquals(java.util.Arrays.asList("String", "Integer", "byte"),
                parse("Ljava/lang/String;Ljava/lang/Integer;B"));
    }
}
