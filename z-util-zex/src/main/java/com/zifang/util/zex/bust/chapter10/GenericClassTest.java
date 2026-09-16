package com.zifang.util.zex.bust.chapter10;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */

import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;

interface GenericInterface<T, R> {
    R call(T t);
}

class GenericFather<T, R> implements GenericInterface<T, R> {

    public T t;

    /**
     * doCall方法。
     * * @param t T类型参数
     *
     * @return static <T,R> R类型返回值
     */
    public static <T, R> R doCall(T t) {
        return null;
    }

    @Override
    /**
     * call方法。
     *      * @param t T类型参数
     * @return R类型返回值
     */
    public R call(T t) {
        return null;
    }
}

class GenericSon extends GenericFather<String, Integer> implements GenericInterface<String, Integer> {

    /**
     * doA方法。
     * * @param list ListString类型参数
     *
     * @return static Map<String,Integer>类型返回值
     */
    public static Map<String, Integer> doA(List<String> list) {
        return null;
    }

}


/**
 * GenericClassTest类。
 */
public class GenericClassTest {

    @Test
    /**
     * test001方法。
     */
    public void test001() throws NoSuchMethodException {
        GenericSon genericSon = new GenericSon();
        Class<?> c1 = genericSon.getClass();

        System.out.println("\n解析继承的父类泛型信息----");
        if (c1.getGenericSuperclass() instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) c1.getGenericSuperclass();
            for (Type type : parameterizedType.getActualTypeArguments()) {
                System.out.println("获得到父类泛型：" + type.getTypeName());
            }
        }

        System.out.println("\n解析实现的接口泛型信息----");
        for (Type type : c1.getGenericInterfaces()) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                for (Type item : parameterizedType.getActualTypeArguments()) {
                    System.out.println("获得到接口泛型：" + item.getTypeName());
                }
            }
        }

        System.out.println("\n解析doA方法返回类型泛型信息----");
        Method method = c1.getMethod("doA", List.class);
        Type type = method.getGenericReturnType();
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            for (Type item : parameterizedType.getActualTypeArguments()) {
                System.out.println("获得到方法返回泛型信息：" + item.getTypeName());
            }
        }

        System.out.println("\n解析doA方法入参类型泛型信息----");
        Type[] params = method.getGenericParameterTypes();
        for (Type param : params) {
            if (param instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                for (Type item : parameterizedType.getActualTypeArguments()) {
                    System.out.println("获得到方法入参泛型信息：" + item.getTypeName());
                }
            }
        }
    }

    @Test
    /**
     * test002方法。
     */
    public void test002() {
        GenericFather<String, Integer> father = new GenericFather<>();

        System.out.println("\n解析泛化类型泛型信息----");
        Type[] types = father.getClass().getTypeParameters();
        for (Type type : types) {
            if (type instanceof TypeVariable) {
                TypeVariable typeVariable = (TypeVariable) type;
                System.out.println("获得到泛化参数名称：" + typeVariable.getName());
            }
        }
    }

}
