package com.zifang.util.devops.docker.dto;

import java.util.Map;

/**
 * Docker 网络信息
 * <p>
 * 用于封装Docker网络（Network）的完整元数据信息，
 * 包括网络ID、名称、驱动、作用域、子网、网关、标签等属性。
 *
 * @author zifang
 * @version 1.0.0
 */
public class NetworkDTO {

    private String id;
    private String name;
    private String driver;
    private String scope;
    private boolean internal;
    private boolean attachable;
    private String subnet;
    private String gateway;
    private Map<String, String> labels;
    private int containerCount;

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
     * isInternal方法。
     *
     * @return boolean类型返回值
     */
    public boolean isInternal() {
        return internal;
    }

    /**
     * setInternal方法。
     * * @param internal boolean类型参数
     */
    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    /**
     * isAttachable方法。
     *
     * @return boolean类型返回值
     */
    public boolean isAttachable() {
        return attachable;
    }

    /**
     * setAttachable方法。
     * * @param attachable boolean类型参数
     */
    public void setAttachable(boolean attachable) {
        this.attachable = attachable;
    }

    /**
     * getSubnet方法。
     *
     * @return String类型返回值
     */
    public String getSubnet() {
        return subnet;
    }

    /**
     * setSubnet方法。
     * * @param subnet String类型参数
     */
    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }

    /**
     * getGateway方法。
     *
     * @return String类型返回值
     */
    public String getGateway() {
        return gateway;
    }

    /**
     * setGateway方法。
     * * @param gateway String类型参数
     */
    public void setGateway(String gateway) {
        this.gateway = gateway;
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
     * getContainerCount方法。
     *
     * @return int类型返回值
     */
    public int getContainerCount() {
        return containerCount;
    }

    /**
     * setContainerCount方法。
     * * @param containerCount int类型参数
     */
    public void setContainerCount(int containerCount) {
        this.containerCount = containerCount;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "NetworkDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", driver='" + driver + '\'' +
                ", scope='" + scope + '\'' +
                '}';
    }
}
