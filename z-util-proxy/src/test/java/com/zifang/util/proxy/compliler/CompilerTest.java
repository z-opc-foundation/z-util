package com.zifang.util.proxy.compliler;

import javax.swing.*;
import javax.tools.*;
import javax.tools.JavaFileObject.Kind;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * @author zifang Horstmann
 * @version 1.00 2007-10-28
 */

/**
 * CompilerTest类。
 */
public class CompilerTest {
    /**
     * main方法。
     * * @param args final类型参数
     *
     * @return static void类型返回值
     */
    public static void main(final String[] args) throws IOException, ClassNotFoundException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        final List<ByteArrayJavaClass> classFileObjects = new ArrayList<>();

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        JavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
        fileManager = new ForwardingJavaFileManager<JavaFileManager>(fileManager) {
            /**
             * getJavaFileForOutput方法。
             *      * @param location Location类型参数
             * @param className final类型参数
             * @param kind Kind类型参数
             * @param sibling FileObject类型参数
             * @return JavaFileObject类型返回值
             */
            public JavaFileObject getJavaFileForOutput(Location location, final String className,
                                                       Kind kind, FileObject sibling) throws IOException {
                System.out.println("fileManager----" + className);
                if (className.startsWith("x.")) {
                    ByteArrayJavaClass fileObject = new ByteArrayJavaClass(className);
                    classFileObjects.add(fileObject);
                    return fileObject;
                } else return super.getJavaFileForOutput(location, className, kind, sibling);
            }
        };


        String frameClassName = args.length == 0 ? "cn.com.infcn.compiler.ButtonFrame" : args[0];
        JavaFileObject source = buildSource(frameClassName);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null,
                null, Arrays.asList(source));
        Boolean result = task.call();

        for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics())
            System.out.println(d.getKind() + ": " + d.getMessage(null));
        fileManager.close();
        if (!result) {
            System.out.println("Compilation failed.");
            System.exit(1);
        }

        EventQueue.invokeLater(new Runnable() {
            /**
             * run方法。
             */
            public void run() {
                try {
                    Map<String, byte[]> byteCodeMap = new HashMap<>();
                    for (ByteArrayJavaClass cl : classFileObjects)
                        byteCodeMap.put(cl.getName().substring(1), cl.getBytes());
                    ClassLoader loader = new MapClassLoader(byteCodeMap);
                    JFrame frame = (JFrame) loader.loadClass("x.Frame").newInstance();
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setTitle("CompilerTest");
                    frame.setVisible(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    /*
     * Builds the source for the subclass that implements the addEventHandlers method.
     * @return a file object containing the source in a string builder
     */
    static JavaFileObject buildSource(String superclassName)
            throws IOException, ClassNotFoundException {
        StringBuilderJavaSource source = new StringBuilderJavaSource("x.Frame");
        source.append("package x;\n");
        source.append("public class Frame extends " + superclassName + " {");
        source.append("protected void addEventHandlers() {");
        final Properties props = new Properties();
        props.load(Class.forName(superclassName).getResourceAsStream("action.properties"));
        for (Map.Entry<Object, Object> e : props.entrySet()) {
            String beanName = (String) e.getKey();
            String eventCode = (String) e.getValue();
            source.append(beanName + ".addActionListener(new java.awt.event.ActionListener() {");
            source.append("public void actionPerformed(java.awt.event.ActionEvent event) {");
            source.append(eventCode);
            source.append("} } );");
        }
        source.append("} }");
        return source;
    }
}
