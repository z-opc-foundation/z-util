package com.zifang.util.core;

/**
 * ResourceUtil类。
 */
public class ResourceUtil {
    /**
     * getFileFromResource方法。
     * * @param path String类型参数
     *
     * @return static String类型返回值
     */
    public static String getFileFromResource(String path) {
        return ResourceUtil.class.getResource("/").getPath() + path;
    }

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        System.out.println(getFileFromResource("aa"));
    }


    /**
     * 1. Class.getResourceAsStream(String path) ：
     * <p>
     * path 不以’/'开头时默认是从此类所在的包下取资源，以’/'开头则是从ClassPath根下获取。
     * 其只是通过path构造一个绝对路径，最终还是由ClassLoader获取 资 源 。
     */
    /**
     * test1方法。
     *
     * @return static void类型返回值
     */
    public static void test1() {
        //1. 要加载的文件和.class文件在同一目录下，例如：com.x.y 下有类me.class ,同时有资源文件myfile.xml
        ResourceUtil.class.getResourceAsStream("myfile.xml");

        //2. 在GetResourceAsStreamDemo.class目录的子目录下，例如：com.x.y 下有类GetResourceAsStreamDemo.class ,同时在 com.x.y.file 目录下有资源文件myfile.xml
        ResourceUtil.class.getResourceAsStream("file/myfile.xml");

        //第三：不在me.class目录下，也不在子目录下，例如：com.x.y 下有类me.class ,同时在 com.x.file 目录下有资源文件myfile.xml
        ResourceUtil.class.getResourceAsStream("/com/x/file/myfile.xml");
    }

    /**
     * 2. Class.getClassLoader.getResourceAsStream(String path) ：
     * <p>
     * 默认则是从ClassPath根下获取，path不能以’/'开头，最终是由ClassLoader获取资源。
     */
    /**
     * test2方法。
     *
     * @return static void类型返回值
     */
    public static void test2() {
        //最前面不能加/
        ResourceUtil.class.getClassLoader().getResource("com/x/file/myfile.xml");
    }

    /**
     * 3. ServletContext. getResourceAsStream(String path)：
     * 默认从WebAPP根目录下取资源，Tomcat下path是否以’/'开头无所谓，当然这和具体的容器实现有关。
     */
    /**
     * test3方法。
     *
     * @return static void类型返回值
     */
    public static void test3() {

    }

    /**
     * 4. Jsp下的application内置对象就是上面的ServletContext的一种实现。
     */
    /**
     * test4方法。
     *
     * @return static void类型返回值
     */
    public static void test4() {

    }
}
