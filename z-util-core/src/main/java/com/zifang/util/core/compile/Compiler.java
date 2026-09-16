package com.zifang.util.core.compile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zifang
 */
public class Compiler {

    private static CustomerClassLoader defineClassLoader = new CustomerClassLoader(Thread.currentThread().getContextClassLoader());

    /**
     * compile方法。
     * * @param packageName String类型参数
     *
     * @param simpleName String类型参数
     * @param script     String类型参数
     * @return static Class<?>类型返回值
     */
    public static Class<?> compile(String packageName, String simpleName, String script) {
        Class<?> clazz = null;

        try {
            Map<String, BytesJavaFileObject> map = CustomerJavaCompiler.compile(simpleName, script);
            String className = packageName + "." + simpleName;

            BytesJavaFileObject bytesJavaFileObject = map.get(className);
            if (bytesJavaFileObject == null) {
                throw new RuntimeException(String.format("cannot found class:%s", className));
            }

            clazz = defineClassLoader.defineClass(className, bytesJavaFileObject.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clazz;
    }

    /**
     * compile方法。
     * * @param scripts ListStringJavaFileObject类型参数
     *
     * @param getClass String类型参数
     * @return static Class<?>类型返回值
     */
    public static Class<?> compile(List<StringJavaFileObject> scripts, String getClass) {
        Class<?> clazz = null;
        Class returnClass = null;
        try {
            Map<String, BytesJavaFileObject> map = CustomerJavaCompiler.compile(scripts);

            for (Map.Entry<String, BytesJavaFileObject> entry : map.entrySet()) {

                BytesJavaFileObject bytesJavaFileObject = map.get(entry.getKey());
                if (bytesJavaFileObject == null) {
                    throw new RuntimeException(String.format("cannot found class:%s", entry.getKey()));
                }

                if (entry.getKey().equals(getClass)) {
                    returnClass = defineClassLoader.defineClass(entry.getKey(), bytesJavaFileObject.getBytes());
                } else {
                    // 主动拉到jvm
                    clazz = defineClassLoader.defineClass(entry.getKey(), bytesJavaFileObject.getBytes());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnClass;
    }


    /**
     * compile方法。
     * * @param scriptCodeMap MapString,String类型参数
     *
     * @param getClass String类型参数
     * @return static Class<?>类型返回值
     */
    public static Class<?> compile(Map<String, String> scriptCodeMap, String getClass) {
        List<StringJavaFileObject> scripts = new ArrayList<>();
        scriptCodeMap.forEach((key, value) -> scripts.add(new StringJavaFileObject(key, value)));
        return compile(scripts, getClass);
    }
}
