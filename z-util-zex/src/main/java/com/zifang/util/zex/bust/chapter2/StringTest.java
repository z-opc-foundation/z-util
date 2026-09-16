package com.zifang.util.zex.bust.chapter2;

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

/**
 * StringTest类。
 */
public class StringTest {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        String str = "123";
        Integer sum = 0;
        for (char c : str.toCharArray()) {
            int n = c - '0';
            sum = sum * 10 + n;
        }
        System.out.println(sum);

    }

    @Test
    /**
     * test001方法。
     */
    public void test001() {
        String a = "123";
        System.out.println(a);
    }

    @Test
    /**
     * test002方法。
     */
    public void test002() {
        String a = new String("123");
        System.out.println(a);
    }

    @Test
    /**
     * test003方法。
     */
    public void test003() {
        String a = new String("12😄4");

        // 打印5
        System.out.println(a.length());
        // 打印4
        System.out.println(a.codePoints().count());

    }

    @Test
    /**
     * test004方法。
     */
    public void test004() {

        String a = new String("12😄4");
        a.indexOf("\uD83D\uDE04");
        // 打印5
        System.out.println(a.length());
        // 打印4
        System.out.println(a.codePoints().count());

    }

    @Test
    /**
     * test005方法。
     */
    public void test005() {

        String a = new String("abcd😄e");
        assert a.indexOf('a') == 0;
        assert a.indexOf("bc") == 1;
        assert a.indexOf('c') == 2;
        assert a.indexOf("😄") == 4;
        assert a.indexOf("e") == 6;

    }

    @Test
    /**
     * test006方法。
     */
    public void test006() {
        System.out.println();
        "aa".toLowerCase();
    }

    @Test
    /**
     * test007方法。
     */
    public void test007() {
        String str = String.format("Hi,%s", "早安");
        System.out.println(str);
        str = String.format("Hi,%s:%s.%s", "大哥", "二弟", "三弟");
        System.out.println(str);
        System.out.printf("字母a的大写是：%c %n", 'A');
        System.out.printf("3>7的结果是：%b %n", 3 > 7);
        System.out.printf("100的一半是：%d %n", 100 / 2);
        System.out.printf("100的16进制数是：%x %n", 100);
        System.out.printf("100的8进制数是：%o %n", 100);
        System.out.printf("50元的书打8.5折扣是：%f 元%n", 50 * 0.85);
        System.out.printf("上面价格的16进制数是：%a %n", 50 * 0.85);
        System.out.printf("上面价格的指数表示：%e %n", 50 * 0.85);
        System.out.printf("上面价格的指数和浮点数结果的长度较短的是：%g %n", 50 * 0.85);
        System.out.printf("上面的折扣是%d%% %n", 85);
        System.out.printf("字母A的散列码是：%h %n", 'A');
    }

    @Test
    /**
     * test009方法。
     */
    public void test009() {
        String s0 = "1";
        String s1 = new String("1");

        // false,
        System.out.println(s0 == s1);
        // true
        System.out.println(s0.intern() == s0);
        // false
        System.out.println(s1.intern() == s1);

        String ss0 = new String("1");


        System.out.println();
    }

    @Test
    /**
     * test010方法。
     */
    public void test010() {
        String s0 = "1" + "2";
        String s1 = "1" + new String("2");
        String s2 = new String("1") + new String("2");

        // true
        System.out.println("12" == s0);
        System.out.println("12" == s1);
        System.out.println("12" == s2);
    }

//    public static void main(String[] args) {
//        for(Method method: Collections.class.getDeclaredMethods()){
//
//            StringBuffer stringBuffer = new StringBuffer();
//            stringBuffer.append(Modifier.isStatic(method.getModifiers())?"static ":" ");
//            stringBuffer.append(method.getName());q
//
//            List<String> strs = new ArrayList<>();
//            for(Parameter parameter : method.getParameters()){
//                strs.add(parameter.getType().getSimpleName() + " " + parameter.getName());
//            }
//            stringBuffer.append("(")
//                    .append(String.join(",", strs))
//                    .append(");");
//            System.out.println(stringBuffer.toString());
//
//        }
//    }

    @Test
    /**
     * test011方法。
     */
    public void test011() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("123456");
        stringBuilder.setLength(7);
    }
}
