package com.zifang.util.devops.git.operations;

import com.zifang.util.devops.git.operations.core.*;
import com.zifang.util.devops.git.operations.jgit.JGitExecutor;
import com.zifang.util.devops.git.operations.shell.ShellExecutor;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.RebaseResult;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.PushResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * git 操作统一入口（混合 JGit + shell 兜底）
 * <p>
 * <b>设计原则：</b>
 * <ul>
 *   <li>常规操作（init/clone/add/commit/push/pull/log/diff/branch/checkout/merge/rebase/tag）走 JGit，</li>
 *   <li>JGit 弱支持或不支持的操作（worktree、bisect、archive、stash 等）走 shell，</li>
 *   <li>所有方法返回 {@link GitResult}，不直接抛异常（除非参数非法）。</li>
 * </ul>
 *
 * <b>典型用法：</b>
 * <pre>{@code
 * // 1. 克隆
 * GitResult<GitRepository> r = GitClient.clone("https://github.com/x/y.git", new File("/tmp/y"), null, null);
 * if (!r.isSuccess()) { log(r.getStderr()); return; }
 * GitRepository repo = r.getData();
 *
 * // 2. 文件操作
 * GitClient.add(repo, ".").isSuccess();
 * GitResult<String> cmt = GitClient.commit(repo, new GitAuthor("zifang","z@x.com"), "feat: xxx", false);
 *
 * // 3. 查看历史
 * for (GitCommit c : GitClient.log(repo, 20).getData()) { ... }
 *
 * // 4. 推送
 * GitClient.push(repo, "origin", "main", null, null);
 * }</pre>
 *
 * @author zifang
 * @version 1.0.0
 */
public final class GitClient {

    private static final JGitExecutor J = new JGitExecutor();
    private static final ShellExecutor SHELL = new ShellExecutor();

    private GitClient() {
    }

    // ==================== 仓库生命周期 ====================

    /**
     * 在指定目录初始化一个新仓库
     */
    public static GitResult<GitRepository> init(String path) {
        return init(new File(path));
    }

    /**
     * 在指定目录初始化一个新仓库
     */
    public static GitResult<GitRepository> init(File dir) {
        return J.init(dir);
    }

    /**
     * 打开已有仓库
     */
    public static GitResult<GitRepository> open(String path) {
        return open(new File(path));
    }

    /**
     * 打开已有仓库
     */
    public static GitResult<GitRepository> open(File repoDir) {
        return J.open(repoDir);
    }

    /**
     * 克隆远程仓库
     */
    public static GitResult<GitRepository> clone(String url, String targetPath) {
        return clone(url, new File(targetPath), null, null);
    }

    /**
     * 克隆远程仓库
     */
    public static GitResult<GitRepository> clone(String url, File targetDir) {
        return clone(url, targetDir, null, null);
    }

    /**
     * 克隆远程仓库（带凭证）
     */
    public static GitResult<GitRepository> clone(String url, File targetDir, String username, String password) {
        // 优先 JGit 克隆；JGit 在某些 sparse / LFS 场景下会失败，失败时回退 shell
        GitResult<GitRepository> r = J.clone(url, targetDir, username, password);
        if (r.isSuccess()) {
            return r;
        }
        if (!SHELL.isAvailable()) {
            return r;
        }
        return cloneViaShell(url, targetDir, username, password, r);
    }

    private static GitResult<GitRepository> cloneViaShell(String url, File targetDir,
                                                          String username, String password, GitResult<GitRepository> prev) {
        java.util.List<String> args = new ArrayList<>();
        args.add("clone");
        if (username != null && password != null) {
            String authUrl = injectAuth(url, username, password);
            args.add(authUrl);
        } else {
            args.add(url);
        }
        args.add(targetDir.getAbsolutePath());
        GitResult<String> r = SHELL.exec(args.toArray(new String[0]));
        if (r.isSuccess()) {
            // shell 成功后我们再 open 一下转成 GitRepository
            return J.open(targetDir);
        }
        return GitResult.fail("JGit 失败: " + prev.getMessage() + " | shell 失败: " + r.getStderr());
    }

    private static String injectAuth(String url, String username, String password) {
        // 形如 https://github.com/x/y.git -> https://user:pwd@github.com/x/y.git
        int schemeEnd = url.indexOf("://");
        if (schemeEnd < 0) {
            return url;
        }
        return url.substring(0, schemeEnd + 3) + username + ":" + password + "@" + url.substring(schemeEnd + 3);
    }

    /**
     * 检查当前环境是否可用：JGit 始终可用，shell 视系统是否有 git 二进制
     */
    public static boolean isShellAvailable() {
        return SHELL.isAvailable();
    }

    // ==================== 文件操作 ====================

    /**
     * 暂存文件（add）。patterns 为空表示全部
     */
    public static GitResult<Void> add(GitRepository repo, String... patterns) {
        return J.add(repo, patterns);
    }

    /**
     * 暂存全部
     */
    public static GitResult<Void> addAll(GitRepository repo) {
        return J.add(repo, ".");
    }

    /**
     * 移除文件
     *
     * @param cached true 表示仅从索引移除（--cached），文件保留在工作区
     */
    public static GitResult<Void> rm(GitRepository repo, boolean cached, String... patterns) {
        return J.rm(repo, cached, patterns);
    }

    /**
     * 工作区状态
     */
    public static GitResult<GitStatus> status(GitRepository repo) {
        return J.status(repo);
    }

    /**
     * 工作区与 HEAD 的 diff（未暂存变更）
     */
    public static GitResult<List<GitDiffEntry>> diff(GitRepository repo) {
        return GitResult.success(J.diffWorkTree(repo));
    }

    /**
     * HEAD 与暂存区的 diff（已暂存未提交）
     */
    public static GitResult<List<GitDiffEntry>> diffCached(GitRepository repo) {
        return GitResult.success(J.diffIndex(repo));
    }

    /**
     * 两个 ref 之间的 diff
     */
    public static GitResult<List<GitDiffEntry>> diff(GitRepository repo, String oldRef, String newRef) {
        return GitResult.success(J.diffRefs(repo, oldRef, newRef));
    }

    // ==================== 提交 ====================

    /**
     * 使用当前 git config 中的 user.name/user.email 提交
     */
    public static GitResult<String> commit(GitRepository repo, String message) {
        return commit(repo, null, message, false);
    }

    /**
     * 提交（指定 author/committer）
     */
    public static GitResult<String> commit(GitRepository repo, GitAuthor author, String message) {
        return commit(repo, author, message, false);
    }

    /**
     * 提交
     *
     * @param all    true 表示自动暂存已跟踪文件的修改（git commit -a）
     * @param author 为 null 时使用仓库配置
     */
    public static GitResult<String> commit(GitRepository repo, GitAuthor author, String message, boolean all) {
        if (message == null || message.isEmpty()) {
            return GitResult.fail("commit message 不能为空");
        }
        return J.commit(repo, author, message, all);
    }

    /**
     * 重置到指定提交
     *
     * @param mode soft / mixed / hard / keep / merge（默认 mixed）
     */
    public static GitResult<Void> reset(GitRepository repo, String ref, String mode) {
        return J.reset(repo, ref, mode);
    }

    /**
     * 重置 HEAD 一次（保留工作区修改）
     */
    public static GitResult<Void> resetHead(GitRepository repo) {
        return J.reset(repo, "HEAD", "mixed");
    }

    // ==================== 日志 ====================

    /**
     * 列出最近 maxCount 条提交
     */
    public static GitResult<List<GitCommit>> log(GitRepository repo, int maxCount) {
        return J.log(repo, maxCount);
    }

    /**
     * 列出全部提交
     */
    public static GitResult<List<GitCommit>> log(GitRepository repo) {
        return J.log(repo, -1);
    }

    /**
     * 显示单条提交
     */
    public static GitResult<GitCommit> show(GitRepository repo, String ref) {
        return J.show(repo, ref);
    }

    // ==================== 分支 ====================

    /**
     * 列出所有分支（local + remote）
     */
    public static GitResult<List<GitBranch>> branchList(GitRepository repo) {
        return J.branchList(repo, GitBranch.Type.ALL);
    }

    /**
     * 列出本地分支
     */
    public static GitResult<List<GitBranch>> branchListLocal(GitRepository repo) {
        return J.branchList(repo, GitBranch.Type.LOCAL);
    }

    /**
     * 列出远程分支
     */
    public static GitResult<List<GitBranch>> branchListRemote(GitRepository repo) {
        return J.branchList(repo, GitBranch.Type.REMOTE);
    }

    /**
     * 创建分支（不切换）
     */
    public static GitResult<Void> branchCreate(GitRepository repo, String name) {
        return J.branchCreate(repo, name);
    }

    /**
     * 删除分支
     *
     * @param force 强制删除未合并分支
     */
    public static GitResult<Void> branchDelete(GitRepository repo, String name, boolean force) {
        return J.branchDelete(repo, name, force);
    }

    /**
     * 切换分支或恢复文件
     */
    public static GitResult<Void> checkout(GitRepository repo, String ref) {
        return J.checkout(repo, ref);
    }

    /**
     * 创建并切换到新分支
     */
    public static GitResult<Void> checkoutNewBranch(GitRepository repo, String name) {
        return J.checkoutNewBranch(repo, name);
    }

    /**
     * 当前分支名
     */
    public static GitResult<String> currentBranch(GitRepository repo) {
        return J.currentBranch(repo);
    }

    // ==================== 远程 ====================

    /**
     * 列出所有远程
     */
    public static GitResult<List<GitRemote>> remoteList(GitRepository repo) {
        return J.remoteList(repo);
    }

    /**
     * 添加远程
     */
    public static GitResult<Void> remoteAdd(GitRepository repo, String name, String url) {
        return J.remoteAdd(repo, name, url);
    }

    /**
     * 移除远程
     */
    public static GitResult<Void> remoteRemove(GitRepository repo, String name) {
        return J.remoteRemove(repo, name);
    }

    // ==================== 同步 ====================

    /**
     * fetch
     */
    public static GitResult<FetchResult> fetch(GitRepository repo, String remote) {
        return fetch(repo, remote, null, null);
    }

    /**
     * fetch（带凭证）
     */
    public static GitResult<FetchResult> fetch(GitRepository repo, String remote, String username, String password) {
        return J.fetch(repo, remote, username, password);
    }

    /**
     * pull (fetch + merge)
     */
    public static GitResult<PullResult> pull(GitRepository repo, String remote, String branch) {
        return pull(repo, remote, branch, null, null);
    }

    /**
     * pull（带凭证）
     */
    public static GitResult<PullResult> pull(GitRepository repo, String remote, String branch,
                                             String username, String password) {
        return J.pull(repo, remote, branch, username, password);
    }

    /**
     * push
     *
     * @param refspec 形如 "main" 或 "main:remote-main"；null 表示推送当前分支
     */
    public static GitResult<Iterable<PushResult>> push(GitRepository repo, String remote, String refspec) {
        return push(repo, remote, refspec, null, null);
    }

    /**
     * push（带凭证）
     */
    public static GitResult<Iterable<PushResult>> push(GitRepository repo, String remote, String refspec,
                                                       String username, String password) {
        return J.push(repo, remote, refspec, username, password);
    }

    // ==================== 合并 / 变基 ====================

    /**
     * merge 指定分支/提交到当前
     */
    public static GitResult<MergeResult> merge(GitRepository repo, String ref) {
        return J.merge(repo, ref);
    }

    /**
     * 在当前分支上 rebase 到 upstream
     */
    public static GitResult<RebaseResult> rebase(GitRepository repo, String upstream) {
        return J.rebase(repo, upstream);
    }

    // ==================== Tag ====================

    /**
     * 列出全部 tag
     */
    public static GitResult<List<GitTag>> tagList(GitRepository repo) {
        return J.tagList(repo);
    }

    /**
     * 创建轻量 tag
     */
    public static GitResult<Void> tagCreate(GitRepository repo, String name) {
        return J.tagCreate(repo, name, null, false, null);
    }

    /**
     * 在指定 ref 上创建轻量 tag
     */
    public static GitResult<Void> tagCreate(GitRepository repo, String name, String ref) {
        return J.tagCreate(repo, name, null, false, ref);
    }

    /**
     * 创建带注释的 tag
     */
    public static GitResult<Void> tagCreateAnnotated(GitRepository repo, String name, String message) {
        return J.tagCreate(repo, name, message, true, null);
    }

    /**
     * 在指定 ref 上创建带注释的 tag
     */
    public static GitResult<Void> tagCreateAnnotated(GitRepository repo, String name, String message, String ref) {
        return J.tagCreate(repo, name, message, true, ref);
    }

    /**
     * 删除本地 tag
     */
    public static GitResult<Void> tagDelete(GitRepository repo, String name) {
        return J.tagDelete(repo, name);
    }

    // ==================== Shell 兜底区 ====================
    // 下面这些操作 JGit 支持不完整或没有，使用 git CLI

    /**
     * 工作区清理：移除未跟踪文件（git clean -fd）
     */
    public static GitResult<String> clean(GitRepository repo, boolean force, boolean includeIgnored) {
        String[] args;
        if (includeIgnored) {
            args = new String[]{"clean", "-fdx"};
        } else if (force) {
            args = new String[]{"clean", "-fd"};
        } else {
            args = new String[]{"clean", "-nd"}; // dry-run
        }
        return SHELL.exec(repo.getWorkDir(), args);
    }

    /**
     * stash 暂存当前修改
     *
     * @param includeUntracked true 表示把未跟踪文件也一起暂存（git stash -u）
     */
    public static GitResult<String> stash(GitRepository repo, boolean includeUntracked, String message) {
        java.util.List<String> args = new ArrayList<>();
        args.add("stash");
        args.add("push");
        if (includeUntracked) {
            args.add("-u");
        }
        if (message != null && !message.isEmpty()) {
            args.add("-m");
            args.add(message);
        }
        return SHELL.exec(repo.getWorkDir(), args.toArray(new String[0]));
    }

    /**
     * stash 弹出最近一次
     */
    public static GitResult<String> stashPop(GitRepository repo) {
        return SHELL.exec(repo.getWorkDir(), "stash", "pop");
    }

    /**
     * 列出 stash
     */
    public static GitResult<String> stashList(GitRepository repo) {
        return SHELL.exec(repo.getWorkDir(), "stash", "list");
    }

    /**
     * blame 单文件
     */
    public static GitResult<String> blame(GitRepository repo, String path) {
        return SHELL.exec(repo.getWorkDir(), "blame", path);
    }

    /**
     * archive 导出（zip/tar 等）
     *
     * @param format     "zip" / "tar" / "tar.gz" 等
     * @param ref        导出哪个 ref（null 表示 HEAD）
     * @param outputPath 输出文件路径
     */
    public static GitResult<String> archive(GitRepository repo, String ref, String format, String outputPath) {
        String realRef = ref == null ? "HEAD" : ref;
        if (format == null) {
            format = "tar";
        }
        return SHELL.exec(repo.getWorkDir(), "archive", "--format=" + format, "-o", outputPath, realRef);
    }

    /**
     * worktree add
     */
    public static GitResult<String> worktreeAdd(GitRepository repo, String path, String branch) {
        java.util.List<String> args = new ArrayList<>();
        args.add("worktree");
        args.add("add");
        if (branch != null && !branch.isEmpty()) {
            args.add("-b");
            args.add(branch);
        }
        args.add(path);
        return SHELL.exec(repo.getWorkDir(), args.toArray(new String[0]));
    }

    /**
     * worktree list（原始文本）
     */
    public static GitResult<String> worktreeList(GitRepository repo) {
        return SHELL.exec(repo.getWorkDir(), "worktree", "list");
    }

    /**
     * worktree remove
     */
    public static GitResult<String> worktreeRemove(GitRepository repo, String path, boolean force) {
        return SHELL.exec(repo.getWorkDir(), "worktree", "remove", force ? "--force" : "--", path);
    }

    /**
     * 任意 git 命令（逃生口，shell only）
     *
     * @return stdout 文本
     */
    public static GitResult<String> execRaw(GitRepository repo, String... args) {
        return SHELL.exec(repo.getWorkDir(), args);
    }

    /**
     * 在任意目录执行任意 git 命令（无仓库对象，逃生口）
     */
    public static GitResult<String> execRaw(String... args) {
        return SHELL.exec(null, args);
    }
}
