package com.zifang.util.core.io.archive;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.*;

/**
 * ZipUtilTest类。
 */
public class ZipUtilTest {

    private File tempBaseDir;

    @Before
    /**
     * setUp方法。
     */
    public void setUp() throws Exception {
        tempBaseDir = Files.createTempDirectory("zip-test-base").toFile();
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

    // ==================== zipFile (folder -> zip) ====================

    @Test
    /**
     * testZipFileFolder方法。
     */
    public void testZipFileFolder() throws Exception {
        // Create a folder with files
        File folder = new File(tempBaseDir, "myfolder");
        folder.mkdirs();
        File file1 = new File(folder, "file1.txt");
        File file2 = new File(folder, "file2.txt");
        Files.write(file1.toPath(), "content1".getBytes(StandardCharsets.UTF_8));
        Files.write(file2.toPath(), "content2".getBytes(StandardCharsets.UTF_8));

        File targetDir = new File(tempBaseDir, "target");
        targetDir.mkdirs();

        ZipUtil.zipFile(folder.getPath(), targetDir.getPath());

        // Verify zip file created
        File zipFile = new File(targetDir, "myfolder.zip");
        assertTrue(zipFile.exists());
        assertTrue(zipFile.length() > 0);

        // Verify zip content
        try (ZipFile zf = new ZipFile(zipFile)) {
            assertEquals(2, zf.size());
        }
    }

    // ==================== zipFolder ====================

    @Test
    /**
     * testZipFolder方法。
     */
    public void testZipFolder() throws Exception {
        File folder = new File(tempBaseDir, "tozip");
        folder.mkdirs();
        File file = new File(folder, "data.txt");
        Files.write(file.toPath(), "zip me".getBytes(StandardCharsets.UTF_8));

        File targetDir = new File(tempBaseDir, "zipped");
        targetDir.mkdirs();

        ZipUtil.zipFolder(folder.getPath(), targetDir.getPath(), "result");

        File zipFile = new File(targetDir, "result.zip");
        assertTrue(zipFile.exists());

        // Unzip and verify
        File unzipDir = new File(tempBaseDir, "unzipped");
        unzipDir.mkdirs();
        ZipUtil.unZip(zipFile.getPath(), unzipDir.getPath());

        File unzippedFile = new File(unzipDir, "tozip/data.txt");
        assertTrue(unzippedFile.exists());
        assertEquals("zip me", new String(Files.readAllBytes(unzippedFile.toPath()), StandardCharsets.UTF_8));
    }

    @Test
    /**
     * testZipFolderWithExplicitExtension方法。
     */
    public void testZipFolderWithExplicitExtension() throws Exception {
        File folder = new File(tempBaseDir, "tozip2");
        folder.mkdirs();
        new File(folder, "dummy.txt").createNewFile();

        File targetDir = new File(tempBaseDir, "zipped2");
        targetDir.mkdirs();

        ZipUtil.zipFolder(folder.getPath(), targetDir.getPath(), "result.zip");
        assertTrue(new File(targetDir, "result.zip").exists());
    }

    @Test(expected = IOException.class)
    /**
     * testZipFolderNonExistent方法。
     */
    public void testZipFolderNonExistent() throws Exception {
        ZipUtil.zipFolder("/non/existent", tempBaseDir.getPath(), "result");
    }

    @Test(expected = IOException.class)
    /**
     * testZipFolderIsFile方法。
     */
    public void testZipFolderIsFile() throws Exception {
        File tmpFile = File.createTempFile("notafolder", ".txt", tempBaseDir);
        ZipUtil.zipFolder(tmpFile.getPath(), tempBaseDir.getPath(), "result");
    }

    // ==================== zipFile (single file -> zip) ====================

    @Test
    /**
     * testZipSingleFile方法。
     */
    public void testZipSingleFile() throws Exception {
        File srcFile = new File(tempBaseDir, "single.txt");
        Files.write(srcFile.toPath(), "single file content".getBytes(StandardCharsets.UTF_8));

        File zipFile = new File(tempBaseDir, "single.zip");

        ZipUtil.zipFile(srcFile.getPath(), zipFile.getPath(), "renamed.txt");

        assertTrue(zipFile.exists());
        try (ZipFile zf = new ZipFile(zipFile)) {
            ZipEntry entry = zf.getEntry("renamed.txt");
            assertNotNull(entry);
        }
    }

    // ==================== unZip ====================

    @Test
    /**
     * testUnZip方法。
     */
    public void testUnZip() throws Exception {
        // Create a zip manually
        File zipFile = new File(tempBaseDir, "manual.zip");
        try (ZipOutputStream zos = new ZipOutputStream(new java.io.FileOutputStream(zipFile))) {
            ZipEntry entry = new ZipEntry("test.txt");
            zos.putNextEntry(entry);
            zos.write("hello zip".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            ZipEntry entry2 = new ZipEntry("sub/");
            zos.putNextEntry(entry2);
            zos.closeEntry();
        }

        File unzipDir = new File(tempBaseDir, "unzip-result");
        unzipDir.mkdirs();

        ZipUtil.unZip(zipFile.getPath(), unzipDir.getPath());

        File extracted = new File(unzipDir, "test.txt");
        assertTrue(extracted.exists());
        assertEquals("hello zip", new String(Files.readAllBytes(extracted.toPath()), StandardCharsets.UTF_8));
    }

    @Test
    /**
     * testUnZipCreatesDestDir方法。
     */
    public void testUnZipCreatesDestDir() throws Exception {
        File zipFile = new File(tempBaseDir, "small.zip");
        try (ZipOutputStream zos = new ZipOutputStream(new java.io.FileOutputStream(zipFile))) {
            ZipEntry entry = new ZipEntry("a.txt");
            zos.putNextEntry(entry);
            zos.write("a".getBytes());
            zos.closeEntry();
        }

        File newDest = new File(tempBaseDir, "brandnew/unzip/dir");
        ZipUtil.unZip(zipFile.getPath(), newDest.getPath());

        assertTrue(newDest.exists());
        assertTrue(new File(newDest, "a.txt").exists());
    }

    @Test(expected = IOException.class)
    /**
     * testUnZipNonExistentZip方法。
     */
    public void testUnZipNonExistentZip() throws Exception {
        ZipUtil.unZip("/non/existent.zip", tempBaseDir.getPath());
    }

    // ==================== zipBytes / unzipFirstEntry / unzipAllEntries ====================

    @Test
    /**
     * testZipBytesRoundTrip方法。
     */
    public void testZipBytesRoundTrip() {
        byte[] data = "hello in-memory zip".getBytes(StandardCharsets.UTF_8);
        byte[] zipped = ZipUtil.zipBytes("entry.txt", data);
        assertTrue(zipped.length > 0);

        byte[] restored = ZipUtil.unzipFirstEntry(zipped);
        assertArrayEquals(data, restored);
    }

    @Test
    /**
     * testZipBytesEmptyData方法。
     */
    public void testZipBytesEmptyData() {
        byte[] zipped = ZipUtil.zipBytes("empty.txt", new byte[0]);
        byte[] restored = ZipUtil.unzipFirstEntry(zipped);
        assertNotNull(restored);
        assertEquals(0, restored.length);
    }

    @Test
    /**
     * testZipBytesAndUnzipAllEntriesNullArguments方法。
     */
    public void testZipBytesAndUnzipAllEntriesNullArguments() {
        try {
            ZipUtil.zipBytes(null, new byte[1]);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
        try {
            ZipUtil.zipBytes("a.txt", null);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
        try {
            ZipUtil.unzipAllEntries(null);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }
    }

    @Test
    /**
     * testUnzipAllEntriesMultiple方法。
     */
    public void testUnzipAllEntriesMultiple() throws Exception {
        ByteArrayOutputStream zipBuffer = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(zipBuffer)) {
            zos.putNextEntry(new ZipEntry("first.txt"));
            zos.write("one".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("dir/"));
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("second.txt"));
            zos.write("two".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        Map<String, byte[]> entries = ZipUtil.unzipAllEntries(zipBuffer.toByteArray());
        assertEquals(2, entries.size());
        assertEquals("one", new String(entries.get("first.txt"), StandardCharsets.UTF_8));
        assertEquals("two", new String(entries.get("second.txt"), StandardCharsets.UTF_8));
        assertFalse(entries.containsKey("dir/"));
    }

    @Test
    /**
     * testUnzipFirstEntryOfEmptyZip方法。
     */
    public void testUnzipFirstEntryOfEmptyZip() throws Exception {
        ByteArrayOutputStream zipBuffer = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(zipBuffer)) {
            // empty zip
        }
        byte[] result = ZipUtil.unzipFirstEntry(zipBuffer.toByteArray());
        assertNull(result);
    }
}
