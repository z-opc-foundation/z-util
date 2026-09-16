package com.zifang.util.devops.docker.dto;

import java.util.Map;

/**
 * Docker 卷信息
 * <p>
 * 用于封装Docker卷（Volume）的完整元数据信息，
 * 包括卷名称、驱动、本地挂载点、作用域、标签、创建时间等属性。
 *
 * @author zifang
 * @version 1.0.0
 */
public class VolumeDTO {

    private String name;
    private String driver;
    private String mountpoint;
    private String scope;
    private Map<String, String> labels;
    private String created;
    private boolean readonly;

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

    /**
     * getDriver方法。
     *
     * @return String类型返回值
     */
    public String getDriver() {
        return driver;
    }

    /**
     * setDriver方法。
     * * @param driver String类型参数
     */
    public void setDriver(String driver) {
        this.driver = driver;
    }

    /**
     * getMountpoint方法。
     *
     * @return String类型返回值
     */
    public String getMountpoint() {
        return mountpoint;
    }

    /**
     * setMountpoint方法。
     * * @param mountpoint String类型参数
     */
    public void setMountpoint(String mountpoint) {
        this.mountpoint = mountpoint;
    }

    /**
     * getScope方法。
     *
     * @return String类型返回值
     */
    public String getScope() {
        return scope;
    }

    /**
     * setScope方法。
     * * @param scope String类型参数
     */
    public void setScope(String scope) {
        this.scope = scope;
    }

    /**
     * getLabels方法。
     *
     * @return Map<String, String>类型返回值
     */
    public Map<String, String> getLabels() {
        return labels;
    }

    /**
     * setLabels方法。
     * * @param labels MapString,类型参数
     */
    public void setLabels(Map<String, String> labels) {
        this.labels = labels;
    }

    /**
     * getCreated方法。
     *
     * @return String类型返回值
     */
    public String getCreated() {
        return created;
    }

    /**
     * setCreated方法。
     * * @param created String类型参数
     */
    public void setCreated(String created) {
        this.created = created;
    }

    /**
     * isReadonly方法。
     *
     * @return boolean类型返回值
     */
    public boolean isReadonly() {
        return readonly;
    }

    /**
     * setReadonly方法。
     * * @param readonly boolean类型参数
     */
    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "VolumeDTO{" +
                "name='" + name + '\'' +
                ", driver='" + driver + '\'' +
                ", mountpoint='" + mountpoint + '\'' +
                ", scope='" + scope + '\'' +
                '}';
    }
}
