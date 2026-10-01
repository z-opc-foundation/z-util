package com.zifang.util.devops.docker.dto;

import java.util.List;
import java.util.Map;

/**
 * Docker 镜像信息
 * <p>
 * 用于封装Docker镜像（Image）的完整元数据信息，
 * 包括镜像ID、仓库名、标签、创建时间、大小、标签列表等属性。
 *
 * @author zifang
 * @version 1.0.0
 */
public class ImageDTO {

    private String id;
    private String repository;
    private String tag;
    private String created;
    private String size;
    private long sizeBytes;
    private Map<String, String> labels;
    private List<String> repoTags;
    private List<String> ports;

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
     * getRepository方法。
     *
     * @return String类型返回值
     */
    public String getRepository() {
        return repository;
    }

    /**
     * setRepository方法。
     * * @param repository String类型参数
     */
    public void setRepository(String repository) {
        this.repository = repository;
    }

    /**
     * getTag方法。
     *
     * @return String类型返回值
     */
    public String getTag() {
        return tag;
    }

    /**
     * setTag方法。
     * * @param tag String类型参数
     */
    public void setTag(String tag) {
        this.tag = tag;
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
     * getSize方法。
     *
     * @return String类型返回值
     */
    public String getSize() {
        return size;
    }

    /**
     * setSize方法。
     * * @param size String类型参数
     */
    public void setSize(String size) {
        this.size = size;
    }

    /**
     * getSizeBytes方法。
     *
     * @return long类型返回值
     */
    public long getSizeBytes() {
        return sizeBytes;
    }

    /**
     * setSizeBytes方法。
     * * @param sizeBytes long类型参数
     */
    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
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
     * getRepoTags方法。
     *
     * @return List<String>类型返回值
     */
    public List<String> getRepoTags() {
        return repoTags;
    }

    /**
     * setRepoTags方法。
     * * @param repoTags ListString类型参数
     */
    public void setRepoTags(List<String> repoTags) {
        this.repoTags = repoTags;
    }

    /**
     * getPorts方法。
     *
     * @return List<String>类型返回值
     */
    public List<String> getPorts() {
        return ports;
    }

    /**
     * setPorts方法。
     * * @param ports ListString类型参数
     */
    public void setPorts(List<String> ports) {
        this.ports = ports;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "ImageDTO{" +
                "id='" + id + '\'' +
                ", repository='" + repository + '\'' +
                ", tag='" + tag + '\'' +
                ", size='" + size + '\'' +
                '}';
    }
}
