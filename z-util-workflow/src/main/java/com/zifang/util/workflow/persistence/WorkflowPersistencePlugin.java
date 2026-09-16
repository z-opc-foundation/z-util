package com.zifang.util.workflow.persistence;

import java.util.List;

/**
 * Pluggable persistence strategy for workflow snapshots.
 * Implementations can store snapshots in files, databases, or other backends.
 */
public interface WorkflowPersistencePlugin {

    /**
     * Save a workflow snapshot to the persistent store.
     * If a snapshot with the same processId already exists, it should be overwritten.
     */
    void save(WorkflowSnapshot snapshot);

    /**
     * Load a workflow snapshot by processId.
     *
     * @param processId the process identifier
     * @return the loaded snapshot, or null if not found
     */
    WorkflowSnapshot load(String processId);

    /**
     * Delete a workflow snapshot by processId.
     */
    void delete(String processId);

    /**
     * List all available process IDs in the persistent store.
     *
     * @return list of process IDs
     */
    List<String> listProcessIds();
}
