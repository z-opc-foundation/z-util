package com.zifang.util.db.test0;

import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.Objects;

@Table(name = "resource_item")
/**
 * ResourceItem类。
 */
public class ResourceItem {
    private Long id;
    private String cmsId;
    private String type;
    private int ownerId;
    private Boolean shared;
    private Timestamp createTime;
    private Timestamp updateTime;

    /**
     * getId方法。
     *
     * @return long类型返回值
     */
    public Long getId() {
        return id;
    }

    /**
     * setId方法。
     * * @param id long类型参数
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * getCmsId方法。
     *
     * @return String类型返回值
     */
    public String getCmsId() {
        return cmsId;
    }

    /**
     * setCmsId方法。
     * * @param cmsId String类型参数
     */
    public void setCmsId(String cmsId) {
        this.cmsId = cmsId;
    }

    /**
     * getType方法。
     *
     * @return String类型返回值
     */
    public String getType() {
        return type;
    }

    /**
     * setType方法。
     * * @param type String类型参数
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * getOwnerId方法。
     *
     * @return int类型返回值
     */
    public int getOwnerId() {
        return ownerId;
    }

    /**
     * setOwnerId方法。
     * * @param ownerId int类型参数
     */
    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * getShared方法。
     *
     * @return boolean类型返回值
     */
    public Boolean getShared() {
        return shared;
    }

    /**
     * setShared方法。
     * * @param shared boolean类型参数
     */
    public void setShared(Boolean shared) {
        this.shared = shared;
    }

    /**
     * getCreateTime方法。
     *
     * @return Timestamp类型返回值
     */
    public Timestamp getCreateTime() {
        return createTime;
    }

    /**
     * setCreateTime方法。
     * * @param createTime Timestamp类型参数
     */
    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    /**
     * getUpdateTime方法。
     *
     * @return Timestamp类型返回值
     */
    public Timestamp getUpdateTime() {
        return updateTime;
    }

    /**
     * setUpdateTime方法。
     * * @param updateTime Timestamp类型参数
     */
    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "ResourceItem{id=" + id + ", cmsId=" + cmsId + ", type=" + type + ", ownerId=" + ownerId + ", shared=" + shared + ", createTime=" + createTime + ", updateTime=" + updateTime + "}";
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
        ResourceItem that = (ResourceItem) o;
        return ownerId == that.ownerId &&
                Objects.equals(id, that.id) &&
                Objects.equals(cmsId, that.cmsId) &&
                Objects.equals(type, that.type) &&
                Objects.equals(shared, that.shared) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(updateTime, that.updateTime);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return Objects.hash(id, cmsId, type, ownerId, shared, createTime, updateTime);
    }
}
