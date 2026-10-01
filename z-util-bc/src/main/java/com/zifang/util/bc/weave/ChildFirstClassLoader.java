package com.zifang.util.bc.weave;

import java.util.Map;

/**
 * 子优先 ClassLoader：map 里的名字一律用给定字节定义，绕过父加载器中的同名类。
 * <p>
 * 用于字节级改写验证——织入后的类与原类同名，父优先委派会拿到原始类导致织入静默失效。
 */
public class ChildFirstClassLoader extends ClassLoader {

    private final Map<String, byte[]> definitions;

    public ChildFirstClassLoader(Map<String, byte[]> definitions, ClassLoader parent) {
        super(parent);
        this.definitions = definitions;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        byte[] bytes = definitions.get(name);
        if (bytes == null) {
            return super.loadClass(name, resolve);
        }
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = findLoadedClass(name);
            if (c == null) {
                c = defineClass(name, bytes, 0, bytes.length);
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }
}
