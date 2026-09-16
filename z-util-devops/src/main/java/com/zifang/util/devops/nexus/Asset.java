package com.zifang.util.devops.nexus;


/**
 * Nexus 仓库中的资产（Asset）实体类
 * <p>
 * 代表 Nexus 组件库中的一个文件资产，包含资产的路径、下载地址、校验和等信息。
 *
 * @author zifang
 * @version 1.0.0
 */
public class Asset {

    private String id;

    private String repository;

    private String path;

    private String downloadUrl;

    private Checksum checksum;

    private String format;

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
     * getPath方法。
     *
     * @return String类型返回值
     */
    public String getPath() {
        return path;
    }

    /**
     * setPath方法。
     * * @param path String类型参数
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * getDownloadUrl方法。
     *
     * @return String类型返回值
     */
    public String getDownloadUrl() {
        return downloadUrl;
    }

    /**
     * setDownloadUrl方法。
     * * @param downloadUrl String类型参数
     */
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    /**
     * getChecksum方法。
     *
     * @return Checksum类型返回值
     */
    public Checksum getChecksum() {
        return checksum;
    }

    /**
     * setChecksum方法。
     * * @param checksum Checksum类型参数
     */
    public void setChecksum(Checksum checksum) {
        this.checksum = checksum;
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

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "Asset{id=" + id + ", repository=" + repository + ", path=" + path + ", downloadUrl=" + downloadUrl + ", checksum=" + checksum + ", format=" + format + "}";
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
        Asset asset = (Asset) o;
        return java.util.Objects.equals(id, asset.id) && java.util.Objects.equals(repository, asset.repository) && java.util.Objects.equals(path, asset.path) && java.util.Objects.equals(downloadUrl, asset.downloadUrl) && java.util.Objects.equals(checksum, asset.checksum) && java.util.Objects.equals(format, asset.format);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return java.util.Objects.hash(id, repository, path, downloadUrl, checksum, format);
    }
}
