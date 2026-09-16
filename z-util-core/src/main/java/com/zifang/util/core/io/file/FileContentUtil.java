package com.zifang.util.core.io.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * Utility class for reading and writing file content.
 * <p>
 * This class provides static methods for common file operations such as
 * reading files as strings, lines, or bytes, and writing content to files.
 * All methods use try-with-resources to ensure proper resource management.
 * </p>
 * <p>
 * This class is inspired by Apache Commons IO's FileUtils but provides a
 * simplified, focused set of utilities for file content operations.
 * </p>
 *
 * @author zifang
 */
public final class FileContentUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileContentUtil.class);

    private static final int BUFFER_SIZE = 8192;

    private FileContentUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ==================== Read String Methods ====================

    /**
     * Reads the entire contents of a file as a string using UTF-8 charset.
     *
     * @param file the file to read
     * @return the entire file contents as a string
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException if file is null
     */
    public static String readString(File file) throws IOException {
        return readString(file, StandardCharsets.UTF_8);
    }

    /**
     * Reads the entire contents of a file as a string using the specified charset.
     *
     * @param file    the file to read
     * @param charset the charset to use for decoding
     * @return the entire file contents as a string
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException if file is null or charset is null
     */
    public static String readString(File file, Charset charset) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }
        if (charset == null) {
            throw new IllegalArgumentException("Charset must not be null");
        }
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist: " + file.getAbsolutePath());
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("Not a file: " + file.getAbsolutePath());
        }
        if (!file.canRead()) {
            throw new IllegalArgumentException("File is not readable: " + file.getAbsolutePath());
        }

        logger.debug("Reading file as string: {}", file.getAbsolutePath());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset))) {
            StringBuilder content = new StringBuilder();
            char[] buffer = new char[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = reader.read(buffer)) != -1) {
                content.append(buffer, 0, bytesRead);
            }
            return content.toString();
        }
    }

    /**
     * Reads the entire contents of a file specified by path as a string using UTF-8 charset.
     *
     * @param filePath the path to the file to read
     * @return the entire file contents as a string
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException if filePath is null or empty
     */
    public static String readString(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path must not be null or empty");
        }
        return readString(new File(filePath), StandardCharsets.UTF_8);
    }

    // ==================== Read Lines Methods ====================

    /**
     * Reads all lines from a file using UTF-8 charset.
     *
     * @param file the file to read
     * @return a list of all lines in the file, with line separators removed
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException if file is null
     */
    public static List<String> readLines(File file) throws IOException {
        return readLines(file, StandardCharsets.UTF_8.name());
    }

    /**
     * Reads all lines from a file using the specified encoding.
     *
     * @param file     the file to read
     * @param encoding the encoding to use
     * @return a list of all lines in the file, with line separators removed
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException if file is null or encoding is null/empty
     */
    public static List<String> readLines(File file, String encoding) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }
        if (encoding == null || encoding.trim().isEmpty()) {
            throw new IllegalArgumentException("Encoding must not be null or empty");
        }
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist: " + file.getAbsolutePath());
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("Not a file: " + file.getAbsolutePath());
        }

        logger.debug("Reading file lines: {}", file.getAbsolutePath());

        return Files.readAllLines(file.toPath(), Charset.forName(encoding));
    }

    // ==================== Read Bytes Methods ====================

    /**
     * Reads the entire contents of a file as a byte array.
     *
     * @param file the file to read
     * @return the entire file contents as a byte array
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException if file is null
     */
    public static byte[] readBytes(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist: " + file.getAbsolutePath());
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("Not a file: " + file.getAbsolutePath());
        }
        if (!file.canRead()) {
            throw new IllegalArgumentException("File is not readable: " + file.getAbsolutePath());
        }

        logger.debug("Reading file bytes: {}", file.getAbsolutePath());

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }

    // ==================== Write String Methods ====================

    /**
     * Writes a string to a file using UTF-8 charset, overwriting any existing content.
     *
     * @param file    the file to write
     * @param content the content to write
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException if file is null
     */
    public static void writeString(File file, String content) throws IOException {
        writeString(file, content, StandardCharsets.UTF_8);
    }

    /**
     * Writes a string to a file using the specified charset, overwriting any existing content.
     *
     * @param file    the file to write
     * @param content the content to write
     * @param charset the charset to use for encoding
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException if file is null or charset is null
     */
    public static void writeString(File file, String content, Charset charset) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }
        if (content == null) {
            throw new IllegalArgumentException("Content must not be null");
        }
        if (charset == null) {
            throw new IllegalArgumentException("Charset must not be null");
        }

        logger.debug("Writing string to file: {}", file.getAbsolutePath());

        createParentDirectoriesIfNeeded(file);

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset))) {
            writer.write(content);
        }
    }

    // ==================== Write Lines Methods ====================

    /**
     * Writes a list of lines to a file using UTF-8 charset, overwriting any existing content.
     * Each line is written with the platform's line separator.
     *
     * @param file  the file to write
     * @param lines the lines to write
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException if file is null
     */
    public static void writeLines(File file, List<String> lines) throws IOException {
        writeLines(file, lines, StandardCharsets.UTF_8, false);
    }

    /**
     * Writes a list of lines to a file with the specified charset.
     *
     * @param file    the file to write
     * @param lines   the lines to write
     * @param charset the charset to use for encoding
     * @param append  whether to append to the file instead of overwriting
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException if file is null or charset is null
     */
    private static void writeLines(File file, List<String> lines, Charset charset, boolean append) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }
        if (charset == null) {
            throw new IllegalArgumentException("Charset must not be null");
        }

        logger.debug("Writing lines to file: {} (append={})", file.getAbsolutePath(), append);

        createParentDirectoriesIfNeeded(file);

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), charset))) {
            if (lines != null) {
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    if (line != null) {
                        writer.write(line);
                    }
                    if (i < lines.size() - 1) {
                        writer.newLine();
                    }
                }
            }
        }
    }

    // ==================== Append String Methods ====================

    /**
     * Appends a string to a file using UTF-8 charset.
     *
     * @param file    the file to append to
     * @param content the content to append
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException if file is null
     */
    public static void appendString(File file, String content) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }

        logger.debug("Appending string to file: {}", file.getAbsolutePath());

        createParentDirectoriesIfNeeded(file);

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            if (content != null) {
                writer.write(content);
            }
        }
    }

    /**
     * Appends a single line to a file using UTF-8 charset.
     * The line will be followed by the platform's line separator.
     *
     * @param file the file to append to
     * @param line the line to append
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException if file is null
     */
    public static void appendLine(File file, String line) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }

        logger.debug("Appending line to file: {}", file.getAbsolutePath());

        createParentDirectoriesIfNeeded(file);

        boolean needsSeparator = file.exists() && file.length() > 0;
        if (needsSeparator) {
            String existingContent = readString(file);
            if (existingContent.endsWith("\n")) {
                needsSeparator = false;
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            if (needsSeparator) {
                writer.write("\n");
            }
            if (line != null) {
                writer.write(line);
            }
        }
    }

    // ==================== Count Lines Method ====================

    /**
     * Counts the number of lines in a file.
     *
     * @param file the file to count lines in
     * @return the number of lines in the file
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException if file is null
     */
    public static long countLines(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist: " + file.getAbsolutePath());
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("Not a file: " + file.getAbsolutePath());
        }

        logger.debug("Counting lines in file: {}", file.getAbsolutePath());

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            long lineCount = 0;
            while (reader.readLine() != null) {
                lineCount++;
            }
            return lineCount;
        }
    }

    // ==================== Read Fully Methods ====================

    /**
     * Reads all remaining bytes from an input stream.
     *
     * @param in the input stream to read from
     * @return all bytes read from the stream
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException if input stream is null
     */
    public static byte[] readFully(InputStream in) throws IOException {
        if (in == null) {
            throw new IllegalArgumentException("InputStream must not be null");
        }

        logger.debug("Reading all bytes from InputStream");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = in.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        return buffer.toByteArray();
    }

    /**
     * Reads all remaining characters from a reader.
     *
     * @param reader the reader to read from
     * @return all characters read from the reader
     * @throws IOException              if an I/O error occurs
     * @throws IllegalArgumentException if reader is null
     */
    public static String readFully(Reader reader) throws IOException {
        if (reader == null) {
            throw new IllegalArgumentException("Reader must not be null");
        }

        logger.debug("Reading all text from Reader");

        StringBuilder content = new StringBuilder();
        char[] buffer = new char[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = reader.read(buffer)) != -1) {
            content.append(buffer, 0, bytesRead);
        }
        return content.toString();
    }

    // ==================== Helper Methods ====================

    /**
     * Creates parent directories for the file if they don't exist.
     *
     * @param file the file whose parent directories need to be created
     * @throws IOException if directory creation fails
     */
    private static void createParentDirectoriesIfNeeded(File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                if (!parent.exists()) {
                    throw new IOException("Failed to create directory: " + parent.getAbsolutePath());
                }
            }
        }
    }
}
