package com.zifang.util.proxy.a.decompile.core;

/**
 * 访问标志转换器
 * <p>
 * 将JVM访问标志转换为Java源码中的访问修饰符。
 */
public class AccessFlagConvertor {

    /**
     * classAccessFlagConvertor方法。
     * * @param hexString String类型参数
     *
     * @return static String类型返回值
     */
    public static String classAccessFlagConvertor(String hexString) {
        String[] array = new String[4];
        for (int i = 0; i < 4; i++) {
            int startPointer = i;
            array[i] = hexString.substring(startPointer, startPointer + 1);
        }

        StringBuffer result = new StringBuffer();
        if (array[3].equals("1")) {
            result.append("public ");
            ;
        }
        if (array[3].equals("2")) {
            result.append("private ");
        }
        if (array[3].equals("4")) {
            result.append("protected ");
        }
        if (array[3].equals("8")) {
            result.append("static ");
        }
        if (array[3].equals("9")) {
            result.append("public static ");
        }
        if (array[3].equals("A")) {
            result.append("private static ");
        }
        if (array[3].equals("C")) {
            result.append("protected static ");
        }

        if (array[2].equals("1")) {
            result.append("final ");
        }
        if (array[1].equals("1")) {
            result.append("native ");
        }
        if (array[1].equals("4")) {
            result.append("abstract ");
        }
        if (array[0].equals("1")) {
            result.append("synthetic ");
        }
        return result.toString();
    }

    /**
     * methodAccessFlagConvertor方法。
     * * @param hexString String类型参数
     *
     * @return static String类型返回值
     */
    public static String methodAccessFlagConvertor(String hexString) { // 0x0001
        String[] array = new String[4];
        for (int i = 0; i < 4; i++) {
            int startPointer = i;
            array[i] = hexString.substring(startPointer, startPointer + 1);
        }

        StringBuffer result = new StringBuffer();
        if (array[3].equals("1")) {
            result.append("public ");
            ;
        }
        if (array[3].equals("2")) {
            result.append("private ");
        }
        if (array[3].equals("4")) {
            result.append("protected ");
        }
        if (array[3].equals("8")) {
            result.append("static ");
        }
        if (array[3].equals("9")) {
            result.append("public static ");
        }
        if (array[3].equals("A")) {
            result.append("private static ");
        }
        if (array[3].equals("C")) {
            result.append("protected static ");
        }

        if (array[2].equals("1")) {
            result.append("final ");
        }
        if (array[1].equals("1")) {
            result.append("native ");
        }
        if (array[1].equals("4")) {
            result.append("abstract ");
        }
        if (array[0].equals("1")) {
            result.append("synthetic ");
        }
        return result.toString();
    }

    /**
     * 字段 访问标识 转换器。
     *
     * @param hexString
     * @return
     */
    public static String fieldAccessFlagConvertor(String hexString) {
        String[] array = new String[4];
        for (int i = 0; i < 4; i++) {
            int startPointer = i;
            array[i] = hexString.substring(startPointer, startPointer + 1);
        }

        StringBuffer result = new StringBuffer();
        if (array[3].equals("1")) {
            result.append("public ");
            ;
        }
        if (array[3].equals("2")) {
            result.append("private ");
        }
        if (array[3].equals("4")) {
            result.append("protected ");
        }
        if (array[3].equals("8")) {
            result.append("static ");
        }
        if (array[3].equals("9")) {
            result.append("public static ");
        }
        if (array[3].equals("A")) {
            result.append("private static ");
        }
        if (array[3].equals("C")) {
            result.append("protected static ");
        }

        if (array[2].equals("1")) {
            result.append("final ");
        }
        if (array[1].equals("1")) {
            result.append("native ");
        }
        if (array[1].equals("4")) {
            result.append("abstract ");
        }
        if (array[0].equals("1")) {
            result.append("synthetic ");
        }
        return result.toString();
    }

}
