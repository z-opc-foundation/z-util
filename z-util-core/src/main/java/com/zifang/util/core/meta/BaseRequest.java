package com.zifang.util.core.meta;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base request class with common fields for all request objects.
 *
 * @author zifang
 */
public class BaseRequest implements Serializable {

    private static final long serialVersionUID = -4216068195245037766L;

    /**
     * Unique identifier for the request.
     */
    private String id;

    /**
     * Creation timestamp.
     */
    private LocalDateTime createTime;

    /**
     * Last update timestamp.
     */
    private LocalDateTime updateTime;

    /**
     * Default constructor.
     */
    public BaseRequest() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    /**
     * getId方法。
     *
     * @return String类型返回值
     */
    public String getId() {
        return id;
    }

    /**
     * setId方法。
     * * @param id String类型参数
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * getCreateTime方法。
     *
     * @return LocalDateTime类型返回值
     */
    public LocalDateTime getCreateTime() {
        return createTime;
    }

    /**
     * setCreateTime方法。
     * * @param createTime LocalDateTime类型参数
     */
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    /**
     * getUpdateTime方法。
     *
     * @return LocalDateTime类型返回值
     */
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    /**
     * setUpdateTime方法。
     * * @param updateTime LocalDateTime类型参数
     */
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "BaseRequest{" +
                "id='" + id + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}