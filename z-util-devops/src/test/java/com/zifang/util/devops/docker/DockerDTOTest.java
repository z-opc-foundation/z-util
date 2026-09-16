package com.zifang.util.devops.docker;

import com.zifang.util.devops.docker.dto.ContainerDTO;
import com.zifang.util.devops.docker.dto.ImageDTO;
import com.zifang.util.devops.docker.dto.NetworkDTO;
import com.zifang.util.devops.docker.dto.VolumeDTO;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * DockerDTOTest类。
 */
public class DockerDTOTest {

    @Test
    /**
     * testContainerDTO方法。
     */
    public void testContainerDTO() {
        ContainerDTO dto = new ContainerDTO();
        dto.setId("abc123");
        dto.setName("nginx");
        dto.setImage("nginx:latest");
        dto.setState("running");
        dto.setStatus("Up 5 minutes");
        dto.setCommand("/bin/bash");

        assertEquals("abc123", dto.getId());
        assertEquals("nginx", dto.getName());
        assertEquals("nginx:latest", dto.getImage());
        assertEquals("running", dto.getState());
        assertEquals("Up 5 minutes", dto.getStatus());
        assertEquals("/bin/bash", dto.getCommand());

        String str = dto.toString();
        assertTrue(str.contains("nginx"));
        assertTrue(str.contains("running"));
    }

    @Test
    /**
     * testContainerDTOPortMapping方法。
     */
    public void testContainerDTOPortMapping() {
        ContainerDTO.PortMapping pm = new ContainerDTO.PortMapping();
        pm.setHostIp("0.0.0.0");
        pm.setHostPort(8080);
        pm.setContainerPort(80);
        pm.setProtocol("tcp");

        assertEquals("0.0.0.0", pm.getHostIp());
        assertEquals(8080, pm.getHostPort());
        assertEquals(80, pm.getContainerPort());
        assertEquals("tcp", pm.getProtocol());
        assertEquals("8080:80/tcp", pm.toString());
    }

    @Test
    /**
     * testImageDTO方法。
     */
    public void testImageDTO() {
        ImageDTO dto = new ImageDTO();
        dto.setId("sha256:abc123");
        dto.setRepository("nginx");
        dto.setTag("latest");
        dto.setSize("150MB");
        dto.setSizeBytes(150 * 1024 * 1024);
        dto.setRepoTags(Arrays.asList("nginx:latest", "nginx:1.19"));

        assertEquals("sha256:abc123", dto.getId());
        assertEquals("nginx", dto.getRepository());
        assertEquals("latest", dto.getTag());
        assertEquals("150MB", dto.getSize());
        assertEquals(150 * 1024 * 1024, dto.getSizeBytes());
        assertEquals(2, dto.getRepoTags().size());

        String str = dto.toString();
        assertTrue(str.contains("nginx"));
    }

    @Test
    /**
     * testNetworkDTO方法。
     */
    public void testNetworkDTO() {
        NetworkDTO dto = new NetworkDTO();
        dto.setId("net123");
        dto.setName("bridge");
        dto.setDriver("bridge");
        dto.setScope("local");
        dto.setSubnet("172.17.0.0/16");
        dto.setGateway("172.17.0.1");
        dto.setContainerCount(3);

        assertEquals("net123", dto.getId());
        assertEquals("bridge", dto.getName());
        assertEquals("bridge", dto.getDriver());
        assertEquals("local", dto.getScope());
        assertEquals("172.17.0.0/16", dto.getSubnet());
        assertEquals("172.17.0.1", dto.getGateway());
        assertEquals(3, dto.getContainerCount());

        String str = dto.toString();
        assertTrue(str.contains("bridge"));
    }

    @Test
    /**
     * testVolumeDTO方法。
     */
    public void testVolumeDTO() {
        VolumeDTO dto = new VolumeDTO();
        dto.setName("data_volume");
        dto.setDriver("local");
        dto.setMountpoint("/var/lib/docker/volumes/data_volume/_data");
        dto.setScope("local");
        dto.setCreated("2024-01-01T00:00:00Z");

        assertEquals("data_volume", dto.getName());
        assertEquals("local", dto.getDriver());
        assertTrue(dto.getMountpoint().contains("data_volume"));
        assertEquals("local", dto.getScope());
        assertEquals("2024-01-01T00:00:00Z", dto.getCreated());

        String str = dto.toString();
        assertTrue(str.contains("data_volume"));
    }
}
