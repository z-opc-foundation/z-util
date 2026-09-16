package com.zifang.util.core.io.archive;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * JAR 包业务级操作封装
 * <p>
 * 提供按路径读取 entry、按条件过滤等高级 JAR 包操作功能。
 * 实现了 {@link Closeable} 接口，支持 try-with-resources 语法。
 * <p>
 * 示例用法：
 * <pre>{@code
 * // 读取 MANIFEST.MF 内容
 * try (JarWrapper jar = new JarWrapper("/path/to/app.jar")) {
 *     String manifest = jar.getString("META-INF/MANIFEST.MF");
 *     System.out.println(manifest);
 * }
 *
 * // 过滤所有 .class 文件
 * try (JarWrapper jar = new JarWrapper("/path/to/app.jar")) {
 *     Map<String, byte[]> classFiles = jar.getFileBytes(e -> e.getName().endsWith(".class"));
 *     classFiles.forEach((name, bytes) -> System.out.println(name + ": " + bytes.length + " bytes"));
 * }
 * }</pre>
 *
 * @author zifang
 * @see JarFile
 * @see JarEntry
 */
public class JarWrapper implements Closeable {

    private final JarFile jarFile;

    /**
     * 使用 JAR 文件路径创建 JarWrapper
     *
     * @param jarFilePath JAR 文件路径（必须是存在的 .jar 文件）
     * @throws IOException 如果文件不存在或不是有效的 JAR 文件
     */
    public JarWrapper(String jarFilePath) throws IOException {
        this.jarFile = new JarFile(jarFilePath);
    }

    /**
     * 使用 JAR 文件对象创建 JarWrapper
     *
     * @param jarFile JAR 文件对象（不能为 null）
     * @throws IOException 如果文件不存在或不是有效的 JAR 文件
     */
    public JarWrapper(File jarFilePath) throws IOException {
        this.jarFile = new JarFile(jarFilePath);
    }

    /**
     * 将输入流转换为字节数组
     *
     * @param input 输入流（不能为 null）
     * @return 包含输入流全部内容的字节数组
     * @throws IOException 如果读取过程中发生 IO 错误
     */
    private static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }

    /**
     * 获取 JAR 包中所有的条目
     * <p>
     * 返回 JAR 包内所有文件和目录条目的列表，按枚举顺序排列。
     *
     * @return 包含所有 JAR 条目的列表
     */
    public List<JarEntry> getEntries() {
        List<JarEntry> jarEntries = new ArrayList<>();
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            jarEntries.add(entries.nextElement());
        }
        return jarEntries;
    }

    /**
     * 根据路径读取 JAR 中指定 entry 的字节数组
     * <p>
     * 根据 JAR 内的路径获取对应条目的二进制内容。
     * 如果条目不存在或无法读取，返回空数组。
     *
     * @param pathInJar JAR 内的路径，例如 "META-INF/MANIFEST.MF" 或 "com/example/MyClass.class"
     * @return entry 内容字节数组，如果找不到则返回空数组
     * @throws UncheckedIOException 如果读取过程中发生 IO 错误
     */
    public byte[] getBytes(String pathInJar) {
        try {
            JarEntry entry = jarFile.getJarEntry(pathInJar);
            if (entry == null) {
                return new byte[0];
            }
            return toByteArray(jarFile.getInputStream(entry));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 根据路径读取 JAR 中指定 entry 的字符串内容
     * <p>
     * 根据 JAR 内的路径获取对应条目的文本内容，使用 UTF-8 编码。
     * 如果条目不存在或无法读取，返回空字符串。
     *
     * @param pathInJar JAR 内的路径，例如 "META-INF/MANIFEST.MF"
     * @return entry 文本内容，如果找不到则返回空字符串
     * @throws UncheckedIOException 如果读取过程中发生 IO 错误
     */
    public String getString(String pathInJar) {
        byte[] bytes = getBytes(pathInJar);
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * 按条件过滤 JAR entries，并返回路径到字节数组的映射
     * <p>
     * 使用指定的谓词过滤 JAR 包中的条目，返回符合条件的 path -> bytes 映射。
     * 映射保持与枚举相同的顺序（通常是文件系统顺序）。
     *
     * @param predicate 过滤条件，例如 {@code e -> e.getName().endsWith(".class")}
     * @return 符合条件的 path -> bytes 映射，如果没有任何匹配则返回空映射
     * @throws UncheckedIOException 如果读取过程中发生 IO 错误
     */
    public Map<String, byte[]> getFileBytes(Predicate<JarEntry> predicate) {
        Map<String, byte[]> map = new LinkedHashMap<>();
        List<JarEntry> filtered = getEntries().stream()
                .filter(predicate)
                .collect(Collectors.toList());

        for (JarEntry entry : filtered) {
            map.put(entry.getName(), getBytes(entry.getName()));
        }
        return map;
    }

    /**
     * 关闭 JAR 文件
     * <p>
     * 释放底层 JAR 文件资源。建议使用 try-with-resources 语法自动调用此方法。
     *
     * @throws IOException 如果关闭过程中发生 IO 错误
     */
    @Override
    /**
     * close方法。
     */
    public void close() throws IOException {
        if (jarFile != null) {
            jarFile.close();
        }
    }

    /**
     * 非受检 IO 异常包装器
     * <p>
     * 将 IOException 包装为 RuntimeException，方便在 lambda 表达式中使用。
     */
    private static class UncheckedIOException extends RuntimeException {
        private UncheckedIOException(IOException cause) {
            super(cause);
        }
    }
}
