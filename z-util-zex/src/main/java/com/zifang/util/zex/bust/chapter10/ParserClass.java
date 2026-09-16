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

import com.zifang.util.zex.bust.chapter9.AnnotationTest;
import org.junit.Test;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

enum Code {
    Code1, Code2;
}

interface SuperInterface {
    default void defaultFunction() {
    }

    void commonFunction();

}

@Target({ElementType.TYPE,
        ElementType.METHOD,
        ElementType.FIELD,
        ElementType.TYPE_USE,
        ElementType.PARAMETER,
        ElementType.CONSTRUCTOR})
@Documented
@Retention(RetentionPolicy.RUNTIME)
@interface Annotation {
    String value();
}

class Father implements @Annotation("接口注解") SuperInterface {
    private static Integer fatherStaticPrivateField;
    protected Integer fatherProtectedField;

    @Override
    /**
     * commonFunction方法。
     */
    public void commonFunction() {
    }

    /**
     * fatherPublicFunction方法。
     */
    public void fatherPublicFunction() {
    }

    private void fatherPrivateFunction() {
    }

    /**
     * fatherProtectedFunction方法。
     */
    protected void fatherProtectedFunction() {
    }
}

@Annotation("Son类注解")
class Son<@AnnotationTest("类变量类型(泛型)上的注解") T0, T1> extends @Annotation("继承类上的注解") Father {
    @Annotation("son.sonStaticPrivateField:类字段")
    private static Integer sonStaticPrivateField;
    @Annotation("son.sonPublicField:类字段")
    public Integer sonPublicField;
    @Annotation("son.sonProtectedField:类字段")
    protected Integer sonProtectedField;
    @AnnotationTest("son.map:类字段")
    private Map<@AnnotationTest("成员变量泛型上的注解") String, String> map;


    @AnnotationTest("构造函数上的注解")
    /**
     * Son方法。
     */
    public Son() {
    }

    /**
     * Son方法。
     * * @param name String类型参数
     */
    public Son(String name) {
    }

    private Son(String name, Integer age) {
    }


    @AnnotationTest("成员方法上的注解")
    /**
     * sonPublicFunction方法。
     *      * @param @Annotation("son#sonPublicFunction:i方法字段" Object类型参数
     */
    public void sonPublicFunction(@Annotation("son#sonPublicFunction:i方法字段") int i,
                                  @Annotation("son#sonPublicFunction:integer方法字段") Integer integer) {
    }

    private void sonPrivateFunction(@Annotation("son#sonPrivateFunction:is方法字段") int[] is,
                                    @Annotation("son#sonPrivateFunction:integers方法字段") Integer[] integers) {
    }

    public static class SonSubStatic {
    }

    /**
     * SonSub类。
     */
    public class SonSub {
    }
}

/**
 * ParserClass类。
 */
public class ParserClass {

    @Test
    /**
     * test001方法。
     */
    public void test001() {
        // 获得类基础信息
        Class<?> c = ParserClass.class;

        System.out.printf("%s 类包名 : %s%n", c.getName(), c.getPackage().getName());
        System.out.printf("%s 类名  : %s%n", c.getName(), c.getName());
        System.out.printf("%s 类短名 : %s%n", c.getName(), c.getSimpleName());
    }

    @Test
    /**
     * test002方法。
     */
    public void test002() throws ClassNotFoundException {

        System.out.printf("%s 类是否是枚举类型 : %s%n", ParserClass.class.getName(), ParserClass.class.isEnum());
        System.out.printf("%s 类是否是枚举类型 : %s%n", Code.class.getName(), Code.class.isEnum());

        System.out.println("\n");

        System.out.printf("%s 类是否是注解类型 : %s%n", ParserClass.class.getName(), ParserClass.class.isAnnotation());
        System.out.printf("%s 类是否是注解类型 : %s%n", Annotation.class.getName(), Annotation.class.isAnnotation());

        System.out.println("\n");
        System.out.printf("%s 类是否是接口类型 : %s%n", ParserClass.class.getName(), ParserClass.class.isInterface());
        System.out.printf("%s 类是否是接口类型 : %s%n", SuperInterface.class.getName(), SuperInterface.class.isInterface());

        System.out.println("\n");
        System.out.printf("%s 类是否是基本变量类型 : %s%n", ParserClass.class.getName(), ParserClass.class.isPrimitive());
        System.out.printf("%s 类是否是基本变量类型 : %s%n", int.class.getName(), int.class.isPrimitive());

        System.out.println("\n");
        System.out.printf("%s 类是否是数组类型 : %s%n", ParserClass.class.getName(), ParserClass.class.isArray());
        System.out.printf("%s 类是否是数组类型 : %s%n", Class.forName("[I").getName(), Class.forName("[I").isArray());

        System.out.println("\n");
        System.out.printf("%s 类是否继承自 %s: %s%n", ParserClass.class.getName(), Father.class.getName(), ParserClass.class.isAssignableFrom(Father.class));
        System.out.printf("%s 类是否继承自 %s: %s%n", Son.class.getName(), Father.class.getName(), Son.class.isAssignableFrom(Father.class));

        System.out.println("\n");
        // 匿名类（new Runnable() {...}）与局部类（class LocalClass {}）的区别
        System.out.printf("%s 类实例是是匿名类 : %s%n", ParserClass.class.getName(), new ParserClass().getClass().isAnonymousClass());
        System.out.printf("%s 类实例是是匿名类 : %s%n", Son.class.getName(), new Runnable() {
            @Override
            public void run() {
                System.out.println();
            }
        }.getClass().isAnonymousClass());


        System.out.println("\n");
        // 局部类：在方法内部定义的命名类
        class LocalClass {
        }
        System.out.printf("%s 类是否为局部类 : %s%n", LocalClass.class.getName(), LocalClass.class.isLocalClass());
        System.out.printf("%s 类是否为局部类 : %s%n", FieldLocalClass.class.getName(), FieldLocalClass.class.isLocalClass());
        System.out.printf("%s 类是否为局部类 : %s%n", ParserClass.class.getName(), ParserClass.class.isLocalClass());

        System.out.println("\n");
        System.out.printf("%s 类是否为成员类 : %s%n", LocalClass.class.getName(), LocalClass.class.isMemberClass());
        System.out.printf("%s 类是否为成员类 : %s%n", FieldLocalClass.class.getName(), FieldLocalClass.class.isMemberClass());
        System.out.printf("%s 类是否为成员类 : %s%n", ParserClass.class.getName(), ParserClass.class.isMemberClass());

        System.out.println("\n");
        System.out.printf("%s 是否实现注解%s : %s%n", Father.class.getName(), Annotation.class.getName(), Father.class.isAnnotationPresent(Annotation.class));
        System.out.printf("%s 是否实现注解%s : %s%n", Son.class.getName(), Annotation.class.getName(), Son.class.isAnnotationPresent(Annotation.class));

    }

    @Test
    /**
     * test002_1方法。
     */
    public void test002_1() {
        System.out.println(Modifier.isPublic(ParserClass.class.getModifiers()));
    }

    @Test
    /**
     * test003方法。
     */
    public void test003() {
        Class c = Integer[].class;
        System.out.println("是否是一个数组：" + c.isArray());
        System.out.println("数组元素的类类型：" + c.getComponentType().getName());
    }

    @Test
    /**
     * test004方法。
     */
    public void test004() {
        Class c = Son.class;
        Class[] cs = c.getClasses();
        for (Class clazz : cs) {
            System.out.println(clazz.getName());
        }
    }

    @Test
    /**
     * test004_1方法。
     */
    public void test004_1() {
        Class c = Son.SonSub.class;
        System.out.println("1. 获得内部类的声明类：" + c.getDeclaringClass().getName());

        class Inner {
            class InnerInner {
            }
        }
        Class innerInner = Inner.InnerInner.class;
        System.out.println("2. 获得局部内部类的声明类:getDeclaringClass:" + innerInner.getDeclaringClass().getName());
        System.out.println("3. 获得局部内部类的声明类:getEnclosingClass:" + innerInner.getEnclosingClass().getName());

        Runnable runnable = new Runnable() {
            @Override
            /**
             * run方法。
             */
            public void run() {

            }
        };

        System.out.println("4. 获得局部内部类的声明类:getDeclaringClass:" + runnable.getClass().getDeclaringClass());
        System.out.println("5. 获得局部内部类的声明类:getEnclosingClass:" + runnable.getClass().getEnclosingClass());

        // runnable.getClass().getConstructors() 将返回空数组
        System.out.println("6. 获得局部内部类的声明类:getEnclosingConstructor:" + runnable.getClass().getEnclosingConstructor());
        // 返回 public void com.zifang.util.zex.bust.chapter10.ParserClass.test004_1()
        System.out.println("7. 获得局部内部类的声明类:getEnclosingMethod:" + runnable.getClass().getEnclosingMethod());

    }

    @Test
    /**
     * test005方法。
     */
    public void test005() {
        Class<?> c = Son.class;

        System.out.println("getFields-----------");
        for (Field field : c.getFields()) {
            System.out.println("fieldFlag：" + field.getModifiers());
            System.out.println("fieldType：" + field.getType().getName());
            System.out.println("fieldName：" + field.getName());
            System.out.println("");
        }

        System.out.println("getDeclaredFields-----------");
        for (Field field : c.getDeclaredFields()) {
            System.out.println("fieldFlag：" + field.getModifiers());
            System.out.println("fieldType：" + field.getType().getName());
            System.out.println("fieldName：" + field.getName());
            System.out.println("");
        }
    }

    @Test
    /**
     * test006方法。
     */
    public void test006() throws NoSuchFieldException {
        Class<?> c = Son.class;

        System.out.println("getField(sonPublicField)-----------");
        Field field = c.getField("sonPublicField");
        System.out.println("fieldFlag：" + field.getModifiers());
        System.out.println("fieldType：" + field.getType().getName());
        System.out.println("fieldName：" + field.getName());
        System.out.println("");


        System.out.println("getDeclaredField(sonPublicField)-----------");
        Field field2 = c.getDeclaredField("sonStaticPrivateField");
        System.out.println("fieldFlag：" + field2.getModifiers());
        System.out.println("fieldType：" + field2.getType().getName());
        System.out.println("fieldName：" + field2.getName());
        System.out.println("");

    }

    @Test
    /**
     * test007方法。
     */
    public void test007() {
        Class<?> c = Son.class;

        System.out.println("c.getMethods()------");

        for (Method method : c.getMethods()) {

            if (method.getDeclaringClass() == Object.class) {
                continue;
            }

            System.out.println("methodFlag：" + method.getModifiers());
            System.out.println("methodReturnType：" + method.getReturnType().getName());
            System.out.println("methodName：" + method.getName());

            List<String> params = new ArrayList<>();
            for (Parameter parameter : method.getParameters()) {
                params.add(String.format("类型：%s 局部形参名：parameterName:%s", parameter.getType().getName(), parameter.getName()));
            }
            System.out.println("methodParamDesc：" + String.join(",", params));
            System.out.println();
        }

        System.out.println("c.getDeclaredMethods()------");
        for (Method method : c.getDeclaredMethods()) {

            if (method.getDeclaringClass() == Object.class) {
                continue;
            }

            System.out.println("methodFlag：" + method.getModifiers());
            System.out.println("methodReturnType：" + method.getReturnType().getName());
            System.out.println("methodName：" + method.getName());

            List<String> params = new ArrayList<>();
            for (Parameter parameter : method.getParameters()) {
                params.add(String.format("类型：%s 局部形参名：parameterName:%s", parameter.getType().getName(), parameter.getName()));
            }
            System.out.println("methodParamDesc：" + String.join(",", params));
            System.out.println();
        }
    }

    @Test
    /**
     * test008方法。
     */
    public void test008() {
        Class<?> c = Son.class;

        System.out.println("c.getConstructors()------");
        for (Constructor<?> constructor : c.getConstructors()) {

            System.out.println("constructorFlag：" + constructor.getModifiers());

            List<String> params = new ArrayList<>();
            for (Parameter parameter : constructor.getParameters()) {
                params.add(String.format("类型：%s 局部形参名：parameterName:%s", parameter.getType().getName(), parameter.getName()));
            }
            System.out.println("constructorParamDesc：" + String.join(",", params));
            System.out.println();

        }

        System.out.println("c.getDeclaredConstructors()------");
        for (Constructor<?> constructor : c.getDeclaredConstructors()) {

            System.out.println("constructorFlag：" + constructor.getModifiers());

            List<String> params = new ArrayList<>();
            for (Parameter parameter : constructor.getParameters()) {
                params.add(String.format("类型：%s 局部形参名：parameterName:%s", parameter.getType().getName(), parameter.getName()));
            }
            System.out.println("constructorParamDesc：" + String.join(",", params));
            System.out.println();

        }
    }

    @Test
    /**
     * test009方法。
     */
    public void test009() {
        Class<?> c = Son.class;
        System.out.println("son的父类：" + c.getSuperclass().getName());
        System.out.println("son的父类的接口：" + c.getSuperclass().getInterfaces()[0].getName());

    }

    @Test
    /**
     * test010方法。
     */
    public void test010() throws NoSuchMethodException {
        Class<Son> clazz = Son.class;

        System.out.println("\n获取类上的注解-----");
        java.lang.annotation.Annotation[] annotations = clazz.getAnnotations();

        for (java.lang.annotation.Annotation annotation : annotations) {
            System.out.println("存在注解：" + annotation.getClass().getName() + ":value = " + ((Annotation) annotation).value());
        }


        System.out.println("\n获取类上接口的注解-----");
        AnnotatedType[] annotatedInterfaces = Father.class.getAnnotatedInterfaces();

        for (AnnotatedType annotatedType : annotatedInterfaces) {
            System.out.println("存在注解：" + annotatedType.getClass().getName() + ":value = " + (annotatedType.getAnnotation(Annotation.class)).value());
        }

        System.out.println("\n获取类上继承类的注解-----");
        AnnotatedType annotatedSuperclass = Son.class.getAnnotatedSuperclass();
        System.out.println("存在注解：" + annotatedSuperclass.getClass().getName() + ":value = " + (annotatedSuperclass.getAnnotation(Annotation.class)).value());


        System.out.println("\n获取类的类型变量(泛型)的注解-----");
        TypeVariable<Class<Son>>[] typeParameters = clazz.getTypeParameters();
        for (TypeVariable typeVariable : typeParameters) {
            System.out.println("获得泛型占位：" + typeVariable.getName());
            java.lang.annotation.Annotation[] annotations1 = typeVariable.getAnnotations();
            for (java.lang.annotation.Annotation annotation : annotations1) {
                System.out.println("获得泛型占位上的注解：" + annotation);
            }
        }

        System.out.println("\n获取成员变量上的注解-----");
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            System.out.println("字段名称：" + field.getName());
            for (java.lang.annotation.Annotation annotation : field.getAnnotations()) {
                System.out.println("字段注解：" + annotation);
            }
        }

        System.out.println("\n获取成员变量泛型的注解-----");
        for (Field field : fields) {
            if (field.getAnnotatedType() instanceof AnnotatedParameterizedType) {
                AnnotatedParameterizedType annotatedType = (AnnotatedParameterizedType) field.getAnnotatedType();
                AnnotatedType[] typeArguments = annotatedType.getAnnotatedActualTypeArguments();
                for (AnnotatedType typeArgument : typeArguments) {
                    for (java.lang.annotation.Annotation annotation : typeArgument.getAnnotations()) {
                        System.out.println("字段名称：" + field.getName());
                        System.out.println("字段注解：" + annotation);
                    }
                }
            }
        }

        System.out.println("\n获取成员方法上的注解-----");
        Method test = clazz.getMethod("sonPublicFunction", int.class, Integer.class);
        for (java.lang.annotation.Annotation annotation : test.getAnnotations()) {
            System.out.println("方法名称：" + test.getName());
            System.out.println(annotation);
        }

        System.out.println("\n获取成员方法上的注解-----");
        for (Parameter parameter : test.getParameters()) {
            for (java.lang.annotation.Annotation annotation : parameter.getAnnotations()) {
                System.out.println("参数名称：" + parameter.getName());
                System.out.println("参数注解：" + annotation);
            }
        }


        System.out.println("\n获取构造函数上的注解-----");
        Constructor<?> constructor = clazz.getConstructor();
        for (java.lang.annotation.Annotation annotation : constructor.getAnnotations()) {
            System.out.println("构造器描述：" + constructor.toString());
            System.out.println("构造器注解：" + annotation);
        }

    }

    @Test
    /**
     * test011方法。
     */
    public void test011() {

    }

    class FieldLocalClass {
    }
}
