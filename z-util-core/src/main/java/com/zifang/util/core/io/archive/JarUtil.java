package com.zifang.util.core.io.archive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;

/**
 * JAR 包解压工具类
 * <p>
 * 提供 JAR 文件的解压功能，将 JAR 包内的所有内容解压到指定目录。
 * 解压时会保持 JAR 包内的目录结构。
 * <p>
 * 示例用法：
 * <pre>{@code
 * // 解压 JAR 文件到目标目录
 * JarUtil.unPack("/path/to/app.jar", "/target/directory");
 * }</pre>
 *
 * @author zifang
 * @see JarFile
 */
public class JarUtil {

    /**
     * 解压 JAR 文件到指定目录
     * <p>
     * 将 JAR 包内的所有文件解压到目标目录，保持原有的目录结构。
     * 如果目标目录不存在会自动创建。
     *
     * @param jarPath   JAR 文件路径（必须是存在的 .jar 文件）
     * @param targetDir 解压目标目录（如果不存在会自动创建）
     * @throws IOException              如果 JAR 文件不存在或格式无效
     * @throws IllegalArgumentException 如果 jarPath 或 targetDir 为空
     * @throws SecurityException        如果由于安全原因无法读取 JAR 文件
     */
    public static void unPack(String jarPath, String targetDir) throws IOException {
        File jarFile = new File(jarPath);
        File destinationDir = new File(targetDir);

        if (!destinationDir.exists()) {
            destinationDir.mkdirs();
        }

        try (JarFile jar = new JarFile(jarFile)) {
            jar.stream().forEach(entry -> {
                String name = entry.getName();
                File targetFile = new File(destinationDir, name);
                try {
                    if (entry.isDirectory()) {
                        targetFile.mkdirs();
                    } else {
                        targetFile.getParentFile().mkdirs();
                        try (InputStream is = jar.getInputStream(entry);
                             FileOutputStream fos = new FileOutputStream(targetFile)) {
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = is.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
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
