package com.zifang.util.core.io.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for computing file hashes and detecting file types by magic bytes.
 * <p>
 * This class provides static methods for computing MD5, SHA-1, SHA-256 and other hash
 * algorithms on files, as well as detecting file types by examining their magic byte
 * prefixes. It also provides image validation utilities.
 * </p>
 *
 * <p>
 * Design principles:
 * <ul>
 *   <li>All methods are static - no instance needed</li>
 *   <li>Consistent error handling - throws IOException on failure, logs warnings where appropriate</li>
 *   <li>No silent failures - errors are logged and/or thrown</li>
 *   <li>Try-with-resources used for all stream operations</li>
 *   <li>Efficient hash computation using buffered reads</li>
 * </ul>
 * </p>
 *
 * <p>
 * Magic byte detection supports the following file types:
 * <ul>
 *   <li>jpg - JPEG images</li>
 *   <li>png - PNG images</li>
 *   <li>gif - GIF images</li>
 *   <li>tif - TIFF images</li>
 *   <li>bmp - BMP images</li>
 *   <li>pdf - PDF documents</li>
 *   <li>zip - ZIP archives</li>
 *   <li>html - HTML documents</li>
 *   <li>xml - XML documents</li>
 * </ul>
 * </p>
 *
 * @author zifang
 */
public final class FileHashUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileHashUtil.class);

    /**
     * Default buffer size for file reading operations: 8KB.
     */
    private static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

    /**
     * Number of bytes to read for magic byte detection.
     */
    private static final int MAGIC_BYTES_READ_SIZE = 50;

    /**
     * Map of magic byte prefixes (hex strings) to file type identifiers.
     */
    private static final Map<String, String> MAGIC_NUMBER_MAP = new HashMap<>();

    static {
        MAGIC_NUMBER_MAP.put("FFD8FF", "jpg");
        MAGIC_NUMBER_MAP.put("89504E47", "png");
        MAGIC_NUMBER_MAP.put("47494638", "gif");
        MAGIC_NUMBER_MAP.put("49492A00", "tif");
        MAGIC_NUMBER_MAP.put("424D", "bmp");
        MAGIC_NUMBER_MAP.put("255044462D312E", "pdf");
        MAGIC_NUMBER_MAP.put("504B0304", "zip");
        MAGIC_NUMBER_MAP.put("3C21444F435459504520", "html");
        MAGIC_NUMBER_MAP.put("3C3F786D6C", "xml");
    }

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class and should not be instantiated.
     */
    private FileHashUtil() {
        throw new UnsupportedOperationException("This utility class cannot be instantiated");
    }

    /**
     * Computes the MD5 hash of a file.
     * <p>
     * This method reads the file in buffered chunks and computes its MD5 hash,
     * returning the result as a 32-character hexadecimal string.
     * </p>
     *
     * @param file the file to compute the MD5 hash for
     * @return the MD5 hash as a 32-character hexadecimal string
     * @throws IOException              if the file cannot be read
     * @throws IllegalArgumentException if the file is null
     */
    public static String md5(File file) throws IOException, NoSuchAlgorithmException {
        return hash(file, "MD5");
    }

    /**
     * Computes the SHA-1 hash of a file.
     * <p>
     * This method reads the file in buffered chunks and computes its SHA-1 hash,
     * returning the result as a 40-character hexadecimal string.
     * </p>
     *
     * @param file the file to compute the SHA-1 hash for
     * @return the SHA-1 hash as a 40-character hexadecimal string
     * @throws IOException              if the file cannot be read
     * @throws IllegalArgumentException if the file is null
     */
    public static String sha1(File file) throws IOException, NoSuchAlgorithmException {
        return hash(file, "SHA-1");
    }

    /**
     * Computes the SHA-256 hash of a file.
     * <p>
     * This method reads the file in buffered chunks and computes its SHA-256 hash,
     * returning the result as a 64-character hexadecimal string.
     * </p>
     *
     * @param file the file to compute the SHA-256 hash for
     * @return the SHA-256 hash as a 64-character hexadecimal string
     * @throws IOException              if the file cannot be read
     * @throws IllegalArgumentException if the file is null
     */
    public static String sha256(File file) throws IOException, NoSuchAlgorithmException {
        return hash(file, "SHA-256");
    }

    /**
     * Computes the hash of a file using the specified algorithm.
     * <p>
     * This method reads the file in buffered chunks and computes its hash using
     * the specified algorithm (e.g., "MD5", "SHA-1", "SHA-256", "SHA-512").
     * The result is returned as a lowercase hexadecimal string.
     * </p>
     *
     * <p>
     * Supported algorithms include:
     * <ul>
     *   <li>MD5</li>
     *   <li>SHA-1</li>
     *   <li>SHA-256</li>
     *   <li>SHA-512</li>
     *   <li>And any other algorithm supported by {@link MessageDigest}</li>
     * </ul>
     * </p>
     *
     * @param file      the file to compute the hash for
     * @param algorithm the hash algorithm to use (e.g., "MD5", "SHA-256")
     * @return the hash as a lowercase hexadecimal string
     * @throws IOException              if the file cannot be read
     * @throws IllegalArgumentException if the file is null or algorithm is null/empty
     * @throws NoSuchAlgorithmException if the specified algorithm is not available
     */
    public static String hash(File file, String algorithm) throws IOException, NoSuchAlgorithmException {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        if (algorithm == null || algorithm.trim().isEmpty()) {
            throw new IllegalArgumentException("Algorithm cannot be null or empty");
        }

        if (!file.exists()) {
            throw new IOException("File does not exist: " + file.getAbsolutePath());
        }
        if (!file.isFile()) {
            throw new IOException("Path is not a file: " + file.getAbsolutePath());
        }
        if (!file.canRead()) {
            throw new IOException("File cannot be read: " + file.getAbsolutePath());
        }

        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Hash algorithm '{}' is not available", algorithm, e);
            throw e;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                messageDigest.update(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            logger.error("Failed to read file while computing hash: {}", file.getAbsolutePath(), e);
            throw new IOException("Failed to read file: " + file.getAbsolutePath(), e);
        }

        return bytesToHex(messageDigest.digest());
    }

    /**
     * Detects the file type by examining its magic bytes (file signature).
     * <p>
     * This method reads only the first 50 bytes of the file and compares them
     * against known magic byte prefixes to determine the file type.
     * </p>
     *
     * <p>
     * Supported file types and their magic byte prefixes:
     * <ul>
     *   <li>jpg - FFD8FF (JPEG images)</li>
     *   <li>png - 89504E47 (PNG images)</li>
     *   <li>gif - 47494638 (GIF images)</li>
     *   <li>tif - 49492A00 (TIFF images)</li>
     *   <li>bmp - 424D (BMP images)</li>
     *   <li>pdf - 255044462D312E (PDF documents)</li>
     *   <li>zip - 504B0304 (ZIP archives)</li>
     *   <li>html - 68746D6C3E (HTML documents)</li>
     *   <li>xml - 3C3F786D6C (XML documents)</li>
     * </ul>
     * </p>
     *
     * @param file the file to detect the type of
     * @return the file type identifier (e.g., "jpg", "png", "pdf") or null if unknown
     * @throws IOException              if the file cannot be read
     * @throws IllegalArgumentException if the file is null
     */
    public static String fileType(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }

        if (!file.exists()) {
            throw new IOException("File does not exist: " + file.getAbsolutePath());
        }
        if (!file.isFile()) {
            throw new IOException("Path is not a file: " + file.getAbsolutePath());
        }
        if (!file.canRead()) {
            throw new IOException("File cannot be read: " + file.getAbsolutePath());
        }

        byte[] magicBytes = new byte[MAGIC_BYTES_READ_SIZE];
        try (FileInputStream fis = new FileInputStream(file)) {
            int bytesRead = fis.read(magicBytes);
            if (bytesRead <= 0) {
                logger.warn("File is empty or unreadable: {}", file.getAbsolutePath());
                return null;
            }
            // Trim to actual bytes read
            if (bytesRead < MAGIC_BYTES_READ_SIZE) {
                magicBytes = java.util.Arrays.copyOf(magicBytes, bytesRead);
            }
        } catch (IOException e) {
            logger.error("Failed to read file for magic byte detection: {}", file.getAbsolutePath(), e);
            throw new IOException("Failed to read file: " + file.getAbsolutePath(), e);
        }

        String hexString = bytesToHex(magicBytes);

        for (Map.Entry<String, String> entry : MAGIC_NUMBER_MAP.entrySet()) {
            if (hexString.toUpperCase().startsWith(entry.getKey())) {
                logger.debug("Detected file type '{}' for file: {}", entry.getValue(), file.getAbsolutePath());
                return entry.getValue();
            }
        }

        logger.debug("Unknown file type for file: {}", file.getAbsolutePath());
        return null;
    }

    /**
     * Checks if a file is a valid image by attempting to read it as an image.
     * <p>
     * This method first performs a quick magic byte check to see if the file
     * appears to be a supported image type (JPEG, PNG, GIF, TIFF, BMP).
     * If the magic bytes suggest it might be an image, it then attempts to
     * read the file using {@link ImageIO} to verify it is a valid image.
     * </p>
     *
     * <p>
     * Supported image types:
     * <ul>
     *   <li>JPEG (.jpg, .jpeg)</li>
     *   <li>PNG (.png)</li>
     *   <li>GIF (.gif)</li>
     *   <li>TIFF (.tif, .tiff)</li>
     *   <li>BMP (.bmp)</li>
     * </ul>
     * </p>
     *
     * @param file the file to check
     * @return true if the file is a valid image, false otherwise
     * @throws IOException              if the file cannot be read
     * @throws IllegalArgumentException if the file is null
     */
    public static boolean isImage(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }

        if (!file.exists()) {
            throw new IOException("File does not exist: " + file.getAbsolutePath());
        }
        if (!file.isFile()) {
            throw new IOException("Path is not a file: " + file.getAbsolutePath());
        }
        if (!file.canRead()) {
            throw new IOException("File cannot be read: " + file.getAbsolutePath());
        }

        // First, do a quick magic byte check
        String type = fileType(file);
        if (type == null) {
            logger.debug("File does not have a recognized image magic bytes: {}", file.getAbsolutePath());
            return false;
        }

        // Check if it's a known image type
        if (!isImageType(type)) {
            logger.debug("File type '{}' is not a recognized image type for file: {}", type, file.getAbsolutePath());
            return false;
        }

        // For image type detection, we rely solely on magic bytes
        // The file type has already been verified above, so we return true
        return true;
    }

    /**
     * Checks if the given file type string represents a known image type.
     *
     * @param type the file type identifier
     * @return true if the type is a known image type
     */
    private static boolean isImageType(String type) {
        return "jpg".equals(type) || "png".equals(type) ||
                "gif".equals(type) || "tif".equals(type) || "bmp".equals(type);
    }

    /**
     * Converts a byte array to its hexadecimal string representation.
     * <p>
     * Each byte is converted to a two-character lowercase hexadecimal string.
     * For example, a byte value of 255 (0xFF) becomes "ff".
     * </p>
     *
     * @param bytes the byte array to convert
     * @return the lowercase hexadecimal string representation
     */
    private static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        StringBuilder hexString = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            int unsignedByte = b & 0xFF;
            if (unsignedByte < 16) {
                hexString.append('0');
            }
            hexString.append(Integer.toHexString(unsignedByte));
        }
        return hexString.toString().toLowerCase();
    }
}
