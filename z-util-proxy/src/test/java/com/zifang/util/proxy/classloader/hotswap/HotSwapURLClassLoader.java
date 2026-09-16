package com.zifang.util.proxy.classloader.hotswap;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * 只要功能是重新加载更改过的.class文件，达到热替换的作用
 *
 * @author zifang
 */

/**
 * HotSwapURLClassLoader类。
 */
public class HotSwapURLClassLoader extends URLClassLoader {
    // 缓存加载class文件的最后最新修改时间
    /**
     * Long>方法。
     *
     * @return static Map<String, Long> cacheLastModifyTimeMap = new HashMap<String,类型返回值
     */
    public static Map<String, Long> cacheLastModifyTimeMap = new HashMap<String, Long>();
    // 工程class类所在的路径
    public static String projectClassPath = "D:/work/git/jdk_demo/bin/";
    // 所有的测试的类都在同一个包下
    public static String packagePath = "cn/com/infcn/classloader/hotswap/";

    private static HotSwapURLClassLoader hcl = new HotSwapURLClassLoader();

    /**
     * HotSwapURLClassLoader方法。
     */
    public HotSwapURLClassLoader() {
        // 设置ClassLoader加载的路径
        super(getMyURLs());
    }

    /**
     * getClassLoader方法。
     *
     * @return static HotSwapURLClassLoader类型返回值
     */
    public static HotSwapURLClassLoader getClassLoader() {
        return hcl;
    }

    private static URL[] getMyURLs() {
        URL url = null;
        try {
            url = new File(projectClassPath).toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return new URL[]{url};
    }

    /**
     * 重写loadClass，不采用双亲委托机制("java."开头的类还是会由系统默认ClassLoader加载)
     */
    @Override
    /**
     * loadClass方法。
     *      * @param name String类型参数
     * @param resolve boolean类型参数
     * @return Class<?>类型返回值
     */
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class clazz = null;
        // 查看HotSwapURLClassLoader实例缓存下，是否已经加载过class
        // 不同的HotSwapURLClassLoader实例是不共享缓存的
        clazz = findLoadedClass(name);
        if (clazz != null) {
            if (resolve) {
                resolveClass(clazz);
            }
            // 如果class类被修改过，则重新加载
            if (isModify(name)) {
                hcl = new HotSwapURLClassLoader();
                clazz = customLoad(name, hcl);
            }
            return (clazz);
        }

        // 如果类的包名为"java."开始，则有系统默认加载器AppClassLoader加载
//		if (name.startsWith("java.")) {
        if (name.indexOf("hotswap") == -1) {
            try {
                // 得到系统默认的加载cl，即AppClassLoader
                ClassLoader system = ClassLoader.getSystemClassLoader();
                clazz = system.loadClass(name);
                if (clazz != null) {
                    if (resolve)
                        resolveClass(clazz);
                    return (clazz);
                }
            } catch (ClassNotFoundException e) {
                // Ignore
            }
        }

        return customLoad(name, this);
    }

    /**
     * load方法。
     * * @param name String类型参数
     *
     * @return Class类型返回值
     */
    public Class load(String name) throws Exception {
        return loadClass(name);
    }

    /**
     * 自定义加载
     *
     * @param name
     * @param cl
     * @return
     * @throws ClassNotFoundException
     */
    /**
     * customLoad方法。
     * * @param name String类型参数
     *
     * @param cl ClassLoader类型参数
     * @return Class类型返回值
     */
    public Class customLoad(String name, ClassLoader cl) throws ClassNotFoundException {
        return customLoad(name, false, cl);
    }

    /**
     * 自定义加载
     *
     * @param name
     * @param resolve
     * @return
     * @throws ClassNotFoundException
     */
    /**
     * customLoad方法。
     * * @param name String类型参数
     *
     * @param resolve boolean类型参数
     * @param cl      ClassLoader类型参数
     * @return Class类型返回值
     */
    public Class customLoad(String name, boolean resolve, ClassLoader cl) throws ClassNotFoundException {
        // findClass()调用的是URLClassLoader里面重载了ClassLoader的findClass()方法
        Class clazz = ((HotSwapURLClassLoader) cl).findClass(name);
        if (resolve)
            ((HotSwapURLClassLoader) cl).resolveClass(clazz);
        // 缓存加载class文件的最后修改时间
        long lastModifyTime = getClassLastModifyTime(name);
        cacheLastModifyTimeMap.put(name, lastModifyTime);
        return clazz;
    }

    /**
     * loadClass方法。
     * * @param name String类型参数
     *
     * @return Class<?>类型返回值
     */
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return loadClass(name, false);
    }

    @Override
    /**
     * findClass方法。
     *      * @param name String类型参数
     * @return Class<?>类型返回值
     */
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // TODO Auto-generated method stub
        return super.findClass(name);
    }

    /**
     * @param name
     * @return .class文件最新的修改时间
     */
    private long getClassLastModifyTime(String name) {
        String path = getClassCompletePath(name);
        File file = new File(path);
        if (!file.exists()) {
            throw new RuntimeException(new FileNotFoundException(name));
        }
        return file.lastModified();
    }

    /**
     * 判断这个文件跟上次比是否修改过
     *
     * @param name
     * @return
     */
    private boolean isModify(String name) {
        long lastmodify = getClassLastModifyTime(name);
        long previousModifyTime = cacheLastModifyTimeMap.get(name);
        return lastmodify > previousModifyTime;
    }

    /**
     * @param name
     * @return .class文件的完整路径 (e.g. E:/A.class)
     */
    private String getClassCompletePath(String name) {
        String simpleName = name.substring(name.lastIndexOf(".") + 1);
        return projectClassPath + packagePath + simpleName + ".class";
    }

}