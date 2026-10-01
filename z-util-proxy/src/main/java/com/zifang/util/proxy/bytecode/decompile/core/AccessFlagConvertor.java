package com.zifang.util.proxy.bytecode.decompile.core;

/**
 * 访问标志转换器
 * <p>
 * 将JVM访问标志转换为Java源码中的访问修饰符。
 * 按位掩码解析（ACC_PUBLIC=0x0001 / ACC_PRIVATE=0x0002 / ACC_PROTECTED=0x0004 /
 * ACC_STATIC=0x0008 / ACC_FINAL=0x0010 / ACC_NATIVE=0x0100 / ACC_ABSTRACT=0x0400 /
 * ACC_SYNTHETIC=0x1000），与 flag 组合方式无关。
 */
public class AccessFlagConvertor {

    private static String convert(int flags) {
        StringBuilder result = new StringBuilder();
        if ((flags & 0x0001) != 0) {
            result.append("public ");
        }
        if ((flags & 0x0002) != 0) {
            result.append("private ");
        }
        if ((flags & 0x0004) != 0) {
            result.append("protected ");
        }
        if ((flags & 0x0008) != 0) {
            result.append("static ");
        }
        if ((flags & 0x0010) != 0) {
            result.append("final ");
        }
        if ((flags & 0x0100) != 0) {
            result.append("native ");
        }
        if ((flags & 0x0400) != 0) {
            result.append("abstract ");
        }
        if ((flags & 0x1000) != 0) {
            result.append("synthetic ");
        }
        return result.toString();
    }

    /**
     * 类访问标志转换
     *
     * @param hexString 4位大写十六进制，如 "0031"
     */
    public static String classAccessFlagConvertor(String hexString) {
        return convert(Integer.parseInt(hexString, 16));
    }

    /**
     * 方法访问标志转换
     *
     * @param hexString 4位大写十六进制，如 "0001"
     */
    public static String methodAccessFlagConvertor(String hexString) {
        return convert(Integer.parseInt(hexString, 16));
    }

    /**
     * 字段访问标志转换
     *
     * @param hexString 4位大写十六进制，如 "0019"
     */
    public static String fieldAccessFlagConvertor(String hexString) {
        return convert(Integer.parseInt(hexString, 16));
    }
}
