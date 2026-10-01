package com.zifang.util.devops.nexus;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Maven组件
 * <p>
 * 用于表示Nexus仓库中的Maven构件（Component），
 * 包含构件的ID、仓库名称、组ID、构件名、版本号、格式等信息。
 *
 * @author zifang
 * @version 1.0.0
 */
public class Component implements Comparable<Component> {

    private String id;

    private String repository;

    private String group;

    private String name;

    private String version;

    private String format;

    private List<Asset> assets = new ArrayList<>();

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
     * getGroup方法。
     *
     * @return String类型返回值
     */
    public String getGroup() {
        return group;
    }

    /**
     * setGroup方法。
     * * @param group String类型参数
     */
    public void setGroup(String group) {
        this.group = group;
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
     * getVersion方法。
     *
     * @return String类型返回值
     */
    public String getVersion() {
        return version;
    }

    /**
     * setVersion方法。
     * * @param version String类型参数
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * getFormat方法。
     *
     * @return String类型返回值
     */
    public String getFormat() {
        return format;
    }

    /**
     * setFormat方法。
     * * @param format String类型参数
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * getAssets方法。
     *
     * @return List<Asset>类型返回值
     */
    public List<Asset> getAssets() {
        return assets;
    }

    /**
     * setAssets方法。
     * * @param assets ListAsset类型参数
     */
    public void setAssets(List<Asset> assets) {
        this.assets = assets;
    }

    /**
     * 比较两个组件的版本号
     *
     * @param o 要比较的组件
     * @return 版本号比较结果
     */
    @Override
    /**
     * compareTo方法。
     *      * @param o Component类型参数
     * @return int类型返回值
     */
    public int compareTo(Component o) {
        return this.version.compareTo(o.getVersion());
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "Component{id=" + id + ", repository=" + repository + ", group=" + group + ", name=" + name + ", version=" + version + ", format=" + format + ", assets=" + assets + "}";
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
        Component component = (Component) o;
        return Objects.equals(id, component.id) && Objects.equals(repository, component.repository) && Objects.equals(group, component.group) && Objects.equals(name, component.name) && Objects.equals(version, component.version) && Objects.equals(format, component.format) && Objects.equals(assets, component.assets);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return Objects.hash(id, repository, group, name, version, format, assets);
    }
}
