package com.zifang.util.ai.inheritance.chapter5;

/**
 * Simple course module abstraction, which defines the Professors teaching the module.
 */

/**
 * Module类。
 */
public class Module {
    private final int moduleId;
    private final String moduleCode;
    private final String module;
    private final int[] professorIds;

    /**
     * Initialize new Module
     *
     * @param moduleId
     * @param moduleCode
     * @param module
     * @param professorIds
     */
    /**
     * Module方法。
     * * @param moduleId int类型参数
     *
     * @param moduleCode   String类型参数
     * @param module       String类型参数
     * @param professorIds int[]类型参数
     */
    public Module(int moduleId, String moduleCode, String module, int[] professorIds) {
        this.moduleId = moduleId;
        this.moduleCode = moduleCode;
        this.module = module;
        this.professorIds = professorIds;
    }

    /**
     * Get moduleId
     *
     * @return moduleId
     */
    /**
     * getModuleId方法。
     *
     * @return int类型返回值
     */
    public int getModuleId() {
        return this.moduleId;
    }

    /**
     * Get module code
     *
     * @return moduleCode
     */
    /**
     * getModuleCode方法。
     *
     * @return String类型返回值
     */
    public String getModuleCode() {
        return this.moduleCode;
    }

    /**
     * Get module name
     *
     * @return moduleName
     */
    /**
     * getModuleName方法。
     *
     * @return String类型返回值
     */
    public String getModuleName() {
        return this.module;
    }

    /**
     * Get random professor Id
     *
     * @return professorId
     */
    /**
     * getRandomProfessorId方法。
     *
     * @return int类型返回值
     */
    public int getRandomProfessorId() {
        int professorId = professorIds[(int) (professorIds.length * Math.random())];
        return professorId;
    }
}
