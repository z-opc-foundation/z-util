package com.zifang.util.devops.docker.dto;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * ImageDTO 类测试
 */

/**
 * ImageDTOTest类。
 */
public class ImageDTOTest {

    @Test
    /**
     * testDefaultConstructor方法。
     */
    public void testDefaultConstructor() {
        ImageDTO dto = new ImageDTO();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getRepository());
        assertNull(dto.getTag());
        assertNull(dto.getCreated());
        assertNull(dto.getSize());
        assertEquals(0L, dto.getSizeBytes());
        assertNull(dto.getLabels());
        assertNull(dto.getRepoTags());
        assertNull(dto.getPorts());
    }

    @Test
    /**
     * testIdSetterAndGetter方法。
     */
    public void testIdSetterAndGetter() {
        ImageDTO dto = new ImageDTO();
        dto.setId("sha256:abc123");
        assertEquals("sha256:abc123", dto.getId());
    }

    @Test
    /**
     * testRepositorySetterAndGetter方法。
     */
    public void testRepositorySetterAndGetter() {
        ImageDTO dto = new ImageDTO();
        dto.setRepository("nginx");
        assertEquals("nginx", dto.getRepository());
    }

    @Test
    /**
     * testTagSetterAndGetter方法。
     */
    public void testTagSetterAndGetter() {
        ImageDTO dto = new ImageDTO();
        dto.setTag("latest");
        assertEquals("latest", dto.getTag());

        dto.setTag("1.19.0");
        assertEquals("1.19.0", dto.getTag());
    }

    @Test
    /**
     * testCreatedSetterAndGetter方法。
     */
    public void testCreatedSetterAndGetter() {
        ImageDTO dto = new ImageDTO();
        dto.setCreated("2023-01-15T10:30:00Z");
        assertEquals("2023-01-15T10:30:00Z", dto.getCreated());
    }

    @Test
    /**
     * testSizeSetterAndGetter方法。
     */
    public void testSizeSetterAndGetter() {
        ImageDTO dto = new ImageDTO();
        dto.setSize("142MB");
        assertEquals("142MB", dto.getSize());
    }

    @Test
    /**
     * testSizeBytesSetterAndGetter方法。
     */
    public void testSizeBytesSetterAndGetter() {
        ImageDTO dto = new ImageDTO();
        dto.setSizeBytes(148897280L);
        assertEquals(148897280L, dto.getSizeBytes());
    }

    @Test
    /**
     * testLabelsSetterAndGetter方法。
     */
    public void testLabelsSetterAndGetter() {
        ImageDTO dto = new ImageDTO();
        Map<String, String> labels = new HashMap<>();
        labels.put("maintainer", "test@example.com");
        labels.put("version", "1.0");

        dto.setLabels(labels);

        assertNotNull(dto.getLabels());
        assertEquals(2, dto.getLabels().size());
        assertEquals("test@example.com", dto.getLabels().get("maintainer"));
        assertEquals("1.0", dto.getLabels().get("version"));
    }

    @Test
    /**
     * testRepoTagsSetterAndGetter方法。
     */
    public void testRepoTagsSetterAndGetter() {
        ImageDTO dto = new ImageDTO();
        List<String> repoTags = Arrays.asList("nginx:latest", "nginx:1.19.0", "nginx:alpine");

        dto.setRepoTags(repoTags);

        assertNotNull(dto.getRepoTags());
        assertEquals(3, dto.getRepoTags().size());
        assertTrue(dto.getRepoTags().contains("nginx:latest"));
    }

    @Test
    /**
     * testPortsSetterAndGetter方法。
     */
    public void testPortsSetterAndGetter() {
        ImageDTO dto = new ImageDTO();
        List<String> ports = Arrays.asList("80/tcp", "443/tcp");

        dto.setPorts(ports);

        assertNotNull(dto.getPorts());
        assertEquals(2, dto.getPorts().size());
        assertEquals("80/tcp", dto.getPorts().get(0));
    }

    @Test
    /**
     * testToString方法。
     */
    public void testToString() {
        ImageDTO dto = new ImageDTO();
        dto.setId("sha256:abc123");
        dto.setRepository("nginx");
        dto.setTag("latest");

        String str = dto.toString();

        assertNotNull(str);
        assertTrue(str.contains("ImageDTO"));
        assertTrue(str.contains("nginx"));
        assertTrue(str.contains("latest"));
    }

    @Test
    /**
     * testCompleteImageDTO方法。
     */
    public void testCompleteImageDTO() {
        ImageDTO dto = new ImageDTO();
        dto.setId("sha256:def456");
        dto.setRepository("postgres");
        dto.setTag("13");
        dto.setCreated("2023-06-01T15:00:00Z");
        dto.setSize("374MB");
        dto.setSizeBytes(392113456L);

        Map<String, String> labels = new HashMap<>();
        labels.put("org.opencontainers.image.vendor", "PostgreSQL");
        dto.setLabels(labels);

        List<String> repoTags = Arrays.asList("postgres:13", "postgres:latest");
        dto.setRepoTags(repoTags);

        List<String> ports = Arrays.asList("5432/tcp");
        dto.setPorts(ports);

        // Verify
        assertEquals("sha256:def456", dto.getId());
        assertEquals("postgres", dto.getRepository());
        assertEquals("13", dto.getTag());
        assertEquals("2023-06-01T15:00:00Z", dto.getCreated());
        assertEquals("374MB", dto.getSize());
        assertEquals(392113456L, dto.getSizeBytes());
        assertEquals("PostgreSQL", dto.getLabels().get("org.opencontainers.image.vendor"));
        assertEquals(2, dto.getRepoTags().size());
        assertEquals(1, dto.getPorts().size());
    }
}
