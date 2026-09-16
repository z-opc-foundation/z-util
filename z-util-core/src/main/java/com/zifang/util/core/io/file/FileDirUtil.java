package com.zifang.util.core.io.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for directory and file operations.
 * <p>
 * This class provides static methods for common directory operations such as
 * creating directories, deleting directories and files, listing files, and
 * searching for files. It is designed as a clean, well-documented alternative
 * to FileUtils from Apache Commons IO.
 * </p>
 * <p>
 * All methods are static and thread-safe. This class cannot be instantiated.
 * </p>
 *
 * @author zifang
 * @version 1.0.0
 * @since 2026-05-21
 */
public final class FileDirUtil {

    /**
     * SLF4J logger instance for logging operations.
     */
    private static final Logger logger = LoggerFactory.getLogger(FileDirUtil.class);

    /**
     * Private constructor to prevent instantiation.
     */
    private FileDirUtil() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    /**
     * Creates a directory (including any necessary parent directories) from the given path string.
     * <p>
     * This method attempts to create the directory and all parent directories specified by the path.
     * If the directory already exists, this method does nothing and returns normally.
     * </p>
     *
     * @param dirPath the path of the directory to create
     * @throws IOException if the directory cannot be created or if an I/O error occurs
     */
    public static void mkdir(String dirPath) throws IOException {
        if (dirPath == null || dirPath.trim().isEmpty()) {
            throw new IOException("Directory path cannot be null or empty");
        }
        mkdir(new File(dirPath));
    }

    /**
     * Creates a directory (including any necessary parent directories) from the given File object.
     * <p>
     * This method attempts to create the directory and all parent directories.
     * If the directory already exists, this method does nothing and returns normally.
     * </p>
     *
     * @param dir the File object representing the directory to create
     * @throws IOException if the directory cannot be created or if an I/O error occurs
     */
    public static void mkdir(File dir) throws IOException {
        if (dir == null) {
            throw new IOException("Directory file cannot be null");
        }
        if (dir.exists()) {
            if (dir.isDirectory()) {
                logger.debug("Directory already exists: {}", dir.getAbsolutePath());
                return;
            } else {
                throw new IOException("Cannot create directory, a file already exists with the same path: " + dir.getAbsolutePath());
            }
        }
        if (!dir.mkdirs() && !dir.exists()) {
            throw new IOException("Failed to create directory: " + dir.getAbsolutePath());
        }
        logger.debug("Created directory: {}", dir.getAbsolutePath());
    }

    /**
     * Creates a directory and all parent directories from the given path string.
     * <p>
     * This method is equivalent to {@link #mkdir(File)} and is provided for consistency
     * with common naming conventions.
     * </p>
     *
     * @param dirPath the path of the directory to create
     * @throws IOException if the directory cannot be created or if an I/O error occurs
     */
    public static void mkdirs(String dirPath) throws IOException {
        mkdir(dirPath);
    }

    /**
     * Empties the specified directory by deleting all files and subdirectories within it.
     * <p>
     * The directory itself is not deleted, only its contents.
     * If any file or directory cannot be deleted, the method continues to delete other contents
     * and throws an IOException after the operation completes.
     * </p>
     *
     * @param dir the directory to empty
     * @throws IOException if the directory cannot be emptied or if an I/O error occurs
     */
    public static void cleanDir(File dir) throws IOException {
        if (dir == null) {
            throw new IOException("Directory file cannot be null");
        }
        if (!dir.exists()) {
            throw new IOException("Directory does not exist: " + dir.getAbsolutePath());
        }
        if (!dir.isDirectory()) {
            throw new IOException("Not a directory: " + dir.getAbsolutePath());
        }

        File[] files = dir.listFiles();
        if (files == null) {
            throw new IOException("Failed to list contents of directory: " + dir.getAbsolutePath());
        }

        IOException exception = null;
        for (File file : files) {
            try {
                if (file.isDirectory()) {
                    deleteDir(file);
                } else {
                    if (!file.delete()) {
                        exception = new IOException("Failed to delete file: " + file.getAbsolutePath());
                    }
                }
            } catch (IOException e) {
                exception = e;
                logger.error("Error deleting file or directory: {}", file.getAbsolutePath(), e);
            }
        }

        if (exception != null) {
            throw exception;
        }
        logger.debug("Emptied directory: {}", dir.getAbsolutePath());
    }

    /**
     * Deletes a directory and all its contents recursively.
     * <p>
     * This method deletes the directory and all files and subdirectories within it.
     * The deletion is performed recursively, meaning all contents are deleted before
     * the directory itself is deleted.
     * </p>
     *
     * @param dir the directory to delete
     * @throws IOException if the directory cannot be deleted or if an I/O error occurs
     */
    public static void deleteDir(File dir) throws IOException {
        if (dir == null) {
            throw new IOException("Directory file cannot be null");
        }
        if (!dir.exists()) {
            logger.debug("Directory does not exist, nothing to delete: {}", dir.getAbsolutePath());
            return;
        }
        if (!dir.isDirectory()) {
            throw new IOException("Not a directory: " + dir.getAbsolutePath());
        }

        File[] files = dir.listFiles();
        if (files == null) {
            throw new IOException("Failed to list contents of directory: " + dir.getAbsolutePath());
        }

        IOException exception = null;
        for (File file : files) {
            try {
                if (file.isDirectory()) {
                    deleteDir(file);
                } else {
                    if (!file.delete()) {
                        exception = new IOException("Failed to delete file: " + file.getAbsolutePath());
                    }
                }
            } catch (IOException e) {
                exception = e;
                logger.error("Error deleting file or directory: {}", file.getAbsolutePath(), e);
            }
        }

        if (!dir.delete()) {
            exception = new IOException("Failed to delete directory: " + dir.getAbsolutePath());
        }

        if (exception != null) {
            throw exception;
        }
        logger.debug("Deleted directory: {}", dir.getAbsolutePath());
    }

    /**
     * Deletes a directory and all its contents recursively from the given path string.
     * <p>
     * This method is equivalent to {@link #deleteDir(File)} and is provided for convenience
     * when working with path strings.
     * </p>
     *
     * @param dirPath the path of the directory to delete
     * @throws IOException if the directory cannot be deleted or if an I/O error occurs
     */
    public static void deleteDir(String dirPath) throws IOException {
        if (dirPath == null || dirPath.trim().isEmpty()) {
            throw new IOException("Directory path cannot be null or empty");
        }
        deleteDir(new File(dirPath));
    }

    /**
     * Deletes a single file.
     * <p>
     * This method deletes the file if it exists. If the file is a directory,
     * it must be empty for deletion to succeed. Use {@link #deleteDir(File)} for
     * deleting non-empty directories.
     * </p>
     *
     * @param file the file to delete
     * @throws IOException if the file cannot be deleted or if an I/O error occurs
     */
    public static void deleteFile(File file) throws IOException {
        if (file == null) {
            throw new IOException("File cannot be null");
        }
        if (!file.exists()) {
            logger.debug("File does not exist, nothing to delete: {}", file.getAbsolutePath());
            return;
        }
        if (!file.isFile()) {
            throw new IOException("Not a regular file: " + file.getAbsolutePath());
        }
        if (!file.delete()) {
            throw new IOException("Failed to delete file: " + file.getAbsolutePath());
        }
        logger.debug("Deleted file: {}", file.getAbsolutePath());
    }

    /**
     * Deletes a single file from the given path string.
     * <p>
     * This method is equivalent to {@link #deleteFile(File)} and is provided for convenience
     * when working with path strings.
     * </p>
     *
     * @param filePath the path of the file to delete
     * @throws IOException if the file cannot be deleted or if an I/O error occurs
     */
    public static void deleteFile(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IOException("File path cannot be null or empty");
        }
        deleteFile(new File(filePath));
    }

    /**
     * Checks whether the specified directory is empty (contains no files or subdirectories).
     *
     * @param dir the directory to check
     * @return {@code true} if the directory is empty, {@code false} otherwise
     * @throws IOException if the directory cannot be accessed or if an I/O error occurs
     */
    public static boolean isEmptyDir(File dir) throws IOException {
        if (dir == null) {
            throw new IOException("Directory file cannot be null");
        }
        if (!dir.exists()) {
            throw new IOException("Directory does not exist: " + dir.getAbsolutePath());
        }
        if (!dir.isDirectory()) {
            // A file (non-directory) is considered not empty for isEmptyDir
            return false;
        }

        String[] list = dir.list();
        if (list == null) {
            throw new IOException("Failed to list contents of directory: " + dir.getAbsolutePath());
        }
        boolean empty = list.length == 0;
        logger.debug("Directory {} is {}empty", dir.getAbsolutePath(), empty ? "" : "not ");
        return empty;
    }

    /**
     * Lists all files in the specified directory (non-recursive, excludes directories).
     * <p>
     * This method returns only regular files, not subdirectories. The returned list is
     * a new ArrayList and can be modified by the caller.
     * </p>
     *
     * @param dir the directory to list files from
     * @return a list of File objects representing the files in the directory, or an empty list if the directory is empty
     * @throws IOException if the directory cannot be accessed or if an I/O error occurs
     */
    public static List<File> listFiles(File dir) throws IOException {
        if (dir == null) {
            throw new IOException("Directory file cannot be null");
        }
        if (!dir.exists()) {
            throw new IOException("Directory does not exist: " + dir.getAbsolutePath());
        }
        if (!dir.isDirectory()) {
            throw new IOException("Not a directory: " + dir.getAbsolutePath());
        }

        File[] files = dir.listFiles();
        if (files == null) {
            throw new IOException("Failed to list contents of directory: " + dir.getAbsolutePath());
        }

        List<File> result = new ArrayList<>();
        for (File file : files) {
            if (file.isFile()) {
                result.add(file);
            }
        }
        logger.debug("Listed {} files in directory: {}", result.size(), dir.getAbsolutePath());
        return result;
    }

    /**
     * Lists all files in the specified directory recursively, including files in subdirectories.
     * <p>
     * This method returns all regular files within the directory and all its subdirectories.
     * The returned list is a new ArrayList and can be modified by the caller.
     * </p>
     *
     * @param dir the root directory to list files from
     * @return a list of File objects representing all files found, or an empty list if no files are found
     * @throws IOException if the directory cannot be accessed or if an I/O error occurs
     */
    public static List<File> listFilesRecursively(File dir) throws IOException {
        if (dir == null) {
            throw new IOException("Directory file cannot be null");
        }
        if (!dir.exists()) {
            throw new IOException("Directory does not exist: " + dir.getAbsolutePath());
        }
        if (!dir.isDirectory()) {
            throw new IOException("Not a directory: " + dir.getAbsolutePath());
        }

        List<File> result = new ArrayList<>();
        listFilesRecursivelyHelper(dir, result);
        logger.debug("Listed {} files recursively in directory: {}", result.size(), dir.getAbsolutePath());
        return result;
    }

    /**
     * Helper method for recursive file listing.
     *
     * @param dir    the current directory
     * @param result the list to accumulate files into
     * @throws IOException if an I/O error occurs during directory traversal
     */
    private static void listFilesRecursivelyHelper(File dir, List<File> result) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) {
            throw new IOException("Failed to list contents of directory: " + dir.getAbsolutePath());
        }

        for (File file : files) {
            if (file.isDirectory()) {
                listFilesRecursivelyHelper(file, result);
            } else {
                result.add(file);
            }
        }
    }

    /**
     * Lists all files in the specified directory (non-recursive) that have the given postfix/extension.
     * <p>
     * The postfix is typically the file extension including the dot, for example ".txt" or ".java".
     * If the postfix does not start with a dot, it will be appended automatically.
     * The comparison is case-insensitive.
     * </p>
     *
     * @param dir     the directory to list files from
     * @param postfix the file postfix/extension to filter by (e.g., ".txt" or "txt")
     * @return a list of File objects representing the matching files, or an empty list if no files match
     * @throws IOException if the directory cannot be accessed or if an I/O error occurs
     */
    public static List<File> listFiles(File dir, String postfix) throws IOException {
        if (dir == null) {
            throw new IOException("Directory file cannot be null");
        }
        if (!dir.exists()) {
            throw new IOException("Directory does not exist: " + dir.getAbsolutePath());
        }
        if (!dir.isDirectory()) {
            throw new IOException("Not a directory: " + dir.getAbsolutePath());
        }
        if (postfix == null || postfix.isEmpty()) {
            return listFiles(dir);
        }

        String filterPostfix = postfix.startsWith(".") ? postfix : "." + postfix;
        File[] files = dir.listFiles();
        if (files == null) {
            throw new IOException("Failed to list contents of directory: " + dir.getAbsolutePath());
        }

        List<File> result = new ArrayList<>();
        for (File file : files) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(filterPostfix.toLowerCase())) {
                result.add(file);
            }
        }
        logger.debug("Listed {} files with postfix '{}' in directory: {}", result.size(), filterPostfix, dir.getAbsolutePath());
        return result;
    }

    /**
     * Searches for a file with the given name recursively within the specified directory.
     * <p>
     * The search is performed depth-first and returns the first matching file found.
     * The file name comparison is case-sensitive.
     * </p>
     *
     * @param dir      the directory to search within
     * @param fileName the name of the file to search for
     * @return the File object if found, or {@code null} if not found
     * @throws IOException if the directory cannot be accessed or if an I/O error occurs
     */
    public static File searchFile(File dir, String fileName) throws IOException {
        if (dir == null) {
            throw new IOException("Directory file cannot be null");
        }
        if (!dir.exists()) {
            throw new IOException("Directory does not exist: " + dir.getAbsolutePath());
        }
        if (!dir.isDirectory()) {
            throw new IOException("Not a directory: " + dir.getAbsolutePath());
        }
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IOException("File name cannot be null or empty");
        }

        File result = searchFileHelper(dir, fileName);
        if (result != null) {
            logger.debug("Found file '{}' at: {}", fileName, result.getAbsolutePath());
        } else {
            logger.debug("File '{}' not found in directory: {}", fileName, dir.getAbsolutePath());
        }
        return result;
    }

    /**
     * Helper method for recursive file search.
     *
     * @param dir      the current directory
     * @param fileName the name of the file to search for
     * @return the File object if found, or {@code null} if not found
     * @throws IOException if an I/O error occurs during directory traversal
     */
    private static File searchFileHelper(File dir, String fileName) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) {
            throw new IOException("Failed to list contents of directory: " + dir.getAbsolutePath());
        }

        for (File file : files) {
            if (file.getName().equals(fileName)) {
                return file;
            }
            if (file.isDirectory()) {
                File found = searchFileHelper(file, fileName);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /**
     * Ensures that the specified directory exists.
     * <p>
     * If the directory does not exist, this method creates it (including any necessary parent directories).
     * If the directory already exists, this method does nothing.
     * </p>
     *
     * @param dir the directory whose existence is to be ensured
     * @throws IOException if the directory cannot be created or if an I/O error occurs
     */
    public static void ensureDirExists(File dir) throws IOException {
        if (dir == null) {
            throw new IOException("Directory file cannot be null");
        }
        if (dir.exists()) {
            if (dir.isDirectory()) {
                logger.debug("Directory already exists: {}", dir.getAbsolutePath());
                return;
            } else {
                throw new IOException("Cannot create directory, a file already exists with the same path: " + dir.getAbsolutePath());
            }
        }
        if (!dir.mkdirs() && !dir.exists()) {
            throw new IOException("Failed to create directory: " + dir.getAbsolutePath());
        }
        logger.debug("Ensured directory exists: {}", dir.getAbsolutePath());
    }
}
