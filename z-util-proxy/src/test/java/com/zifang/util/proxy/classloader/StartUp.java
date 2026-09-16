package com.zifang.util.proxy.classloader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 当StartUp测试类启动后， 我们可以手动修改personClass类， 并修改sayHello方法的内容。   将会看到热替换的效果。
 */

/**
 * StartUp类。
 */
public class StartUp {

    /**
     * 注意， 测试类中在加载hot.Person的时候，使用的是myclassLoader的findClass方法。
     * 而不是loadClass方法， 因为loadClass方法由于双亲委派模式，会将hot.Person交给
     * myClassLoader的父ClassLoader进行加载。 而其父ClassLoader对加载的Class做了缓存，
     * 如果发现该类已经加载过， 就不会再加载第二次。  就算改类已经被改变
     * <p>
     * <p>
     * 注意：同一个ClassLoader不能多次加载同一个类。 如果重复的加载同一个类
     * ， 将会抛出 loader (instance of  hot/MyClassLoader)
     * : attempted  duplicate class definition for name: "hot/Person" 异
     * 常。  所以，在替换Class的时候，  加载该Class的ClassLoader也必须用新的。
     *
     * @throws ClassNotFoundException
     */
    /**
     * test1方法。
     *
     * @return static void类型返回值
     */
    public static void test1() throws Exception {

        int i = 0;

        while (true) {
            MyClassLoader mcl = new MyClassLoader();
//			System.out.println(mcl.getParent());
            Class<?> personClass = mcl.findClass("cn.com.infcn.classloader.Person");

            try {
                Object person = personClass.newInstance();
                Method sayHelloMethod = personClass.getMethod("sayHello");
                sayHelloMethod.invoke(person);
//                System.out.println(++i);  
            } catch (InstantiationException e1) {
                e1.printStackTrace();
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * test2方法。
     *
     * @return static void类型返回值
     */
    public static void test2() throws Exception {

        while (true) {
            MyClassLoader mcl = new MyClassLoader();
//			System.out.println(mcl.getParent());
//			System.out.println(mcl.getParent().getParent());
//			System.out.println(mcl.getParent().getParent().getParent());
            Class<?> personClass = mcl.findClass("cn.com.infcn.classloader.Person");

            try {
                IPerson person = (IPerson) personClass.newInstance();
                person.sayHello();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws Exception {
        test2();
    }
}
