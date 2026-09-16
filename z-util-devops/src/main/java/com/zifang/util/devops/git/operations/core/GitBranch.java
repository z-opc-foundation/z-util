package com.zifang.util.devops.git.operations.core;

/**
 * 分支信息
 *
 * @author zifang
 * @version 1.0.0
 */
public class GitBranch {

    /**
     * 分支简称（如 main, origin/main）
     */
    private String name;
    /**
     * 完整 ref 名（refs/heads/main, refs/remotes/origin/main）
     */
    private String fullName;
    /**
     * 分支指向的提交 SHA
     */
    private String sha;
    /**
     * 是否为当前 HEAD
     */
    private boolean current;
    /**
     * 是否为远程分支
     */
    private boolean remote;
    /**
     * 跟踪的上游分支简称（可能为 null）
     */
    private String upstream;

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
     * getFullName方法。
     *
     * @return String类型返回值
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * setFullName方法。
     * * @param fullName String类型参数
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * getSha方法。
     *
     * @return String类型返回值
     */
    public String getSha() {
        return sha;
    }

    /**
     * setSha方法。
     * * @param sha String类型参数
     */
    public void setSha(String sha) {
        this.sha = sha;
    }

    /**
     * isCurrent方法。
     *
     * @return boolean类型返回值
     */
    public boolean isCurrent() {
        return current;
    }

    /**
     * setCurrent方法。
     * * @param current boolean类型参数
     */
    public void setCurrent(boolean current) {
        this.current = current;
    }

    /**
     * isRemote方法。
     *
     * @return boolean类型返回值
     */
    public boolean isRemote() {
        return remote;
    }

    /**
     * setRemote方法。
     * * @param remote boolean类型参数
     */
    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    /**
     * getUpstream方法。
     *
     * @return String类型返回值
     */
    public String getUpstream() {
        return upstream;
    }

    /**
     * setUpstream方法。
     * * @param upstream String类型参数
     */
    public void setUpstream(String upstream) {
        this.upstream = upstream;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return (current ? "* " : "  ") + name + (upstream != null ? " -> " + upstream : "");
    }

    /**
     * Type枚举。
     */
    public enum Type {
        LOCAL, REMOTE, ALL
    }
}
