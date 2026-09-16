package com.zifang.util.core.io.file;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.*;

/**
 * FileCopyUtilTest类。
 */
public class FileCopyUtilTest {

    private File tempBaseDir;

    @Before
    /**
     * setUp方法。
     */
    public void setUp() throws Exception {
        tempBaseDir = Files.createTempDirectory("copy-test-base").toFile();
        tempBaseDir.deleteOnExit();
    }

    @After
    /**
     * tearDown方法。
     */
    public void tearDown() throws Exception {
        if (tempBaseDir != null && tempBaseDir.exists()) {
            deleteRecursively(tempBaseDir);
        }
    }

    private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }

    // ==================== copyFile ====================

    @Test
    /**
     * testCopyFileBasic方法。
     */
    public void testCopyFileBasic() throws Exception {
        File src = new File(tempBaseDir, "src.txt");
        Files.write(src.toPath(), "hello world".getBytes(StandardCharsets.UTF_8));

        File dest = new File(tempBaseDir, "dest.txt");
        FileCopyUtil.copyFile(src, dest);

        assertTrue(dest.exists());
        assertEquals("hello world", new String(Files.readAllBytes(dest.toPath()), StandardCharsets.UTF_8));
    }

    @Test
    /**
     * testCopyFileOverwritesExisting方法。
     */
    public void testCopyFileOverwritesExisting() throws Exception {
        File src = new File(tempBaseDir, "src2.txt");
        Files.write(src.toPath(), "new content".getBytes(StandardCharsets.UTF_8));

        File dest = new File(tempBaseDir, "dest2.txt");
        Files.write(dest.toPath(), "old content".getBytes(StandardCharsets.UTF_8));

        FileCopyUtil.copyFile(src, dest);
        assertEquals("new content", new String(Files.readAllBytes(dest.toPath()), StandardCharsets.UTF_8));
    }

    @Test(expected = NullPointerException.class)
    /**
     * testCopyFileWithNullSrc方法。
     */
    public void testCopyFileWithNullSrc() throws Exception {
        File dest = new File(tempBaseDir, "dest-null.txt");
        FileCopyUtil.copyFile(null, dest);
    }

    @Test(expected = NullPointerException.class)
    /**
     * testCopyFileWithNullDest方法。
     */
    public void testCopyFileWithNullDest() throws Exception {
        File src = new File(tempBaseDir, "src-null.txt");
        src.createNewFile();
        FileCopyUtil.copyFile(src, null);
    }

    @Test
    /**
     * testCopyFileWithSubdirCreation方法。
     */
    public void testCopyFileWithSubdirCreation() throws Exception {
        File src = new File(tempBaseDir, "src-subdir.txt");
        Files.write(src.toPath(), "content".getBytes(StandardCharsets.UTF_8));

        File dest = new File(new File(tempBaseDir, "sub"), "dest.txt");
        FileCopyUtil.copyFile(src, dest);

        assertTrue(dest.exists());
        assertEquals("content", new String(Files.readAllBytes(dest.toPath()), StandardCharsets.UTF_8));
    }

    // ==================== copyFile with overwrite flag ====================

    @Test
    /**
     * testCopyFileNoOverwrite方法。
     */
    public void testCopyFileNoOverwrite() throws Exception {
        File src = new File(tempBaseDir, "src3.txt");
        Files.write(src.toPath(), "source".getBytes(StandardCharsets.UTF_8));

        File dest = new File(tempBaseDir, "dest3.txt");
        Files.write(dest.toPath(), "dest".getBytes(StandardCharsets.UTF_8));

        FileCopyUtil.copyFile(src, dest, false);
        // dest should be unchanged
        assertEquals("dest", new String(Files.readAllBytes(dest.toPath()), StandardCharsets.UTF_8));
    }

    @Test(expected = java.nio.file.FileAlreadyExistsException.class)
    @org.junit.Ignore("与 testCopyFileNoOverwrite 行为矛盾，实际应静默跳过")
    /**
     * testCopyFileNoOverwriteThrows方法。
     */
    public void testCopyFileNoOverwriteThrows() throws Exception {
        File src = new File(tempBaseDir, "src4.txt");
        Files.write(src.toPath(), "source".getBytes(StandardCharsets.UTF_8));

        File dest = new File(tempBaseDir, "dest4.txt");
        Files.write(dest.toPath(), "dest".getBytes(StandardCharsets.UTF_8));

        FileCopyUtil.copyFile(src, dest, false);
    }

    // ==================== copyFileToDir ====================

    @Test
    /**
     * testCopyFileToDir方法。
     */
    public void testCopyFileToDir() throws Exception {
        File src = new File(tempBaseDir, "src5.txt");
        Files.write(src.toPath(), "hello".getBytes(StandardCharsets.UTF_8));

        File destDir = new File(tempBaseDir, "destdir5");
        destDir.mkdirs();

        FileCopyUtil.copyFileToDir(src, destDir);

        File dest = new File(destDir, "src5.txt");
        assertTrue(dest.exists());
        assertEquals("hello", new String(Files.readAllBytes(dest.toPath()), StandardCharsets.UTF_8));
    }

    // ==================== copyDir ====================

    @Test
    /**
     * testCopyDirBasic方法。
     */
    public void testCopyDirBasic() throws Exception {
        File srcDir = new File(tempBaseDir, "srcdir");
        srcDir.mkdirs();
        File srcFile1 = new File(srcDir, "file1.txt");
        File srcFile2 = new File(srcDir, "file2.txt");
        Files.write(srcFile1.toPath(), "content1".getBytes(StandardCharsets.UTF_8));
        Files.write(srcFile2.toPath(), "content2".getBytes(StandardCharsets.UTF_8));

        File destDir = new File(tempBaseDir, "destdir");
        FileCopyUtil.copyDir(srcDir, destDir.getAbsolutePath());

        assertTrue(destDir.exists());
        assertTrue(new File(destDir, "file1.txt").exists());
        assertTrue(new File(destDir, "file2.txt").exists());
        assertEquals("content1", new String(Files.readAllBytes(new File(destDir, "file1.txt").toPath()), StandardCharsets.UTF_8));
    }

    @Test
    /**
     * testCopyDirWithSubdirs方法。
     */
    public void testCopyDirWithSubdirs() throws Exception {
        File srcDir = new File(tempBaseDir, "srcdir2");
        srcDir.mkdirs();
        File subDir = new File(srcDir, "subdir");
        subDir.mkdirs();
        File srcFile = new File(subDir, "nested.txt");
        Files.write(srcFile.toPath(), "nested content".getBytes(StandardCharsets.UTF_8));

        File destDir = new File(tempBaseDir, "destdir2");
        FileCopyUtil.copyDir(srcDir, destDir.getAbsolutePath());

        assertTrue(new File(destDir, "subdir/nested.txt").exists());
        assertEquals("nested content", new String(Files.readAllBytes(new File(destDir, "subdir/nested.txt").toPath()), StandardCharsets.UTF_8));
    }

    @Test(expected = NullPointerException.class)
    /**
     * testCopyDirWithNullSrc方法。
     */
    public void testCopyDirWithNullSrc() throws Exception {
        File destDir = new File(tempBaseDir, "dest-null-dir");
        FileCopyUtil.copyDir((File) null, (String) null);
    }

    @Test(expected = NullPointerException.class)
    /**
     * testCopyDirWithNullDest方法。
     */
    public void testCopyDirWithNullDest() throws Exception {
        File srcDir = new File(tempBaseDir, "src-null-dir");
        srcDir.mkdirs();
        FileCopyUtil.copyDir(srcDir, null);
    }

    // ==================== moveFile ====================

    @Test
    /**
     * testMoveFile方法。
     */
    public void testMoveFile() throws Exception {
        File src = new File(tempBaseDir, "move-src.txt");
        Files.write(src.toPath(), "to be moved".getBytes(StandardCharsets.UTF_8));

        File dest = new File(tempBaseDir, "move-dest.txt");
        FileCopyUtil.moveFile(src, dest);

        assertFalse(src.exists());
        assertTrue(dest.exists());
        assertEquals("to be moved", new String(Files.readAllBytes(dest.toPath()), StandardCharsets.UTF_8));
    }

    @Test(expected = IOException.class)
    /**
     * testMoveFileSrcNotExists方法。
     */
    public void testMoveFileSrcNotExists() throws Exception {
        File src = new File(tempBaseDir, "non-existent-move.txt");
        File dest = new File(tempBaseDir, "move-dest2.txt");
        FileCopyUtil.moveFile(src, dest);
    }

    // ==================== moveDir ====================

    @Test
    /**
     * testMoveDir方法。
     */
    public void testMoveDir() throws Exception {
        File srcDir = new File(tempBaseDir, "move-src-dir");
        srcDir.mkdirs();
        File srcFile = new File(srcDir, "file.txt");
        Files.write(srcFile.toPath(), "dir content".getBytes(StandardCharsets.UTF_8));

        File destDir = new File(tempBaseDir, "move-dest-dir");
        FileCopyUtil.moveDir(srcDir, destDir);

        assertFalse(srcDir.exists());
        assertTrue(destDir.exists());
        assertTrue(new File(destDir, "file.txt").exists());
    }

    @Test(expected = IOException.class)
    /**
     * testMoveDirSrcNotExists方法。
     */
    public void testMoveDirSrcNotExists() throws Exception {
        File srcDir = new File(tempBaseDir, "non-existent-move-dir");
        File destDir = new File(tempBaseDir, "move-dest-dir2");
        FileCopyUtil.moveDir(srcDir, destDir);
    }

    // ==================== copyStream ====================

    @Test
    /**
     * testCopyStreamBasic方法。
     */
    public void testCopyStreamBasic() throws Exception {
        byte[] data = "stream content".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(data);
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();

        FileCopyUtil.copyStream(in, out);

        assertArrayEquals(data, out.toByteArray());
    }

    @Test
    /**
     * testCopyStreamEmpty方法。
     */
    public void testCopyStreamEmpty() throws Exception {
        InputStream in = new ByteArrayInputStream(new byte[0]);
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();

        FileCopyUtil.copyStream(in, out);

        assertArrayEquals(new byte[0], out.toByteArray());
    }

    @Test
    /**
     * testCopyStreamLarge方法。
     */
    public void testCopyStreamLarge() throws Exception {
        byte[] large = new byte[100_000];
        for (int i = 0; i < large.length; i++) {
            large[i] = (byte) (i % 256);
        }
        InputStream in = new ByteArrayInputStream(large);
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();

        FileCopyUtil.copyStream(in, out);

        assertArrayEquals(large, out.toByteArray());
    }

    @Test(expected = NullPointerException.class)
    /**
     * testCopyStreamWithNullInput方法。
     */
    public void testCopyStreamWithNullInput() throws Exception {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        FileCopyUtil.copyStream(null, out);
    }

    @Test(expected = NullPointerException.class)
    /**
     * testCopyStreamWithNullOutput方法。
     */
    public void testCopyStreamWithNullOutput() throws Exception {
        InputStream in = new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8));
        FileCopyUtil.copyStream(in, null);
    }
}
