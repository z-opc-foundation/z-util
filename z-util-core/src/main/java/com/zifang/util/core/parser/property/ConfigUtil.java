package com.zifang.util.core.parser.property;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * 配置资源加载工具类
 * <p>
 * 该工具类提供了一套便捷的方法来加载配置文件，
 * 按照ClassLoader的优先级顺序查找资源。
 * </p>
 * <p>
 * ClassLoader查找顺序：
 * <ol>
 *   <li>Thread.currentThread().getContextClassLoader() - 线程上下文类加载器</li>
 *   <li>ConfigUtil.class.getClassLoader() - 当前类的类加载器</li>
 *   <li>ClassLoader.getSystemClassLoader() - 系统类加载器</li>
 * </ol>
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 * // 加载配置文件
 * URL url = ConfigUtil.findAsResource("config.properties");
 *
 * // 转换为文件路径
 * String path = ConfigUtil.toFilePath("config.properties");
 *
 * // 加载Properties
 * Properties props = ConfigUtil.loadProperties("application.properties");
 * </pre>
 * </p>
 *
 * @author zifang
 * @see Properties
 * @see ClassLoader
 */
public class ConfigUtil {

    /**
     * 查找配置文件资源
     * <p>
     * 按照ClassLoader优先级顺序查找指定的资源：
     * <ol>
     *   <li>线程上下文类加载器</li>
     *   <li>当前类的类加载器</li>
     *   <li>系统类加载器</li>
     * </ol>
     * 返回第一个找到的资源。
     * </p>
     * <p>
     * 资源路径可以是相对路径（如 "config.properties"）或绝对路径（如 "/com/example/config.xml"）。
     * </p>
     *
     * @param path 资源路径（相对路径或绝对路径）
     * @return 资源URL，未找到返回null
     */
    public static URL findAsResource(String path) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl != null) {
            URL url = cl.getResource(path);
            if (url != null) {
                return url;
            }
        }
        URL url = ConfigUtil.class.getClassLoader().getResource(path);
        if (url != null) {
            return url;
        }
        return ClassLoader.getSystemClassLoader().getResource(path);
    }

    /**
     * 将资源路径转换为文件系统路径
     * <p>
     * 该方法首先调用 {@link #findAsResource(String)} 查找资源，
     * 然后将资源的URL转换为本地文件系统的路径。
     * </p>
     * <p>
     * 注意：此方法只能处理文件系统的资源，无法处理JAR包内的资源。
     * </p>
     *
     * @param path 资源路径
     * @return 文件系统路径
     * @throws IllegalArgumentException 如果资源未找到
     */
    public static String toFilePath(String path) {
        URL url = findAsResource(path);
        if (url == null) {
            throw new IllegalArgumentException("Resource not found: " + path);
        }
        return new File(url.getFile()).getPath();
    }

    /**
     * 从资源路径加载 Properties 配置文件
     * <p>
     * 该方法首先调用 {@link #findAsResource(String)} 查找资源，
     * 然后打开输入流加载为 Properties 对象。
     * </p>
     * <p>
     * 使用完毕后不需要手动关闭输入流，该方法会自动关闭。
     * </p>
     * <p>
     * 示例：
     * <pre>
     * Properties props = ConfigUtil.loadProperties("application.properties");
     * String value = props.getProperty("key");
     * </pre>
     * </p>
     *
     * @param path 资源路径
     * @return Properties 对象，加载后的配置内容
     * @throws IOException 如果资源未找到或读取失败
     */
    public static Properties loadProperties(String path) throws IOException {
        URL url = findAsResource(path);
        if (url == null) {
            throw new IOException("Resource not found: " + path);
        }
        Properties properties = new Properties();
        try (InputStream is = url.openStream()) {
            properties.load(is);
        }
        return properties;
    }
}
