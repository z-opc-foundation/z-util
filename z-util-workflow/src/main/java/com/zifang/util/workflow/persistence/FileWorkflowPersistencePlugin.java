package com.zifang.util.workflow.persistence;


import com.zifang.util.json.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * File-based implementation of WorkflowPersistencePlugin.
 * Each process snapshot is saved as a JSON file in the configured directory.
 * Default directory is ~/.z-workflow/.
 */
public class FileWorkflowPersistencePlugin implements WorkflowPersistencePlugin {

    private static final String DEFAULT_DIR = System.getProperty("user.home") + File.separator + ".z-workflow";

    private final File storageDir;

    /**
     * Constructor using default directory (~/.z-workflow/).
     */
    public FileWorkflowPersistencePlugin() {
        this(DEFAULT_DIR);
    }

    /**
     * Constructor with custom directory path.
     *
     * @param directoryPath the directory path to store snapshot files
     */
    public FileWorkflowPersistencePlugin(String directoryPath) {
        this.storageDir = new File(directoryPath);
        ensureDirectoryExists();
    }

    private void ensureDirectoryExists() {
        if (!storageDir.exists()) {
            boolean created = storageDir.mkdirs();
            if (!created) {
                throw new RuntimeException("Failed to create persistence directory: " + storageDir.getAbsolutePath());
            }
        }
    }

    private File getSnapshotFile(String processId) {
        return new File(storageDir, processId + ".json");
    }

    @Override
    public void save(WorkflowSnapshot snapshot) {
        try {
            File file = getSnapshotFile(snapshot.getProcessId());
            snapshot.setLastUpdatedTime(System.currentTimeMillis());
            Files.write(file.toPath(), JsonUtil.toJson(snapshot).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save workflow snapshot for process: " + snapshot.getProcessId(), e);
        }
    }

    @Override
    public WorkflowSnapshot load(String processId) {
        File file = getSnapshotFile(processId);
        if (!file.exists()) {
            return null;
        }
        try {
            return JsonUtil.fromJson(new String(java.nio.file.Files.readAllBytes(file.toPath())), WorkflowSnapshot.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load workflow snapshot for process: " + processId, e);
        }
    }

    @Override
    public void delete(String processId) {
        File file = getSnapshotFile(processId);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                throw new RuntimeException("Failed to delete workflow snapshot for process: " + processId);
            }
        }
    }

    @Override
    public List<String> listProcessIds() {
        List<String> processIds = new ArrayList<>();
        File[] files = storageDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                String processId = name.substring(0, name.length() - ".json".length());
                processIds.add(processId);
            }
        }
        return processIds;
    }
}
