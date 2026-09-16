package com.zifang.util.workflow.json;

import com.zifang.util.json.JsonUtil;
import com.zifang.util.workflow.config.WorkflowConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 工作流配置JSON序列化器。
 * <p>
 * 使用JsonUtil实现WorkflowConfiguration与JSON格式之间的相互转换。
 * 支持将配置序列化为JSON字符串或文件，以及从JSON字符串或文件反序列化。
 *
 * @see WorkflowConfiguration
 * @see JsonUtil
 */
public class WorkflowConfigurationSerializer {

    /**
     * Serialize WorkflowConfiguration to JSON string
     *
     * @param config the workflow configuration to serialize
     * @return JSON string representation
     */
    public String toJson(WorkflowConfiguration config) {
        return JsonUtil.toJson(config);
    }

    /**
     * Deserialize WorkflowConfiguration from JSON string
     *
     * @param json JSON string
     * @return WorkflowConfiguration instance
     */
    public WorkflowConfiguration fromJson(String json) {
        return JsonUtil.fromJson(json, WorkflowConfiguration.class);
    }

    /**
     * Write WorkflowConfiguration to file
     *
     * @param config the workflow configuration to write
     * @param file   the target file
     */
    public void toJsonFile(WorkflowConfiguration config, File file) {
        try {
            Path path = Paths.get(file.toURI());
            Files.createDirectories(path.getParent());
            Files.write(path, JsonUtil.toJson(config).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write WorkflowConfiguration to file: " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Read WorkflowConfiguration from file
     *
     * @param file the source file
     * @return WorkflowConfiguration instance
     */
    public WorkflowConfiguration fromJsonFile(File file) {
        try {
            return JsonUtil.fromJson(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8), WorkflowConfiguration.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read WorkflowConfiguration from file: " + file.getAbsolutePath(), e);
        }
    }
}
