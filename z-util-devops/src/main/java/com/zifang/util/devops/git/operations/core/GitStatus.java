package com.zifang.util.devops.git.operations.core;

import java.util.ArrayList;
import java.util.List;

/**
 * 工作区状态
 * <p>
 * 汇总已暂存、未暂存、未跟踪三类文件变化，并暴露当前分支、是否为干净工作区。
 *
 * @author zifang
 * @version 1.0.0
 */
public class GitStatus {

    /**
     * 当前分支名（detached HEAD 时为 null）
     */
    private String branch;
    /**
     * 是否处于 detached HEAD 状态
     */
    private boolean detached;
    /**
     * 当前 HEAD 的 SHA（detached 时也有值）
     */
    private String headSha;
    /**
     * 工作区是否完全干净
     */
    private boolean clean;
    /**
     * 已暂存（staged）的文件路径，相对工作区
     */
    private List<String> added = new ArrayList<>();
    private List<String> changed = new ArrayList<>();
    private List<String> removed = new ArrayList<>();
    private List<String> untracked = new ArrayList<>();
    /**
     * 未暂存（modified）
     */
    private List<String> modified = new ArrayList<>();
    private List<String> deleted = new ArrayList<>();
    private List<String> missing = new ArrayList<>();

    /**
     * getBranch方法。
     *
     * @return String类型返回值
     */
    public String getBranch() {
        return branch;
    }

    /**
     * setBranch方法。
     * * @param branch String类型参数
     */
    public void setBranch(String branch) {
        this.branch = branch;
    }

    /**
     * isDetached方法。
     *
     * @return boolean类型返回值
     */
    public boolean isDetached() {
        return detached;
    }

    /**
     * setDetached方法。
     * * @param detached boolean类型参数
     */
    public void setDetached(boolean detached) {
        this.detached = detached;
    }

    /**
     * getHeadSha方法。
     *
     * @return String类型返回值
     */
    public String getHeadSha() {
        return headSha;
    }

    /**
     * setHeadSha方法。
     * * @param headSha String类型参数
     */
    public void setHeadSha(String headSha) {
        this.headSha = headSha;
    }

    /**
     * isClean方法。
     *
     * @return boolean类型返回值
     */
    public boolean isClean() {
        return clean;
    }

    /**
     * setClean方法。
     * * @param clean boolean类型参数
     */
    public void setClean(boolean clean) {
        this.clean = clean;
    }

    /**
     * getAdded方法。
     *
     * @return List<String>类型返回值
     */
    public List<String> getAdded() {
        return added;
    }

    /**
     * setAdded方法。
     * * @param added ListString类型参数
     */
    public void setAdded(List<String> added) {
        this.added = added;
    }

    /**
     * getChanged方法。
     *
     * @return List<String>类型返回值
     */
    public List<String> getChanged() {
        return changed;
    }

    /**
     * setChanged方法。
     * * @param changed ListString类型参数
     */
    public void setChanged(List<String> changed) {
        this.changed = changed;
    }

    /**
     * getRemoved方法。
     *
     * @return List<String>类型返回值
     */
    public List<String> getRemoved() {
        return removed;
    }

    /**
     * setRemoved方法。
     * * @param removed ListString类型参数
     */
    public void setRemoved(List<String> removed) {
        this.removed = removed;
    }

    /**
     * getUntracked方法。
     *
     * @return List<String>类型返回值
     */
    public List<String> getUntracked() {
        return untracked;
    }

    /**
     * setUntracked方法。
     * * @param untracked ListString类型参数
     */
    public void setUntracked(List<String> untracked) {
        this.untracked = untracked;
    }

    /**
     * getModified方法。
     *
     * @return List<String>类型返回值
     */
    public List<String> getModified() {
        return modified;
    }

    /**
     * setModified方法。
     * * @param modified ListString类型参数
     */
    public void setModified(List<String> modified) {
        this.modified = modified;
    }

    /**
     * getDeleted方法。
     *
     * @return List<String>类型返回值
     */
    public List<String> getDeleted() {
        return deleted;
    }

    /**
     * setDeleted方法。
     * * @param deleted ListString类型参数
     */
    public void setDeleted(List<String> deleted) {
        this.deleted = deleted;
    }

    /**
     * getMissing方法。
     *
     * @return List<String>类型返回值
     */
    public List<String> getMissing() {
        return missing;
    }

    /**
     * setMissing方法。
     * * @param missing ListString类型参数
     */
    public void setMissing(List<String> missing) {
        this.missing = missing;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "GitStatus{branch='" + branch + "', clean=" + clean +
                ", added=" + added.size() + ", modified=" + modified.size() +
                ", deleted=" + deleted.size() + ", untracked=" + untracked.size() + "}";
    }
}
