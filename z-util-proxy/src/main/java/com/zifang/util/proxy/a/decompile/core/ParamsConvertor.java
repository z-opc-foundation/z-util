package com.zifang.util.proxy.a.decompile.core;

import java.util.ArrayList;
import java.util.List;

/**
 * 参数转换器
 * <p>
 * 将字节码参数描述符转换为Java方法签名。
 */
public class ParamsConvertor {

    /**
     * 字段的类型
     *
     * @param str
     * @return
     */
    public static String paramsConvertorFieldType(String str) {
        String origal = str;
        boolean flag = false;
        if (str.length() == 1 || !str.contains("[")) {
            // 如果不是数组
            flag = true;
        } else {
            if (!str.contains("/")) {
                // 如果是数组
                flag = false;
                str = str.substring(str.length() - 1);
            }
        }

        String result = "";
        switch (str) {
            case "B":
                result = "byte";
                break;
            case "C":
                result = "char";
                break;
            case "D":
                result = "double";
                break;
            case "F":
                result = "float";
                break;
            case "I":
                result = "int";
                break;
            case "J":
                result = "long";
                break;
            case "S":
                result = "short";
                break;
            case "Z":
                result = "boolean";
                break;
            case "V":
                result = "void";
                break;
            default:
                result = str.substring(str.lastIndexOf("/") + 1, str.length() - 1);
                break;
        }
        if (flag) {
            return result;
        } else {
            // 后边加上 n-1个[]
            for (int i = 0; i < origal.length() - 1; i++) {
                result = result + "[]";
            }
            return result;
        }
    }

    /**
     * 带泛型的参数
     *
     * @param signature
     * @param str
     * @return
     */
    public static String paramsConvertorFieldTypeWithGeneric(String signature, String str) {
        String inner = signature.substring(signature.indexOf("<") + 1, signature.indexOf(">"));
        List<String> params = new ArrayList<>();
        ParamsHandler.getParams(inner, params);

        StringBuffer sBuffer = new StringBuffer();
        for (String temp : params) {
            sBuffer.append(temp + ", ");
        }
        String result = sBuffer.toString();

        return str.substring(str.lastIndexOf("/") + 1, str.length() - 1) + "<"
                + result.substring(0, result.length() - 2) + ">";
    }

    /**
     * 方法参数
     *
     * @param str descriptor中的参数部分，如 "ILjava/lang/String;)V" -> "ILjava/lang/String;"
     * @return Java 参数列表字符串
     */
    public static String paramsConvertorMethodParams(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < str.length()) {
            char c = str.charAt(i);
            String type = parseType(str, i);
            if (!result.toString().isEmpty()) {
                result.append(", ");
            }
            result.append(paramsConvertorFieldType(type));
            i += type.length();
        }
        return result.toString();
    }

    /**
     * 解析类型描述符
     */
    private static String parseType(String str, int i) {
        char c = str.charAt(i);
        switch (c) {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'Z':
            case 'V':
                return String.valueOf(c);
            case 'L':
                int semicolon = str.indexOf(';', i);
                return str.substring(i, semicolon + 1);
            case '[':
                return String.valueOf(c) + parseType(str, i + 1);
            default:
                return String.valueOf(c);
        }
    }

    /**
     * 方法的返回值类型
     *
     * @param str
     * @return
     */
    public static String paramsConvertorMethodReturnType(String str) {
        String origal = str;
        boolean flag = false;
        if (str.length() == 1 || !str.contains("[")) {
            // 如果不是数组
            flag = true;
        } else {
            if (!str.contains("/")) {
                // 如果是数组
                flag = false;
                str = str.substring(str.length() - 1);
            }
        }
        String result = "";
        switch (str) {
            case "B":
                result = "byte";
                break;
            case "C":
                result = "char";
                break;
            case "D":
                result = "double";
                break;
            case "F":
                result = "float";
                break;
            case "I":
                result = "int";
                break;
            case "J":
                result = "long";
                break;
            case "S":
                result = "short";
                break;
            case "Z":
                result = "boolean";
                break;
            case "V":
                result = "void";
                break;
            default:
                result = str.substring(str.lastIndexOf("/") + 1, str.length() - 1);
                break;
        }
        if (flag) {
            return result;
        } else {
            // 后边加上 n-1个[]
            for (int i = 0; i < origal.length() - 1; i++) {
                result = result + "[]";
            }
            return result;
        }
    }

}
