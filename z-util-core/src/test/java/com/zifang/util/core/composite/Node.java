package com.zifang.util.core.composite;

/**
 * Node类。
 */
public class Node {
    private int id;
    private int parentId;
    private String name;

    /**
     * Node方法。
     * * @param id int类型参数
     *
     * @param parentId int类型参数
     * @param name     String类型参数
     */
    public Node(int id, int parentId, String name) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
    }

    /**
     * getId方法。
     *
     * @return int类型返回值
     */
    public int getId() {
        return id;
    }

    /**
     * setId方法。
     * * @param id int类型参数
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * getParentId方法。
     *
     * @return int类型返回值
     */
    public int getParentId() {
        return parentId;
    }

    /**
     * setParentId方法。
     * * @param parentId int类型参数
     */
    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    /**
     * getName方法。
     *
     * @return String类型返回值
     */
    public String getName() {
        return name;
    }

    /**
     * setName方法。
     * * @param name String类型参数
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return name;
    }

    @Override
    /**
     * equals方法。
     *      * @param o Object类型参数
     * @return boolean类型返回值
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return id == node.id && parentId == node.parentId &&
                java.util.Objects.equals(name, node.name);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return java.util.Objects.hash(id, parentId, name);
    }
}