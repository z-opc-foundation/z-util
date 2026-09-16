package com.zifang.util.devops.nexus;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Asset 类测试
 */

/**
 * AssetTest类。
 */
public class AssetTest {

    @Test
    /**
     * testDefaultConstructor方法。
     */
    public void testDefaultConstructor() {
        Asset asset = new Asset();
        assertNotNull(asset);
        assertNull(asset.getId());
        assertNull(asset.getRepository());
        assertNull(asset.getPath());
        assertNull(asset.getDownloadUrl());
        assertNull(asset.getChecksum());
        assertNull(asset.getFormat());
    }

    @Test
    /**
     * testIdSetterAndGetter方法。
     */
    public void testIdSetterAndGetter() {
        Asset asset = new Asset();
        asset.setId("asset-123");
        assertEquals("asset-123", asset.getId());
    }

    @Test
    /**
     * testRepositorySetterAndGetter方法。
     */
    public void testRepositorySetterAndGetter() {
        Asset asset = new Asset();
        asset.setRepository("maven-releases");
        assertEquals("maven-releases", asset.getRepository());
    }

    @Test
    /**
     * testPathSetterAndGetter方法。
     */
    public void testPathSetterAndGetter() {
        Asset asset = new Asset();
        asset.setPath("com/example/artifact-1.0.jar");
        assertEquals("com/example/artifact-1.0.jar", asset.getPath());
    }

    @Test
    /**
     * testDownloadUrlSetterAndGetter方法。
     */
    public void testDownloadUrlSetterAndGetter() {
        Asset asset = new Asset();
        asset.setDownloadUrl("https://nexus.example.com/repository/maven-releases/com/example/artifact-1.0.jar");
        assertEquals("https://nexus.example.com/repository/maven-releases/com/example/artifact-1.0.jar", asset.getDownloadUrl());
    }

    @Test
    /**
     * testChecksumSetterAndGetter方法。
     */
    public void testChecksumSetterAndGetter() {
        Asset asset = new Asset();

        Checksum checksum = new Checksum();
        checksum.setSha1("abc123sha1");
        checksum.setMd5("abc123md5");

        asset.setChecksum(checksum);

        assertNotNull(asset.getChecksum());
        assertEquals("abc123sha1", asset.getChecksum().getSha1());
        assertEquals("abc123md5", asset.getChecksum().getMd5());
    }

    @Test
    /**
     * testFormatSetterAndGetter方法。
     */
    public void testFormatSetterAndGetter() {
        Asset asset = new Asset();
        asset.setFormat("jar");
        assertEquals("jar", asset.getFormat());

        asset.setFormat("war");
        assertEquals("war", asset.getFormat());
    }

    @Test
    /**
     * testEquals方法。
     */
    public void testEquals() {
        Asset asset1 = new Asset();
        asset1.setId("id1");
        asset1.setRepository("repo");
        asset1.setPath("path1");
        asset1.setDownloadUrl("url1");
        asset1.setFormat("jar");

        Asset asset2 = new Asset();
        asset2.setId("id1");
        asset2.setRepository("repo");
        asset2.setPath("path1");
        asset2.setDownloadUrl("url1");
        asset2.setFormat("jar");

        assertEquals(asset1, asset2);
    }

    @Test
    /**
     * testEqualsWithDifferentId方法。
     */
    public void testEqualsWithDifferentId() {
        Asset asset1 = new Asset();
        asset1.setId("id1");

        Asset asset2 = new Asset();
        asset2.setId("id2");

        assertNotEquals(asset1, asset2);
    }

    @Test
    /**
     * testEqualsWithSameObject方法。
     */
    public void testEqualsWithSameObject() {
        Asset asset = new Asset();
        assertEquals(asset, asset);
    }

    @Test
    /**
     * testEqualsWithNull方法。
     */
    public void testEqualsWithNull() {
        Asset asset = new Asset();
        assertNotEquals(asset, null);
    }

    @Test
    /**
     * testEqualsWithDifferentClass方法。
     */
    public void testEqualsWithDifferentClass() {
        Asset asset = new Asset();
        assertNotEquals(asset, "not an asset");
    }

    @Test
    /**
     * testHashCode方法。
     */
    public void testHashCode() {
        Asset asset1 = new Asset();
        asset1.setId("id1");
        asset1.setPath("path1");

        Asset asset2 = new Asset();
        asset2.setId("id1");
        asset2.setPath("path1");

        assertEquals(asset1.hashCode(), asset2.hashCode());
    }

    @Test
    /**
     * testHashCodeConsistency方法。
     */
    public void testHashCodeConsistency() {
        Asset asset = new Asset();
        asset.setId("id1");

        int hashCode1 = asset.hashCode();
        int hashCode2 = asset.hashCode();

        assertEquals(hashCode1, hashCode2);
    }

    @Test
    /**
     * testToString方法。
     */
    public void testToString() {
        Asset asset = new Asset();
        asset.setId("test-id");
        asset.setPath("test/path");

        String str = asset.toString();

        assertNotNull(str);
        assertTrue(str.contains("Asset"));
        assertTrue(str.contains("test-id"));
    }

    @Test
    /**
     * testCompleteAsset方法。
     */
    public void testCompleteAsset() {
        Asset asset = new Asset();
        asset.setId("asset-full");
        asset.setRepository("maven-releases");
        asset.setPath("com/example/full-asset-1.0.jar");
        asset.setDownloadUrl("https://nexus.example.com/repository/maven-releases/com/example/full-asset-1.0.jar");
        asset.setFormat("jar");

        Checksum checksum = new Checksum();
        checksum.setSha1("sha1hash");
        checksum.setMd5("md5hash");
        asset.setChecksum(checksum);

        // Verify
        assertEquals("asset-full", asset.getId());
        assertEquals("maven-releases", asset.getRepository());
        assertEquals("com/example/full-asset-1.0.jar", asset.getPath());
        assertEquals("https://nexus.example.com/repository/maven-releases/com/example/full-asset-1.0.jar", asset.getDownloadUrl());
        assertEquals("jar", asset.getFormat());
        assertNotNull(asset.getChecksum());
        assertEquals("sha1hash", asset.getChecksum().getSha1());
        assertEquals("md5hash", asset.getChecksum().getMd5());
    }
}
