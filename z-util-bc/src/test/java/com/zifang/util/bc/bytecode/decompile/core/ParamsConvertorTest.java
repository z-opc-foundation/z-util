package com.zifang.util.bc.bytecode.decompile.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * ParamsConvertor 测试：字段描述符 / 方法参数 / 返回类型 → Java 类型名。
 */
public class ParamsConvertorTest {

    @Test
    public void primitiveFieldTypes() {
        assertEquals("byte", ParamsConvertor.paramsConvertorFieldType("B"));
        assertEquals("char", ParamsConvertor.paramsConvertorFieldType("C"));
        assertEquals("double", ParamsConvertor.paramsConvertorFieldType("D"));
        assertEquals("float", ParamsConvertor.paramsConvertorFieldType("F"));
        assertEquals("int", ParamsConvertor.paramsConvertorFieldType("I"));
        assertEquals("long", ParamsConvertor.paramsConvertorFieldType("J"));
        assertEquals("short", ParamsConvertor.paramsConvertorFieldType("S"));
        assertEquals("boolean", ParamsConvertor.paramsConvertorFieldType("Z"));
        assertEquals("void", ParamsConvertor.paramsConvertorFieldType("V"));
    }

    @Test
    public void objectTypeUsesSimpleName() {
        assertEquals("String", ParamsConvertor.paramsConvertorFieldType("Ljava/lang/String;"));
        assertEquals("List", ParamsConvertor.paramsConvertorFieldType("Ljava/util/List;"));
    }

    @Test
    public void primitiveArraysAppendBrackets() {
        assertEquals("int[]", ParamsConvertor.paramsConvertorFieldType("[I"));
        assertEquals("int[][]", ParamsConvertor.paramsConvertorFieldType("[[I"));
        assertEquals("long[]", ParamsConvertor.paramsConvertorFieldType("[J"));
    }

    @Test
    public void objectArraysAppendBrackets() {
        assertEquals("String[]", ParamsConvertor.paramsConvertorFieldType("[Ljava/lang/String;"));
        assertEquals("String[][]", ParamsConvertor.paramsConvertorFieldType("[[Ljava/lang/String;"));
        assertEquals("List[]", ParamsConvertor.paramsConvertorFieldType("[Ljava/util/List;"));
    }

    @Test
    public void methodParamsMixed() {
        assertEquals("int, String, long[]",
                ParamsConvertor.paramsConvertorMethodParams("ILjava/lang/String;[J"));
        assertEquals("", ParamsConvertor.paramsConvertorMethodParams(""));
        assertEquals("boolean",
                ParamsConvertor.paramsConvertorMethodParams("Z"));
    }

    @Test
    public void methodParamsWithObjectArray() {
        assertEquals("String[], int",
                ParamsConvertor.paramsConvertorMethodParams("[Ljava/lang/String;I"));
    }

    @Test
    public void methodReturnTypes() {
        assertEquals("void", ParamsConvertor.paramsConvertorMethodReturnType("V"));
        assertEquals("double[]", ParamsConvertor.paramsConvertorMethodReturnType("[D"));
        assertEquals("String[]", ParamsConvertor.paramsConvertorMethodReturnType("[Ljava/lang/String;"));
        assertEquals("int", ParamsConvertor.paramsConvertorMethodReturnType("I"));
    }

    @Test
    public void genericFieldType() {
        assertEquals("Map<String, Integer>",
                ParamsConvertor.paramsConvertorFieldTypeWithGeneric(
                        "Ljava/util/Map<Ljava/lang/String;Ljava/lang/Integer;>;",
                        "Ljava/util/Map;"));
    }

    @Test
    public void genericFieldTypeNestedGenerics() {
        assertEquals("Map<String, List>",
                ParamsConvertor.paramsConvertorFieldTypeWithGeneric(
                        "Ljava/util/Map<Ljava/lang/String;Ljava/util/List;>;",
                        "Ljava/util/Map;"));
    }
}
