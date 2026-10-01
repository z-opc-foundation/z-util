package com.zifang.util.devops.nexus;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * NexusModelTest类。
 */
public class NexusModelTest {

    @Test
    /**
     * testChecksum方法。
     */
    public void testChecksum() {
        Checksum cs = new Checksum();
        cs.setSha1("abc123def456");
        cs.setMd5("md5hash123");

        assertEquals("abc123def456", cs.getSha1());
        assertEquals("md5hash123", cs.getMd5());

        String str = cs.toString();
        assertTrue(str.contains("sha1"));
        assertTrue(str.contains("md5"));
    }

    @Test
    /**
     * testChecksumEquals方法。
     */
    public void testChecksumEquals() {
        Checksum cs1 = new Checksum();
        cs1.setSha1("abc");
        cs1.setMd5("md5");

        Checksum cs2 = new Checksum();
        cs2.setSha1("abc");
        cs2.setMd5("md5");

        Checksum cs3 = new Checksum();
        cs3.setSha1("xyz");
        cs3.setMd5("md5");

        assertEquals(cs1, cs2);
        assertNotEquals(cs1, cs3);
        assertEquals(cs1.hashCode(), cs2.hashCode());
    }

    @Test
    /**
     * testAsset方法。
     */
    public void testAsset() {
        Asset asset = new Asset();
        asset.setId("asset-id-1");
        asset.setRepository("maven-releases");
        asset.setPath("com/example/artifact/1.0.0/artifact-1.0.0.jar");
        asset.setDownloadUrl("http://nexus.example.com/repository/maven-releases/com/example/artifact/1.0.0/artifact-1.0.0.jar");
        asset.setFormat("jar");

        Checksum cs = new Checksum();
        cs.setSha1("sha1val");
        cs.setMd5("md5val");
        asset.setChecksum(cs);

        assertEquals("asset-id-1", asset.getId());
        assertEquals("maven-releases", asset.getRepository());
        assertEquals("jar", asset.getFormat());
        assertNotNull(asset.getChecksum());
        assertEquals("sha1val", asset.getChecksum().getSha1());
    }

    @Test
    /**
     * testAssetEquals方法。
     */
    public void testAssetEquals() {
        Asset a1 = new Asset();
        a1.setId("id1");
        a1.setRepository("repo");
        a1.setPath("path");
        a1.setDownloadUrl("url");
        a1.setFormat("jar");

        Asset a2 = new Asset();
        a2.setId("id1");
        a2.setRepository("repo");
        a2.setPath("path");
        a2.setDownloadUrl("url");
        a2.setFormat("jar");

        Asset a3 = new Asset();
        a3.setId("id3");

        assertEquals(a1, a2);
        assertNotEquals(a1, a3);
    }

    @Test
    /**
     * testComponent方法。
     */
    public void testComponent() {
        Component comp = new Component();
        comp.setId("comp-id");
        comp.setRepository("maven-releases");
        comp.setGroup("com.example");
        comp.setName("artifact");
        comp.setVersion("1.0.0");
        comp.setFormat("jar");

        assertEquals("comp-id", comp.getId());
        assertEquals("maven-releases", comp.getRepository());
        assertEquals("com.example", comp.getGroup());
        assertEquals("artifact", comp.getName());
        assertEquals("1.0.0", comp.getVersion());
        assertEquals("jar", comp.getFormat());
    }

    @Test
    /**
     * testComponentCompareTo方法。
     */
    public void testComponentCompareTo() {
        Component c1 = new Component();
        c1.setVersion("1.0.0");

        Component c2 = new Component();
        c2.setVersion("2.0.0");

        Component c3 = new Component();
        c3.setVersion("1.0.0");

        assertTrue(c1.compareTo(c2) < 0);
        assertTrue(c2.compareTo(c1) > 0);
        assertEquals(0, c1.compareTo(c3));
    }

    @Test
    /**
     * testComponentEquals方法。
     */
    public void testComponentEquals() {
        Component c1 = new Component();
        c1.setId("id1");
        c1.setGroup("group");
        c1.setName("name");
        c1.setVersion("1.0.0");

        Component c2 = new Component();
        c2.setId("id1");
        c2.setGroup("group");
        c2.setName("name");
        c2.setVersion("1.0.0");

        Component c3 = new Component();
        c3.setId("id3");

        assertEquals(c1, c2);
        assertNotEquals(c1, c3);
    }
}
