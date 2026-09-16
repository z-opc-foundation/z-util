package com.zifang.util.ai.inheritance.chapter5;

/**
 * A simple "group-of-students" abstraction. Defines the modules that the group is enrolled in.
 */

/**
 * Group类。
 */
public class Group {
    private final int groupId;
    private final int groupSize;
    private final int[] moduleIds;

    /**
     * Initialize Group
     *
     * @param groupId
     * @param groupSize
     * @param moduleIds
     */
    /**
     * Group方法。
     * * @param groupId int类型参数
     *
     * @param groupSize int类型参数
     * @param moduleIds int[]类型参数
     */
    public Group(int groupId, int groupSize, int[] moduleIds) {
        this.groupId = groupId;
        this.groupSize = groupSize;
        this.moduleIds = moduleIds;
    }

    /**
     * Get groupId
     *
     * @return groupId
     */
    /**
     * getGroupId方法。
     *
     * @return int类型返回值
     */
    public int getGroupId() {
        return this.groupId;
    }

    /**
     * Get groupSize
     *
     * @return groupSize
     */
    /**
     * getGroupSize方法。
     *
     * @return int类型返回值
     */
    public int getGroupSize() {
        return this.groupSize;
    }

    /**
     * Get array of group's moduleIds
     *
     * @return moduleIds
     */
    /**
     * getModuleIds方法。
     *
     * @return int[]类型返回值
     */
    public int[] getModuleIds() {
        return this.moduleIds;
    }
}
