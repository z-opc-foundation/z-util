package com.zifang.util.proxy.compliler.demo;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;

/**
 * CompilerTest类。
 */
public class CompilerTest extends ClassLoader {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws IOException {
        //编译class
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null);
        Iterable iterable = manager.getJavaFileObjects("");
        JavaCompiler.CompilationTask task = compiler.getTask(null, manager, null, null, null, iterable);
        task.call();
        manager.close();
    }

    /**
     * a方法。
     */
    public void a() {
        //从class loader 里面搞出来
        //defineClass()
    }
}
