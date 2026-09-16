package com.zifang.util.devops.docker.dto;

import java.util.List;
import java.util.Map;

/**
 * Docker 容器信息
 * <p>
 * 用于封装Docker容器的完整元数据信息，
 * 包括容器ID、名称、镜像、状态、端口映射、标签、网络等属性。
 *
 * @author zifang
 * @version 1.0.0
 */
public class ContainerDTO {

    private String id;
    private String name;
    private String image;
    private String imageId;
    private String command;
    private String created;
    private String state;
    private String status;
    private String port;
    private Map<String, String> labels;
    private List<PortMapping> ports;
    private String mount;
    private String network;
    private List<String> networks;

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
     * getImage方法。
     *
     * @return String类型返回值
     */
    public String getImage() {
        return image;
    }

    /**
     * setImage方法。
     * * @param image String类型参数
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * getImageId方法。
     *
     * @return String类型返回值
     */
    public String getImageId() {
        return imageId;
    }

    /**
     * setImageId方法。
     * * @param imageId String类型参数
     */
    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    /**
     * getCommand方法。
     *
     * @return String类型返回值
     */
    public String getCommand() {
        return command;
    }

    /**
     * setCommand方法。
     * * @param command String类型参数
     */
    public void setCommand(String command) {
        this.command = command;
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
     * getState方法。
     *
     * @return String类型返回值
     */
    public String getState() {
        return state;
    }

    /**
     * setState方法。
     * * @param state String类型参数
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * getStatus方法。
     *
     * @return String类型返回值
     */
    public String getStatus() {
        return status;
    }

    /**
     * setStatus方法。
     * * @param status String类型参数
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * getPort方法。
     *
     * @return String类型返回值
     */
    public String getPort() {
        return port;
    }

    /**
     * setPort方法。
     * * @param port String类型参数
     */
    public void setPort(String port) {
        this.port = port;
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
     * getPorts方法。
     *
     * @return List<PortMapping>类型返回值
     */
    public List<PortMapping> getPorts() {
        return ports;
    }

    /**
     * setPorts方法。
     * * @param ports ListPortMapping类型参数
     */
    public void setPorts(List<PortMapping> ports) {
        this.ports = ports;
    }

    /**
     * getMount方法。
     *
     * @return String类型返回值
     */
    public String getMount() {
        return mount;
    }

    /**
     * setMount方法。
     * * @param mount String类型参数
     */
    public void setMount(String mount) {
        this.mount = mount;
    }

    /**
     * getNetwork方法。
     *
     * @return String类型返回值
     */
    public String getNetwork() {
        return network;
    }

    /**
     * setNetwork方法。
     * * @param network String类型参数
     */
    public void setNetwork(String network) {
        this.network = network;
    }

    /**
     * getNetworks方法。
     *
     * @return List<String>类型返回值
     */
    public List<String> getNetworks() {
        return networks;
    }

    /**
     * setNetworks方法。
     * * @param networks ListString类型参数
     */
    public void setNetworks(List<String> networks) {
        this.networks = networks;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "ContainerDTO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", image='" + image + '\'' +
                ", state='" + state + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    /**
     * 端口映射
     * <p>
     * 用于表示容器与宿主机之间的端口映射关系
     */
    public static class PortMapping {
        private String hostIp;
        private int hostPort;
        private int containerPort;
        private String protocol;

        /**
         * getHostIp方法。
         *
         * @return String类型返回值
         */
        public String getHostIp() {
            return hostIp;
        }

        /**
         * setHostIp方法。
         * * @param hostIp String类型参数
         */
        public void setHostIp(String hostIp) {
            this.hostIp = hostIp;
        }

        /**
         * getHostPort方法。
         *
         * @return int类型返回值
         */
        public int getHostPort() {
            return hostPort;
        }

        /**
         * setHostPort方法。
         * * @param hostPort int类型参数
         */
        public void setHostPort(int hostPort) {
            this.hostPort = hostPort;
        }

        /**
         * getContainerPort方法。
         *
         * @return int类型返回值
         */
        public int getContainerPort() {
            return containerPort;
        }

        /**
         * setContainerPort方法。
         * * @param containerPort int类型参数
         */
        public void setContainerPort(int containerPort) {
            this.containerPort = containerPort;
        }

        /**
         * getProtocol方法。
         *
         * @return String类型返回值
         */
        public String getProtocol() {
            return protocol;
        }

        /**
         * setProtocol方法。
         * * @param protocol String类型参数
         */
        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        @Override
        /**
         * toString方法。
         * @return String类型返回值
         */
        public String toString() {
            return hostPort + ":" + containerPort + "/" + protocol;
        }
    }
}
