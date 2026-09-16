package com.zifang.util.db.sequence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * 节点号分配器（本地文件模式）
 * <p>
 * 用于为每台机器分配唯一节点号，不依赖任何中心服务。
 *
 * <p>支持两种模式：
 * <ul>
 *   <li>手动指定：配置文件或启动参数指定 nodeId</li>
 *   <li>自动分配：首次启动时在本地文件生成并持久化 nodeId</li>
 * </ul>
 *
 * <p>文件存储位置：~/.z-util/sequence/node-id.properties
 *
 * <p>集群部署时，确保每台机器的 nodeId 不同（手动模式更可靠）
 */
public class NodeIdAllocator {

    private static final Logger log = LoggerFactory.getLogger(NodeIdAllocator.class);

    private static final String NODE_ID_FILE = ".z-util/sequence/node-id.properties";
    private static final String KEY_NODE_ID = "nodeId";
    private static final String KEY_NODE_UUID = "nodeUuid";

    // 最大支持 1024 个节点（与 Snowflake 的 nodeId 位数匹配）
    private static final int MAX_NODE_ID = 1023;

    /**
     * 分配节点号
     *
     * @param specifiedNodeId 指定节点号（小于 0 表示自动分配）
     * @return 节点号（0 ~ 1023）
     */
    public static long allocate(long specifiedNodeId) {
        if (specifiedNodeId >= 0 && specifiedNodeId <= MAX_NODE_ID) {
            log.info("使用手动指定节点号: {}", specifiedNodeId);
            return specifiedNodeId;
        }
        return allocateAuto();
    }

    private static long allocateAuto() {
        Path filePath = getNodeIdFilePath();
        Properties props = loadOrCreateProps(filePath);

        String nodeIdStr = props.getProperty(KEY_NODE_ID);

        if (nodeIdStr != null) {
            long nodeId = Long.parseLong(nodeIdStr);
            String nodeUuid = props.getProperty(KEY_NODE_UUID, "unknown");
            log.info("从本地文件加载节点号: nodeId={}, nodeUuid={}", nodeId, nodeUuid);
            return nodeId;
        }

        // 首次启动，随机分配并持久化
        long nodeId = allocateRandomNodeId();
        props.setProperty(KEY_NODE_ID, String.valueOf(nodeId));
        props.setProperty(KEY_NODE_UUID, generateNodeUuid());

        try {
            Files.createDirectories(filePath.getParent());
            try (java.io.OutputStream os = Files.newOutputStream(filePath)) {
                props.store(os, "Snowflake Node ID Configuration");
            }
            log.info("节点号已分配并保存: nodeId={}", nodeId);
        } catch (IOException e) {
            log.warn("保存节点号文件失败，但节点号已分配: nodeId={}", nodeId, e);
        }

        return nodeId;
    }

    /**
     * 随机分配一个节点号（简单策略，适合单节点部署）
     * 生产环境建议手动指定 nodeId 避免多机冲突
     */
    private static long allocateRandomNodeId() {
        long nodeId = (long) (Math.random() * (MAX_NODE_ID + 1));
        log.warn("自动分配节点号: {}（生产环境建议手动指定唯一节点号）", nodeId);
        return nodeId;
    }

    private static Properties loadOrCreateProps(Path filePath) {
        if (Files.exists(filePath)) {
            Properties props = new Properties();
            try (java.io.InputStream is = Files.newInputStream(filePath)) {
                props.load(is);
                return props;
            } catch (IOException e) {
                log.warn("读取节点号文件失败，将重新创建: {}", filePath, e);
            }
        }
        return new Properties();
    }

    private static Path getNodeIdFilePath() {
        String home = System.getProperty("user.home");
        return Paths.get(home, NODE_ID_FILE).toAbsolutePath();
    }

    private static String generateNodeUuid() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 验证节点号是否合法
     */
    public static boolean isValid(long nodeId) {
        return nodeId >= 0 && nodeId <= MAX_NODE_ID;
    }
}
