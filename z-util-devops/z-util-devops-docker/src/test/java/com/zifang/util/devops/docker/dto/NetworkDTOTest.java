package com.zifang.util.devops.docker.dto;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * NetworkDTO 类测试
 */

/**
 * NetworkDTOTest类。
 */
public class NetworkDTOTest {

    @Test
    /**
     * testDefaultConstructor方法。
     */
    public void testDefaultConstructor() {
        NetworkDTO dto = new NetworkDTO();
        assertNotNull(dto);
        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getDriver());
        assertNull(dto.getScope());
        assertFalse(dto.isInternal());
        assertFalse(dto.isAttachable());
        assertNull(dto.getSubnet());
        assertNull(dto.getGateway());
        assertNull(dto.getLabels());
        assertEquals(0, dto.getContainerCount());
    }

    @Test
    /**
     * testIdSetterAndGetter方法。
     */
    public void testIdSetterAndGetter() {
        NetworkDTO dto = new NetworkDTO();
        dto.setId("abc123def456");
        assertEquals("abc123def456", dto.getId());
    }

    @Test
    /**
     * testNameSetterAndGetter方法。
     */
    public void testNameSetterAndGetter() {
        NetworkDTO dto = new NetworkDTO();
        dto.setName("my-network");
        assertEquals("my-network", dto.getName());
    }

    @Test
    /**
     * testDriverSetterAndGetter方法。
     */
    public void testDriverSetterAndGetter() {
        NetworkDTO dto = new NetworkDTO();
        dto.setDriver("bridge");
        assertEquals("bridge", dto.getDriver());

        dto.setDriver("host");
        assertEquals("host", dto.getDriver());
    }

    @Test
    /**
     * testScopeSetterAndGetter方法。
     */
    public void testScopeSetterAndGetter() {
        NetworkDTO dto = new NetworkDTO();
        dto.setScope("local");
        assertEquals("local", dto.getScope());

        dto.setScope("swarm");
        assertEquals("swarm", dto.getScope());
    }

    @Test
    /**
     * testInternalSetterAndGetter方法。
     */
    public void testInternalSetterAndGetter() {
        NetworkDTO dto = new NetworkDTO();
        assertFalse(dto.isInternal());

        dto.setInternal(true);
        assertTrue(dto.isInternal());

        dto.setInternal(false);
        assertFalse(dto.isInternal());
    }

    @Test
    /**
     * testAttachableSetterAndGetter方法。
     */
    public void testAttachableSetterAndGetter() {
        NetworkDTO dto = new NetworkDTO();
        assertFalse(dto.isAttachable());

        dto.setAttachable(true);
        assertTrue(dto.isAttachable());

        dto.setAttachable(false);
        assertFalse(dto.isAttachable());
    }

    @Test
    /**
     * testSubnetSetterAndGetter方法。
     */
    public void testSubnetSetterAndGetter() {
        NetworkDTO dto = new NetworkDTO();
        dto.setSubnet("172.17.0.0/16");
        assertEquals("172.17.0.0/16", dto.getSubnet());
    }

    @Test
    /**
     * testGatewaySetterAndGetter方法。
     */
    public void testGatewaySetterAndGetter() {
        NetworkDTO dto = new NetworkDTO();
        dto.setGateway("172.17.0.1");
        assertEquals("172.17.0.1", dto.getGateway());
    }

    @Test
    /**
     * testLabelsSetterAndGetter方法。
     */
    public void testLabelsSetterAndGetter() {
        NetworkDTO dto = new NetworkDTO();
        Map<String, String> labels = new HashMap<>();
        labels.put("env", "production");
        labels.put("owner", "team-a");

        dto.setLabels(labels);

        assertNotNull(dto.getLabels());
        assertEquals(2, dto.getLabels().size());
        assertEquals("production", dto.getLabels().get("env"));
        assertEquals("team-a", dto.getLabels().get("owner"));
    }

    @Test
    /**
     * testContainerCountSetterAndGetter方法。
     */
    public void testContainerCountSetterAndGetter() {
        NetworkDTO dto = new NetworkDTO();
        assertEquals(0, dto.getContainerCount());

        dto.setContainerCount(5);
        assertEquals(5, dto.getContainerCount());

        dto.setContainerCount(100);
        assertEquals(100, dto.getContainerCount());
    }

    @Test
    /**
     * testToString方法。
     */
    public void testToString() {
        NetworkDTO dto = new NetworkDTO();
        dto.setId("net123");
        dto.setName("test-net");
        dto.setDriver("bridge");
        dto.setScope("local");

        String str = dto.toString();

        assertNotNull(str);
        assertTrue(str.contains("NetworkDTO"));
        assertTrue(str.contains("test-net"));
        assertTrue(str.contains("bridge"));
    }

    @Test
    /**
     * testCompleteNetworkDTO方法。
     */
    public void testCompleteNetworkDTO() {
        NetworkDTO dto = new NetworkDTO();
        dto.setId("network-uuid-123");
        dto.setName("backend-network");
        dto.setDriver("bridge");
        dto.setScope("swarm");
        dto.setInternal(true);
        dto.setAttachable(false);
        dto.setSubnet("10.0.0.0/8");
        dto.setGateway("10.0.0.1");
        dto.setContainerCount(3);

        Map<String, String> labels = new HashMap<>();
        labels.put("app", "backend");
        labels.put("version", "2.0");
        dto.setLabels(labels);

        // Verify
        assertEquals("network-uuid-123", dto.getId());
        assertEquals("backend-network", dto.getName());
        assertEquals("bridge", dto.getDriver());
        assertEquals("swarm", dto.getScope());
        assertTrue(dto.isInternal());
        assertFalse(dto.isAttachable());
        assertEquals("10.0.0.0/8", dto.getSubnet());
        assertEquals("10.0.0.1", dto.getGateway());
        assertEquals(3, dto.getContainerCount());
        assertEquals(2, dto.getLabels().size());
    }

    @Test
    /**
     * testDockerNetworkTypes方法。
     */
    public void testDockerNetworkTypes() {
        // Bridge network
        NetworkDTO bridge = new NetworkDTO();
        bridge.setName("bridge");
        bridge.setDriver("bridge");
        bridge.setScope("local");
        assertEquals("bridge", bridge.getName());
        assertEquals("bridge", bridge.getDriver());

        // Host network
        NetworkDTO host = new NetworkDTO();
        host.setName("host");
        host.setDriver("host");
        host.setScope("local");
        assertEquals("host", host.getName());
        assertEquals("host", host.getDriver());

        // Overlay network
        NetworkDTO overlay = new NetworkDTO();
        overlay.setName("overlay-net");
        overlay.setDriver("overlay");
        overlay.setScope("swarm");
        assertEquals("overlay-net", overlay.getName());
        assertEquals("overlay", overlay.getDriver());
        assertEquals("swarm", overlay.getScope());

        // None network
        NetworkDTO none = new NetworkDTO();
        none.setName("none");
        none.setDriver("null");
        assertEquals("none", none.getName());
        assertEquals("null", none.getDriver());
    }
}
