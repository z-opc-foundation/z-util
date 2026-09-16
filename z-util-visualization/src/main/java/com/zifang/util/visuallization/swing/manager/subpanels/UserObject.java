package com.zifang.util.visuallization.swing.manager.subpanels;

import java.util.Objects;

/**
 * 用户对象数据类
 * 存储用户ID和显示名称信息
 */
public class UserObject {

    private Integer id;

    private String displayName;

    /**
     * 创建用户对象
     */
    public UserObject() {
    }

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置用户ID
     *
     * @param id 用户ID
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取显示名称
     *
     * @return 显示名称
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 设置显示名称
     *
     * @param displayName 显示名称
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 返回显示名称（用于树形节点显示）
     *
     * @return 显示名称
     */
    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return displayName;
    }

    /**
     * 判断当前用户对象与指定对象是否相等
     * 比较基于用户ID和显示名称
     *
     * @param o 要比较的对象
     * @return 如果ID和显示名称都相同返回true，否则返回false
     */
    @Override
    /**
     * equals方法。
     *      * @param o Object类型参数
     * @return boolean类型返回值
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserObject that = (UserObject) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(displayName, that.displayName);
    }

    /**
     * 返回当前用户对象的哈希码
     * 基于用户ID和显示名称计算
     *
     * @return 哈希码值
     */
    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return Objects.hash(id, displayName);
    }
}
