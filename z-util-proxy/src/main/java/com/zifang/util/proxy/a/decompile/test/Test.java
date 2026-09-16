package com.zifang.util.proxy.a.decompile.test;

import com.zifang.util.proxy.a.decompile.app.App;

/**
 * 测试类
 * <p>
 * 用于测试类文件反编译功能。
 */
public class Test {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        // 示例：反编译一个 class 文件
        String classPath = "/path/to/YourClass.class";

        // 方式1：直接反编译文件
        App.decompile(classPath);

        // 方式2：反编译并指定输出目录
        // App.decompile(classPath, "/output/directory");

        // 方式3：使用类名反编译（需要 classpath 中有该类）
        // App.decompileByName("com.example.MyClass");

        // 方式4：使用 Class 对象反编译
        // App.decompile(MyClass.class);
    }
}