package com.zifang.util.zex.bust.chapter4.enumCase;

/**
 * Java编程知识整理类。
 * <p>
 * 此类整理了Java编程中的各类知识点。
 * 包含基础语法、并发、IO、设计模式等内容。
 *
 * @author zifang
 * @version 1.0
 */
public enum HungryLevelEnum2 {
    HUNGRY_LEVEL_1("一成饱"),
    HUNGRY_LEVEL_2("五成饱"),
    HUNGRY_LEVEL_3("十成饱");

    private String description;

    HungryLevelEnum2(String description) {
        this.description = description;
    }

    /**
     * getDescription方法。
     *
     * @return String类型返回值
     */
    public String getDescription() {
        return description;
    }

    /**
     * setDescription方法。
     * * @param description String类型参数
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
