package com.zifang.util.devops.git.operations.core;

/**
 * 远程仓库信息
 *
 * @author zifang
 * @version 1.0.0
 */
public class GitRemote {

    private String name;
    private String fetchUrl;
    private String pushUrl;

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
     * getFetchUrl方法。
     *
     * @return String类型返回值
     */
    public String getFetchUrl() {
        return fetchUrl;
    }

    /**
     * setFetchUrl方法。
     * * @param fetchUrl String类型参数
     */
    public void setFetchUrl(String fetchUrl) {
        this.fetchUrl = fetchUrl;
    }

    /**
     * getPushUrl方法。
     *
     * @return String类型返回值
     */
    public String getPushUrl() {
        return pushUrl;
    }

    /**
     * setPushUrl方法。
     * * @param pushUrl String类型参数
     */
    public void setPushUrl(String pushUrl) {
        this.pushUrl = pushUrl;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "GitRemote{name='" + name + "', fetchUrl='" + fetchUrl + "'}";
    }
}
