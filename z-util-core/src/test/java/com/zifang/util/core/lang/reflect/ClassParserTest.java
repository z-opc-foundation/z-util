package com.zifang.util.core.lang.reflect;

import com.zifang.util.core.lang.converter.IConverter;
import org.junit.Test;

import java.lang.reflect.Type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

interface IA1 {
}


interface IA2 extends IA21 {
}

interface IA21 {
}

interface D extends IConverter<Integer, Long> {
}

/**
 * ClassParserTest类。
 */
public class ClassParserTest {

    @Test
    /**
     * test1方法。
     */
    public void test1() {
        ClassParser a = new ClassParserFactory().getInstance(A.class);

        assertEquals(1, a.getCurrentPublicField().size());
        assertEquals(1, a.getCurrentPrivateField().size());
        assertEquals(1, a.getCurrentProtectedField().size());
        assertEquals(3, a.getCurrentAllField().size());

        assertEquals(6, a.getCurrentAllMethod().size());
        assertEquals(1, a.getCurrentPrivateMethod().size());
        assertEquals(1, a.getCurrentProtectedMethod().size());
        assertEquals(3, a.getCurrentPublicMethod().size());
        assertEquals(1, a.getCurrentDefaultMethod().size());

        assertTrue(a.isNormalClass());
    }

    @Test
    /**
     * test2方法。
     */
    public void test2() {
        ClassParser a = new ClassParserFactory().getInstance(A.class);
    }

    @Test
    @org.junit.Ignore("Lambda泛型擦除作业测试")
    /**
     * test3方法。
     */
    public void test3() {
        ClassParser a = new ClassParserFactory().getInstance(A.class);
        ClassParser b = new ClassParserFactory().getInstance(B.class);
        ClassParser c = new ClassParserFactory().getInstance(C.class);
        ClassParser d = new ClassParserFactory().getInstance(D.class);

        Type type1 = a.getGenericType(IConverter.class);
        Type type2 = b.getGenericType(IConverter.class);
        Type type3 = c.getGenericType(IConverter.class);
        Type type4 = d.getGenericType(IConverter.class);

        assert type1 != null;
        assert type2 == null;
        assert type3 != null;
        assert type4 != null;

    }
}

class A extends AbstractA implements IA1, IA2, IConverter<Integer, Long> {
    public String a3;
    protected String a2;
    private String a1;

    /**
     * t1方法。
     */
    public void t1() {
    }

    private void t2() {
    }

    /**
     * t3方法。
     */
    protected void t3() {
    }

    void t4() {
    }

    @Override
    /**
     * to方法。
     *      * @param integer int类型参数
     * @param aLong long类型参数
     * @return long类型返回值
     */
    public Long to(Integer integer, Long aLong) {
        return null;
    }
}

abstract class AbstractA extends B {
}

class B {
}

class BA extends A {
    @Override
    /**
     * to方法。
     *      * @param integer int类型参数
     * @param aLong long类型参数
     * @return long类型返回值
     */
    public Long to(Integer integer, Long aLong) {
        return null;
    }
}

class C implements D {
    @Override
    /**
     * to方法。
     *      * @param integer int类型参数
     * @param aLong long类型参数
     * @return long类型返回值
     */
    public Long to(Integer integer, Long aLong) {
        return null;
    }
}
