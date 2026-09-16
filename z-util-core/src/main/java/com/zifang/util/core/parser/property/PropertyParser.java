package com.zifang.util.core.parser.property;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * 属性文件解析器。
 * <p>
 * 提供读取和解析 .properties 文件的功能。
 *
 * @author zifang
 */
public class PropertyParser {

    /**
     * 从输入流加载属性。
     *
     * @param inputStream 输入流
     * @return 属性映射
     * @throws IOException 读取异常
     */
    public static Properties load(InputStream inputStream) throws IOException {
        Properties props = new Properties();
        props.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        return props;
    }

    /**
     * 从文件加载属性。
     *
     * @param file 配置文件
     * @return 属性映射
     * @throws IOException 读取异常
     */
    public static Properties load(File file) throws IOException {
        return load(file.toPath());
    }

    /**
     * 从 Path 加载属性。
     *
     * @param path 文件路径
     * @return 属性映射
     * @throws IOException 读取异常
     */
    public static Properties load(Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            return load(is);
        }
    }

    /**
     * 从类路径加载属性文件。
     *
     * @param resource 资源路径
     * @return 属性映射
     * @throws IOException 读取异常
     */
    public static Properties loadFromClasspath(String resource) throws IOException {
        InputStream is = PropertyParser.class.getClassLoader().getResourceAsStream(resource);
        if (is == null) {
            throw new IOException("Resource not found: " + resource);
        }
        try {
            return load(is);
        } finally {
            is.close();
        }
    }

    /**
     * 将属性保存到文件。
     *
     * @param props  属性映射
     * @param file   目标文件
     * @param header 文件头注释（可为 null）
     * @throws IOException 写入异常
     */
    public static void store(Properties props, File file, String header) throws IOException {
        try (OutputStream os = new FileOutputStream(file);
             OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            props.store(writer, header);
        }
    }

    /**
     * 获取字符串属性值。
     *
     * @param props      属性映射
     * @param key        属性键
     * @param defaultVal 默认值
     * @return 属性值或默认值
     */
    public static String getString(Properties props, String key, String defaultVal) {
        return props.getProperty(key, defaultVal);
    }

    /**
     * 获取整型属性值。
     *
     * @param props      属性映射
     * @param key        属性键
     * @param defaultVal 默认值
     * @return 属性值或默认值
     */
    public static int getInt(Properties props, String key, int defaultVal) {
        String val = props.getProperty(key);
        if (val == null) {
            return defaultVal;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    /**
     * 获取长整型属性值。
     *
     * @param props      属性映射
     * @param key        属性键
     * @param defaultVal 默认值
     * @return 属性值或默认值
     */
    public static long getLong(Properties props, String key, long defaultVal) {
        String val = props.getProperty(key);
        if (val == null) {
            return defaultVal;
        }
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    /**
     * 获取布尔型属性值。
     *
     * @param props      属性映射
     * @param key        属性键
     * @param defaultVal 默认值
     * @return 属性值或默认值
     */
    public static boolean getBoolean(Properties props, String key, boolean defaultVal) {
        String val = props.getProperty(key);
        if (val == null) {
            return defaultVal;
        }
        return Boolean.parseBoolean(val.trim());
    }
}