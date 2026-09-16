package com.zifang.util.core.compile;


import javax.tools.*;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Java 源码动态编译器。
 * <p>
 * 提供将 Java 源码字符串动态编译为字节码的能力。
 *
 * @author zifang
 * @see JavaCompiler
 * @see BytesJavaFileObject
 */
public class CustomerJavaCompiler {

    /**
     * ArrayList<>方法。
     *
     * @return static List<String> jarInnerLibPathList = new类型返回值
     */
    public static List<String> jarInnerLibPathList = new ArrayList<>();
    private static Boolean jarInnerLibPathListGeneratedFlag = false;

    /**
     * generateClassPath方法。
     *
     * @return static void类型返回值
     */
    public static void generateClassPath() {
        if (!jarInnerLibPathListGeneratedFlag) {
//            ApplicationHome  applicationHome = new ApplicationHome();
//            if(applicationHome.getSource() != null){
//                String jarPath = applicationHome.getSource().getAbsolutePath();
//
//                String to = new File(jarPath).getParentFile().getParentFile().toString()+"/unpack";
//                String tempTo = System.getProperty("java.io.tmpdir")+"/unpack";
//                try {
//                    Unpack.fun(jarPath, to);
//                    for(File file : new File(to + "/BOOT-INF/lib").listFiles()){
//                        jarInnerLibPathList.add(file.getAbsolutePath());
//                    }
//                    jarInnerLibPathListGeneratedFlag = true;
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    LoggerUtil.error("产生ClassPath失败"+ e.getMessage());
//                }
        } else {
            jarInnerLibPathListGeneratedFlag = true;
        }
    }

    /**
     * 将javaCode 编译成为类
     */
    public static Map<String, BytesJavaFileObject> compile(String className, String javaCode) throws Exception {

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        CustomerJavaFileManager fileManager = JavaFileManagerFactory.getJavaFileManager(compiler.getStandardFileManager(diagnostics, null, null));

        List<JavaFileObject> javaFileObjects = new ArrayList<>();
        javaFileObjects.add(new StringJavaFileObject(className, javaCode));

        // 产生附带jar
        generateClassPath();
        String classPath = System.getProperty("java.class.path");
        if (jarInnerLibPathList != null && jarInnerLibPathList.size() > 0) {
            classPath = classPath + ":" + String.join(":", jarInnerLibPathList);
        }


        //使用编译选项可以改变默认编译行为。编译选项是一个元素为String类型的Iterable集合
        List<String> options = new ArrayList<>();
        options.add("-encoding");
        options.add("UTF-8");
        options.add("-classpath");
        options.add(classPath);

        StringWriter outWriter = new StringWriter();
        JavaCompiler.CompilationTask task = compiler.getTask(outWriter, fileManager, diagnostics, options, null, javaFileObjects);
        // 编译源代码
        boolean success = task.call();

        if (success) {
            return fileManager.getFileObjectHashMap();
        } else {


            //如果想得到具体的编译错误，可以对Diagnostics进行扫描
            StringBuilder error = new StringBuilder();
            for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
                error.append(compilePrint(diagnostic));
            }
//            LoggerUtil.error("编译失败. \noutWriter:{} \ndiagnostics info:{}", outWriter.toString(), error.toString());
        }

        return new HashMap<>(0);
    }


    /**
     * compile方法。
     * * @param stringJavaFileObjects ListStringJavaFileObject类型参数
     *
     * @return static Map<String, BytesJavaFileObject>类型返回值
     */
    public static Map<String, BytesJavaFileObject> compile(List<StringJavaFileObject> stringJavaFileObjects) throws Exception {

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        CustomerJavaFileManager fileManager = JavaFileManagerFactory.getJavaFileManager(compiler.getStandardFileManager(diagnostics, null, null));

        List<JavaFileObject> javaFileObjects = new ArrayList<>();
        javaFileObjects.addAll(stringJavaFileObjects);

        // 产生附带jar
        generateClassPath();
        String classPath = System.getProperty("java.class.path");
        if (jarInnerLibPathList != null && jarInnerLibPathList.size() > 0) {
            classPath = classPath + ":" + String.join(":", jarInnerLibPathList);
        }


        //使用编译选项可以改变默认编译行为。编译选项是一个元素为String类型的Iterable集合
        List<String> options = new ArrayList<>();
        options.add("-encoding");
        options.add("UTF-8");
        options.add("-classpath");
        options.add(classPath);

        StringWriter outWriter = new StringWriter();
        JavaCompiler.CompilationTask task = compiler.getTask(outWriter, fileManager, diagnostics, options, null, javaFileObjects);
        // 编译源代码
        boolean success = task.call();

        if (success) {
            return fileManager.getFileObjectHashMap();
        } else {
            //如果想得到具体的编译错误，可以对Diagnostics进行扫描
            StringBuilder error = new StringBuilder();
            for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
                error.append(compilePrint(diagnostic));
            }
//            LoggerUtil.error("编译失败. \noutWriter:{} \ndiagnostics info:{}", outWriter.toString(), error.toString());
        }

        return new HashMap<>(0);
    }


    private static String compilePrint(Diagnostic diagnostic) {
        return "Code:[" + diagnostic.getCode() + "]\n" +
                "Kind:[" + diagnostic.getKind() + "]\n" +
                "Position:[" + diagnostic.getPosition() + "]\n" +
                "Start Position:[" + diagnostic.getStartPosition() + "]\n" +
                "End Position:[" + diagnostic.getEndPosition() + "]\n" +
                "Source:[" + diagnostic.getSource() + "]\n" +
                "Message:[" + diagnostic.getMessage(null) + "]\n" +
                "LineNumber:[" + diagnostic.getLineNumber() + "]\n" +
                "ColumnNumber:[" + diagnostic.getColumnNumber() + "]\n";
    }
}
