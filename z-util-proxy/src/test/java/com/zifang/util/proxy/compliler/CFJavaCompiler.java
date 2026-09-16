package com.zifang.util.proxy.compliler;


import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.*;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CFJavaCompiler类。
 */
public class CFJavaCompiler {
    private static final Logger log = LoggerFactory.getLogger(CFJavaCompiler.class);

    /**
     * 将javaCode 编译成为类
     */
    /**
     * compile方法。
     * * @param className String类型参数
     *
     * @param javaCode String类型参数
     * @return static Map<String, BytesJavaFileObject>类型返回值
     */
    public static Map<String, BytesJavaFileObject> compile(String className, String javaCode) throws Exception {

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        CFJavaFileManager fileManager = JavaFileManagerFactory.getJavaFileManager(compiler.getStandardFileManager(diagnostics, null, null));

        List<JavaFileObject> javaFileObjects = new ArrayList<>();
        javaFileObjects.add(new StringJavaFileObject(className, javaCode));

        //使用编译选项可以改变默认编译行为。编译选项是一个元素为String类型的Iterable集合
        List<String> options = new ArrayList<>();
        options.add("-encoding");
        options.add("UTF-8");
        options.add("-classpath");
        options.add(System.getProperty("java.class.path"));


        StringWriter outWriter = new StringWriter();
        JavaCompiler.CompilationTask task = compiler.getTask(outWriter, fileManager, diagnostics, options, null, javaFileObjects);
        // 编译源代码
        boolean success = task.call();

        if (success) {
            return fileManager.getFileObjectHashMap();
        } else {

            log.info(javaCode);

            //如果想得到具体的编译错误，可以对Diagnostics进行扫描
            StringBuilder error = new StringBuilder();
            for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
                error.append(compilePrint(diagnostic));
            }
            log.error("编译失败. \noutWriter:{} \ndiagnostics info:{}", outWriter.toString(), error.toString());
        }

        return Maps.newHashMapWithExpectedSize(0);
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
