package com.zifang.util.devops.git.operations.core;

import java.util.Date;

/**
 * 提交作者/提交人
 * <p>
 * 复用于 author 和 committer 两种身份。
 *
 * @author zifang
 * @version 1.0.0
 */
public class GitAuthor {

    private String name;
    private String email;
    private Date when;

    /**
     * GitAuthor方法。
     */
    public GitAuthor() {
    }

    /**
     * GitAuthor方法。
     * * @param name String类型参数
     *
     * @param email String类型参数
     */
    public GitAuthor(String name, String email) {
        this.name = name;
        this.email = email;
    }

    /**
     * GitAuthor方法。
     * * @param name String类型参数
     *
     * @param email String类型参数
     * @param when  Date类型参数
     */
    public GitAuthor(String name, String email, Date when) {
        this.name = name;
        this.email = email;
        this.when = when;
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
     * getEmail方法。
     *
     * @return String类型返回值
     */
    public String getEmail() {
        return email;
    }

    /**
     * setEmail方法。
     * * @param email String类型参数
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * getWhen方法。
     *
     * @return Date类型返回值
     */
    public Date getWhen() {
        return when;
    }

    /**
     * setWhen方法。
     * * @param when Date类型参数
     */
    public void setWhen(Date when) {
        this.when = when;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return name + " <" + email + ">";
    }
}
