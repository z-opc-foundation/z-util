package com.zifang.util.devops.git.operations.core;

/**
 * 单条 diff 记录
 *
 * @author zifang
 * @version 1.0.0
 */
public class GitDiffEntry {

    private ChangeType changeType;
    private String oldPath;
    private String newPath;
    /**
     * 旧 blob SHA（可空）
     */
    private String oldSha;
    /**
     * 新 blob SHA（可空）
     */
    private String newSha;
    /**
     * 是否为二进制
     */
    private boolean binary;
    /**
     * 完整 diff 文本（旧+新），可选
     */
    private String patch;

    /**
     * getChangeType方法。
     *
     * @return ChangeType类型返回值
     */
    public ChangeType getChangeType() {
        return changeType;
    }

    /**
     * setChangeType方法。
     * * @param changeType ChangeType类型参数
     */
    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    /**
     * getOldPath方法。
     *
     * @return String类型返回值
     */
    public String getOldPath() {
        return oldPath;
    }

    /**
     * setOldPath方法。
     * * @param oldPath String类型参数
     */
    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }

    /**
     * getNewPath方法。
     *
     * @return String类型返回值
     */
    public String getNewPath() {
        return newPath;
    }

    /**
     * setNewPath方法。
     * * @param newPath String类型参数
     */
    public void setNewPath(String newPath) {
        this.newPath = newPath;
    }

    /**
     * getOldSha方法。
     *
     * @return String类型返回值
     */
    public String getOldSha() {
        return oldSha;
    }

    /**
     * setOldSha方法。
     * * @param oldSha String类型参数
     */
    public void setOldSha(String oldSha) {
        this.oldSha = oldSha;
    }

    /**
     * getNewSha方法。
     *
     * @return String类型返回值
     */
    public String getNewSha() {
        return newSha;
    }

    /**
     * setNewSha方法。
     * * @param newSha String类型参数
     */
    public void setNewSha(String newSha) {
        this.newSha = newSha;
    }

    /**
     * isBinary方法。
     *
     * @return boolean类型返回值
     */
    public boolean isBinary() {
        return binary;
    }

    /**
     * setBinary方法。
     * * @param binary boolean类型参数
     */
    public void setBinary(boolean binary) {
        this.binary = binary;
    }

    /**
     * getPatch方法。
     *
     * @return String类型返回值
     */
    public String getPatch() {
        return patch;
    }

    /**
     * setPatch方法。
     * * @param patch String类型参数
     */
    public void setPatch(String patch) {
        this.patch = patch;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return changeType + " " + (oldPath != null ? oldPath : "") +
                (newPath != null && !newPath.equals(oldPath) ? " -> " + newPath : "");
    }

    /**
     * ChangeType枚举。
     */
    public enum ChangeType {
        ADD, DELETE, MODIFY, RENAME, COPY
    }
}
