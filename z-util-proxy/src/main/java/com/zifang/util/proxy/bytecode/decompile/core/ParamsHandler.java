package com.zifang.util.proxy.bytecode.decompile.core;

import java.util.ArrayList;
import java.util.List;

/**
 * 参数处理器
 * <p>
 * 处理字节码方法参数的转换和解析。
 */
public class ParamsHandler {

    /**
     * 获取 方法的参数，放入 List 中
     * <p>
     * 递归方法解析 类似 Ljava/util/Map<Ljava/lang/String;Lcom/jiefupay/service/Test;>;I   的参数
     * 方法：
     * 1.切分头部，中部，尾部。
     * 2.如果是全部基本数据类型的进行 paramsList 填充（判断是否为基本类型数组）；
     * 3.如果是引用类型判断是否是泛型参数，如果不是泛型直接填充（判断是否为引用类型数组）；
     * 4.如果是泛型参数，解析泛型<>中的参数，利用递归。
     *
     * @param src
     * @param params
     */
    public static void getParams(String src, List<String> params) {

        // 如果不存在任何参数
        if (src.length() == 0) {
            return;
        }
        // 如果都是基本数据类型
        if (!src.contains(";")) {
            pureBasicTypeFill(src, params);
        }
        // 如果存在引用类型
        if (src.contains(";")) {

            /**********************************************************************************************************
             * 开头
             */
            // 引用类型前的 '[' 个数（如 "[Ljava/lang/String;I" 中 String 前有 1 个）
            int lIdx = src.indexOf("L");
            int dims = 0;
            while (lIdx - 1 - dims >= 0 && src.charAt(lIdx - 1 - dims) == '[') {
                dims++;
            }
            String front = src.substring(0, lIdx - dims);
            // 填充
            pureBasicTypeFill(front, params);


            /**********************************************************************************************************
             * 中间和尾部
             */
            // 中间
            String middle = "";
            // 尾部
            String laString = "";

            String after = src.substring(src.indexOf("L"), src.indexOf(";") + 1);
            if (after.contains("<")) {
                // 带有泛型的参数
                middle = src.substring(src.indexOf("L"), src.indexOf(">") + 2);
                // Ljava/util/Map<Ljava/lang/String;Lcom/jiefupay/service/Test;>;

                String genericParamsString = middle.substring(middle.indexOf("<") + 1, middle.indexOf(">"));

                // 暂时存放泛型参数的list ，也调用本方法，进行解析。
                List<String> genericParams = new ArrayList<>();
                getParams(genericParamsString, genericParams);

                // 遍历泛型参数 list，进行拼接字符串。
                StringBuffer sBuffer = new StringBuffer();
                for (String temp : genericParams) {
                    sBuffer.append(temp + ", ");
                }
                String result = sBuffer.toString();

                // 泛型参数的名称
                String tempStringafter = after.substring(0, after.indexOf("<") + 1);
                String paramName = middle.substring(tempStringafter.lastIndexOf("/") + 1, tempStringafter.indexOf("<"));
                // 泛型变量<>中的字符串
                String genericparams = result.substring(0, result.length() - 2);
                // 添加到 paramsList
                params.add(paramName + "<" + genericparams + ">" + brackets(dims));

                laString = src.substring(src.indexOf(">") + 2);
            } else {
                // 不带泛型的参数
                middle = after.substring(after.indexOf("L"), after.indexOf(";"));
                params.add(middle.substring(middle.lastIndexOf("/") + 1, middle.length()) + brackets(dims));
                laString = src.substring(src.indexOf(";") + 1);
            }

            // 尾部填充
            getParams(laString, params);

        }
    }

    private static String brackets(int dims) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dims; i++) {
            sb.append("[]");
        }
        return sb.toString();
    }

    /**
     * 类型解析
     *
     * @param src
     * @param params
     */
    public static void pureBasicTypeFill(String src, List<String> params) {
        for (int i = 0; i < src.length(); i++) {
            String content = "";

            // 判断是否为一维数组
            if (src.substring(i, i + 1).equals("[")) {
                content = src.substring(i, i + 2);
                // 判断是否为二维数组
                if (src.substring(i, i + 2).equals("[[")) {
                    content = src.substring(i, i + 3);
                    // 判断是否是三维数组
                    if (src.substring(i, i + 3).equals("[[[")) {
                        content = src.substring(i, i + 4);
                        if (src.substring(i, i + 4).equals("[[[[")) {
                            content = src.substring(i, i + 5);
                        } else {
                            content = src.substring(i, i + 4);
                            i++;
                            i++;
                            i++;
                        }
                    } else {
                        content = src.substring(i, i + 3);
                        i++;
                        i++;
                    }
                } else {
                    content = src.substring(i, i + 2);
                    i++;
                }
            } else {
                content = src.substring(i, i + 1);
            }

            String type = ParamsConvertor.paramsConvertorMethodReturnType(content);
            params.add(type);
        }
    }
}
