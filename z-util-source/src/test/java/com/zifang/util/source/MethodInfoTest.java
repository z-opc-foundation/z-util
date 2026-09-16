package com.zifang.util.source;

import com.zifang.util.source.generator.info.MethodInfo;
import com.zifang.util.source.generator.info.MethodParameterPair;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * MethodInfo 模型测试
 */

/**
 * MethodInfoTest类。
 */
public class MethodInfoTest {

    @Test
    /**
     * testBasicSetters方法。
     */
    public void testBasicSetters() {
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setMethodName("getName");
        methodInfo.setReturnType("String");
        methodInfo.setModifier(java.lang.reflect.Modifier.PUBLIC);

        assertEquals("getName", methodInfo.getMethodName());
        assertEquals("String", methodInfo.getReturnType());
        assertEquals(java.lang.reflect.Modifier.PUBLIC, methodInfo.getModifier().intValue());
    }

    @Test
    /**
     * testSignature方法。
     */
    public void testSignature() {
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setReturnType("String");
        methodInfo.setMethodName("getName");

        assertEquals("String getName();", methodInfo.signature());
    }

    @Test
    /**
     * testSignatureWithParameters方法。
     */
    public void testSignatureWithParameters() {
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setReturnType("String");
        methodInfo.setMethodName("getName");

        List<MethodParameterPair> params = new ArrayList<>();
        MethodParameterPair pair = new MethodParameterPair();
        pair.setParamType("String");
        pair.setParamName("name");
        params.add(pair);
        methodInfo.setMethodParameterPairs(params);

        assertEquals("String getName(String name);", methodInfo.signature());
    }

    @Test
    /**
     * testFullSignature方法。
     */
    public void testFullSignature() {
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setReturnType("String");
        methodInfo.setMethodName("getName");

        assertEquals("String getName();", methodInfo.fullSignature());
    }

    @Test
    /**
     * testEquals方法。
     */
    public void testEquals() {
        MethodInfo m1 = new MethodInfo();
        m1.setMethodName("getName");
        m1.setReturnType("String");

        MethodInfo m2 = new MethodInfo();
        m2.setMethodName("getName");
        m2.setReturnType("String");

        MethodInfo m3 = new MethodInfo();
        m3.setMethodName("setName");
        m3.setReturnType("void");

        assertTrue(m1.equals(m2));
        assertFalse(m1.equals(m3));
    }

    @Test
    /**
     * testEqualsWithParameters方法。
     */
    public void testEqualsWithParameters() {
        MethodInfo m1 = new MethodInfo();
        m1.setMethodName("getName");
        m1.setReturnType("String");

        List<MethodParameterPair> params1 = new ArrayList<>();
        MethodParameterPair pair1 = new MethodParameterPair();
        pair1.setParamType("String");
        pair1.setParamName("name");
        params1.add(pair1);
        m1.setMethodParameterPairs(params1);

        MethodInfo m2 = new MethodInfo();
        m2.setMethodName("getName");
        m2.setReturnType("String");

        List<MethodParameterPair> params2 = new ArrayList<>();
        MethodParameterPair pair2 = new MethodParameterPair();
        pair2.setParamType("String");
        pair2.setParamName("name");
        params2.add(pair2);
        m2.setMethodParameterPairs(params2);

        assertTrue(m1.equals(m2));
    }

    @Test
    /**
     * testEqualsWithDifferentParameters方法。
     */
    public void testEqualsWithDifferentParameters() {
        MethodInfo m1 = new MethodInfo();
        m1.setMethodName("getName");
        m1.setReturnType("String");

        List<MethodParameterPair> params1 = new ArrayList<>();
        MethodParameterPair pair1 = new MethodParameterPair();
        pair1.setParamType("String");
        pair1.setParamName("name");
        params1.add(pair1);
        m1.setMethodParameterPairs(params1);

        MethodInfo m2 = new MethodInfo();
        m2.setMethodName("getName");
        m2.setReturnType("String");

        List<MethodParameterPair> params2 = new ArrayList<>();
        MethodParameterPair pair2 = new MethodParameterPair();
        pair2.setParamType("int");
        pair2.setParamName("id");
        params2.add(pair2);
        m2.setMethodParameterPairs(params2);

        assertFalse(m1.equals(m2));
    }

    @Test
    /**
     * testStatements方法。
     */
    public void testStatements() {
        MethodInfo methodInfo = new MethodInfo();
        List<String> statements = new ArrayList<>();
        statements.add("return this.name;");
        statements.add("return null;");
        methodInfo.setStatements(statements);

        assertEquals(2, methodInfo.getStatements().size());
        assertEquals("return this.name;", methodInfo.getStatements().get(0));
    }

    @Test
    /**
     * testAnnotations方法。
     */
    public void testAnnotations() {
        MethodInfo methodInfo = new MethodInfo();
        com.zifang.util.source.generator.info.AnnotationInfo annot =
                new com.zifang.util.source.generator.info.AnnotationInfo("Override");
        methodInfo.getAnnotations().add(annot);

        assertEquals(1, methodInfo.getAnnotations().size());
        assertEquals("Override", methodInfo.getAnnotations().get(0).getType());
    }

    @Test
    /**
     * testGetParameterStrWithEmptyParams方法。
     */
    public void testGetParameterStrWithEmptyParams() {
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setReturnType("String");
        methodInfo.setMethodName("getName");
        methodInfo.setMethodParameterPairs(new ArrayList<>());

        // 空参数列表应该产生正确的签名
        assertEquals("String getName();", methodInfo.signature());
    }
}