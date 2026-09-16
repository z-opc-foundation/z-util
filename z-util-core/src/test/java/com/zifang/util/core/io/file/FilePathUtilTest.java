package com.zifang.util.core.io.file;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * FilePathUtilTest类。
 */
public class FilePathUtilTest {

    // ==================== getName ====================

    @Test
    /**
     * testGetNameWithUnixPath方法。
     */
    public void testGetNameWithUnixPath() {
        assertEquals("file.txt", FilePathUtil.getName("/home/user/file.txt"));
    }

    @Test
    /**
     * testGetNameWithWindowsPath方法。
     */
    public void testGetNameWithWindowsPath() {
        assertEquals("file.txt", FilePathUtil.getName("C:\\Users\\user\\file.txt"));
    }

    @Test
    /**
     * testGetNameWithFileOnly方法。
     */
    public void testGetNameWithFileOnly() {
        assertEquals("file.txt", FilePathUtil.getName("file.txt"));
    }

    @Test
    /**
     * testGetNameWithTrailingSeparator方法。
     */
    public void testGetNameWithTrailingSeparator() {
        assertEquals("", FilePathUtil.getName("/home/user/"));
    }

    @Test
    /**
     * testGetNameWithPathOnly方法。
     */
    public void testGetNameWithPathOnly() {
        assertEquals("user", FilePathUtil.getName("/home/user"));
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testGetNameWithNull方法。
     */
    public void testGetNameWithNull() {
        FilePathUtil.getName(null);
    }

    // ==================== getBaseName ====================

    @Test
    /**
     * testGetBaseNameWithExtension方法。
     */
    public void testGetBaseNameWithExtension() {
        assertEquals("file", FilePathUtil.getBaseName("/home/user/file.txt"));
    }

    @Test
    /**
     * testGetBaseNameWithoutExtension方法。
     */
    public void testGetBaseNameWithoutExtension() {
        assertEquals("file", FilePathUtil.getBaseName("/home/user/file"));
    }

    @Test
    /**
     * testGetBaseNameWithHiddenFile方法。
     */
    public void testGetBaseNameWithHiddenFile() {
        assertEquals(".bashrc", FilePathUtil.getBaseName("/home/user/.bashrc"));
    }

    @Test
    /**
     * testGetBaseNameWithMultipleDots方法。
     */
    public void testGetBaseNameWithMultipleDots() {
        assertEquals("archive.tar", FilePathUtil.getBaseName("/home/user/archive.tar.gz"));
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testGetBaseNameWithNull方法。
     */
    public void testGetBaseNameWithNull() {
        FilePathUtil.getBaseName(null);
    }

    // ==================== getExtension ====================

    @Test
    /**
     * testGetExtensionWithNormalFile方法。
     */
    public void testGetExtensionWithNormalFile() {
        assertEquals("txt", FilePathUtil.getExtension("/home/user/file.txt"));
    }

    @Test
    /**
     * testGetExtensionWithNoExtension方法。
     */
    public void testGetExtensionWithNoExtension() {
        assertEquals("", FilePathUtil.getExtension("/home/user/file"));
    }

    @Test
    /**
     * testGetExtensionWithHiddenFile方法。
     */
    public void testGetExtensionWithHiddenFile() {
        assertEquals("bashrc", FilePathUtil.getExtension("/home/user/.bashrc"));
    }

    @Test
    /**
     * testGetExtensionWithMultipleDots方法。
     */
    public void testGetExtensionWithMultipleDots() {
        assertEquals("gz", FilePathUtil.getExtension("/home/user/archive.tar.gz"));
    }

    @Test
    /**
     * testGetExtensionWithPathOnly方法。
     */
    public void testGetExtensionWithPathOnly() {
        assertEquals("", FilePathUtil.getExtension("/home/user/"));
    }

    @Test(expected = IllegalArgumentException.class)
    /**
     * testGetExtensionWithNull方法。
     */
    public void testGetExtensionWithNull() {
        FilePathUtil.getExtension(null);
    }

    // ==================== getParent ====================

    @Test
    /**
     * testGetParentWithUnixPath方法。
     */
    public void testGetParentWithUnixPath() {
        assertEquals("/home/user", FilePathUtil.getParent("/home/user/file.txt"));
    }

    @Test
    /**
     * testGetParentWithWindowsPath方法。
     */
    public void testGetParentWithWindowsPath() {
        assertEquals("C:\\Users\\user", FilePathUtil.getParent("C:\\Users\\user\\file.txt"));
    }

    @Test
    /**
     * testGetParentWithNoParent方法。
     */
    public void testGetParentWithNoParent() {
        assertNull(FilePathUtil.getParent("file.txt"));
    }

    @Test
    /**
     * testGetParentWithNull方法。
     */
    public void testGetParentWithNull() {
        assertNull(FilePathUtil.getParent(null));
    }

    // ==================== normalize ====================

    @Test
    /**
     * testNormalizeWithDots方法。
     */
    public void testNormalizeWithDots() {
        String result = FilePathUtil.normalize("/home/user/../user/file.txt");
        assertEquals("/home/user/file.txt", result);
    }

    @Test
    /**
     * testNormalizeWithCurrentDir方法。
     */
    public void testNormalizeWithCurrentDir() {
        String result = FilePathUtil.normalize("/home/user/./file.txt");
        assertEquals("/home/user/file.txt", result);
    }

    @Test
    /**
     * testNormalizeWithDoubleSlashes方法。
     */
    public void testNormalizeWithDoubleSlashes() {
        String result = FilePathUtil.normalize("/home//user//file.txt");
        assertEquals("/home/user/file.txt", result);
    }

    @Test
    /**
     * testNormalizeWithWindowsBackslashes方法。
     */
    public void testNormalizeWithWindowsBackslashes() {
        String result = FilePathUtil.normalize("C:\\Users\\..\\Users\\file.txt");
        assertEquals("C:\\Users\\file.txt", result);
    }

    @Test
    /**
     * testNormalizeWithNull方法。
     */
    public void testNormalizeWithNull() {
        assertNull(FilePathUtil.normalize(null));
    }

    // ==================== toUNIXPath ====================

    @Test
    /**
     * testToUNIXPathWithWindowsPath方法。
     */
    public void testToUNIXPathWithWindowsPath() {
        String result = FilePathUtil.toUNIXPath("C:\\Users\\user\\file.txt");
        assertEquals("C:/Users/user/file.txt", result);
    }

    @Test
    /**
     * testToUNIXPathWithUnixPath方法。
     */
    public void testToUNIXPathWithUnixPath() {
        String result = FilePathUtil.toUNIXPath("/home/user/file.txt");
        assertEquals("/home/user/file.txt", result);
    }

    @Test
    /**
     * testToUNIXPathWithNull方法。
     */
    public void testToUNIXPathWithNull() {
        assertNull(FilePathUtil.toUNIXPath(null));
    }

    // ==================== getPrefix ====================

    @Test
    /**
     * testGetPrefixWithUnixPath方法。
     */
    public void testGetPrefixWithUnixPath() {
        assertEquals("/", FilePathUtil.getPrefix("/home/user/file.txt"));
    }

    @Test
    /**
     * testGetPrefixWithWindowsDriveLetter方法。
     */
    public void testGetPrefixWithWindowsDriveLetter() {
        String prefix = FilePathUtil.getPrefix("C:\\Users\\user\\file.txt");
        assertEquals("C:\\", prefix);
    }

    @Test
    /**
     * testGetPrefixWithUNCPath方法。
     */
    public void testGetPrefixWithUNCPath() {
        String prefix = FilePathUtil.getPrefix("\\\\server\\share\\file.txt");
        assertEquals("\\\\server\\share\\", prefix);
    }

    @Test
    /**
     * testGetPrefixWithRelativePath方法。
     */
    public void testGetPrefixWithRelativePath() {
        assertEquals("", FilePathUtil.getPrefix("user/file.txt"));
    }

    @Test
    /**
     * testGetPrefixWithNull方法。
     */
    public void testGetPrefixWithNull() {
        assertNull(FilePathUtil.getPrefix(null));
    }

    // ==================== getSuffix ====================

    @Test
    /**
     * testGetSuffixWithMultipleDots方法。
     */
    public void testGetSuffixWithMultipleDots() {
        assertEquals("tar.gz", FilePathUtil.getSuffix("/home/user/archive.tar.gz"));
    }

    @Test
    /**
     * testGetSuffixWithNoSuffix方法。
     */
    public void testGetSuffixWithNoSuffix() {
        assertEquals("", FilePathUtil.getSuffix("/home/user/file"));
    }

    @Test
    /**
     * testGetSuffixWithSingleDot方法。
     */
    public void testGetSuffixWithSingleDot() {
        assertEquals("", FilePathUtil.getSuffix("/home/user/file."));
    }

    @Test
    /**
     * testGetSuffixWithNull方法。
     */
    public void testGetSuffixWithNull() {
        assertNull(FilePathUtil.getSuffix(null));
    }

    // ==================== trimExtension ====================

    @Test
    /**
     * testTrimExtension方法。
     */
    public void testTrimExtension() {
        assertEquals("/home/user/file", FilePathUtil.trimExtension("/home/user/file.txt"));
    }

    @Test
    /**
     * testTrimExtensionWithNoExtension方法。
     */
    public void testTrimExtensionWithNoExtension() {
        assertEquals("/home/user/file", FilePathUtil.trimExtension("/home/user/file"));
    }

    @Test
    /**
     * testTrimExtensionWithNull方法。
     */
    public void testTrimExtensionWithNull() {
        assertNull(FilePathUtil.trimExtension(null));
    }

    // ==================== getSubpath ====================

    @Test
    /**
     * testGetSubpath方法。
     */
    public void testGetSubpath() {
        String result = FilePathUtil.getSubpath("/home/user/file.txt", "/home");
        assertEquals("user/file.txt", result);
    }

    @Test
    /**
     * testGetSubpathWithNoCommonPrefix方法。
     */
    public void testGetSubpathWithNoCommonPrefix() {
        String result = FilePathUtil.getSubpath("/home/user/file.txt", "/var");
        assertEquals("/home/user/file.txt", result);
    }

    @Test
    /**
     * testGetSubpathWithNull方法。
     */
    public void testGetSubpathWithNull() {
        assertNull(FilePathUtil.getSubpath(null, "/home"));
    }

    @Test
    /**
     * testGetSubpathWithNullBaseDir方法。
     */
    public void testGetSubpathWithNullBaseDir() {
        assertNull(FilePathUtil.getSubpath("/home/user/file.txt", null));
    }

    // ==================== isExist / isDirectory / isFile ====================

    @Test
    /**
     * testIsExistWithExistingFile方法。
     */
    public void testIsExistWithExistingFile() throws Exception {
        java.io.File tmp = java.io.File.createTempFile("pathtest", ".txt");
        try {
            assertTrue(FilePathUtil.isExist(tmp.getPath()));
        } finally {
            tmp.delete();
        }
    }

    @Test
    /**
     * testIsExistWithNonExistingPath方法。
     */
    public void testIsExistWithNonExistingPath() {
        assertFalse(FilePathUtil.isExist("/non/existing/path/12345.txt"));
    }

    @Test
    /**
     * testIsExistWithNull方法。
     */
    public void testIsExistWithNull() {
        assertFalse(FilePathUtil.isExist(null));
    }

    @Test
    /**
     * testIsDirectoryWithDir方法。
     */
    public void testIsDirectoryWithDir() throws Exception {
        java.io.File tmpDir = java.io.File.createTempFile("dirtest", "");
        tmpDir.delete();
        tmpDir.mkdirs();
        try {
            assertTrue(FilePathUtil.isDirectory(tmpDir.getPath()));
            assertFalse(FilePathUtil.isFile(tmpDir.getPath()));
        } finally {
            tmpDir.delete();
        }
    }

    @Test
    /**
     * testIsFileWithFile方法。
     */
    public void testIsFileWithFile() throws Exception {
        java.io.File tmp = java.io.File.createTempFile("filetest", ".txt");
        try {
            assertTrue(FilePathUtil.isFile(tmp.getPath()));
            assertFalse(FilePathUtil.isDirectory(tmp.getPath()));
        } finally {
            tmp.delete();
        }
    }

    @Test
    /**
     * testIsDirectoryWithNull方法。
     */
    public void testIsDirectoryWithNull() {
        assertFalse(FilePathUtil.isDirectory(null));
    }

    @Test
    /**
     * testIsFileWithNull方法。
     */
    public void testIsFileWithNull() {
        assertFalse(FilePathUtil.isFile(null));
    }
}
