package com.zifang.util.devops.git.operations;

import com.zifang.util.devops.git.operations.core.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import static org.junit.Assert.*;

/**
 * GitClient 单元测试：覆盖 init / add / commit / log / branch / status / tag 等核心操作
 * <p>
 * 使用 JUnit 4。每个测试在临时目录里 init 一个新仓库，互不干扰。
 */

/**
 * GitClientTest类。
 */
public class GitClientTest {

    private File workDir;
    private GitRepository repo;

    private static File createTempDir(String prefix) throws Exception {
        File dir = File.createTempFile(prefix, "");
        if (!dir.delete() || !dir.mkdirs()) {
            throw new IllegalStateException("无法创建临时目录: " + dir);
        }
        return dir;
    }

    private static void writeFile(File f, String content) throws Exception {
        try (FileWriter w = new FileWriter(f)) {
            w.write(content);
        }
    }

    private static void deleteRecursive(File f) {
        if (f == null || !f.exists()) {
            return;
        }
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            if (children != null) {
                for (File c : children) {
                    deleteRecursive(c);
                }
            }
        }
        f.delete();
    }

    @Before
    /**
     * setUp方法。
     */
    public void setUp() throws Exception {
        workDir = createTempDir("git-client-test");
        GitResult<GitRepository> r = GitClient.init(workDir);
        assertTrue("init 失败: " + r.getMessage(), r.isSuccess());
        repo = r.getData();
        assertNotNull(repo);
        assertEquals(workDir.getAbsolutePath(), repo.getPath());
    }

    @After
    /**
     * tearDown方法。
     */
    public void tearDown() throws Exception {
        deleteRecursive(workDir);
    }

    @Test
    /**
     * testInit方法。
     */
    public void testInit() {
        assertNotNull(repo.getGitDir());
        assertTrue(repo.getGitDir().exists());
        assertTrue(new File(repo.getGitDir(), "HEAD").exists());
    }

    @Test
    /**
     * testOpenAfterInit方法。
     */
    public void testOpenAfterInit() {
        GitResult<GitRepository> r = GitClient.open(workDir);
        assertTrue(r.isSuccess());
        assertNotNull(r.getData());
    }

    @Test
    /**
     * testAddAndCommit方法。
     */
    public void testAddAndCommit() throws Exception {
        File f = new File(workDir, "hello.txt");
        writeFile(f, "hello world\n");
        // add
        GitResult<Void> addR = GitClient.add(repo, "hello.txt");
        assertTrue("add 失败: " + addR.getMessage(), addR.isSuccess());
        // status 应显示有暂存
        GitResult<GitStatus> st = GitClient.status(repo);
        assertTrue(st.isSuccess());
        assertFalse("暂存后工作区应不干净", st.getData().isClean());
        assertTrue(st.getData().getAdded().contains("hello.txt"));

        // commit
        GitAuthor me = new GitAuthor("tester", "tester@example.com");
        GitResult<String> cmt = GitClient.commit(repo, me, "initial commit");
        assertTrue("commit 失败: " + cmt.getMessage() + " | " + cmt.getStderr(), cmt.isSuccess());
        assertNotNull(cmt.getData());
        assertTrue(cmt.getData().matches("[0-9a-f]{40}"));

        // 提交后 status 应 clean
        GitResult<GitStatus> st2 = GitClient.status(repo);
        assertTrue(st2.getData().isClean());
    }

    @Test
    /**
     * testLog方法。
     */
    public void testLog() throws Exception {
        // 没有提交时 log 应返回空
        GitResult<List<GitCommit>> empty = GitClient.log(repo, 10);
        assertTrue(empty.isSuccess());
        assertTrue(empty.getData().isEmpty());

        // 提交两次
        commitFile("a.txt", "A", "commit 1");
        commitFile("b.txt", "B", "commit 2");

        GitResult<List<GitCommit>> log = GitClient.log(repo, 10);
        assertTrue(log.isSuccess());
        List<GitCommit> commits = log.getData();
        assertEquals(2, commits.size());
        assertEquals("commit 2", commits.get(0).getShortMessage());
        assertEquals("commit 1", commits.get(1).getShortMessage());
        assertNotNull(commits.get(0).getAuthor());
        assertEquals("tester", commits.get(0).getAuthor().getName());
    }

    @Test
    /**
     * testShow方法。
     */
    public void testShow() throws Exception {
        commitFile("x.txt", "x", "the commit");
        GitResult<List<GitCommit>> log = GitClient.log(repo, 1);
        String sha = log.getData().get(0).getSha();

        GitResult<GitCommit> show = GitClient.show(repo, sha);
        assertTrue(show.isSuccess());
        assertEquals(sha, show.getData().getSha());
        assertEquals("the commit", show.getData().getShortMessage());
    }

    @Test
    /**
     * testBranch方法。
     */
    public void testBranch() throws Exception {
        commitFile("init.txt", "init", "first");
        // 初始应该有 HEAD 指向一个分支（init 后的默认分支名是 master 或 main）
        GitResult<String> cur = GitClient.currentBranch(repo);
        assertTrue(cur.isSuccess());
        assertNotNull(cur.getData());

        // 创建并切换到 feature 分支
        GitResult<Void> create = GitClient.branchCreate(repo, "feature");
        assertTrue(create.isSuccess());
        GitResult<Void> checkout = GitClient.checkout(repo, "feature");
        assertTrue(checkout.isSuccess());

        GitResult<String> cur2 = GitClient.currentBranch(repo);
        assertTrue(cur2.isSuccess());
        assertEquals("feature", cur2.getData());

        // 列出本地分支
        GitResult<List<GitBranch>> local = GitClient.branchListLocal(repo);
        assertTrue(local.isSuccess());
        boolean hasFeature = false;
        for (GitBranch b : local.getData()) {
            if ("feature".equals(b.getName())) {
                hasFeature = true;
                assertTrue(b.isCurrent());
            }
        }
        assertTrue(hasFeature);

        // 切回去再删
        GitClient.checkout(repo, cur.getData());
        GitResult<Void> del = GitClient.branchDelete(repo, "feature", false);
        assertTrue(del.isSuccess());
    }

    @Test
    /**
     * testCheckoutNewBranch方法。
     */
    public void testCheckoutNewBranch() throws Exception {
        commitFile("init.txt", "init", "first");
        GitResult<Void> r = GitClient.checkoutNewBranch(repo, "dev");
        assertTrue(r.isSuccess());
        assertEquals("dev", GitClient.currentBranch(repo).getData());
    }

    @Test
    /**
     * testTag方法。
     */
    public void testTag() throws Exception {
        commitFile("init.txt", "init", "first");
        GitResult<Void> lt = GitClient.tagCreate(repo, "v0.1");
        assertTrue(lt.isSuccess());

        GitResult<Void> at = GitClient.tagCreateAnnotated(repo, "v0.2", "second release");
        assertTrue(at.isSuccess());

        GitResult<List<GitTag>> list = GitClient.tagList(repo);
        assertTrue(list.isSuccess());
        assertEquals(2, list.getData().size());

        // 删除
        GitResult<Void> del = GitClient.tagDelete(repo, "v0.1");
        assertTrue(del.isSuccess());
        assertEquals(1, GitClient.tagList(repo).getData().size());
    }

    @Test
    /**
     * testResetSoft方法。
     */
    public void testResetSoft() throws Exception {
        commitFile("a.txt", "a", "first");
        commitFile("b.txt", "b", "second");
        GitResult<List<GitCommit>> before = GitClient.log(repo, 10);
        assertEquals(2, before.getData().size());

        // soft reset 到 HEAD~1：b 变成已暂存
        GitResult<Void> r = GitClient.reset(repo, "HEAD~1", "soft");
        assertTrue("reset 失败: " + r.getMessage(), r.isSuccess());
        assertEquals(1, GitClient.log(repo, 10).getData().size());
    }

    @Test
    /**
     * testDiff方法。
     */
    public void testDiff() throws Exception {
        commitFile("a.txt", "line1\n", "first");
        // 修改并 add
        writeFile(new File(workDir, "a.txt"), "line1\nline2\n");
        GitClient.add(repo, "a.txt");

        // diffCached：HEAD vs 索引（已暂存但未提交，应有变化）
        GitResult<List<com.zifang.util.devops.git.operations.core.GitDiffEntry>> cached = GitClient.diffCached(repo);
        assertTrue(cached.isSuccess());
        assertFalse(cached.getData().isEmpty());

        // diff：工作区 vs 索引（已暂存，工作区与索引一致，应为空）
        GitResult<List<com.zifang.util.devops.git.operations.core.GitDiffEntry>> wt = GitClient.diff(repo);
        assertTrue(wt.isSuccess());
        assertTrue(wt.getData().isEmpty());

        // diffRefs：HEAD vs HEAD 应为空
        GitResult<List<com.zifang.util.devops.git.operations.core.GitDiffEntry>> same = GitClient.diff(repo, "HEAD", "HEAD");
        assertTrue(same.isSuccess());
        assertTrue(same.getData().isEmpty());
    }

    @Test
    /**
     * testRemoteAddListRemove方法。
     */
    public void testRemoteAddListRemove() throws Exception {
        GitResult<Void> add = GitClient.remoteAdd(repo, "origin", "https://example.com/foo.git");
        assertTrue(add.isSuccess());

        GitResult<List<com.zifang.util.devops.git.operations.core.GitRemote>> list = GitClient.remoteList(repo);
        assertTrue(list.isSuccess());
        assertEquals(1, list.getData().size());
        assertEquals("origin", list.getData().get(0).getName());

        GitResult<Void> rm = GitClient.remoteRemove(repo, "origin");
        assertTrue(rm.isSuccess());
        assertEquals(0, GitClient.remoteList(repo).getData().size());
    }

    @Test
    /**
     * testCommitWithEmptyMessage方法。
     */
    public void testCommitWithEmptyMessage() {
        GitResult<String> r = GitClient.commit(repo, null, "", false);
        assertFalse(r.isSuccess());
        assertTrue(r.getMessage().contains("message"));
    }

    // ==================== 工具方法 ====================

    @Test
    /**
     * testOpenNonExistent方法。
     */
    public void testOpenNonExistent() {
        GitResult<GitRepository> r = GitClient.open(new File("/tmp/this/does/not/exist/" + System.nanoTime()));
        assertFalse(r.isSuccess());
    }

    @Test
    /**
     * testShellAvailable方法。
     */
    public void testShellAvailable() {
        // 不强求 shell 可用，方法本身不能抛异常
        boolean available = GitClient.isShellAvailable();
        // 这里只打印，由 CI 决定是否断言
        assertTrue(available == true || available == false);
    }

    @Test
    /**
     * testCleanDryRun方法。
     */
    public void testCleanDryRun() throws Exception {
        // 未跟踪文件
        writeFile(new File(workDir, "untracked.txt"), "junk");
        GitResult<String> r = GitClient.clean(repo, false, false);
        assertTrue("clean dry-run 失败: " + r.getStderr(), r.isSuccess());
        // 仍然存在（dry-run 不会删）
        assertTrue(new File(workDir, "untracked.txt").exists());
    }

    private void commitFile(String name, String content, String message) throws Exception {
        writeFile(new File(workDir, name), content);
        GitClient.add(repo, name);
        GitResult<String> r = GitClient.commit(repo, new GitAuthor("tester", "tester@example.com"), message);
        assertTrue("commit 失败: " + r.getMessage() + " | " + r.getStderr(), r.isSuccess());
    }
}
