package com.zifang.util.expr.sql.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个类的方法为 SQL 函数。
 * 方法必须是 static 的，第一个参数为当前行的 Map（列名→值），其余参数为函数实参。
 * <p>
 * 用法：
 * <pre>
 * public class MyFunctions {
 *     &#64;SqlFunction("my_func")
 *     public static Object myFunc(Map<String, Object> row, Object arg1) {
 *         return arg1;
 *     }
 * }
 * SqlFunctionRegistry.get().register(MyFunctions.class);
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SqlFunction {
    /**
     * 函数名（不区分大小写）
     */
    String value();
}