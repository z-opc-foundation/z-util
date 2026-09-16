package com.zifang.util.ai.inheritance.chapter5;

/**
 * Simple Professor abstraction.
 */

/**
 * Professor类。
 */
public class Professor {
    private final int professorId;
    private final String professorName;

    /**
     * Initalize new Professor
     *
     * @param professorId   The ID for this professor
     * @param professorName The name of this professor
     */
    /**
     * Professor方法。
     * * @param professorId int类型参数
     *
     * @param professorName String类型参数
     */
    public Professor(int professorId, String professorName) {
        this.professorId = professorId;
        this.professorName = professorName;
    }

    /**
     * Get professorId
     *
     * @return professorId
     */
    /**
     * getProfessorId方法。
     *
     * @return int类型返回值
     */
    public int getProfessorId() {
        return this.professorId;
    }

    /**
     * Get professor's name
     *
     * @return professorName
     */
    /**
     * getProfessorName方法。
     *
     * @return String类型返回值
     */
    public String getProfessorName() {
        return this.professorName;
    }
}
