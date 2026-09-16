package com.zifang.util.core.lang;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author: zifang
 * @time: 2019-05-08 17:11:00
 * @description: class type util
 * @version: JDK 1.8
 */
public class ClassUtil {

    /**
     * base type wrapper list
     */
    private static final List<String> BASE_WRAP_TYPE_LIST = new ArrayList<>();
    /**
     * base type list
     */
    private static final List<String> BASE_TYPE_LIST = new ArrayList<>();

    static {
        BASE_TYPE_LIST.add("int");
        BASE_TYPE_LIST.add("double");
        BASE_TYPE_LIST.add("long");
        BASE_TYPE_LIST.add("short");
        BASE_TYPE_LIST.add("byte");
        BASE_TYPE_LIST.add("boolean");
        BASE_TYPE_LIST.add("char");
        BASE_TYPE_LIST.add("float");

        BASE_WRAP_TYPE_LIST.add("java.lang.Integer");
        BASE_WRAP_TYPE_LIST.add("java.lang.Double");
        BASE_WRAP_TYPE_LIST.add("java.lang.Float");
        BASE_WRAP_TYPE_LIST.add("java.lang.Long");
        BASE_WRAP_TYPE_LIST.add("java.lang.Short");
        BASE_WRAP_TYPE_LIST.add("java.lang.Byte");
        BASE_WRAP_TYPE_LIST.add("java.lang.Boolean");
        BASE_WRAP_TYPE_LIST.add("java.lang.Character");
    }

    /**
     * 判断类名是否为Java基本类型（如int、double等）
     *
     * @param className 类的全限定名
     * @return 如果是基本类型返回true，否则返回false
     */
    public static boolean isPrimitive(String className) {
        return BASE_TYPE_LIST.contains(className);
    }

    /**
     * 判断类名是否为基本类型的包装类（如java.lang.Integer、java.lang.Double等）
     *
     * @param className 类的全限定名
     * @return 如果是包装类返回true，否则返回false
     */
    public static boolean isBaseWrap(String className) {
        return BASE_WRAP_TYPE_LIST.contains(className);
    }

    /**
     * 判断Class对象是否为基本类型的包装类
     *
     * @param clazz Class对象
     * @return 如果是包装类返回true，否则返回false
     */
    public static boolean isBaseWrap(Class<?> clazz) {
        return isBaseWrap(clazz.getCanonicalName());
    }

    /**
     * 判断类名是否为基本类型或其包装类
     *
     * @param className 类的全限定名
     * @return 如果是基本类型或包装类返回true，否则返回false
     */
    public static boolean isBaseOrWrap(String className) {
        return isPrimitive(className) || isBaseWrap(className);
    }

    /**
     * 判断Class对象是否为基本类型或其包装类
     *
     * @param clazz Class对象
     * @return 如果是基本类型或包装类返回true，否则返回false
     */
    public static boolean isBaseOrWrap(Class<?> clazz) {
        return isBaseOrWrap(clazz.getCanonicalName());
    }

    /**
     * 判断Class对象是否为基本类型、包装类或String类型
     *
     * @param clazz Class对象
     * @return 如果是基本类型、包装类或String返回true，否则返回false
     */
    public static boolean isBaseOrWrapOrString(Class<?> clazz) {
        return isBaseOrWrap(clazz.getCanonicalName()) || isSameClass(clazz, String.class);
    }

    /**
     * 判断对象是否为基本类型或其包装类的实例
     *
     * @param object 待检查的对象
     * @return 如果是基本类型或包装类实例返回true，null返回false
     */
    public static boolean isBaseOrWrap(Object object) {
        return null != object && isBaseOrWrap(object.getClass());
    }

    /**
     * 判断两个Class对象是否相同，通过类加载器和规范类名进行比较
     *
     * @param clazz 第一个Class对象
     * @param clz   第二个Class对象
     * @return 如果两者相同返回true，否则返回false
     */
    public static boolean isSameClass(Class<?> clazz, Class<?> clz) {
        if (null == clazz && null == clz) {
            return true;
        }
        if (null == clazz || null == clz) {
            return false;
        }
        return clazz.isAssignableFrom(clz) && clz.isAssignableFrom(clazz)
                && clazz.getCanonicalName().equals(clz.getCanonicalName())
                && clazz.getClassLoader() == clz.getClassLoader();
    }

    /**
     * 判断两个Class对象的类名是否相同（不考虑类加载器差异）
     *
     * @param clazz 第一个Class对象
     * @param clz   第二个Class对象
     * @return 如果类名相同返回true，否则返回false
     */
    public static boolean isSameNameClass(Class<?> clazz, Class<?> clz) {
        if (null == clazz && null == clz) {
            return true;
        }
        if (null == clazz || null == clz) {
            return false;
        }
        return clazz.getCanonicalName().equals(clz.getCanonicalName());
    }

    /**
     * 获取简短的类名，将包名中的每个部分缩写为首字母加点的形式
     * 例如：com.example.TestClass -> c.e.TestClass
     *
     * @param className 类的全限定名
     * @return 简化后的类名，如果输入为null则返回null
     */
    public static String getShortClassName(String className) {
        if (className == null) {
            return null;
        } else {
            String[] ss = className.split("\\.");
            StringBuilder sb = new StringBuilder(className.length());

            for (int i = 0; i < ss.length; ++i) {
                String s = ss[i];
                if (i != ss.length - 1) {
                    sb.append(s.charAt(0)).append('.');
                } else {
                    sb.append(s);
                }
            }

            return sb.toString();
        }
    }

    /**
     * 判断Class对象是否为JDK原生类型（如List.class、Map.class等）
     *
     * @param clazz Class对象
     * @return 如果是JDK原生类型返回true（即Class.getClassLoader()返回null）
     */
    public static boolean isOriginJdkType(Class<?> clazz) {
        return null == clazz.getClassLoader();
    }

    /**
     * 判断Class对象是否为Java原始数值类型（long、int、short、byte）
     *
     * @param clazz Class对象
     * @return 如果是原始数值类型返回true，否则返回false
     */
    public static boolean isPrimitiveNumberType(Class<?> clazz) {
        return long.class.isAssignableFrom(clazz) || int.class.isAssignableFrom(clazz)
                || short.class.isAssignableFrom(clazz) || byte.class.isAssignableFrom(clazz);
    }

    /**
     * 判断Class对象是否为Java原始浮点数值类型（double、float）
     *
     * @param clazz Class对象
     * @return 如果是原始浮点数值类型返回true，否则返回false
     */
    public static boolean isPrimitiveFloatingPointNumberType(Class<?> clazz) {
        return double.class.isAssignableFrom(clazz) || float.class.isAssignableFrom(clazz);
    }

    /**
     * 将对象数组转换为Class对象数组
     *
     * <p>如果对象为null，则对应位置返回null。基本类型会被转换为对应的包装类。</p>
     *
     * @param array Object对象数组
     * @return Class对象数组，如果输入为null则返回null
     */
    public static Class<?>[] toClass(final Object... array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return ArraysUtil.EMPTY_CLASS_ARRAY;
        }
        final Class<?>[] classes = new Class[array.length];
        for (int i = 0; i < array.length; i++) {
            classes[i] = array[i] == null ? null : array[i].getClass();
        }
        return classes;
    }

    /**
     * 将Class对象数组转换为字符串表示形式，格式为：(Class1Name, Class2Name, ...)
     *
     * @param argTypes Class对象数组
     * @return 格式化后的参数字符串
     */
    public static String argumentTypesToString(Class<?>[] argTypes) {
        StringBuilder buf = new StringBuilder();
        buf.append("(");
        if (argTypes != null) {
            for (int i = 0; i < argTypes.length; i++) {
                if (i > 0) {
                    buf.append(", ");
                }
                Class<?> c = argTypes[i];
                buf.append((c == null) ? "null" : c.getName());
            }
        }
        buf.append(")");
        return buf.toString();
    }

    /**
     * 通过无参构造函数创建实例
     *
     * @param clazz 目标类，为null时抛出异常
     * @param <T>   目标类型
     * @return 新实例
     * @throws RuntimeException 类型为null、无无参构造函数或构造不可访问时抛出
     */
    public static <T> T newInstance(Class<T> clazz) {
        if (clazz == null) {
            throw new RuntimeException("Create new instance of null class failed");
        }
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Create new instance of " + clazz.getName()
                    + " failed: " + e.getMessage(), e);
        }
    }

    /**
     * 扫描指定包下的所有类（含子包）
     * <p>
     * 同时支持目录形式的类路径和jar包形式，无法加载的类（依赖缺失、访问受限等）会被跳过。
     *
     * @param packageName 包名，如"com.example.pkg"
     * @return 扫描到的类列表；包不存在时返回空列表
     * @throws RuntimeException 类路径资源读取失败时抛出
     */
    public static List<Class<?>> scanClasses(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        if (packageName == null || packageName.isEmpty()) {
            return classes;
        }
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ClassUtil.class.getClassLoader();
        }
        try {
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                if ("file".equals(url.getProtocol())) {
                    scanDirectory(new File(url.toURI()), packageName, classes);
                } else if ("jar".equals(url.getProtocol())) {
                    scanJar(url, path, classes);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Scan classes of package " + packageName
                    + " failed: " + e.getMessage(), e);
        }
        return classes;
    }

    /**
     * 递归扫描目录形式的类路径
     */
    private static void scanDirectory(File dir, String packageName, List<Class<?>> classes) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "."
                        + file.getName().substring(0, file.getName().length() - ".class".length());
                addClassIfLoadable(className, classes);
            }
        }
    }

    /**
     * 扫描jar包形式的类路径，剥离"file:...!"前缀后打开jar
     */
    private static void scanJar(URL url, String path, List<Class<?>> classes) throws IOException {
        String jarPath = url.getPath();
        if (jarPath.startsWith("file:")) {
            jarPath = jarPath.substring("file:".length());
        }
        int exclamationIndex = jarPath.indexOf('!');
        if (exclamationIndex >= 0) {
            jarPath = jarPath.substring(0, exclamationIndex);
        }
        try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!entry.isDirectory() && name.startsWith(path) && name.endsWith(".class")) {
                    String className = name.substring(0, name.length() - ".class".length())
                            .replace('/', '.');
                    addClassIfLoadable(className, classes);
                }
            }
        }
    }

    /**
     * 加载类并加入结果，无法加载时静默跳过
     */
    private static void addClassIfLoadable(String className, List<Class<?>> classes) {
        try {
            classes.add(Class.forName(className, false, Thread.currentThread().getContextClassLoader()));
        } catch (Throwable ignore) {
            // 依赖缺失或不可访问的类跳过
        }
    }

}
