package com.zifang.util.zex.bytecode.ctclass;

/**
 * Java字节码操作示例类。
 * <p>
 * 此类展示了Java字节码的读取和操作技术。
 * 使用Javassist或ASM框架进行字节码增强。
 *
 * @author zifang
 * @version 1.0
 */

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import java.net.MalformedURLException;

/**
 * CtClassTest类。
 */
public class CtClassTest {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws MalformedURLException, NotFoundException {

        ClassPool pool = new ClassPool(ClassPool.getDefault());
        pool.appendClassPath("/Users/zifang/Downloads/trash/bpmn-engine-1.2.1-SNAPSHOT.jar");
        CtClass ctClass = pool.get("com.come2future.bpmn.WorkflowContext");
        System.out.println(ctClass);

        ClassPool classPool = ClassPool.getDefault();
        classPool.insertClassPath("/Users/zifang/Downloads/trash");

//        URL u = new File("/Users/zifang/Downloads/bpmn-engine-1.2.1-SNAPSHOT.jar").toURI().toURL();
//        URL[] US= new URL[]{u};
//        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//        CustomerUrlClassLoader c = new CustomerUrlClassLoader(US,classLoader);
//        //c.classes;
        System.out.println();

    }

}
