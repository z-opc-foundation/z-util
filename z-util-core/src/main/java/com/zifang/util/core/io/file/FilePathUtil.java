package com.zifang.util.core.io.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for file path, name, and extension manipulation.
 * <p>
 * This class provides static methods for common file path operations such as
 * extracting file names, extensions, normalizing paths, and checking file types.
 * It is designed as a clean, well-documented alternative to FileUtils and FilenameUtils
 * from Apache Commons IO.
 * </p>
 * <p>
 * All methods are static and thread-safe. This class cannot be instantiated.
 * </p>
 *
 * @author zifang
 * @version 1.0.0
 * @since 2026-05-21
 */
public final class FilePathUtil {

    /**
     * SLF4J logger instance for logging operations.
     */
    private static final Logger logger = LoggerFactory.getLogger(FilePathUtil.class);

    /**
     * The Unix path separator character.
     */
    private static final char UNIX_SEPARATOR = '/';

    /**
     * The Windows path separator character.
     */
    private static final char WINDOWS_SEPARATOR = '\\';

    /**
     * The extension separator character.
     */
    private static final char EXTENSION_SEPARATOR = '.';

    /**
     * Private constructor to prevent instantiation.
     * This class provides only static utility methods.
     */
    private FilePathUtil() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    /**
     * Gets the file name from the given file path.
     * <p>
     * This method extracts the final component of the path, which is typically
     * the file name including any extension. If the path ends with a separator,
     * an empty string is returned.
     * </p>
     * <p>
     * Examples:
     * <ul>
     *   <li>{@code getName("/home/user/file.txt")} returns {@code "file.txt"}</li>
     *   <li>{@code getName("/home/user/")} returns {@code ""}</li>
     *   <li>{@code getName("file.txt")} returns {@code "file.txt"}</li>
     * </ul>
     * </p>
     *
     * @param filePath the file path to extract the name from
     * @return the file name, or empty string if path ends with separator
     * @throws IllegalArgumentException if filePath is null
     */
    public static String getName(String filePath) {
        if (filePath == null) {
            throw new IllegalArgumentException("File path must not be null");
        }
        if (filePath.isEmpty()) {
            return "";
        }
        int separatorIndex = Math.max(filePath.lastIndexOf(UNIX_SEPARATOR), filePath.lastIndexOf(WINDOWS_SEPARATOR));
        return filePath.substring(separatorIndex + 1);
    }

    /**
     * Gets the base name of the file without its extension.
     * <p>
     * This method extracts the file name and removes the extension portion.
     * The extension is defined as the portion of the file name after the last
     * dot (.) character. If no extension is found, the full file name is returned.
     * </p>
     * <p>
     * Examples:
     * <ul>
     *   <li>{@code getBaseName("/home/user/file.txt")} returns {@code "file"}</li>
     *   <li>{@code getBaseName("/home/user/archive.tar.gz")} returns {@code "archive.tar"}</li>
     *   <li>{@code getBaseName("/home/user/file")} returns {@code "file"}</li>
     * </ul>
     * </p>
     *
     * @param filePath the file path to extract the base name from
     * @return the base name without extension, or empty string if path ends with separator
     * @throws IllegalArgumentException if filePath is null
     */
    public static String getBaseName(String filePath) {
        String name = getName(filePath);
        if (name.isEmpty()) {
            return "";
        }
        int extensionIndex = name.lastIndexOf(EXTENSION_SEPARATOR);
        // Hidden files (starting with '.') without other dots should return the full name
        if (extensionIndex <= 0) {
            return name;
        }
        return name.substring(0, extensionIndex);
    }

    /**
     * Gets the extension of the file path.
     * <p>
     * This method extracts the file extension from the given path. The extension
     * is defined as the portion of the file name after the last dot (.) character.
     * </p>
     * <p>
     * Examples:
     * <ul>
     *   <li>{@code getExtension("/home/user/file.txt")} returns {@code "txt"}</li>
     *   <li>{@code getExtension("/home/user/archive.tar.gz")} returns {@code "gz"}</li>
     *   <li>{@code getExtension("/home/user/file")} returns {@code ""}</li>
     *   <li>{@code getExtension("/home/user/file.")} returns {@code ""}</li>
     * </ul>
     * </p>
     *
     * @param path the file path to extract the extension from
     * @return the file extension without the leading dot, or empty string if no extension
     * @throws IllegalArgumentException if path is null
     */
    public static String getExtension(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Path must not be null");
        }
        String name = getName(path);
        if (name.isEmpty()) {
            return "";
        }
        int extensionIndex = name.lastIndexOf(EXTENSION_SEPARATOR);
        if (extensionIndex == -1 || extensionIndex == name.length() - 1) {
            return "";
        }
        return name.substring(extensionIndex + 1);
    }

    /**
     * Gets the parent directory path of the given file path.
     * <p>
     * This method extracts the parent directory portion of the path.
     * The parent is defined as everything before the last path separator.
     * </p>
     * <p>
     * Examples:
     * <ul>
     *   <li>{@code getParent("/home/user/file.txt")} returns {@code "/home/user"}</li>
     *   <li>{@code getParent("/home/user/")} returns {@code "/home/user"}</li>
     *   <li>{@code getParent("file.txt")} returns {@code ""}</li>
     * </ul>
     * </p>
     *
     * @param filePath the file path to extract the parent from
     * @return the parent directory path, or empty string if no parent
     * @throws IllegalArgumentException if filePath is null
     */
    public static String getParent(String filePath) {
        if (filePath == null) {
            return null;
        }
        if (filePath.isEmpty()) {
            return "";
        }
        int separatorIndex = Math.max(filePath.lastIndexOf(UNIX_SEPARATOR), filePath.lastIndexOf(WINDOWS_SEPARATOR));
        if (separatorIndex == -1) {
            return null;
        }
        return filePath.substring(0, separatorIndex);
    }

    /**
     * Normalizes the given path by removing redundant separators and resolving dot and dot-dot references.
     * <p>
     * This method cleans up a file path by:
     * <ul>
     *   <li>Converting all separators to the system default</li>
     *   <li>Removing redundant separators</li>
     *   <li>Resolving dot references (.) to the current directory</li>
     *   <li>Resolving dot-dot references (..) to the parent directory</li>
     * </ul>
     * </p>
     * <p>
     * Examples:
     * <ul>
     *   <li>{@code normalize("/home/user/../user/./file.txt")} returns {@code "/home/user/file.txt"}</li>
     *   <li>{@code normalize("./file.txt")} returns {@code "file.txt"}</li>
     *   <li>{@code normalize("/home//user/")} returns {@code "/home/user"}</li>
     * </ul>
     * </p>
     *
     * @param path the path to normalize
     * @return the normalized path, or empty string if path is empty
     * @throws IllegalArgumentException if path is null
     */
    public static String normalize(String path) {
        if (path == null) {
            return null;
        }
        if (path.isEmpty()) {
            return "";
        }
        try {
            // Determine if path uses Windows backslashes
            boolean hasBackslashes = path.indexOf(WINDOWS_SEPARATOR) != -1;
            // Convert backslashes to forward slashes for proper handling
            String unixPath = path.replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
            Path normalizedPath = Paths.get(unixPath).normalize();
            String result = normalizedPath.toString();
            // Convert back to backslashes if original path used them
            if (hasBackslashes) {
                result = result.replace(UNIX_SEPARATOR, WINDOWS_SEPARATOR);
            }
            return result;
        } catch (Exception e) {
            logger.warn("Failed to normalize path '{}': {}", path, e.getMessage());
            return path;
        }
    }

    /**
     * Converts all backslash separators to forward slashes (Unix-style paths).
     * <p>
     * This method is useful for ensuring consistent path formatting across
     * different operating systems, particularly when working with paths
     * that may have been constructed on Windows but need to be used in a
     * Unix-like environment.
     * </p>
     * <p>
     * Examples:
     * <ul>
     *   <li>{@code toUNIXPath("C:\\Users\\user\\file.txt")} returns {@code "C:/Users/user/file.txt"}</li>
     *   <li>{@code toUNIXPath("/home/user/file.txt")} returns {@code "/home/user/file.txt"}</li>
     * </ul>
     * </p>
     *
     * @param path the path to convert
     * @return the path with forward slashes, or empty string if path is empty
     * @throws IllegalArgumentException if path is null
     */
    public static String toUNIXPath(String path) {
        if (path == null) {
            return null;
        }
        if (path.isEmpty()) {
            return "";
        }
        return path.replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
    }

    /**
     * Gets the prefix portion of a filename (the part before the last dot).
     * <p>
     * This is similar to {@link #getBaseName(String)} but operates on the full filename
     * without considering path separators. The prefix is the portion before the
     * last dot in the filename component.
     * </p>
     * <p>
     * Examples:
     * <ul>
     *   <li>{@code getPrefix("file.txt")} returns {@code "file"}</li>
     *   <li>{@code getPrefix("archive.tar.gz")} returns {@code "archive.tar"}</li>
     *   <li>{@code getPrefix("file")} returns {@code "file"}</li>
     * </ul>
     * </p>
     *
     * @param filename the filename to extract the prefix from
     * @return the prefix portion of the filename
     * @throws IllegalArgumentException if filename is null
     */
    public static String getPrefix(String filename) {
        if (filename == null) {
            return null;
        }
        if (filename.isEmpty()) {
            return "";
        }
        if (filename.equals(".") || filename.equals("..")) {
            return "";
        }

        // Check for UNC path: \\server\share\...
        if (filename.startsWith("\\\\")) {
            int secondBackslash = filename.indexOf('\\', 2);
            if (secondBackslash != -1) {
                int thirdBackslash = filename.indexOf('\\', secondBackslash + 1);
                if (thirdBackslash != -1) {
                    // Check if there's a non-backslash char after the third backslash
                    if (thirdBackslash + 1 < filename.length() && filename.charAt(thirdBackslash + 1) != '\\') {
                        // Has file/folder after share, include trailing backslash
                        return filename.substring(0, thirdBackslash + 1);
                    }
                    // No file after share, don't include trailing backslash
                    return filename.substring(0, thirdBackslash);
                }
                // No third backslash, path ends at share name
                return filename + "\\";
            }
            return filename;
        }

        // Check for Windows drive letter: C:\...
        if (filename.length() >= 2 && filename.charAt(1) == ':') {
            char drive = Character.toUpperCase(filename.charAt(0));
            if ((drive >= 'A' && drive <= 'Z') || drive == '\\' || drive == '/') {
                if (filename.length() >= 3 && (filename.charAt(2) == '\\' || filename.charAt(2) == '/')) {
                    return filename.substring(0, 3);
                }
                return filename.substring(0, 2);
            }
        }

        // Check for Unix absolute path: /...
        if (filename.startsWith("/")) {
            // Return just the root "/"
            return "/";
        }

        // Relative path - no prefix
        return "";
    }

    /**
     * Gets the suffix (extension) portion of a filename (the part after the last dot).
     * <p>
     * This is similar to {@link #getExtension(String)} but operates on the full filename
     * without considering path separators. The suffix is the portion after the
     * last dot in the filename.
     * </p>
     * <p>
     * Examples:
     * <ul>
     *   <li>{@code getSuffix("file.txt")} returns {@code "txt"}</li>
     *   <li>{@code getSuffix("archive.tar.gz")} returns {@code "gz"}</li>
     *   <li>{@code getSuffix("file")} returns {@code ""}</li>
     * </ul>
     * </p>
     *
     * @param filename the filename to extract the suffix from
     * @return the suffix (extension) portion of the filename
     * @throws IllegalArgumentException if filename is null
     */
    public static String getSuffix(String filename) {
        if (filename == null) {
            return null;
        }
        if (filename.isEmpty()) {
            return "";
        }
        if (filename.equals(".") || filename.equals("..")) {
            return "";
        }
        int extensionIndex = filename.indexOf(EXTENSION_SEPARATOR);
        if (extensionIndex == -1 || extensionIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(extensionIndex + 1);
    }

    /**
     * Removes the extension from the given filename.
     * <p>
     * This method returns the filename with its extension portion removed.
     * It is equivalent to {@link #getPrefix(String)} when the input is just a filename.
     * </p>
     * <p>
     * Examples:
     * <ul>
     *   <li>{@code trimExtension("file.txt")} returns {@code "file"}</li>
     *   <li>{@code trimExtension("archive.tar.gz")} returns {@code "archive.tar"}</li>
     *   <li>{@code trimExtension("file")} returns {@code "file"}</li>
     * </ul>
     * </p>
     *
     * @param filename the filename to trim the extension from
     * @return the filename without its extension
     * @throws IllegalArgumentException if filename is null
     */
    public static String trimExtension(String filename) {
        if (filename == null) {
            return null;
        }
        if (filename.isEmpty()) {
            return "";
        }
        if (filename.equals(".") || filename.equals("..")) {
            return filename;
        }
        int extensionIndex = filename.lastIndexOf(EXTENSION_SEPARATOR);
        if (extensionIndex <= 0) {
            return filename;
        }
        return filename.substring(0, extensionIndex);
    }

    /**
     * Gets the relative path from a base directory to a file.
     * <p>
     * This method calculates the relative path between a base directory
     * and a file path. It returns the portion of the file's path that is
     * relative to the base.
     * </p>
     * <p>
     * Examples:
     * <ul>
     *   <li>{@code getSubpath("/home/user/file.txt", "/home")} returns {@code "user/file.txt"}</li>
     *   <li>{@code getSubpath("/home/user/file.txt", "/var")} returns {@code "/home/user/file.txt"}</li>
     * </ul>
     * </p>
     *
     * @param fullPath the full file path
     * @param basePath the base directory path
     * @return the relative path from base to file, or the full path if no common prefix
     * @throws IllegalArgumentException if fullPath or basePath is null
     */
    public static String getSubpath(String fullPath, String basePath) {
        if (fullPath == null) {
            return null;
        }
        if (basePath == null) {
            return null;
        }
        String normalizedFullPath = toUNIXPath(normalize(fullPath));
        String normalizedBasePath = toUNIXPath(normalize(basePath));

        if (!normalizedFullPath.startsWith(normalizedBasePath)) {
            // If no common prefix, return the full path as-is
            return fullPath;
        }

        String subpath = normalizedFullPath.substring(normalizedBasePath.length());
        if (subpath.startsWith("/") || subpath.startsWith("\\")) {
            subpath = subpath.substring(1);
        }
        return subpath;
    }

    /**
     * Checks if the given path exists (either as a file or directory).
     * <p>
     * This method tests whether the file or directory denoted by the given
     * path exists in the file system.
     * </p>
     *
     * @param path the path to check
     * @return {@code true} if the path exists, {@code false} otherwise
     * @throws IllegalArgumentException if path is null
     */
    public static boolean isExist(String path) {
        if (path == null) {
            return false;
        }
        try {
            return Files.exists(Paths.get(path));
        } catch (Exception e) {
            logger.warn("Failed to check existence of path '{}': {}", path, e.getMessage());
            return false;
        }
    }

    /**
     * Checks if the given path is a directory.
     * <p>
     * This method tests whether the file denoted by the given path is
     * a directory in the file system.
     * </p>
     *
     * @param path the path to check
     * @return {@code true} if the path is a directory and exists, {@code false} otherwise
     * @throws IllegalArgumentException if path is null
     */
    public static boolean isDirectory(String path) {
        if (path == null) {
            return false;
        }
        try {
            return Files.isDirectory(Paths.get(path));
        } catch (Exception e) {
            logger.warn("Failed to check if path '{}' is a directory: {}", path, e.getMessage());
            return false;
        }
    }

    /**
     * Checks if the given path is a regular file.
     * <p>
     * This method tests whether the file denoted by the given path is
     * a regular file (not a directory) in the file system.
     * </p>
     *
     * @param path the path to check
     * @return {@code true} if the path is a regular file and exists, {@code false} otherwise
     * @throws IllegalArgumentException if path is null
     */
    public static boolean isFile(String path) {
        if (path == null) {
            return false;
        }
        try {
            return Files.isRegularFile(Paths.get(path));
        } catch (Exception e) {
            logger.warn("Failed to check if path '{}' is a file: {}", path, e.getMessage());
            return false;
        }
    }
}
