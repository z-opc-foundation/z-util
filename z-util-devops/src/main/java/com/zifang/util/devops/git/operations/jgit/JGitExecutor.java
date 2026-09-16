package com.zifang.util.devops.git.operations.jgit;

import com.zifang.util.devops.git.operations.GitException;
import com.zifang.util.devops.git.operations.GitResult;
import com.zifang.util.devops.git.operations.core.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.util.io.NullOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * JGit 执行器（JGit 5.13.3 兼容版）
 * <p>
 * 封装 JGit API，屏蔽其与 Git 1:1 命令行的差异，对外暴露 DTO。
 * GitClient 在内部通过本类完成大部分操作，shell 仅做兜底（diff/stash/clean/blame/archive/worktree）。
 *
 * @author zifang
 * @version 1.0.0
 */
public class JGitExecutor {

    // ==================== 仓库生命周期 ====================

    private static ResetCommand.ResetType parseResetType(String mode) {
        if (mode == null) {
            return ResetCommand.ResetType.MIXED;
        }
        switch (mode.toLowerCase()) {
            case "soft":
                return ResetCommand.ResetType.SOFT;
            case "hard":
                return ResetCommand.ResetType.HARD;
            case "keep":
                return ResetCommand.ResetType.KEEP;
            case "merge":
                return ResetCommand.ResetType.MERGE;
            default:
                return ResetCommand.ResetType.MIXED;
        }
    }

    private static GitCommit toGitCommit(RevCommit rc) {
        GitCommit c = new GitCommit();
        c.setSha(rc.getName());
        c.setShortSha(rc.getName().substring(0, Math.min(12, rc.getName().length())));
        c.setMessage(rc.getFullMessage());
        c.setShortMessage(rc.getShortMessage());
        c.setAuthor(toAuthor(rc.getAuthorIdent()));
        c.setCommitter(toAuthor(rc.getCommitterIdent()));
        List<String> parents = new ArrayList<>();
        for (RevCommit p : rc.getParents()) {
            parents.add(p.getName());
        }
        c.setParentShas(parents);
        return c;
    }

    private static GitAuthor toAuthor(PersonIdent p) {
        if (p == null) {
            return null;
        }
        // JGit 5.13：PersonIdent.getWhen() 直接返回 Date（getWhenAsDate 是 6.x 加的）
        return new GitAuthor(p.getName(), p.getEmailAddress(), p.getWhen());
    }

    // ==================== 工作区操作 ====================

    private static List<com.zifang.util.devops.git.operations.core.GitDiffEntry> scanDiff(
            Repository r, AbstractTreeIterator oldIt, AbstractTreeIterator newIt) throws IOException {
        List<com.zifang.util.devops.git.operations.core.GitDiffEntry> out = new ArrayList<>();
        try (DiffFormatter df = new DiffFormatter(NullOutputStream.INSTANCE)) {
            df.setRepository(r);
            df.setDiffComparator(RawTextComparator.DEFAULT);
            df.setDetectRenames(true);
            for (DiffEntry e : df.scan(oldIt, newIt)) {
                com.zifang.util.devops.git.operations.core.GitDiffEntry g =
                        new com.zifang.util.devops.git.operations.core.GitDiffEntry();
                g.setChangeType(toChangeType(e.getChangeType()));
                g.setOldPath(e.getOldPath());
                g.setNewPath(e.getNewPath());
                g.setOldSha(e.getOldId().name());
                g.setNewSha(e.getNewId().name());
                out.add(g);
            }
        }
        return out;
    }

    private static com.zifang.util.devops.git.operations.core.GitDiffEntry.ChangeType toChangeType(DiffEntry.ChangeType t) {
        switch (t) {
            case ADD:
                return com.zifang.util.devops.git.operations.core.GitDiffEntry.ChangeType.ADD;
            case DELETE:
                return com.zifang.util.devops.git.operations.core.GitDiffEntry.ChangeType.DELETE;
            case MODIFY:
                return com.zifang.util.devops.git.operations.core.GitDiffEntry.ChangeType.MODIFY;
            case RENAME:
                return com.zifang.util.devops.git.operations.core.GitDiffEntry.ChangeType.RENAME;
            case COPY:
                return com.zifang.util.devops.git.operations.core.GitDiffEntry.ChangeType.COPY;
            default:
                return com.zifang.util.devops.git.operations.core.GitDiffEntry.ChangeType.MODIFY;
        }
    }

    private static String extractShortName(String fullName) {
        if (fullName.startsWith(Constants.R_HEADS)) {
            return fullName.substring(Constants.R_HEADS.length());
        }
        if (fullName.startsWith(Constants.R_REMOTES)) {
            return fullName.substring(Constants.R_REMOTES.length());
        }
        if (fullName.startsWith(Constants.R_TAGS)) {
            return fullName.substring(Constants.R_TAGS.length());
        }
        return fullName;
    }

    private static Git openGit(GitRepository repo) {
        try {
            Repository r = new org.eclipse.jgit.storage.file.FileRepositoryBuilder()
                    .setGitDir(repo.getGitDir())
                    .setWorkTree(repo.getWorkDir())
                    .build();
            return new Git(r);
        } catch (IOException e) {
            throw new GitException("打开仓库失败: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unused")
    private static List<String> asList(String... s) {
        return s == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(s));
    }

    @SuppressWarnings("unused")
    private static Set<String> asSet(String... s) {
        Set<String> set = new LinkedHashSet<>();
        if (s != null) {
            set.addAll(Arrays.asList(s));
        }
        return set;
    }

    // ==================== 日志 ====================

    @SuppressWarnings("unused")
    private static void ensureNotInState(Repository repo, org.eclipse.jgit.lib.RepositoryState... forbidden) {
        org.eclipse.jgit.lib.RepositoryState cur = repo.getRepositoryState();
        Set<org.eclipse.jgit.lib.RepositoryState> set = new HashSet<>(Arrays.asList(forbidden));
        if (set.contains(cur)) {
            throw new GitException("仓库当前状态不允许该操作: " + cur);
        }
    }

    @SuppressWarnings("unused")
    private static RevTree unused(ObjectId id) {
        return null; // 占位以防编译器警告
    }

    /**
     * init方法。
     * * @param dir File类型参数
     *
     * @return GitResult<GitRepository>类型返回值
     */
    public GitResult<GitRepository> init(File dir) {
        if (dir == null) {
            return GitResult.fail("目录不能为空");
        }
        try {
            if (!dir.exists() && !dir.mkdirs()) {
                return GitResult.fail("无法创建目录: " + dir);
            }
            try (Git git = Git.init().setDirectory(dir).call()) {
                File gitDir = git.getRepository().getDirectory();
                return GitResult.success(new GitRepository(dir, gitDir));
            }
        } catch (Exception e) {
            return GitResult.fail("初始化失败: " + e.getMessage());
        }
    }

    /**
     * open方法。
     * * @param repoDir File类型参数
     *
     * @return GitResult<GitRepository>类型返回值
     */
    public GitResult<GitRepository> open(File repoDir) {
        if (repoDir == null || !repoDir.exists()) {
            return GitResult.fail("目录不存在: " + repoDir);
        }
        try {
            Repository repo = new org.eclipse.jgit.storage.file.FileRepositoryBuilder()
                    .setMustExist(true)
                    .findGitDir(repoDir)
                    .build();
            return GitResult.success(new GitRepository(repo.getWorkTree(), repo.getDirectory()));
        } catch (IOException e) {
            return GitResult.fail("打开仓库失败: " + e.getMessage());
        }
    }

    // ==================== Diff ====================
    // JGit 5.13 的 diff API 跟新版本有差异；为了稳定，我们走 DirCache + FileTreeIterator
    // 返回带 ChangeType / 路径 / sha 的 GitDiffEntry 列表，但不包含 patch 文本（要 patch 走 shell diff）。

    /**
     * clone方法。
     * * @param url String类型参数
     *
     * @param targetDir File类型参数
     * @param username  String类型参数
     * @param password  String类型参数
     * @return GitResult<GitRepository>类型返回值
     */
    public GitResult<GitRepository> clone(String url, File targetDir, String username, String password) {
        if (url == null || url.isEmpty()) {
            return GitResult.fail("url 不能为空");
        }
        if (targetDir == null) {
            return GitResult.fail("目标目录不能为空");
        }
        CloneCommand cmd = Git.cloneRepository()
                .setURI(url)
                .setDirectory(targetDir);
        if (username != null && password != null) {
            cmd.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
        }
        try (Git git = cmd.call()) {
            Repository repo = git.getRepository();
            return GitResult.success(new GitRepository(repo.getWorkTree(), repo.getDirectory()));
        } catch (GitAPIException e) {
            return GitResult.fail("克隆失败: " + e.getMessage());
        }
    }

    /**
     * add方法。
     * * @param repo GitRepository类型参数
     *
     * @param patterns String...类型参数
     * @return GitResult<Void>类型返回值
     */
    public GitResult<Void> add(GitRepository repo, String... patterns) {
        try (Git git = openGit(repo)) {
            org.eclipse.jgit.api.AddCommand cmd = git.add();
            if (patterns == null || patterns.length == 0) {
                cmd.addFilepattern(".");
            } else {
                for (String p : patterns) {
                    if (p != null && !p.isEmpty()) {
                        cmd.addFilepattern(p);
                    }
                }
            }
            cmd.call();
            return GitResult.success(null);
        } catch (Exception e) {
            return GitResult.fail("add 失败: " + e.getMessage());
        }
    }

    /**
     * rm方法。
     * * @param repo GitRepository类型参数
     *
     * @param cached   boolean类型参数
     * @param patterns String...类型参数
     * @return GitResult<Void>类型返回值
     */
    public GitResult<Void> rm(GitRepository repo, boolean cached, String... patterns) {
        try (Git git = openGit(repo)) {
            org.eclipse.jgit.api.RmCommand cmd = git.rm();
            if (cached) {
                cmd.setCached(true);
            }
            for (String p : patterns) {
                if (p != null && !p.isEmpty()) {
                    cmd.addFilepattern(p);
                }
            }
            cmd.call();
            return GitResult.success(null);
        } catch (Exception e) {
            return GitResult.fail("rm 失败: " + e.getMessage());
        }
    }

    /**
     * status方法。
     * * @param repo GitRepository类型参数
     *
     * @return GitResult<GitStatus>类型返回值
     */
    public GitResult<GitStatus> status(GitRepository repo) {
        try (Git git = openGit(repo)) {
            Status s = git.status().call();
            GitStatus out = new GitStatus();
            out.setAdded(new ArrayList<>(s.getAdded()));
            out.setChanged(new ArrayList<>(s.getChanged()));
            out.setRemoved(new ArrayList<>(s.getRemoved()));
            out.setUntracked(new ArrayList<>(s.getUntracked()));
            out.setModified(new ArrayList<>(s.getModified()));
            // JGit 5.13 没有 getDeleted()，用 getMissing() 表示"在工作区中消失的文件"
            out.setDeleted(new ArrayList<>(s.getMissing()));
            out.setMissing(new ArrayList<>(s.getMissing()));
            out.setClean(s.isClean());
            ObjectId head = git.getRepository().resolve(Constants.HEAD);
            out.setHeadSha(head == null ? null : head.name());
            String branch = git.getRepository().getBranch();
            out.setBranch(branch);
            // detached 判定：HEAD 是 SHA 而不是 ref
            out.setDetached(branch != null && !branch.startsWith("refs/heads/"));
            return GitResult.success(out);
        } catch (Exception e) {
            return GitResult.fail("status 失败: " + e.getMessage());
        }
    }

    /**
     * commit方法。
     * * @param repo GitRepository类型参数
     *
     * @param author  GitAuthor类型参数
     * @param message String类型参数
     * @param all     boolean类型参数
     * @return GitResult<String>类型返回值
     */
    public GitResult<String> commit(GitRepository repo, GitAuthor author, String message, boolean all) {
        try (Git git = openGit(repo)) {
            org.eclipse.jgit.api.CommitCommand cmd = git.commit();
            if (author != null) {
                PersonIdent ident = author.getWhen() != null
                        ? new PersonIdent(author.getName(), author.getEmail(), author.getWhen(), java.util.TimeZone.getDefault())
                        : new PersonIdent(author.getName(), author.getEmail());
                cmd.setAuthor(ident).setCommitter(ident);
            }
            if (all) {
                cmd.setAll(true);
            }
            RevCommit rev = cmd.setMessage(message).call();
            return GitResult.success(rev.getName());
        } catch (Exception e) {
            return GitResult.fail("commit 失败: " + e.getMessage());
        }
    }

    // ==================== 分支 ====================

    /**
     * reset方法。
     * * @param repo GitRepository类型参数
     *
     * @param ref  String类型参数
     * @param mode String类型参数
     * @return GitResult<Void>类型返回值
     */
    public GitResult<Void> reset(GitRepository repo, String ref, String mode) {
        try (Git git = openGit(repo)) {
            ResetCommand cmd = git.reset();
            if (ref != null) {
                cmd.setRef(ref);
            }
            cmd.setMode(parseResetType(mode));
            cmd.call();
            return GitResult.success(null);
        } catch (Exception e) {
            return GitResult.fail("reset 失败: " + e.getMessage());
        }
    }

    /**
     * log方法。
     * * @param repo GitRepository类型参数
     *
     * @param maxCount int类型参数
     * @return GitResult<List<GitCommit>>类型返回值
     */
    public GitResult<List<GitCommit>> log(GitRepository repo, int maxCount) {
        try (Git git = openGit(repo)) {
            // HEAD 不存在（空仓库）时，JGit log 会抛 NoHeadException。
            // 这里视作"没有提交"，返回空 list。
            ObjectId head = git.getRepository().resolve(Constants.HEAD);
            if (head == null) {
                return GitResult.success(new ArrayList<>());
            }
            LogCommand cmd = git.log();
            if (maxCount > 0) {
                cmd.setMaxCount(maxCount);
            }
            Iterable<RevCommit> commits = cmd.call();
            List<GitCommit> out = new ArrayList<>();
            for (RevCommit rc : commits) {
                out.add(toGitCommit(rc));
            }
            return GitResult.success(out);
        } catch (Exception e) {
            return GitResult.fail("log 失败: " + e.getMessage());
        }
    }

    /**
     * show方法。
     * * @param repo GitRepository类型参数
     *
     * @param ref String类型参数
     * @return GitResult<GitCommit>类型返回值
     */
    public GitResult<GitCommit> show(GitRepository repo, String ref) {
        try (Git git = openGit(repo)) {
            ObjectId oid = git.getRepository().resolve(ref);
            if (oid == null) {
                return GitResult.fail("找不到引用: " + ref);
            }
            try (RevWalk walk = new RevWalk(git.getRepository())) {
                RevCommit rc = walk.parseCommit(oid);
                return GitResult.success(toGitCommit(rc));
            }
        } catch (Exception e) {
            return GitResult.fail("show 失败: " + e.getMessage());
        }
    }

    /**
     * diffWorkTree方法。
     * * @param repo GitRepository类型参数
     *
     * @return List<com.zifang.util.devops.git.operations.core.GitDiffEntry>类型返回值
     */
    public List<com.zifang.util.devops.git.operations.core.GitDiffEntry> diffWorkTree(GitRepository repo) {
        // 对应 `git diff`：工作区 vs 索引（不是 vs HEAD）
        try (Git git = openGit(repo)) {
            Repository r = git.getRepository();
            DirCache dc = r.readDirCache();
            AbstractTreeIterator oldIt = new DirCacheIterator(dc);
            AbstractTreeIterator newIt = new FileTreeIterator(r);
            return scanDiff(r, oldIt, newIt);
        } catch (IOException e) {
            throw new GitException("diff 失败: " + e.getMessage(), e);
        }
    }

    /**
     * diffIndex方法。
     * * @param repo GitRepository类型参数
     *
     * @return List<com.zifang.util.devops.git.operations.core.GitDiffEntry>类型返回值
     */
    public List<com.zifang.util.devops.git.operations.core.GitDiffEntry> diffIndex(GitRepository repo) {
        try (Git git = openGit(repo)) {
            Repository r = git.getRepository();
            ObjectId headCommit = r.resolve(Constants.HEAD);
            if (headCommit == null) {
                return new ArrayList<>();
            }
            ObjectId headTree;
            try (RevWalk walk = new RevWalk(r)) {
                headTree = walk.parseCommit(headCommit).getTree().getId();
            }
            CanonicalTreeParser oldIt = new CanonicalTreeParser();
            try (ObjectReader reader = r.newObjectReader()) {
                oldIt.reset(reader, headTree);
            }
            DirCache dc = r.readDirCache();
            AbstractTreeIterator newIt = new DirCacheIterator(dc);
            return scanDiff(r, oldIt, newIt);
        } catch (IOException e) {
            throw new GitException("diff 失败: " + e.getMessage(), e);
        }
    }

    /**
     * diffRefs方法。
     * * @param repo GitRepository类型参数
     *
     * @param oldRef String类型参数
     * @param newRef String类型参数
     * @return List<com.zifang.util.devops.git.operations.core.GitDiffEntry>类型返回值
     */
    public List<com.zifang.util.devops.git.operations.core.GitDiffEntry> diffRefs(GitRepository repo, String oldRef, String newRef) {
        try (Git git = openGit(repo)) {
            Repository r = git.getRepository();
            ObjectId oldCommit = r.resolve(oldRef);
            ObjectId newCommit = r.resolve(newRef);
            if (oldCommit == null || newCommit == null) {
                throw new GitException("无法解析引用: oldRef=" + oldRef + " newRef=" + newRef);
            }
            ObjectId oldTree;
            ObjectId newTree;
            try (RevWalk walk = new RevWalk(r)) {
                oldTree = walk.parseCommit(oldCommit).getTree().getId();
                newTree = walk.parseCommit(newCommit).getTree().getId();
            }
            CanonicalTreeParser oldIt = new CanonicalTreeParser();
            CanonicalTreeParser newIt = new CanonicalTreeParser();
            try (ObjectReader reader = r.newObjectReader()) {
                oldIt.reset(reader, oldTree);
                newIt.reset(reader, newTree);
            }
            return scanDiff(r, oldIt, newIt);
        } catch (IOException e) {
            throw new GitException("diff 失败: " + e.getMessage(), e);
        }
    }

    /**
     * branchList方法。
     * * @param repo GitRepository类型参数
     *
     * @param type GitBranch.Type类型参数
     * @return GitResult<List<GitBranch>>类型返回值
     */
    public GitResult<List<GitBranch>> branchList(GitRepository repo, GitBranch.Type type) {
        try (Git git = openGit(repo)) {
            ListBranchCommand cmd = git.branchList();
            if (type == GitBranch.Type.REMOTE) {
                cmd = cmd.setListMode(ListBranchCommand.ListMode.REMOTE);
            } else {
                cmd = cmd.setListMode(ListBranchCommand.ListMode.ALL);
            }
            List<Ref> refs = cmd.call();
            String currentFull = git.getRepository().getFullBranch();
            List<GitBranch> out = new ArrayList<>();
            for (Ref ref : refs) {
                GitBranch b = new GitBranch();
                b.setFullName(ref.getName());
                b.setSha(ref.getObjectId() == null ? null : ref.getObjectId().name());
                b.setCurrent(ref.getName().equals(currentFull));
                b.setRemote(ref.getName().startsWith(Constants.R_REMOTES));
                b.setName(extractShortName(ref.getName()));
                out.add(b);
            }
            return GitResult.success(out);
        } catch (Exception e) {
            return GitResult.fail("branchList 失败: " + e.getMessage());
        }
    }

    // ==================== 远程 ====================

    /**
     * branchCreate方法。
     * * @param repo GitRepository类型参数
     *
     * @param name String类型参数
     * @return GitResult<Void>类型返回值
     */
    public GitResult<Void> branchCreate(GitRepository repo, String name) {
        try (Git git = openGit(repo)) {
            git.branchCreate().setName(name).call();
            return GitResult.success(null);
        } catch (Exception e) {
            return GitResult.fail("branchCreate 失败: " + e.getMessage());
        }
    }

    /**
     * branchDelete方法。
     * * @param repo GitRepository类型参数
     *
     * @param name  String类型参数
     * @param force boolean类型参数
     * @return GitResult<Void>类型返回值
     */
    public GitResult<Void> branchDelete(GitRepository repo, String name, boolean force) {
        try (Git git = openGit(repo)) {
            git.branchDelete().setBranchNames(name).setForce(force).call();
            return GitResult.success(null);
        } catch (Exception e) {
            return GitResult.fail("branchDelete 失败: " + e.getMessage());
        }
    }

    /**
     * checkout方法。
     * * @param repo GitRepository类型参数
     *
     * @param ref String类型参数
     * @return GitResult<Void>类型返回值
     */
    public GitResult<Void> checkout(GitRepository repo, String ref) {
        try (Git git = openGit(repo)) {
            git.checkout().setName(ref).call();
            return GitResult.success(null);
        } catch (Exception e) {
            return GitResult.fail("checkout 失败: " + e.getMessage());
        }
    }

    // ==================== 同步 ====================

    /**
     * checkoutNewBranch方法。
     * * @param repo GitRepository类型参数
     *
     * @param name String类型参数
     * @return GitResult<Void>类型返回值
     */
    public GitResult<Void> checkoutNewBranch(GitRepository repo, String name) {
        try (Git git = openGit(repo)) {
            git.checkout().setCreateBranch(true).setName(name).call();
            return GitResult.success(null);
        } catch (Exception e) {
            return GitResult.fail("checkoutNewBranch 失败: " + e.getMessage());
        }
    }

    /**
     * currentBranch方法。
     * * @param repo GitRepository类型参数
     *
     * @return GitResult<String>类型返回值
     */
    public GitResult<String> currentBranch(GitRepository repo) {
        try (Git git = openGit(repo)) {
            String b = git.getRepository().getBranch();
            if (b == null || b.isEmpty()) {
                return GitResult.success(null);
            }
            // getBranch() 在 detached 时会返回 SHA，去掉
            if (b.matches("[0-9a-f]{7,40}")) {
                return GitResult.success(null);
            }
            return GitResult.success(b);
        } catch (Exception e) {
            return GitResult.fail("currentBranch 失败: " + e.getMessage());
        }
    }

    /**
     * remoteList方法。
     * * @param repo GitRepository类型参数
     *
     * @return GitResult<List<GitRemote>>类型返回值
     */
    public GitResult<List<GitRemote>> remoteList(GitRepository repo) {
        try (Git git = openGit(repo)) {
            StoredConfig cfg = git.getRepository().getConfig();
            Set<String> names = cfg.getSubsections("remote");
            List<GitRemote> out = new ArrayList<>();
            for (String n : names) {
                GitRemote g = new GitRemote();
                g.setName(n);
                g.setFetchUrl(cfg.getString("remote", n, "url"));
                g.setPushUrl(cfg.getString("remote", n, "pushurl"));
                out.add(g);
            }
            return GitResult.success(out);
        } catch (Exception e) {
            return GitResult.fail("remoteList 失败: " + e.getMessage());
        }
    }

    // ==================== 合并 / 变基 ====================

    /**
     * remoteAdd方法。
     * * @param repo GitRepository类型参数
     *
     * @param name String类型参数
     * @param url  String类型参数
     * @return GitResult<Void>类型返回值
     */
    public GitResult<Void> remoteAdd(GitRepository repo, String name, String url) {
        try (Git git = openGit(repo)) {
            git.remoteAdd().setName(name).setUri(new URIish(url)).call();
            return GitResult.success(null);
        } catch (Exception e) {
            return GitResult.fail("remoteAdd 失败: " + e.getMessage());
        }
    }

    /**
     * remoteRemove方法。
     * * @param repo GitRepository类型参数
     *
     * @param name String类型参数
     * @return GitResult<Void>类型返回值
     */
    public GitResult<Void> remoteRemove(GitRepository repo, String name) {
        try (Git git = openGit(repo)) {
            git.remoteRemove().setRemoteName(name).call();
            return GitResult.success(null);
        } catch (Exception e) {
            return GitResult.fail("remoteRemove 失败: " + e.getMessage());
        }
    }

    // ==================== Tag ====================

    /**
     * fetch方法。
     * * @param repo GitRepository类型参数
     *
     * @param remote   String类型参数
     * @param username String类型参数
     * @param password String类型参数
     * @return GitResult<FetchResult>类型返回值
     */
    public GitResult<FetchResult> fetch(GitRepository repo, String remote, String username, String password) {
        try (Git git = openGit(repo)) {
            FetchCommand cmd = git.fetch();
            if (remote != null && !remote.isEmpty()) {
                cmd.setRemote(remote);
            }
            if (username != null && password != null) {
                cmd.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
            }
            FetchResult r = cmd.call();
            return GitResult.success(r);
        } catch (Exception e) {
            return GitResult.fail("fetch 失败: " + e.getMessage());
        }
    }

    /**
     * pull方法。
     * * @param repo GitRepository类型参数
     *
     * @param remote   String类型参数
     * @param branch   String类型参数
     * @param username String类型参数
     * @param password String类型参数
     * @return GitResult<PullResult>类型返回值
     */
    public GitResult<PullResult> pull(GitRepository repo, String remote, String branch,
                                      String username, String password) {
        try (Git git = openGit(repo)) {
            // JGit 5.x 的 PullCommand 没有 setBranch/setRefSpec 入口；
            // 直接调用会让 JGit 用仓库配置的 upstream 来 fetch + merge。
            // 如需精确控制 remote/branch，请用 GitClient.pullViaShell。
            PullCommand cmd = git.pull();
            if (remote != null && !remote.isEmpty()) {
                cmd.setRemote(remote);
            }
            if (username != null && password != null) {
                cmd.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
            }
            PullResult r = cmd.call();
            return GitResult.success(r);
        } catch (Exception e) {
            return GitResult.fail("pull 失败: " + e.getMessage());
        }
    }

    /**
     * push方法。
     * * @param repo GitRepository类型参数
     *
     * @param remote   String类型参数
     * @param refspec  String类型参数
     * @param username String类型参数
     * @param password String类型参数
     * @return GitResult<Iterable<PushResult>>类型返回值
     */
    public GitResult<Iterable<PushResult>> push(GitRepository repo, String remote, String refspec,
                                                String username, String password) {
        try (Git git = openGit(repo)) {
            PushCommand cmd = git.push();
            if (remote != null && !remote.isEmpty()) {
                cmd.setRemote(remote);
            }
            if (refspec != null && !refspec.isEmpty()) {
                cmd.setRefSpecs(new RefSpec(refspec));
            }
            if (username != null && password != null) {
                cmd.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
            }
            Iterable<PushResult> r = cmd.call();
            return GitResult.success(r);
        } catch (Exception e) {
            return GitResult.fail("push 失败: " + e.getMessage());
        }
    }

    // ==================== 内部 ====================

    /**
     * merge方法。
     * * @param repo GitRepository类型参数
     *
     * @param ref String类型参数
     * @return GitResult<MergeResult>类型返回值
     */
    public GitResult<MergeResult> merge(GitRepository repo, String ref) {
        try (Git git = openGit(repo)) {
            MergeCommand cmd = git.merge();
            // include 只接受 Ref / AnyObjectId，需要先 resolve
            ObjectId id = git.getRepository().resolve(ref);
            if (id == null) {
                return GitResult.fail("无法解析引用: " + ref);
            }
            cmd.include(id);
            MergeResult r = cmd.call();
            return GitResult.success(r);
        } catch (Exception e) {
            return GitResult.fail("merge 失败: " + e.getMessage());
        }
    }

    /**
     * rebase方法。
     * * @param repo GitRepository类型参数
     *
     * @param upstream String类型参数
     * @return GitResult<RebaseResult>类型返回值
     */
    public GitResult<RebaseResult> rebase(GitRepository repo, String upstream) {
        try (Git git = openGit(repo)) {
            RebaseCommand cmd = git.rebase();
            if (upstream != null) {
                cmd.setUpstream(upstream);
            }
            RebaseResult r = cmd.call();
            return GitResult.success(r);
        } catch (Exception e) {
            return GitResult.fail("rebase 失败: " + e.getMessage());
        }
    }

    /**
     * tagList方法。
     * * @param repo GitRepository类型参数
     *
     * @return GitResult<List<GitTag>>类型返回值
     */
    public GitResult<List<GitTag>> tagList(GitRepository repo) {
        try (Git git = openGit(repo)) {
            List<Ref> refs = git.tagList().call();
            List<GitTag> out = new ArrayList<>();
            for (Ref ref : refs) {
                GitTag t = new GitTag();
                t.setName(extractShortName(ref.getName()));
                t.setSha(ref.getObjectId() == null ? null : ref.getObjectId().name());
                try (RevWalk walk = new RevWalk(git.getRepository())) {
                    RevObject obj = walk.parseAny(ref.getObjectId());
                    if (obj instanceof RevCommit) {
                        RevCommit rc = (RevCommit) obj;
                        if (rc.getFullMessage() != null && !rc.getFullMessage().isEmpty()) {
                            t.setType(GitTag.Type.ANNOTATED);
                            t.setMessage(rc.getShortMessage());
                            t.setTagger(toAuthor(rc.getCommitterIdent()));
                        } else {
                            t.setType(GitTag.Type.LIGHTWEIGHT);
                        }
                    } else {
                        t.setType(GitTag.Type.LIGHTWEIGHT);
                    }
                } catch (Exception ignored) {
                    t.setType(GitTag.Type.LIGHTWEIGHT);
                }
                out.add(t);
            }
            return GitResult.success(out);
        } catch (Exception e) {
            return GitResult.fail("tagList 失败: " + e.getMessage());
        }
    }

    /**
     * tagCreate方法。
     * * @param repo GitRepository类型参数
     *
     * @param name      String类型参数
     * @param message   String类型参数
     * @param annotated boolean类型参数
     * @param ref       String类型参数
     * @return GitResult<Void>类型返回值
     */
    public GitResult<Void> tagCreate(GitRepository repo, String name, String message, boolean annotated, String ref) {
        try (Git git = openGit(repo)) {
            TagCommand cmd = git.tag().setName(name);
            if (annotated) {
                cmd.setMessage(message == null ? "" : message);
            }
            if (ref != null) {
                // TagCommand.setObjectId 需要 RevObject（JGit 5.13）
                try (RevWalk walk = new RevWalk(git.getRepository())) {
                    ObjectId oid = git.getRepository().resolve(ref);
                    if (oid == null) {
                        return GitResult.fail("无法解析引用: " + ref);
                    }
                    RevObject obj = walk.parseAny(oid);
                    cmd.setObjectId(obj);
                }
            }
            cmd.call();
            return GitResult.success(null);
        } catch (Exception e) {
            return GitResult.fail("tagCreate 失败: " + e.getMessage());
        }
    }

    /**
     * tagDelete方法。
     * * @param repo GitRepository类型参数
     *
     * @param name String类型参数
     * @return GitResult<Void>类型返回值
     */
    public GitResult<Void> tagDelete(GitRepository repo, String name) {
        try (Git git = openGit(repo)) {
            git.tagDelete().setTags(name).call();
            return GitResult.success(null);
        } catch (Exception e) {
            return GitResult.fail("tagDelete 失败: " + e.getMessage());
        }
    }
}
