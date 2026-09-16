package com.zifang.util.core.io;

import com.zifang.util.core.io.file.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * File utility class providing common file operations.
 * <p>
 * This class serves as a facade for file operations, delegating to specialized
 * utility classes where appropriate. It maintains backward compatibility with
 * the existing API while providing improved error handling and logging.
 * </p>
 * <p>
 * Specialized utilities:
 * <ul>
 *   <li>{@link FilePathUtil} - Path manipulation, file name/extension extraction</li>
 *   <li>{@link FileContentUtil} - File content reading/writing</li>
 *   <li>{@link FileDirUtil} - Directory operations</li>
 *   <li>{@link FileCopyUtil} - File/directory copy operations</li>
 *   <li>{@link FileHashUtil} - File hashing and type detection</li>
 * </ul>
 * </p>
 *
 * @author zifang
 */
public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    private static final String FOLDER_SEPARATOR = "/";
    private static final char EXTENSION_SEPARATOR = '.';
    private static final int BUFFER_SIZE = 1024 * 1024;

    private FileUtil() {
    }

    // ==================== Copy Operations ====================

    /**
     * Copies a file using FileChannel transferTo for large files.
     * Performance is especially noticeable for large files.
     *
     * @param source source file
     * @param target target file
     */
    public static void copyFileWithChannel(File source, File target) {
        try (FileInputStream inStream = new FileInputStream(source);
             FileOutputStream outStream = new FileOutputStream(target);
             FileChannel in = inStream.getChannel();
             FileChannel out = outStream.getChannel()) {
            in.transferTo(0, in.size(), out);
        } catch (IOException e) {
            logger.error("Failed to copy file {} to {} using channel", source.getAbsolutePath(), target.getAbsolutePath(), e);
        }
    }

    /**
     * Copies a file using FileChannel with buffer.
     *
     * @param source source file
     * @param target target file
     */
    public static void copyFileWithBuffer(File source, File target) {
        try (FileInputStream inStream = new FileInputStream(source);
             FileOutputStream outStream = new FileOutputStream(target);
             FileChannel in = inStream.getChannel();
             FileChannel out = outStream.getChannel()) {
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            while (in.read(buffer) != -1) {
                buffer.flip();
                out.write(buffer);
                buffer.clear();
            }
        } catch (IOException e) {
            logger.error("Failed to copy file {} to {} using buffer", source.getAbsolutePath(), target.getAbsolutePath(), e);
        }
    }

    /**
     * Copies a file using buffered streams.
     *
     * @param source source file
     * @param target target file
     */
    public static void customBufferBufferedStreamCopy(File source, File target) {
        try (InputStream fis = new BufferedInputStream(new FileInputStream(source));
             OutputStream fos = new BufferedOutputStream(new FileOutputStream(target))) {
            byte[] buf = new byte[4096];
            int i;
            while ((i = fis.read(buf)) != -1) {
                fos.write(buf, 0, i);
            }
        } catch (IOException e) {
            logger.error("Failed to copy file {} to {} using buffered stream", source.getAbsolutePath(), target.getAbsolutePath(), e);
        }
    }

    /**
     * Copies a file using unbuffered streams.
     *
     * @param source source file
     * @param target target file
     */
    public static void customBufferStreamCopy(File source, File target) {
        try (InputStream fis = new FileInputStream(source);
             OutputStream fos = new FileOutputStream(target)) {
            byte[] buf = new byte[4096];
            int i;
            while ((i = fis.read(buf)) != -1) {
                fos.write(buf, 0, i);
            }
        } catch (IOException e) {
            logger.error("Failed to copy file {} to {} using stream", source.getAbsolutePath(), target.getAbsolutePath(), e);
        }
    }

    // ==================== Charset Conversion ====================

    /**
     * Converts a file or directory to a specified charset encoding.
     *
     * @param fileName        file to convert
     * @param fromCharsetName source charset
     * @param toCharsetName   target charset
     */
    public static void convert(String fileName, String fromCharsetName, String toCharsetName) {
        convert(new File(fileName), fromCharsetName, toCharsetName, null);
    }

    /**
     * Converts a file or directory to a specified charset encoding.
     *
     * @param file            file to convert
     * @param fromCharsetName source charset
     * @param toCharsetName   target charset
     */
    public static void convert(File file, String fromCharsetName, String toCharsetName) {
        convert(file, fromCharsetName, toCharsetName, null);
    }

    /**
     * Converts a file or directory to a specified charset encoding with filter.
     *
     * @param fileName        file to convert
     * @param fromCharsetName source charset
     * @param toCharsetName   target charset
     * @param filter          filename filter
     */
    public static void convert(String fileName, String fromCharsetName, String toCharsetName, FilenameFilter filter) {
        convert(new File(fileName), fromCharsetName, toCharsetName, filter);
    }

    /**
     * Converts a file or directory to a specified charset encoding with filter.
     *
     * @param file            file to convert
     * @param fromCharsetName source charset
     * @param toCharsetName   target charset
     * @param filter          filename filter
     */
    public static void convert(File file, String fromCharsetName, String toCharsetName, FilenameFilter filter) {
        if (file.isDirectory()) {
            List<File> list = filter != null ? listFileFilter(file, filter) : listFile(file);
            if (list != null && !list.isEmpty()) {
                for (File f : list) {
                    convert(f, fromCharsetName, toCharsetName, filter);
                }
            }
        } else {
            if (filter == null || filter.accept(file.getParentFile(), file.getName())) {
                try {
                    String fileContent = getFileContentFromCharset(file, fromCharsetName);
                    saveFile2Charset(file, toCharsetName, fileContent);
                } catch (IOException e) {
                    logger.error("Failed to convert file {} from {} to {}", file.getAbsolutePath(), fromCharsetName, toCharsetName, e);
                }
            }
        }
    }

    /**
     * Reads file content with specified charset.
     *
     * @param file            file to read
     * @param fromCharsetName charset to use for reading
     * @return file content
     * @throws UnsupportedCharsetException if charset is not supported
     * @throws IOException                 if reading fails
     */
    public static String getFileContentFromCharset(File file, String fromCharsetName) throws IOException {
        if (!Charset.isSupported(fromCharsetName)) {
            throw new UnsupportedCharsetException(fromCharsetName);
        }
        return FileContentUtil.readString(file, Charset.forName(fromCharsetName));
    }

    /**
     * Writes content to file with specified charset.
     *
     * @param file          file to write
     * @param toCharsetName charset to use for writing
     * @param content       content to write
     * @throws UnsupportedCharsetException if charset is not supported
     * @throws IOException                 if writing fails
     */
    public static void saveFile2Charset(File file, String toCharsetName, String content) throws IOException {
        if (!Charset.isSupported(toCharsetName)) {
            throw new UnsupportedCharsetException(toCharsetName);
        }
        FileContentUtil.writeString(file, content, Charset.forName(toCharsetName));
    }

    // ==================== Directory Operations ====================

    /**
     * Renames a file or directory.
     *
     * @param oldPath old path
     * @param newPath new path
     * @return true if rename succeeded
     */
    public static boolean dirRename(String oldPath, String newPath) {
        File oldFile = new File(oldPath);
        File newFile = new File(newPath);
        return oldFile.renameTo(newFile);
    }

    /**
     * Creates a directory recursively.
     *
     * @param file directory to create
     * @throws FileNotFoundException if creation fails
     */
    public static void mkDir(File file) throws FileNotFoundException {
        if (file == null) {
            throw new FileNotFoundException("Directory file cannot be null");
        }

        if (file.getParentFile().exists()) {
            if (file.exists()) {
                return;
            }
            if (!file.mkdir()) {
                throw new FileNotFoundException("Failed to create directory: " + file.getAbsolutePath());
            }
        } else {
            mkDir(file.getParentFile());
            if (!file.exists() && !file.mkdir()) {
                throw new FileNotFoundException("Failed to create directory: " + file.getAbsolutePath());
            }
        }
    }

    /**
     * Creates a directory and all parent directories.
     *
     * @param dirPath directory path
     * @throws IOException if creation fails
     */
    public static void mkdir(String dirPath) throws IOException {
        FileDirUtil.mkdir(dirPath);
    }

    /**
     * Creates a directory and all parent directories.
     *
     * @param dir directory
     * @throws IOException if creation fails
     */
    public static void mkdir(File dir) throws IOException {
        FileDirUtil.mkdir(dir);
    }

    /**
     * Creates a directory and all parent directories.
     *
     * @param dirPath directory path
     * @throws IOException if creation fails
     */
    public static void mkdirs(String dirPath) throws IOException {
        FileDirUtil.mkdirs(dirPath);
    }

    // ==================== Touch Operations ====================

    /**
     * Updates file last modified time. Creates file if it doesn't exist.
     *
     * @param file file to touch
     */
    public static void touch(File file) {
        long currentTime = System.currentTimeMillis();
        if (!file.exists()) {
            logger.debug("File not found: {}, creating new file", file.getName());
            try {
                if (file.createNewFile()) {
                    logger.debug("Created new file: {}", file.getName());
                } else {
                    logger.error("Failed to create file: {}", file.getName());
                }
            } catch (IOException e) {
                logger.error("Failed to create file: {}", file.getName(), e);
            }
        }
        boolean result = file.setLastModified(currentTime);
        if (!result) {
            logger.warn("Failed to set last modified time for file: {}", file.getName());
        }
    }

    /**
     * Updates file last modified time. Creates file if it doesn't exist.
     *
     * @param fileName file to touch
     */
    public static void touch(String fileName) {
        touch(new File(fileName));
    }

    /**
     * Updates multiple files last modified time.
     *
     * @param files files to touch
     */
    public static void touch(File[] files) {
        for (File file : files) {
            touch(file);
        }
    }

    /**
     * Updates multiple files last modified time.
     *
     * @param fileNames files to touch
     */
    public static void touch(String[] fileNames) {
        for (String fileName : fileNames) {
            touch(fileName);
        }
    }

    // ==================== Directory Creation ====================

    /**
     * Creates a directory and all parent directories.
     *
     * @param file directory to create
     * @return true if creation succeeded
     */
    public static boolean makeDirectory(File file) {
        if (file.exists()) {
            return file.isDirectory();
        }
        return file.mkdirs();
    }

    /**
     * Creates a directory and all parent directories.
     *
     * @param fileName directory path
     * @return true if creation succeeded
     */
    public static boolean makeDirectory(String fileName) {
        File file = new File(fileName);
        return makeDirectory(file);
    }

    // ==================== Empty Directory ====================

    /**
     * Empties a directory by deleting all files within it.
     * Does not recurse into subdirectories.
     *
     * @param directory directory to empty
     * @throws IOException if directory cannot be emptied
     */
    public static void emptyDirectory(File directory) throws IOException {
        FileDirUtil.cleanDir(directory);
    }

    /**
     * Empties a directory by deleting all files within it.
     * Does not recurse into subdirectories.
     *
     * @param directoryName directory path
     * @throws IOException if directory cannot be emptied
     */
    public static void emptyDirectory(String directoryName) throws IOException {
        emptyDirectory(new File(directoryName));
    }

    // ==================== Delete Directory ====================

    /**
     * Deletes a directory and all its contents recursively.
     *
     * @param dirName directory path
     * @throws IOException if deletion fails
     */
    public static void deleteDirectory(String dirName) throws IOException {
        FileDirUtil.deleteDir(dirName);
    }

    /**
     * Deletes a directory and all its contents recursively.
     *
     * @param dir directory to delete
     * @throws IOException if deletion fails
     */
    public static void deleteDirectory(File dir) throws IOException {
        FileDirUtil.deleteDir(dir);
    }

    /**
     * Deletes a directory and all its contents recursively.
     *
     * @param dir directory to delete
     * @return true if deletion succeeded
     */
    public static boolean deleteDir(File dir) {
        if (dir == null || !dir.exists()) {
            return false;
        }
        try {
            FileDirUtil.deleteDir(dir);
            return true;
        } catch (IOException e) {
            logger.error("Failed to delete directory: {}", dir.getAbsolutePath(), e);
            return false;
        }
    }

    // ==================== List Files ====================

    /**
     * Lists all files in a directory recursively.
     *
     * @param file   directory
     * @param filter file filter
     * @return array of files
     */
    public static File[] listAll(File file, javax.swing.filechooser.FileFilter filter) {
        ArrayList<File> list = new ArrayList<>();
        File[] files;
        if (!file.exists() || file.isFile()) {
            return null;
        }
        listFilesRecursive(list, file, filter);
        files = new File[list.size()];
        list.toArray(files);
        return files;
    }

    private static void listFilesRecursive(ArrayList<File> list, File file, javax.swing.filechooser.FileFilter filter) {
        if (filter == null) {
            // When no filter, only add files (not directories), and don't add the starting directory itself
            if (file.isFile()) {
                list.add(file);
            }
        } else if (filter.accept(file)) {
            list.add(file);
            if (file.isFile()) {
                return;
            }
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    listFilesRecursive(list, f, filter);
                }
            }
        }
    }

    // ==================== URL Operations ====================

    /**
     * Gets URL for a file.
     *
     * @param file file
     * @return URL
     * @throws MalformedURLException if URL is malformed
     * @deprecated Use File.toURI().toURL() instead
     */
    @Deprecated
    /**
     * getURL方法。
     *      * @param file File类型参数
     * @return static URL类型返回值
     */
    public static URL getURL(File file) throws MalformedURLException {
        String fileURL = "file:/" + file.getAbsolutePath();
        return new URL(fileURL);
    }

    // ==================== Path Operations ====================

    /**
     * Gets filename from path.
     *
     * @param filePath file path
     * @return filename
     */
    public static String getFileName(String filePath) {
        return FilePathUtil.getName(filePath);
    }

    /**
     * Gets absolute path from filename.
     *
     * @param fileName filename
     * @return absolute path
     */
    public static String getFilePath(String fileName) {
        return getPathPart(fileName);
    }

    /**
     * Converts path to UNIX style (forward slashes).
     *
     * @param filePath path to convert
     * @return UNIX style path
     */
    public static String toUNIXpath(String filePath) {
        return FilePathUtil.toUNIXPath(filePath);
    }

    /**
     * Gets UNIX style absolute path.
     *
     * @param fileName filename
     * @return UNIX style path
     */
    public static String getUNIXfilePath(String fileName) {
        return toUNIXpath(new File(fileName).getAbsolutePath());
    }

    /**
     * Gets file type/extension.
     *
     * @param fileName filename
     * @return extension
     */
    public static String getTypePart(String fileName) {
        return FilePathUtil.getSuffix(fileName);
    }

    /**
     * Gets file type/extension.
     *
     * @param file file
     * @return extension
     */
    public static String getFileType(File file) {
        return getTypePart(file.getName());
    }

    /**
     * Checks if file is a valid image.
     *
     * @param filePath file path
     * @return true if valid image
     */
    public static boolean isImage(String filePath) throws IOException {
        return FileHashUtil.isImage(new File(filePath));
    }

    /**
     * Gets file name without extension.
     *
     * @param fileName filename
     * @return name part
     */
    public static String getNamePart(String fileName) {
        return FilePathUtil.getBaseName(fileName);
    }

    /**
     * Gets parent directory path.
     *
     * @param fileName filename
     * @return parent path
     */
    public static String getPathPart(String fileName) {
        return FilePathUtil.getParent(fileName);
    }

    /**
     * Gets index of path separator.
     *
     * @param fileName filename
     * @return index
     */
    public static int getPathIndex(String fileName) {
        int point = fileName.indexOf('/');
        if (point == -1) {
            point = fileName.indexOf('\\');
        }
        return point;
    }

    /**
     * Gets index of path separator after given position.
     *
     * @param fileName  filename
     * @param fromIndex start position
     * @return index
     */
    public static int getPathIndex(String fileName, int fromIndex) {
        int point = fileName.indexOf('/', fromIndex);
        if (point == -1) {
            point = fileName.indexOf('\\', fromIndex);
        }
        return point;
    }

    /**
     * Gets last index of path separator.
     *
     * @param fileName filename
     * @return index
     */
    public static int getPathLsatIndex(String fileName) {
        return Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
    }

    /**
     * Gets last index of path separator before given position.
     *
     * @param fileName  filename
     * @param fromIndex start position
     * @return index
     */
    public static int getPathLsatIndex(String fileName, int fromIndex) {
        int point = fileName.lastIndexOf('/', fromIndex);
        if (point == -1) {
            point = fileName.lastIndexOf('\\', fromIndex);
        }
        return point;
    }

    /**
     * Removes extension from filename.
     *
     * @param filename filename
     * @return filename without extension
     */
    public static String trimType(String filename) {
        return FilePathUtil.trimExtension(filename);
    }

    /**
     * Gets relative path from parent to file.
     *
     * @param fullPath full file path
     * @param basePath base directory path
     * @return relative path
     */
    public static String getSubpath(String fullPath, String basePath) {
        return FilePathUtil.getSubpath(fullPath, basePath);
    }

    /**
     * Validates and creates path if needed.
     *
     * @param path path to validate
     * @return true if path exists or was created
     */
    public static boolean pathValidate(String path) {
        String[] arraypath = path.split("/");
        String tmppath = "";
        for (int i = 0; i < arraypath.length; i++) {
            tmppath += "/" + arraypath[i];
            File d = new File(tmppath.substring(1));
            if (!d.exists()) {
                logger.debug("Creating directory: {}", tmppath.substring(1));
                if (!d.mkdir()) {
                    return false;
                }
            }
        }
        return true;
    }

    // ==================== File Read Operations ====================

    /**
     * Reads string from URL.
     *
     * @param url     URL to read
     * @param charset charset to use
     * @return content as string
     * @throws IOException if reading fails
     */
    public static String readString(URL url, String charset) throws IOException {
        if (url == null) {
            throw new NullPointerException("Empty url provided!");
        }
        try (InputStream in = url.openStream()) {
            byte[] b = new byte[in.available()];
            in.read(b);
            return new String(b, charset);
        }
    }

    /**
     * Gets file content as string.
     *
     * @param f file
     * @return content
     * @throws IOException if reading fails
     */
    public static String getFileContent(File f) throws IOException {
        return FileContentUtil.readString(f);
    }

    /**
     * Gets file content as string.
     *
     * @param path file path
     * @return content
     * @throws IOException if reading fails
     */
    public static String getFileContent(String path) throws IOException {
        if (!path.startsWith("/")) {
            path = FileUtil.class.getResource("/").getPath() + path;
        }
        return FileContentUtil.readString(new File(path), StandardCharsets.UTF_8);
    }

    /**
     * Generates module template file.
     *
     * @param path          file path
     * @param modulecontent content
     * @return true if succeeded
     * @throws IOException if writing fails
     */
    public static boolean genModuleTpl(String path, String modulecontent) throws IOException {
        path = getUNIXfilePath(path);
        String[] patharray = path.split("/");
        String modulepath = "";
        for (int i = 0; i < patharray.length - 1; i++) {
            modulepath += "/" + patharray[i];
        }
        File d = new File(modulepath.substring(1));
        if (!d.exists()) {
            if (!pathValidate(modulepath.substring(1))) {
                return false;
            }
        }
        FileWriter fw = new FileWriter(path);
        fw.write(modulecontent);
        fw.close();
        return true;
    }

    /**
     * Gets picture extension name.
     *
     * @param pic_path picture file path
     * @return extension without dot (e.g., "jpg") or null if no extension
     */
    public static String getPicExtendName(String pic_path) {
        pic_path = getUNIXfilePath(pic_path);
        if (!isFileExist(pic_path)) {
            return null;
        }
        // Check for image extensions
        if (isFileExist(pic_path + ".gif")) {
            return "gif";
        }
        if (isFileExist(pic_path + ".jpeg")) {
            return "jpeg";
        }
        if (isFileExist(pic_path + ".jpg")) {
            return "jpg";
        }
        if (isFileExist(pic_path + ".png")) {
            return "png";
        }
        // File exists but no image extension found - extract extension from filename
        int lastDot = pic_path.lastIndexOf('.');
        int lastSep = Math.max(pic_path.lastIndexOf('/'), pic_path.lastIndexOf('\\'));
        if (lastDot > lastSep && lastDot < pic_path.length() - 1) {
            return pic_path.substring(lastDot + 1);
        }
        return null;
    }

    // ==================== Copy File Operations ====================

    /**
     * Copies a file.
     *
     * @param in  input file
     * @param out output file
     * @return true if succeeded
     */
    public static boolean CopyFile(File in, File out) {
        try {
            FileCopyUtil.copyFile(in, out);
            return true;
        } catch (IOException e) {
            logger.error("Failed to copy file {} to {}", in.getAbsolutePath(), out.getAbsolutePath(), e);
            return false;
        }
    }

    /**
     * Saves content to file.
     *
     * @param content content to write
     * @param path    file path
     * @return true if succeeded
     */
    public static boolean SaveFileAs(String content, String path) {
        try {
            FileContentUtil.writeString(new File(path), content);
            return true;
        } catch (IOException e) {
            logger.error("Failed to save file: {}", path, e);
            return false;
        }
    }

    /**
     * Reads file as byte array.
     *
     * @param fileName file path
     * @return byte array
     */
    public static byte[] readFileByBytes(String fileName) {
        try {
            return FileContentUtil.readBytes(new File(fileName));
        } catch (IOException e) {
            logger.error("Failed to read file: {}", fileName, e);
            return null;
        }
    }

    /**
     * Reads file as chars.
     *
     * @param fileName file path
     */
    public static void readFileByChars(String fileName) {
        File file = new File(fileName);
        try (Reader reader = new InputStreamReader(new FileInputStream(file))) {
            int tempchar;
            while ((tempchar = reader.read()) != -1) {
                if (((char) tempchar) != '\r') {
                    System.out.print((char) tempchar);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to read file: {}", fileName, e);
        }
    }

    /**
     * Reads file as lines.
     *
     * @param fileName file path
     * @return list of lines
     */
    public static List<String> readFileByLines(String fileName) {
        try {
            return FileContentUtil.readLines(new File(fileName));
        } catch (IOException e) {
            logger.error("Failed to read file: {}", fileName, e);
            return new ArrayList<>();
        }
    }

    /**
     * Reads file randomly.
     *
     * @param fileName file path
     */
    public static void readFileByRandomAccess(String fileName) {
        try (RandomAccessFile randomFile = new RandomAccessFile(fileName, "r")) {
            long fileLength = randomFile.length();
            int beginIndex = (int) ((fileLength > 4) ? 4 : 0);
            randomFile.seek(beginIndex);
            byte[] bytes = new byte[10];
            int byteread;
            while ((byteread = randomFile.read(bytes)) != -1) {
                System.out.write(bytes, 0, byteread);
            }
        } catch (IOException e) {
            logger.error("Failed to read file randomly: {}", fileName, e);
        }
    }

    /**
     * Reads entire file as string.
     *
     * @param fileName file path
     * @return content
     */
    public static String readFileAll(String fileName) {
        File file = new File(fileName);
        long filelength = file.length();
        if (filelength > Integer.MAX_VALUE) {
            logger.error("File too large: {}", fileName);
            return "";
        }
        byte[] filecontent = new byte[(int) filelength];
        try (FileInputStream in = new FileInputStream(file)) {
            in.read(filecontent);
            return new String(filecontent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("Failed to read file: {}", fileName, e);
            return "";
        }
    }

    /**
     * 从 InputStream 读取全部字节（Java 8 兼容，替代 readAllBytes）
     */
    public static byte[] readAllBytes(InputStream is) throws IOException {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int len;
        while ((len = is.read(buf)) != -1) baos.write(buf, 0, len);
        return baos.toByteArray();
    }

    // ==================== Rename Operations ====================

    /**
     * Renames a file.
     *
     * @param oldFile old file
     * @param newFile new file
     */
    public static void renameTo(File oldFile, File newFile) {
        if (!oldFile.renameTo(newFile)) {
            logger.warn("Failed to rename file {} to {}", oldFile.getAbsolutePath(), newFile.getAbsolutePath());
        }
    }

    // ==================== Exist Check ====================

    /**
     * Checks if path exists.
     *
     * @param filePath path
     * @param isNew    create if not exists
     * @return true if exists
     */
    public static boolean isExist(String filePath, boolean isNew) {
        File file = new File(filePath);
        if (!file.exists() && isNew) {
            return file.mkdirs();
        }
        return file.exists();
    }

    /**
     * Checks if file exists.
     *
     * @param filePath path
     * @return true if exists
     */
    public static boolean isFileExist(String filePath) {
        return new File(filePath).exists();
    }

    // ==================== Filename Generation ====================

    /**
     * Generates filename with timestamp and random number.
     *
     * @param type   file type
     * @param prefix prefix
     * @param suffix suffix
     * @return generated filename
     */
    public static String getFileName(String type, String prefix, String suffix) {
        String date = new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String random = String.valueOf((int) ((Math.random() * 9 + 1) * 1000000000));
        return prefix + date + random + suffix + "." + type;
    }

    /**
     * Generates filename with timestamp and random number.
     *
     * @return generated filename
     */
    public static String getFileName() {
        return getFileName("", "", "");
    }

    // ==================== File Size ====================

    /**
     * Gets file size.
     *
     * @param file file
     * @return file size in bytes
     * @throws IOException if error occurs
     */
    public static long getFileSize(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            return fis.available();
        }
    }

    // ==================== Delete All ====================

    /**
     * Deletes all files and directories recursively.
     *
     * @param dirpath directory path
     */
    public static void deleteAll(String dirpath) {
        File path = new File(dirpath);
        if (!path.exists()) {
            return;
        }
        if (path.isFile()) {
            path.delete();
            return;
        }
        File[] files = path.listFiles();
        if (files != null) {
            for (File file : files) {
                deleteAll(file.getAbsolutePath());
            }
        }
        path.delete();
    }

    // ==================== Copy Operations ====================

    /**
     * Copies file or directory.
     *
     * @param inputFile   source
     * @param outputFile  destination
     * @param isOverWrite overwrite existing
     * @throws IOException if copy fails
     */
    public static void copy(File inputFile, File outputFile, boolean isOverWrite) throws IOException {
        if (inputFile.isFile()) {
            FileCopyUtil.copyFile(inputFile, outputFile, isOverWrite);
        } else {
            if (!outputFile.exists()) {
                outputFile.mkdirs();
            }
            File[] children = inputFile.listFiles();
            if (children != null) {
                for (File child : children) {
                    copy(child, new File(outputFile, child.getName()), isOverWrite);
                }
            }
        }
    }

    /**
     * Copies directory recursively.
     *
     * @param filePath   source path
     * @param targetPath destination path
     */
    public static void copyDir(String filePath, String targetPath) {
        try {
            FileCopyUtil.copyDir(new File(filePath), targetPath);
        } catch (IOException e) {
            logger.error("Failed to copy directory {} to {}", filePath, targetPath, e);
        }
    }

    /**
     * Copies directory recursively.
     *
     * @param filePath   source
     * @param targetPath destination
     */
    public static void copyDir(File filePath, String targetPath) {
        try {
            FileCopyUtil.copyDir(filePath, targetPath);
        } catch (IOException e) {
            logger.error("Failed to copy directory {} to {}", filePath.getAbsolutePath(), targetPath, e);
        }
    }

    // ==================== MD5 Operations ====================

    /**
     * Gets MD5 hash of file.
     *
     * @param file file
     * @return MD5 hash or null if error
     */
    public static String getFileMD5(File file) {
        try {
            return FileHashUtil.md5(file);
        } catch (IOException | NoSuchAlgorithmException e) {
            logger.error("Failed to compute MD5 for file: {}", file.getAbsolutePath(), e);
            return null;
        }
    }

    // ==================== File Suffix ====================

    /**
     * Gets file suffix/extension.
     *
     * @param file file path
     * @return extension without dot
     */
    public static String getFileSuffix(String file) {
        return FilePathUtil.getSuffix(file);
    }

    /**
     * Gets file suffix/extension.
     *
     * @param file file
     * @return extension without dot
     */
    public static String suffix(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.indexOf(".");
        if (dotIndex >= 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        }
        return "";
    }

    // ==================== Hash Operations ====================

    /**
     * Computes file hash.
     *
     * @param file     file
     * @param hashType algorithm (MD5, SHA-1, SHA-256, etc.)
     * @return hash as hex string or empty on error
     */
    public static String fileHash(File file, String hashType) {
        try {
            return FileHashUtil.hash(file, hashType);
        } catch (IOException | NoSuchAlgorithmException e) {
            logger.error("Failed to compute {} hash for file: {}", hashType, file.getAbsolutePath(), e);
            return "";
        }
    }

    // ==================== Line Counting ====================

    /**
     * Counts lines in file.
     *
     * @param file file
     * @return number of lines
     */
    public static int countLines(File file) {
        try {
            return (int) FileContentUtil.countLines(file);
        } catch (IOException e) {
            logger.error("Failed to count lines in file: {}", file.getAbsolutePath(), e);
            return 0;
        }
    }

    // ==================== Lines Operations ====================

    /**
     * Reads all lines from file.
     *
     * @param file file
     * @return list of lines
     */
    public static List<String> lines(File file) {
        try {
            return FileContentUtil.readLines(file);
        } catch (IOException e) {
            logger.error("Failed to read lines from file: {}", file.getAbsolutePath(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Reads all lines from file with encoding.
     *
     * @param file     file
     * @param encoding charset
     * @return list of lines
     */
    public static List<String> lines(File file, String encoding) {
        try {
            return FileContentUtil.readLines(file, encoding);
        } catch (IOException e) {
            logger.error("Failed to read lines from file: {} with encoding {}", file.getAbsolutePath(), encoding, e);
            return new ArrayList<>();
        }
    }

    /**
     * Reads first n lines from file.
     *
     * @param file  file
     * @param lines number of lines
     * @return list of lines
     */
    public static List<String> lines(File file, int lines) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            List<String> list = new ArrayList<>();
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null && count < lines) {
                list.add(line);
                count++;
            }
            return list;
        } catch (IOException e) {
            logger.error("Failed to read {} lines from file: {}", lines, file.getAbsolutePath(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Reads first n lines from file with encoding.
     *
     * @param file     file
     * @param lines    number of lines
     * @param encoding charset
     * @return list of lines
     */
    public static List<String> lines(File file, int lines, String encoding) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding))) {
            List<String> list = new ArrayList<>();
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null && count < lines) {
                list.add(line);
                count++;
            }
            return list;
        } catch (IOException e) {
            logger.error("Failed to read {} lines from file: {} with encoding {}", lines, file.getAbsolutePath(), encoding, e);
            return new ArrayList<>();
        }
    }

    // ==================== Append Line ====================

    /**
     * Appends a line to file.
     *
     * @param file file
     * @param str  line to append
     * @return true if succeeded
     */
    public static boolean appendLine(File file, String str) {
        try {
            FileContentUtil.appendLine(file, str);
            return true;
        } catch (IOException e) {
            logger.error("Failed to append line to file: {}", file.getAbsolutePath(), e);
            return false;
        }
    }

    /**
     * Appends a line to file with encoding.
     *
     * @param file     file
     * @param str      line to append
     * @param encoding charset
     * @return true if succeeded
     */
    public static boolean appendLine(File file, String str, String encoding) {
        try {
            FileContentUtil.appendLine(file, str);
            return true;
        } catch (IOException e) {
            logger.error("Failed to append line to file: {} with encoding {}", file.getAbsolutePath(), encoding, e);
            return false;
        }
    }

    // ==================== Write Operations ====================

    /**
     * Writes string to file (overwrites).
     *
     * @param file file
     * @param str  content
     * @return true if succeeded
     */
    public static boolean writeAppend(File file, String str) {
        try {
            FileContentUtil.appendString(file, str);
            return true;
        } catch (IOException e) {
            logger.error("Failed to write to file: {}", file.getAbsolutePath(), e);
            return false;
        }
    }

    /**
     * Writes string to file with encoding (overwrites).
     *
     * @param file     file
     * @param str      content
     * @param encoding charset
     * @return true if succeeded
     */
    public static boolean write(File file, String str, String encoding) {
        try {
            FileContentUtil.writeString(file, str, Charset.forName(encoding));
            return true;
        } catch (IOException e) {
            logger.error("Failed to write to file: {} with encoding {}", file.getAbsolutePath(), encoding, e);
            return false;
        }
    }

    /**
     * Appends string to file with encoding.
     *
     * @param file     file
     * @param str      content
     * @param encoding charset
     * @return true if succeeded
     */
    public static boolean writeAppend(File file, String str, String encoding) {
        try {
            FileContentUtil.appendString(file, str);
            return true;
        } catch (IOException e) {
            logger.error("Failed to append to file: {} with encoding {}", file.getAbsolutePath(), encoding, e);
            return false;
        }
    }

    /**
     * Writes string to file.
     *
     * @param file file
     * @param str  content
     * @return true if succeeded
     */
    public static boolean write(File file, String str) {
        try {
            FileContentUtil.writeString(file, str);
            return true;
        } catch (IOException e) {
            logger.error("Failed to write to file: {}", file.getAbsolutePath(), e);
            return false;
        }
    }

    /**
     * Appends string to file.
     *
     * @param file     file
     * @param str      content
     * @param encoding charset
     * @return true if succeeded
     */
    public static boolean addWrite(File file, String str, String encoding) {
        try {
            FileContentUtil.appendString(file, str);
            return true;
        } catch (IOException e) {
            logger.error("Failed to add write to file: {} with encoding {}", file.getAbsolutePath(), encoding, e);
            return false;
        }
    }

    // ==================== Clean File ====================

    /**
     * Empties a file quickly.
     *
     * @param file file to empty
     * @return true if succeeded
     */
    public static boolean cleanFile(File file) {
        try (FileWriter fw = new FileWriter(file)) {
            fw.write("");
            return true;
        } catch (IOException e) {
            logger.error("Failed to clean file: {}", file.getAbsolutePath(), e);
            return false;
        }
    }

    // ==================== MIME Type ====================

    /**
     * Gets MIME type of file.
     *
     * @param file file path
     * @return MIME type
     * @throws IOException if error
     */
    public static String mimeType(String file) throws IOException {
        java.net.FileNameMap fileNameMap = URLConnection.getFileNameMap();
        return fileNameMap.getContentTypeFor(file);
    }

    // ==================== File Type Detection ====================

    /**
     * Detects file type by magic bytes.
     *
     * @param file file
     * @return file type (jpg, png, pdf, etc.) or null
     */
    public static String fileType(File file) throws IOException {
        return FileHashUtil.fileType(file);
    }

    /**
     * Gets file modification time.
     *
     * @param file file
     * @return modification date
     */
    public static Date modifyTime(File file) {
        return new Date(file.lastModified());
    }

    // ==================== Copy with Channel ====================

    /**
     * Copies file using FileChannel.
     *
     * @param resourcePath source
     * @param targetPath   destination
     * @return true if succeeded
     */
    public static boolean copy(String resourcePath, String targetPath) {
        return copy(new File(resourcePath), targetPath);
    }

    /**
     * Copies file using FileChannel.
     *
     * @param file       source
     * @param targetFile destination
     * @return true if succeeded
     */
    public static boolean copy(File file, String targetFile) {
        try (FileInputStream fin = new FileInputStream(file);
             FileOutputStream fout = new FileOutputStream(new File(targetFile))) {
            FileChannel in = fin.getChannel();
            FileChannel out = fout.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            while (in.read(buffer) != -1) {
                buffer.flip();
                out.write(buffer);
                buffer.clear();
            }
            return true;
        } catch (IOException e) {
            logger.error("Failed to copy file {} to {}", file.getAbsolutePath(), targetFile, e);
            return false;
        }
    }

    /**
     * Copies file.
     *
     * @param resource source
     * @param target   destination
     * @return true if succeeded
     * @throws IOException if copy fails
     */
    public static boolean copy(File resource, File target) throws IOException {
        if (resource.isFile()) {
            FileCopyUtil.copyFile(resource, target);
            return true;
        }
        File[] files = resource.listFiles();
        if (files == null || files.length == 0) {
            return target.mkdirs();
        }
        target.mkdirs();
        for (File file : files) {
            String targetFilePath = file.getAbsolutePath().substring(resource.getAbsolutePath().length());
            File targetFile = new File(target.getAbsolutePath() + "/" + targetFilePath);
            if (file.isDirectory()) {
                targetFile.mkdirs();
                copy(file, targetFile);
            } else {
                FileCopyUtil.copyFile(file, targetFile);
            }
        }
        return true;
    }

    /**
     * Copies file using FileChannel.
     *
     * @param file       source
     * @param targetFile destination
     * @return true if succeeded
     * @throws IOException if copy fails
     */
    public static boolean copyFile(File file, File targetFile) throws IOException {
        FileCopyUtil.copyFile(file, targetFile);
        return true;
    }

    // ==================== Create Paths ====================

    /**
     * Creates multiple directory levels.
     *
     * @param paths directory path
     * @return true if succeeded
     */
    public static boolean createPaths(String paths) {
        File dir = new File(paths);
        return !dir.exists() && dir.mkdir();
    }

    /**
     * Creates file with parent directories.
     *
     * @param filePath file path
     * @return true if succeeded
     */
    public static boolean createFiles(String filePath) {
        File file = new File(filePath);
        if (file.isDirectory()) {
            if (!file.exists()) {
                return file.mkdirs();
            }
        } else {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                if (dir.mkdirs()) {
                    try {
                        return file.createNewFile();
                    } catch (IOException e) {
                        logger.error("Failed to create file: {}", filePath, e);
                    }
                }
            }
        }
        return false;
    }

    /**
     * Creates file with parent directories.
     *
     * @param file file
     * @return true if succeeded
     */
    public static boolean createFiles(File file) {
        if (file.exists()) {
            return true;
        }
        if (file.isDirectory()) {
            if (!file.exists()) {
                return file.mkdirs();
            }
        } else {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                if (dir.mkdirs()) {
                    try {
                        return file.createNewFile();
                    } catch (IOException e) {
                        logger.error("Failed to create file: {}", file.getAbsolutePath(), e);
                    }
                }
            } else {
                try {
                    return file.createNewFile();
                } catch (IOException e) {
                    logger.error("Failed to create file: {}", file.getAbsolutePath(), e);
                }
            }
        }
        return false;
    }

    /**
     * Creates file with parent directories.
     *
     * @param file    file
     * @param isReNew recreate if exists
     * @return true if succeeded
     */
    public static boolean createFiles(File file, boolean isReNew) {
        if (file.exists()) {
            if (isReNew) {
                if (file.delete()) {
                    try {
                        return file.createNewFile();
                    } catch (IOException e) {
                        logger.error("Failed to create file: {}", file.getAbsolutePath(), e);
                    }
                }
            }
            return true;
        }
        if (file.isDirectory()) {
            if (!file.exists()) {
                return file.mkdirs();
            }
        } else {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                if (dir.mkdirs()) {
                    try {
                        return file.createNewFile();
                    } catch (IOException e) {
                        logger.error("Failed to create file: {}", file.getAbsolutePath(), e);
                    }
                }
            } else {
                try {
                    return file.createNewFile();
                } catch (IOException e) {
                    logger.error("Failed to create file: {}", file.getAbsolutePath(), e);
                }
            }
        }
        return false;
    }

    /**
     * Creates file with parent directories.
     *
     * @param path file path
     * @return true if succeeded
     */
    public static boolean createFile(String path) {
        if (path != null && !path.isEmpty()) {
            try {
                File file = new File(path);
                if (!file.getParentFile().exists() && file.getParentFile().mkdirs()) {
                    return file.createNewFile();
                } else if (file.exists()) {
                    return true;
                } else {
                    return file.createNewFile();
                }
            } catch (Exception e) {
                logger.error("Failed to create file: {}", path, e);
            }
        }
        return false;
    }

    // ==================== Delete File ====================

    /**
     * Deletes a file.
     *
     * @param file file
     * @return true if deleted
     */
    public static boolean deleteFile(File file) {
        return file.delete();
    }

    /**
     * Deletes a directory and all contents.
     *
     * @param file directory
     * @return true if deleted
     */
    public static boolean delete(File file) {
        if (file == null) {
            return false;
        }
        if (file.isFile()) {
            return file.delete();
        }
        File[] files = file.listFiles();
        if (files == null) {
            return file.delete();
        }
        for (File ff : files) {
            if (ff.isDirectory()) {
                delete(ff);
            } else {
                if (ff.length() > 1024 * 1024 * 1024) {
                    cleanFile(ff);
                }
                ff.delete();
            }
        }
        return file.delete();
    }

    /**
     * Deletes a large file quickly.
     *
     * @param file file
     * @return true if deleted
     */
    public static boolean deleteBigFile(File file) {
        return cleanFile(file) && file.delete();
    }

    // ==================== List Files ====================

    /**
     * Lists files in directory recursively.
     *
     * @param path directory path
     * @return list of files
     */
    public static List<File> listFile(String path) {
        return listFile(new File(path));
    }

    /**
     * Lists files in directory recursively.
     *
     * @param path directory
     * @return list of files
     */
    public static List<File> listFile(File path) {
        List<File> list = new ArrayList<>();
        if (path != null && path.exists() && path.isDirectory()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        list.addAll(listFile(file));
                    } else {
                        list.add(file);
                    }
                }
            }
        }
        return list;
    }

    /**
     * Lists all files including directories.
     *
     * @param path directory
     * @return list of all files and directories
     */
    public static List<File> listFileAll(File path) {
        List<File> list = new ArrayList<>();
        File[] files = path.listFiles();
        if (files != null) {
            for (File file : files) {
                list.add(file);
                if (file.isDirectory()) {
                    list.addAll(listFileAll(file));
                }
            }
        }
        return list;
    }

    /**
     * Lists files matching filter.
     *
     * @param path   directory
     * @param filter filename filter
     * @return list of matching files
     */
    public static List<File> listFileFilter(File path, FilenameFilter filter) {
        List<File> list = new ArrayList<>();
        File[] files = path.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    list.addAll(listFileFilter(file, filter));
                } else {
                    if (filter.accept(file.getParentFile(), file.getName())) {
                        list.add(file);
                    }
                }
            }
        }
        return list;
    }

    /**
     * Lists files with postfix filter.
     *
     * @param dirPath  directory
     * @param postfixs extension filter (e.g., ".txt")
     * @return list of matching files
     */
    public static List<File> listFileFilter(File dirPath, String postfixs) {
        List<File> list = new ArrayList<>();
        File[] files = dirPath.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    list.addAll(listFileFilter(file, postfixs));
                } else {
                    String fileName = file.getName().toLowerCase();
                    if (fileName.endsWith(postfixs.toLowerCase())) {
                        list.add(file);
                    }
                }
            }
        }
        return list;
    }

    /**
     * Lists files with suffix.
     *
     * @param dirPath  directory
     * @param postfixs extension filter
     * @return list of matching files
     */
    public static List<File> listFileSuffix(File dirPath, String postfixs) {
        return listFileFilter(dirPath, postfixs);
    }

    /**
     * Searches for file by name.
     *
     * @param dirPath  directory to search
     * @param fileName filename to find
     * @return list of matching files
     */
    public static List<File> searchFile(File dirPath, String fileName) {
        List<File> list = new ArrayList<>();
        File[] files = dirPath.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    list.addAll(searchFile(file, fileName));
                } else {
                    if (file.getName().equals(fileName)) {
                        list.add(file);
                    }
                }
            }
        }
        return list;
    }

    /**
     * Lists files by exact name match.
     *
     * @param dirPath  directory
     * @param fileName filename
     * @return list of matching files
     */
    public static List<File> listFileName(File dirPath, String fileName) {
        return searchFile(dirPath, fileName);
    }

    /**
     * Lists files by name (case insensitive).
     *
     * @param dirPath  directory
     * @param fileName filename
     * @return list of matching files
     */
    public static List<File> listFileNameIgnoreCase(File dirPath, String fileName) {
        List<File> list = new ArrayList<>();
        File[] files = dirPath.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    list.addAll(listFileNameIgnoreCase(file, fileName));
                } else {
                    if (file.getName().equalsIgnoreCase(fileName)) {
                        list.add(file);
                    }
                }
            }
        }
        return list;
    }

    /**
     * Lists files matching predicate.
     *
     * @param path   directory
     * @param filter predicate filter
     * @return list of matching files
     */
    public static List<File> listFileFilter(File path, Predicate<File> filter) {
        List<File> list = new ArrayList<>();
        if (path != null && path.exists() && path.isDirectory()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        list.addAll(listFileFilter(file, filter));
                    } else {
                        if (filter.test(file)) {
                            list.add(file);
                        }
                    }
                }
            }
        }
        return list;
    }

    /**
     * Lists files matching regex pattern.
     *
     * @param dirPath directory
     * @param pattern regex pattern
     * @return list of matching files
     */
    public static List<File> listFileNameReg(File dirPath, Pattern pattern) {
        List<File> list = new ArrayList<>();
        File[] files = dirPath.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    list.addAll(listFileNameReg(file, pattern));
                } else {
                    if (pattern.matcher(file.getName()).matches()) {
                        list.add(file);
                    }
                }
            }
        }
        return list;
    }

    // ==================== Read File from Classpath ====================

    /**
     * Reads file from classpath.
     *
     * @param fileName classpath resource name
     * @return file content
     */
    public static String readFile(String fileName) {
        InputStream fin = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        if (fin == null) {
            logger.error("Resource not found: {}", fileName);
            return "";
        }
        try (InputStreamReader reader = new InputStreamReader(fin)) {
            StringBuilder buffer = new StringBuilder();
            BufferedReader buffReader = new BufferedReader(reader);
            String strTmp;
            while ((strTmp = buffReader.readLine()) != null) {
                buffer.append(strTmp).append("\n");
            }
            return buffer.toString();
        } catch (IOException e) {
            logger.error("Failed to read resource: {}", fileName, e);
            return "";
        }
    }

    // ==================== Process Lines ====================

    /**
     * Processes file lines with consumer.
     *
     * @param file     file to process
     * @param encoding charset
     * @param consumer line processor
     */
    public static void handlerWithLine(File file, String encoding, Consumer<String> consumer) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()), encoding))) {
            String line;
            while ((line = reader.readLine()) != null) {
                consumer.accept(line);
            }
        } catch (IOException e) {
            logger.error("Failed to process file: {}", file.getAbsolutePath(), e);
        }
    }

    /**
     * Processes file lines and collects results.
     *
     * @param file     file to process
     * @param encoding charset
     * @param result   collection for results
     * @param mapper   line to result mapper
     * @param <E>      result type
     */
    public static <E> void processWithLine(File file, String encoding, Collection<E> result, Function<String, E> mapper) {
        if (result == null) {
            logger.info("Result collection is null");
            return;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()), encoding))) {
            String line;
            while ((line = reader.readLine()) != null) {
                E tmpLine = mapper.apply(line);
                if (tmpLine != null) {
                    result.add(tmpLine);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to process file: {}", file.getAbsolutePath(), e);
        }
    }

    // ==================== Temp Folder ====================

    /**
     * Gets system temp folder path.
     *
     * @return temp folder path ending with "/"
     */
    public static String decideTmpFolder() {
        String folderOld = System.getProperty("java.io.tmpdir");
        if (folderOld.endsWith("/")) {
            return folderOld;
        }
        return folderOld + "/";
    }

    /**
     * Gets unique temp folder based on timestamp.
     *
     * @return temp folder path
     */
    public static String decideTempUsableFolder() {
        return decideTmpFolder() + "use_" + System.currentTimeMillis() + "/";
    }

    // ==================== Clean Folder ====================

    /**
     * Cleans folder contents.
     *
     * @param fileFolder folder path
     * @return true if cleaned
     * @throws RuntimeException if not a folder
     */
    public static boolean cleanFolder(String fileFolder) {
        File file = new File(fileFolder);
        if (!file.exists()) {
            return false;
        }
        if (!file.isDirectory()) {
            throw new RuntimeException("Path is not a directory: " + fileFolder);
        }
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    cleanFolder(f.getAbsolutePath());
                }
                f.delete();
            }
        }
        return true;
    }

    // ==================== Stream Operations ====================

    /**
     * Converts input stream to string.
     *
     * @param in input stream
     * @return string content
     * @throws IOException if reading fails
     */
    public static String streamToString(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] b = new byte[4096];
        int n;
        while ((n = in.read(b)) != -1) {
            out.write(b, 0, n);
        }
        return out.toString();
    }

    /**
     * Converts input stream to byte array.
     *
     * @param is input stream
     * @return byte array
     * @throws IOException if reading fails
     */
    public static byte[] stream2Byte(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int len;
        byte[] b = new byte[1024];
        while ((len = is.read(b, 0, b.length)) != -1) {
            baos.write(b, 0, len);
        }
        return baos.toByteArray();
    }

    /**
     * Converts input stream to byte array.
     *
     * @param inStream input stream
     * @return byte array
     * @throws Exception if reading fails
     */
    public static byte[] inputStream2Byte(InputStream inStream) throws Exception {
        int count = 0;
        while (count == 0) {
            count = inStream.available();
        }
        byte[] b = new byte[count];
        inStream.read(b);
        return b;
    }

    /**
     * Saves input stream to file.
     *
     * @param is      input stream
     * @param outfile output file
     */
    public static void streamSaveAsFile(InputStream is, File outfile) {
        try (FileOutputStream fos = new FileOutputStream(outfile)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        } catch (IOException e) {
            logger.error("Failed to save stream to file: {}", outfile.getAbsolutePath(), e);
            throw new RuntimeException(e);
        }
    }

    // ==================== Read Lines Aliases ====================

    /**
     * Reads all lines from file.
     *
     * @param file file
     * @return list of lines
     */
    public static List<String> readLines(File file) {
        return lines(file);
    }

    /**
     * Reads all lines from file with encoding.
     *
     * @param file     file
     * @param encoding charset
     * @return list of lines
     */
    public static List<String> readLines(File file, String encoding) {
        return lines(file, encoding);
    }

    /**
     * Reads file contents as string.
     *
     * @param file file
     * @return content or null on error
     */
    public static String readContents(File file) {
        try (FileInputStream in = new FileInputStream(file)) {
            long filelength = file.length();
            byte[] filecontent = new byte[(int) filelength];
            if (in.read(filecontent) > 0) {
                return new String(filecontent, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            logger.error("Failed to read file: {}", file.getAbsolutePath(), e);
        }
        return null;
    }

    // ==================== Generate File ====================

    /**
     * Generates file with content.
     *
     * @param filePath    file path
     * @param fileContent content
     */
    public static void gennerateFile(String filePath, String fileContent) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        } else {
            file.getParentFile().mkdirs();
        }
        try {
            file.createNewFile();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write(fileContent);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            logger.error("Failed to generate file: {}", filePath, e);
        }
    }

    // ==================== NIO Path Operations (统一 Files.walk 便捷方法) ====================

    /**
     * 递归列出目录下所有文件（仅文件，不含目录）。
     * <p>
     * 等价于
     * <pre>{@code
     * try (Stream<Path> walk = Files.walk(dir)) {
     *     return walk.filter(Files::isRegularFile).collect(Collectors.toList());
     * }
     * }</pre>
     * <p>
     * 当 {@code dir} 不存在或不是目录时，返回空列表（不抛异常）。
     *
     * @param dir 目录路径
     * @return 所有子文件的 {@link Path} 列表
     */
    public static List<Path> listFiles(Path dir) {
        List<Path> result = new ArrayList<>();
        if (dir == null || !Files.isDirectory(dir)) {
            return result;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.filter(Files::isRegularFile).forEach(result::add);
        } catch (IOException e) {
            logger.error("Failed to list files under directory: {}", dir, e);
        }
        return result;
    }

    /**
     * 递归列出目录下所有 {@link File}（仅文件，不含目录）。
     *
     * @param dir 目录
     * @return 所有子文件的 {@link File} 列表
     */
    public static List<File> listFilesAsFile(Path dir) {
        List<File> result = new ArrayList<>();
        if (dir == null || !Files.isDirectory(dir)) {
            return result;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.filter(Files::isRegularFile).forEach(p -> result.add(p.toFile()));
        } catch (IOException e) {
            logger.error("Failed to list files under directory: {}", dir, e);
        }
        return result;
    }

    /**
     * 递归删除目录或文件。
     * <p>
     * 等价于 {@link #deleteDirectory(File)}，但接受 {@link Path} 类型。
     * 删除失败会记录 error 日志并继续，不抛异常。
     *
     * @param path 目录或文件路径
     * @return true 如果整个路径树被成功删除（即使部分子项删除失败也算"尝试了删除"）
     */
    public static boolean deleteRecursively(Path path) {
        if (path == null || !Files.exists(path)) {
            return false;
        }
        try (Stream<Path> walk = Files.walk(path)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException e) {
                    logger.error("Failed to delete path: {}", p, e);
                }
            });
            return true;
        } catch (IOException e) {
            logger.error("Failed to walk directory for deletion: {}", path, e);
            return false;
        }
    }

    /**
     * 递归复制目录或文件。
     * <p>
     * 等价于
     * <pre>{@code
     * try (Stream<Path> walk = Files.walk(source)) {
     *     for (Path src : walk.toArray(Path[]::new)) {
     *         Path dest = target.resolve(source.relativize(src).toString());
     *         Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
     *     }
     * }
     * }</pre>
     * <p>
     * 当 {@code target} 不存在时会自动创建父目录。
     *
     * @param source 源路径（文件或目录）
     * @param target 目标路径（文件或目录）
     * @return true 如果复制成功（所有子项都被处理），false 如果有异常
     */
    public static boolean copyRecursively(Path source, Path target) {
        if (source == null || target == null) {
            return false;
        }
        if (!Files.exists(source)) {
            logger.error("Source path does not exist: {}", source);
            return false;
        }
        try {
            if (Files.isDirectory(source)) {
                Files.createDirectories(target);
                try (Stream<Path> walk = Files.walk(source)) {
                    Path[] paths = walk.toArray(Path[]::new);
                    for (Path src : paths) {
                        Path dest = target.resolve(source.relativize(src).toString());
                        if (Files.isDirectory(src)) {
                            Files.createDirectories(dest);
                        } else {
                            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            } else {
                if (target.getParent() != null) {
                    Files.createDirectories(target.getParent());
                }
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException e) {
            logger.error("Failed to copy {} to {}", source, target, e);
            return false;
        }
    }

    /**
     * 计算目录总大小（所有文件的字节数之和）。
     *
     * @param dir 目录路径
     * @return 总字节数；当目录不存在或读取失败时返回 0
     */
    public static long sizeOfDirectory(Path dir) {
        if (dir == null || !Files.isDirectory(dir)) {
            return 0L;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            return walk.filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        try {
                            return Files.size(p);
                        } catch (IOException e) {
                            return 0L;
                        }
                    })
                    .sum();
        } catch (IOException e) {
            logger.error("Failed to compute size of directory: {}", dir, e);
            return 0L;
        }
    }

    /**
     * 递归查找扩展名匹配的文件。
     *
     * @param dir       目录路径
     * @param extensions 扩展名列表（不含点，如 {@code "java"}、{@code "xml"}）；大小写不敏感
     * @return 匹配的文件路径列表
     */
    public static List<Path> findByExtension(Path dir, String... extensions) {
        List<Path> result = new ArrayList<>();
        if (dir == null || !Files.isDirectory(dir) || extensions == null || extensions.length == 0) {
            return result;
        }
        Set<String> lowerExts = new HashSet<>();
        for (String ext : extensions) {
            if (ext != null) {
                lowerExts.add(ext.toLowerCase());
            }
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.filter(Files::isRegularFile).forEach(p -> {
                String fileName = p.getFileName().toString().toLowerCase();
                int dotIdx = fileName.lastIndexOf('.');
                if (dotIdx >= 0 && dotIdx < fileName.length() - 1) {
                    String ext = fileName.substring(dotIdx + 1);
                    if (lowerExts.contains(ext)) {
                        result.add(p);
                    }
                }
            });
        } catch (IOException e) {
            logger.error("Failed to find files by extension in directory: {}", dir, e);
        }
        return result;
    }
}
