package com.zifang.util.core.lang.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * ExceptionUtil类。
 */
public class ExceptionUtil {

    /**
     * 只返回指定包中的异常堆栈信息
     *
     * @param e           异常信息
     * @param packageName 只转换某个包下的信息
     * @return string
     */
    public static String stackTraceToString(Throwable e, String packageName) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw, true));
        String str = sw.toString();
        if (packageName == null) {
            return str;
        }
        String[] arrs = str.split("\n");
        StringBuilder sbuf = new StringBuilder();
        sbuf.append(arrs[0]).append("\n");
        for (int i = 0; i < arrs.length; i++) {
            String temp = arrs[i];
            if (temp != null && temp.indexOf(packageName) > 0) {
                sbuf.append(temp).append("\n");
            }
        }
        return sbuf.toString();
    }

    /**
     * 获取异常信息
     *
     * @param e 异常信息
     * @return string
     */
    public static String stackTraceToString(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw, true));
        return sw.toString();
    }
}
