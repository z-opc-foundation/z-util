package com.zifang.util.devops.git.operations.core;

import java.io.File;

/**
 * git 仓库句柄
 * <p>
 * 封装已打开的本地仓库，关联工作目录与 .git 目录。
 * 调用方应通过 {@link com.zifang.util.devops.git.operations.GitClient#open(File)}
 * 获取，不要直接 new。
 *
 * @author zifang
 * @version 1.0.0
 */
public class GitRepository {

    private final File workDir;
    private final File gitDir;

    /**
     * 构造仓库句柄
     *
     * @param workDir 工作目录（包含 .git 子目录的目录）
     * @param gitDir  .git 目录
     */
    public GitRepository(File workDir, File gitDir) {
        this.workDir = workDir;
        this.gitDir = gitDir;
    }

    /**
     * 获取工作目录
     */
    public File getWorkDir() {
        return workDir;
    }

    /**
     * 获取 .git 目录
     */
    public File getGitDir() {
        return gitDir;
    }

    /**
     * 仓库路径字符串（用于打印）
     */
    public String getPath() {
        return workDir == null ? null : workDir.getAbsolutePath();
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "GitRepository{path='" + getPath() + "'}";
    }
}
