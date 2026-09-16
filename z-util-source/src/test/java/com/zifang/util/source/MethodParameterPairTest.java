package com.zifang.util.source;

import com.zifang.util.source.generator.info.MethodParameterPair;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * MethodParameterPair 模型测试
 */

/**
 * MethodParameterPairTest类。
 */
public class MethodParameterPairTest {

    @Test
    /**
     * testBasicSetters方法。
     */
    public void testBasicSetters() {
        MethodParameterPair pair = new MethodParameterPair();
        pair.setParamType("String");
        pair.setParamName("name");

        assertEquals("String", pair.getParamType());
        assertEquals("name", pair.getParamName());
    }

    @Test
    /**
     * testToString方法。
     */
    public void testToString() {
        MethodParameterPair pair = new MethodParameterPair();
        pair.setParamType("String");
        pair.setParamName("name");

        assertEquals("String name", pair.toString());
    }

    @Test
    /**
     * testToStringWithDifferentTypes方法。
     */
    public void testToStringWithDifferentTypes() {
        MethodParameterPair pair1 = new MethodParameterPair();
        pair1.setParamType("int");
        pair1.setParamName("age");
        assertEquals("int age", pair1.toString());

        MethodParameterPair pair2 = new MethodParameterPair();
        pair2.setParamType("List<String>");
        pair2.setParamName("items");
        assertEquals("List<String> items", pair2.toString());
    }
}