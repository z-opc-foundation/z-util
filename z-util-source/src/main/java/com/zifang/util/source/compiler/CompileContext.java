package com.zifang.util.source.compiler;

import javax.tools.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * 动态编译执行上下文
 * <p>
 * 封装了Java动态编译所需的全部组件和流程，
 * 包括编译器实例、文件管理器、类加载器、诊断收集器等。
 * 提供简洁的API来完成内存中的源代码编译和类加载。
 *
 * @author zifang
 * @version 1.0.0
 */
public class CompileContext {

    /**
     * 上下文唯一标识，基于时间戳生成
     */
    public static Long id = System.currentTimeMillis();

    /**
     * 系统Java编译器实例
     */
    private JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    /**
     * 自定义编译文件管理器
     */
    private CustomerCompileJavaFileManager customerCompileJavaFileManager;

    /**
     * 自定义类加载器
     */
    private CustomerCompileClassLoader classLoader;

    /**
     * 标准Java文件管理器
     */
    private StandardJavaFileManager standardJavaFileManager;

    /**
     * 诊断信息收集器
     */
    private DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();

    /**
     * 待编译的Java文件对象列表
     */
    private List<CharSequenceJavaFileObject> charSequenceJavaFileObjects = new ArrayList<>();


    /**
     * 添加Java文件对象到编译上下文
     *
     * @param uri            URI
     * @param javaFileObject Java文件对象
     */
    public void addJavaObject(URI uri, CharSequenceJavaFileObject javaFileObject) {

        this.customerCompileJavaFileManager.addJavaFileObject(uri, javaFileObject);

        charSequenceJavaFileObjects.add(javaFileObject);

    }

    /**
     * 初始化编译上下文
     * <p>
     * 初始化编译器实例、标准文件管理器、自定义类加载器和自定义文件管理器
     */
    public void initial() {

        // 获取标准的Java文件管理器实例
        standardJavaFileManager = compiler.getStandardFileManager(diagnosticCollector, null, null);
        // 初始化自定义类加载器
        classLoader = new CustomerCompileClassLoader(Thread.currentThread().getContextClassLoader());
        // 初始化自定义Java文件管理器实例
        customerCompileJavaFileManager = new CustomerCompileJavaFileManager(standardJavaFileManager, classLoader);
    }

    /**
     * 执行编译任务
     * <p>
     * 使用初始化的组件执行Java源代码编译，
     * 编译参数设置为1.8版本以确保兼容性
     */
    public void compile() {

        // 设置编译参数 - 指定编译版本为JDK1.8以提高兼容性
        List<String> options = new ArrayList<>();
        options.add("-source");
        options.add("1.8");
        options.add("-target");
        options.add("1.8");

        // 初始化一个编译任务实例
        JavaCompiler.CompilationTask compilationTask = compiler.getTask(
                null,
                customerCompileJavaFileManager,
                diagnosticCollector,
                options,
                null,
                charSequenceJavaFileObjects
        );
        // 执行编译任务
        Boolean result = compilationTask.call();

        System.out.println("编译状况：" + result);

    }

    /**
     * 从当前编译上下文加载类
     *
     * @param className 类名
     * @return Class对象，如果加载失败返回null
     */
    public Class<?> load(String className) {
        try {
            return this.classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
