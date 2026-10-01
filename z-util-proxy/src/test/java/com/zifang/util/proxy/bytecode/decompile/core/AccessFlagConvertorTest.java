package com.zifang.util.proxy.bytecode.decompile.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * AccessFlagConvertor 测试：十六进制访问标志 → Java 修饰符字符串（含尾部空格）。
 */
public class AccessFlagConvertorTest {

    @Test
    public void noFlagsYieldsEmptyString() {
        assertEquals("", AccessFlagConvertor.classAccessFlagConvertor("0000"));
        assertEquals("", AccessFlagConvertor.methodAccessFlagConvertor("0000"));
        assertEquals("", AccessFlagConvertor.fieldAccessFlagConvertor("0000"));
    }

    @Test
    public void visibilityFlags() {
        assertEquals("public ", AccessFlagConvertor.classAccessFlagConvertor("0001"));
        assertEquals("private ", AccessFlagConvertor.classAccessFlagConvertor("0002"));
        assertEquals("protected ", AccessFlagConvertor.classAccessFlagConvertor("0004"));
    }

    @Test
    public void staticAloneAndCombinedWithVisibility() {
        assertEquals("static ", AccessFlagConvertor.classAccessFlagConvertor("0008"));
        assertEquals("public static ", AccessFlagConvertor.classAccessFlagConvertor("0009"));
        assertEquals("private static ", AccessFlagConvertor.classAccessFlagConvertor("000A"));
        assertEquals("protected static ", AccessFlagConvertor.classAccessFlagConvertor("000C"));
    }

    @Test
    public void finalFlag() {
        assertEquals("final ", AccessFlagConvertor.classAccessFlagConvertor("0010"));
        assertEquals("public final ", AccessFlagConvertor.classAccessFlagConvertor("0011"));
        assertEquals("public static final ", AccessFlagConvertor.classAccessFlagConvertor("0019"));
    }

    @Test
    public void nativeAbstractSynthetic() {
        assertEquals("public native ", AccessFlagConvertor.methodAccessFlagConvertor("0101"));
        assertEquals("public abstract ", AccessFlagConvertor.methodAccessFlagConvertor("0401"));
        assertEquals("public synthetic ", AccessFlagConvertor.methodAccessFlagConvertor("1001"));
    }

    @Test
    public void fieldVariantMatchesClassVariant() {
        assertEquals("public static final ",
                AccessFlagConvertor.fieldAccessFlagConvertor("0019"));
        assertEquals("private ",
                AccessFlagConvertor.fieldAccessFlagConvertor("0002"));
    }

    @Test
    public void realWorldClassFlags() {
        // public final class → 0x0031
        assertEquals("public final ", AccessFlagConvertor.classAccessFlagConvertor("0031"));
        // public class → 0x0021
        assertEquals("public ", AccessFlagConvertor.classAccessFlagConvertor("0021"));
        // public abstract interface 常见口径 0x0601
        assertEquals("public abstract ", AccessFlagConvertor.classAccessFlagConvertor("0601"));
    }
}
