package com.zifang.util.proxy.a.decompile.app;

import com.zifang.util.proxy.a.decompile.core.FileCreator;
import com.zifang.util.proxy.a.decompile.core.SrcCreator;
import com.zifang.util.proxy.a.model.ClassFile;
import com.zifang.util.proxy.a.resolver.ByteCodeResolver;

/**
 * 反编译器入口类
 * <p>
 * 两阶段架构：
 * 1. ByteCodeResolver 解析字节码 → ClassFile
 * 2. SrcCreator 生成 Java 源码
 */
public class App {

    /**
     * 反编译 class 文件
     *
     * @param classPath class 文件路径
     * @param outputDir 输出目录（可选，为 null 则只输出到控制台）
     */
    public static void decompile(String classPath, String outputDir) {
        try {
            // 1. 使用 ByteCodeResolver 解析字节码
            ClassFile classFile = ByteCodeResolver.parseFromFile(classPath);

            // 2. 使用 SrcCreator 直接生成 Java 源码
            String src = SrcCreator.createJavaFileSrc(classFile);
            System.out.println(src);

            // 3. 如果指定了输出目录，则写入文件
            if (outputDir != null && !outputDir.isEmpty()) {
                String className = extractClassName(classFile.getClassName());
                FileCreator.createFile(className, src, outputDir);
                System.out.println("已生成文件: " + outputDir + "/" + className + ".java");
            }

        } catch (Exception e) {
            System.err.println("反编译失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 反编译 class 文件（输出到控制台）
     *
     * @param classPath class 文件路径
     */
    public static void decompile(String classPath) {
        decompile(classPath, null);
    }

    /**
     * 从 class 文件内容反编译
     *
     * @param classData class 文件字节数组
     * @return 生成的 Java 源码
     */
    public static String decompile(byte[] classData) {
        try {
            // 1. 使用 ByteCodeResolver 解析字节码
            ClassFile classFile = ByteCodeResolver.parseFromStream(
                    new java.io.ByteArrayInputStream(classData));

            // 2. 使用 SrcCreator 直接生成 Java 源码
            return SrcCreator.createJavaFileSrc(classFile);

        } catch (Exception e) {
            throw new RuntimeException("反编译失败", e);
        }
    }

    /**
     * 从输入流反编译
     *
     * @param inputStream class 文件输入流
     * @return 生成的 Java 源码
     */
    public static String decompile(java.io.InputStream inputStream) {
        try {
            // 1. 使用 ByteCodeResolver 解析字节码
            ClassFile classFile = ByteCodeResolver.parseFromStream(inputStream);

            // 2. 使用 SrcCreator 直接生成 Java 源码
            return SrcCreator.createJavaFileSrc(classFile);

        } catch (Exception e) {
            throw new RuntimeException("反编译失败", e);
        }
    }

    /**
     * 从类名获取 class 文件路径并反编译
     *
     * @param className   完整类名（如 com.example.MyClass）
     * @param classLoader 类加载器
     */
    public static void decompileByName(String className, ClassLoader classLoader) {
        try {
            String resourcePath = className.replace('.', '/') + ".class";
            java.io.InputStream is = classLoader.getResourceAsStream(resourcePath);
            if (is == null) {
                throw new RuntimeException("找不到类: " + className);
            }

            String src = decompile(is);
            System.out.println(src);

        } catch (Exception e) {
            System.err.println("反编译失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 从类名获取 class 文件路径并反编译（使用默认类加载器）
     *
     * @param className 完整类名（如 com.example.MyClass）
     */
    public static void decompileByName(String className) {
        decompileByName(className, App.class.getClassLoader());
    }

    /**
     * 从 Class 对象反编译
     *
     * @param clazz Class 对象
     */
    public static void decompile(Class<?> clazz) {
        decompileByName(clazz.getName(), clazz.getClassLoader());
    }

    /**
     * 提取类名（不带包路径）
     */
    private static String extractClassName(String fullClassName) {
        if (fullClassName == null) {
            return "Unknown";
        }
        int lastSlash = fullClassName.lastIndexOf('/');
        return lastSlash >= 0 ? fullClassName.substring(lastSlash + 1) : fullClassName;
    }

    /**
     * 主方法 - 命令行入口
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        String classPath = args[0];
        String outputDir = args.length > 1 ? args[1] : null;

        decompile(classPath, outputDir);
    }

    /**
     * 打印使用说明
     */
    private static void printUsage() {
        System.out.println("Java 反编译器");
        System.out.println("用法:");
        System.out.println("  java App <class文件路径> [输出目录]");
        System.out.println("示例:");
        System.out.println("  java App /path/to/MyClass.class");
        System.out.println("  java App /path/to/MyClass.class /output/dir");
    }
}
