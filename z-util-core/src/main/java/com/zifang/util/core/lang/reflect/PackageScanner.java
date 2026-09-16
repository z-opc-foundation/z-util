package com.zifang.util.core.lang.reflect;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * @author zifang
 */
public class PackageScanner {

    private static final String CLASS_SUFFIX = ".class";
    private static final String JAR = "jar";
    private static final String FILE = "file";
    private static final String defaultClassPath = ClassUtil.class.getResource("/").getPath();

    /***
     * 获得包下的所有类
     *
     * @param packageName 包名
     *
     * @return 该包下的类集合
     *
     * */
    public static Set<Class<?>> searchClasses(String packageName) {
        return searchClasses(packageName, null);
    }

    /***
     * 获得包下的所有类
     *
     * @param packageName 包名
     * @param predicate 过滤器
     *
     * @return 该包下的类集合
     *
     * */
    public static Set<Class<?>> searchClasses(String packageName, Predicate<Class<?>> predicate) {
        Set<Class<?>> set = search(packageName);
        if (predicate == null) {
            return set;
        } else {
            return set.stream().filter(predicate).collect(Collectors.toSet());
        }
    }

    private static Set<Class<?>> search(String packageName) {
        Set<Class<?>> classes = new HashSet<>();
        try {
            Enumeration<URL> urlEnumeration = Thread.currentThread().getContextClassLoader().getResources(packageName.replace(".", "/"));
            while (urlEnumeration.hasMoreElements()) {
                URL url = urlEnumeration.nextElement();
                String protocol = url.getProtocol();
                if (JAR.equalsIgnoreCase(protocol)) {
                    JarURLConnection connection = (JarURLConnection) url.openConnection();
                    if (connection != null) {
                        JarFile jarFile = connection.getJarFile();
                        if (jarFile != null) {
                            Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
                            while (jarEntryEnumeration.hasMoreElements()) {
                                JarEntry entry = jarEntryEnumeration.nextElement();
                                String jarEntryName = entry.getName();
                                if (jarEntryName.contains(CLASS_SUFFIX) && jarEntryName.replaceAll("/", ".").startsWith(packageName)) {
                                    String className = jarEntryName.substring(0, jarEntryName.lastIndexOf(".")).replace("/", ".");
                                    classes.add(Class.forName(className));
                                }
                            }
                        }
                    }
                } else if (FILE.equalsIgnoreCase(protocol)) {
                    Set<Class<?>> set = searchFromFile(packageName);
                    classes.addAll(set);
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        return classes;
    }

    /**
     * searchFromFile方法。
     * * @param packageName String类型参数
     *
     * @return static Set<Class<?>>类型返回值
     */
    public static Set<Class<?>> searchFromFile(String packageName) {
        String classpath = defaultClassPath;
        String basePackPath = packageName.replace(".", File.separator);
        String searchPath = classpath + basePackPath;
        return new ClassSearcher().doPath(new File(searchPath), packageName, true);
    }

    private static class ClassSearcher {

        private Set<Class<?>> classPaths = new HashSet<>();

        private Set<Class<?>> doPath(File file, String packageName, boolean flag) {

            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (!flag) {
                    packageName = packageName + "." + file.getName();
                }

                assert files != null;

                for (File f1 : files) {
                    doPath(f1, packageName, false);
                }
            } else {
                if (file.getName().endsWith(CLASS_SUFFIX)) {
                    try {
                        Class<?> clazz = Class.forName(packageName + "." + file.getName().substring(0, file.getName().lastIndexOf(".")));
                        classPaths.add(clazz);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            return classPaths;
        }
    }
}
