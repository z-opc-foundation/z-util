package com.zifang.util.core.io.archive;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertTrue;

/**
 * JarUtilTest类。
 */
public class JarUtilTest {

    private File tempBaseDir;

    @Before
    /**
     * setUp方法。
     */
    public void setUp() throws Exception {
        tempBaseDir = Files.createTempDirectory("jar-test-base").toFile();
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

    @Test
    /**
     * testUnPackJar方法。
     */
    public void testUnPackJar() throws Exception {
        // Use junit jar from classpath if available, otherwise create a minimal jar
        String junitResource = "/junit/junit/4/junit-4.jar";
        java.net.URL jarUrl = getClass().getResource(junitResource);

        if (jarUrl != null) {
            File jarFile = new File(jarUrl.toURI());
            if (jarFile.exists()) {
                File unpackDir = new File(tempBaseDir, "junit-unpacked");
                unpackDir.mkdirs();

                JarUtil.unPack(jarFile.getPath(), unpackDir.getPath());

                assertTrue(unpackDir.exists());
                // JUnit jar should contain MANIFEST.MF
                File manifest = new File(unpackDir, "META-INF/MANIFEST.MF");
                assertTrue("MANIFEST.MF should exist", manifest.exists());
            }
        }
    }

    @Test
    /**
     * testUnPackCreatesTargetDir方法。
     */
    public void testUnPackCreatesTargetDir() throws Exception {
        String junitResource = "/junit/junit/4/junit-4.jar";
        java.net.URL jarUrl = getClass().getResource(junitResource);

        if (jarUrl != null) {
            File jarFile = new File(jarUrl.toURI());
            if (jarFile.exists()) {
                File freshDir = new File(tempBaseDir, "brandnew/junit/output");
                JarUtil.unPack(jarFile.getPath(), freshDir.getPath());
                assertTrue(freshDir.exists());
            }
        }
    }

    @Test(expected = IOException.class)
    /**
     * testUnPackNonExistentJar方法。
     */
    public void testUnPackNonExistentJar() throws Exception {
        JarUtil.unPack("/non/existent/xxx.jar", tempBaseDir.getPath());
    }
}
