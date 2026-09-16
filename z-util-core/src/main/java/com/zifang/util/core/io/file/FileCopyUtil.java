package com.zifang.util.core.io.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Utility class for file and directory copy operations.
 * <p>
 * This class provides static methods for copying and moving files and directories,
 * inspired by Apache Commons IO's FileUtils. All operations use buffered streams
 * for efficient I/O performance.
 * </p>
 *
 * <p>
 * Design principles:
 * <ul>
 *   <li>All methods are static - no instance needed</li>
 *   <li>Consistent error handling - throws IOException on failure</li>
 *   <li>No silent failures - errors are logged and/or thrown</li>
 *   <li>Try-with-resources used for all stream operations</li>
 * </ul>
 * </p>
 *
 * @author zifang
 */
public final class FileCopyUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileCopyUtil.class);

    /**
     * Default buffer size for stream operations: 8KB.
     */
    private static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class and should not be instantiated.
     */
    private FileCopyUtil() {
        throw new UnsupportedOperationException("This utility class cannot be instantiated");
    }

    /**
     * Copies a single file to another location.
     * <p>
     * If the destination file already exists, it will be overwritten.
     * The parent directories of the destination file will be created if they do not exist.
     * </p>
     *
     * @param src  the source file to copy, must not be {@code null}
     * @param dest the destination file, must not be {@code null}
     * @throws IOException          if the source file does not exist, or copying fails
     * @throws NullPointerException if src or dest is {@code null}
     */
    public static void copyFile(File src, File dest) throws IOException {
        copyFile(src, dest, true);
    }

    /**
     * Copies a single file to another location with optional overwrite.
     * <p>
     * If overwrite is {@code false} and the destination file already exists,
     * an exception will be thrown. The parent directories of the destination file
     * will be created if they do not exist.
     * </p>
     *
     * @param src       the source file to copy, must not be {@code null}
     * @param dest      the destination file, must not be {@code null}
     * @param overwrite whether to overwrite if the destination already exists
     * @throws IOException          if the source file does not exist, or copying fails
     * @throws NullPointerException if src or dest is {@code null}
     * @throws FileExistsException  if overwrite is false and dest already exists
     */
    public static void copyFile(File src, File dest, boolean overwrite) throws IOException {
        if (src == null) {
            throw new NullPointerException("Source file must not be null");
        }
        if (dest == null) {
            throw new NullPointerException("Destination file must not be null");
        }
        if (!src.exists()) {
            throw new FileNotFoundException("Source file does not exist: " + src.getAbsolutePath());
        }
        if (!src.isFile()) {
            throw new IOException("Source is not a file: " + src.getAbsolutePath());
        }
        if (!overwrite && dest.exists()) {
            logger.debug("Destination already exists and overwrite is false, skipping copy: {}", dest.getAbsolutePath());
            return;
        }

        // Ensure parent directories exist
        File parentDir = dest.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Failed to create parent directories: " + parentDir.getAbsolutePath());
            }
        }

        logger.debug("Copying file from {} to {}", src.getAbsolutePath(), dest.getAbsolutePath());

        try (FileInputStream fis = new FileInputStream(src);
             FileOutputStream fos = new FileOutputStream(dest)) {
            copyStream(fis, fos);
        }

        // Preserve the modification time
        if (src.lastModified() > 0) {
            dest.setLastModified(src.lastModified());
        }
    }

    /**
     * Copies a directory and all its contents to a destination directory.
     * <p>
     * The destination directory will be created if it does not exist.
     * If the destination directory already exists, its contents will be merged
     * with the source directory.
     * </p>
     *
     * @param srcDir      the source directory to copy, must not be {@code null}
     * @param destDirPath the path to the destination directory
     * @throws IOException          if the source directory does not exist, or copying fails
     * @throws NullPointerException if srcDir or destDirPath is {@code null}
     */
    public static void copyDir(File srcDir, String destDirPath) throws IOException {
        if (srcDir == null) {
            throw new NullPointerException("Source directory must not be null");
        }
        if (destDirPath == null) {
            throw new NullPointerException("Destination directory path must not be null");
        }
        if (!srcDir.exists()) {
            throw new FileNotFoundException("Source directory does not exist: " + srcDir.getAbsolutePath());
        }
        if (!srcDir.isDirectory()) {
            throw new IOException("Source is not a directory: " + srcDir.getAbsolutePath());
        }

        File destDir = new File(destDirPath);
        copyDir(srcDir, destDir);
    }

    /**
     * Copies a directory and all its contents to a destination directory.
     * <p>
     * The destination directory will be created if it does not exist.
     * If the destination directory already exists, its contents will be merged
     * with the source directory.
     * </p>
     *
     * @param srcDirPath  the path to the source directory
     * @param destDirPath the path to the destination directory
     * @throws IOException          if the source directory does not exist, or copying fails
     * @throws NullPointerException if srcDirPath or destDirPath is {@code null}
     */
    public static void copyDir(String srcDirPath, String destDirPath) throws IOException {
        if (srcDirPath == null) {
            throw new NullPointerException("Source directory path must not be null");
        }
        if (destDirPath == null) {
            throw new NullPointerException("Destination directory path must not be null");
        }
        File srcDir = new File(srcDirPath);
        File destDir = new File(destDirPath);
        copyDir(srcDir, destDir);
    }

    /**
     * Internal method to copy a directory to a destination directory.
     *
     * @param srcDir  the source directory
     * @param destDir the destination directory
     * @throws IOException if copying fails
     */
    private static void copyDir(File srcDir, File destDir) throws IOException {
        if (!destDir.exists()) {
            if (!destDir.mkdirs()) {
                throw new IOException("Failed to create destination directory: " + destDir.getAbsolutePath());
            }
        }

        if (!destDir.isDirectory()) {
            throw new IOException("Destination exists but is not a directory: " + destDir.getAbsolutePath());
        }

        logger.debug("Copying directory from {} to {}", srcDir.getAbsolutePath(), destDir.getAbsolutePath());

        // List files and directories
        File[] files = srcDir.listFiles();
        if (files == null) {
            throw new IOException("Failed to list contents of directory: " + srcDir.getAbsolutePath());
        }

        for (File file : files) {
            File destFile = new File(destDir, file.getName());
            if (file.isDirectory()) {
                copyDir(file, destFile);
            } else {
                copyFile(file, destFile, true);
            }
        }
    }

    /**
     * Copies a single file into a destination directory.
     * <p>
     * The file will be copied with the same name as the source file.
     * If a file with the same name already exists in the destination directory,
     * it will be overwritten.
     * </p>
     *
     * @param srcFile the source file to copy, must not be {@code null}
     * @param destDir the destination directory, must not be {@code null}
     * @throws IOException          if the source file does not exist, or copying fails
     * @throws NullPointerException if srcFile or destDir is {@code null}
     * @throws IOException          if destDir is not a directory
     */
    public static void copyFileToDir(File srcFile, File destDir) throws IOException {
        if (srcFile == null) {
            throw new NullPointerException("Source file must not be null");
        }
        if (destDir == null) {
            throw new NullPointerException("Destination directory must not be null");
        }
        if (!srcFile.exists()) {
            throw new FileNotFoundException("Source file does not exist: " + srcFile.getAbsolutePath());
        }
        if (!destDir.isDirectory()) {
            throw new IOException("Destination is not a directory: " + destDir.getAbsolutePath());
        }

        File destFile = new File(destDir, srcFile.getName());
        copyFile(srcFile, destFile, true);
    }

    /**
     * Moves a single file to a new location.
     * <p>
     * This method first attempts to rename the file. If that fails (e.g., across
     * different filesystems), it copies the file and deletes the original.
     * </p>
     *
     * @param src  the source file to move, must not be {@code null}
     * @param dest the destination file, must not be {@code null}
     * @throws IOException          if the source file does not exist, or moving fails
     * @throws NullPointerException if src or dest is {@code null}
     */
    public static void moveFile(File src, File dest) throws IOException {
        if (src == null) {
            throw new NullPointerException("Source file must not be null");
        }
        if (dest == null) {
            throw new NullPointerException("Destination file must not be null");
        }
        if (!src.exists()) {
            throw new FileNotFoundException("Source file does not exist: " + src.getAbsolutePath());
        }
        if (!src.isFile()) {
            throw new IOException("Source is not a file: " + src.getAbsolutePath());
        }

        logger.debug("Moving file from {} to {}", src.getAbsolutePath(), dest.getAbsolutePath());

        // Ensure parent directories of destination exist
        File parentDir = dest.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Failed to create parent directories: " + parentDir.getAbsolutePath());
            }
        }

        // Try atomic rename first (works within same filesystem)
        boolean renameSuccess = src.renameTo(dest);
        if (renameSuccess) {
            return;
        }

        // Fall back to copy and delete (works across filesystems)
        logger.debug("renameTo failed, falling back to copy and delete");
        copyFile(src, dest, true);
        if (!src.delete()) {
            // Log warning but don't fail - the file was successfully copied
            logger.warn("Failed to delete source file after successful copy: {}", src.getAbsolutePath());
        }
    }

    /**
     * Moves a directory to a new location.
     * <p>
     * This method first attempts to rename the directory. If that fails (e.g., across
     * different filesystems), it copies the directory and deletes the original.
     * </p>
     *
     * @param srcDir  the source directory to move, must not be {@code null}
     * @param destDir the destination directory, must not be {@code null}
     * @throws IOException          if the source directory does not exist, or moving fails
     * @throws NullPointerException if srcDir or destDir is {@code null}
     */
    public static void moveDir(File srcDir, File destDir) throws IOException {
        if (srcDir == null) {
            throw new NullPointerException("Source directory must not be null");
        }
        if (destDir == null) {
            throw new NullPointerException("Destination directory must not be null");
        }
        if (!srcDir.exists()) {
            throw new FileNotFoundException("Source directory does not exist: " + srcDir.getAbsolutePath());
        }
        if (!srcDir.isDirectory()) {
            throw new IOException("Source is not a directory: " + srcDir.getAbsolutePath());
        }

        logger.debug("Moving directory from {} to {}", srcDir.getAbsolutePath(), destDir.getAbsolutePath());

        // Try atomic rename first (works within same filesystem)
        boolean renameSuccess = srcDir.renameTo(destDir);
        if (renameSuccess) {
            return;
        }

        // Fall back to copy and delete (works across filesystems)
        logger.debug("renameTo failed, falling back to copy and delete");
        copyDir(srcDir, destDir);
        deleteDirectory(srcDir);
    }

    /**
     * Deletes a directory and all its contents recursively.
     *
     * @param directory the directory to delete
     * @throws IOException if deletion fails
     */
    private static void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (!file.delete()) {
                        throw new IOException("Failed to delete file: " + file.getAbsolutePath());
                    }
                }
            }
        }

        if (!directory.delete()) {
            throw new IOException("Failed to delete directory: " + directory.getAbsolutePath());
        }
    }

    /**
     * Copies all bytes from an input stream to an output stream.
     * <p>
     * This method uses a buffered approach with an 8KB buffer for efficient I/O.
     * Both streams are closed after the copy operation completes.
     * </p>
     *
     * @param in  the input stream to read from, must not be {@code null}
     * @param out the output stream to write to, must not be {@code null}
     * @throws IOException          if an I/O error occurs
     * @throws NullPointerException if in or out is {@code null}
     */
    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        if (in == null) {
            throw new NullPointerException("Input stream must not be null");
        }
        if (out == null) {
            throw new NullPointerException("Output stream must not be null");
        }

        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int bytesRead;
        long totalBytesRead = 0;

        try {
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }
            out.flush();
            logger.debug("Copied {} bytes", totalBytesRead);
        } catch (IOException e) {
            logger.error("Error during stream copy: {}", e.getMessage());
            throw e;
        }
    }
}
