package com.zifang.util.devops.git.operations.core;

import java.util.List;

/**
 * 单条提交信息
 *
 * @author zifang
 * @version 1.0.0
 */
public class GitCommit {

    /**
     * 完整 SHA-1
     */
    private String sha;
    /**
     * 短 SHA（前 7-12 位）
     */
    private String shortSha;
    /**
     * 提交信息（完整，多行可能）
     */
    private String message;
    /**
     * 提交信息首行（subject）
     */
    private String shortMessage;
    /**
     * 作者
     */
    private GitAuthor author;
    /**
     * 提交者
     */
    private GitAuthor committer;
    /**
     * 父提交 SHA 列表（merge commit 通常有 2 个）
     */
    private List<String> parentShas;

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
     * getShortSha方法。
     *
     * @return String类型返回值
     */
    public String getShortSha() {
        return shortSha;
    }

    /**
     * setShortSha方法。
     * * @param shortSha String类型参数
     */
    public void setShortSha(String shortSha) {
        this.shortSha = shortSha;
    }

    /**
     * getMessage方法。
     *
     * @return String类型返回值
     */
    public String getMessage() {
        return message;
    }

    /**
     * setMessage方法。
     * * @param message String类型参数
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * getShortMessage方法。
     *
     * @return String类型返回值
     */
    public String getShortMessage() {
        return shortMessage;
    }

    /**
     * setShortMessage方法。
     * * @param shortMessage String类型参数
     */
    public void setShortMessage(String shortMessage) {
        this.shortMessage = shortMessage;
    }

    /**
     * getAuthor方法。
     *
     * @return GitAuthor类型返回值
     */
    public GitAuthor getAuthor() {
        return author;
    }

    /**
     * setAuthor方法。
     * * @param author GitAuthor类型参数
     */
    public void setAuthor(GitAuthor author) {
        this.author = author;
    }

    /**
     * getCommitter方法。
     *
     * @return GitAuthor类型返回值
     */
    public GitAuthor getCommitter() {
        return committer;
    }

    /**
     * setCommitter方法。
     * * @param committer GitAuthor类型参数
     */
    public void setCommitter(GitAuthor committer) {
        this.committer = committer;
    }

    /**
     * getParentShas方法。
     *
     * @return List<String>类型返回值
     */
    public List<String> getParentShas() {
        return parentShas;
    }

    /**
     * setParentShas方法。
     * * @param parentShas ListString类型参数
     */
    public void setParentShas(List<String> parentShas) {
        this.parentShas = parentShas;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "GitCommit{" + shortSha + " " + shortMessage + "}";
    }
}
