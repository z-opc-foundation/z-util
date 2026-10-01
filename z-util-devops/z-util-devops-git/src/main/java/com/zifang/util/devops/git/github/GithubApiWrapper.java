package com.zifang.util.devops.git.github;

import com.zifang.util.devops.git.github.action.ActionApiWrapper;
import com.zifang.util.devops.git.github.holder.GithubApiHolder;
import com.zifang.util.devops.git.github.issue.IssueApiWrapper;
import com.zifang.util.devops.git.github.org.OrganizationApiWrapper;
import com.zifang.util.devops.git.github.pr.PullRequestApiWrapper;
import com.zifang.util.devops.git.github.release.ReleaseApiWrapper;
import com.zifang.util.devops.git.github.repo.RepositoryApiWrapper;
import com.zifang.util.devops.git.github.user.UserApiWrapper;
import org.kohsuke.github.GitHub;

/**
 * GitHub API 总入口，统一提供各子模块的封装接口。
 * <p>
 * 入口类，提供获取各种GitHub API封装对象的静态方法，
 * 包括RepositoryApiWrapper、IssueApiWrapper、PullRequestApiWrapper等。
 *
 * @author zifang
 * @version 1.0.0
 */
public class GithubApiWrapper {

    private GithubApiWrapper() {
    }

    private static GitHub github() {
        return GithubApiHolder.getInstance().getGithub();
    }

    // ---- Repository ----

    /**
     * 获取仓库 API 封装（不指定仓库）
     *
     * @return RepositoryApiWrapper 实例
     */
    public static RepositoryApiWrapper repo() {
        return new RepositoryApiWrapper(github());
    }

    /**
     * 获取仓库 API 封装（指定仓库）
     *
     * @param owner 仓库所有者
     * @param repo  仓库名称
     * @return RepositoryApiWrapper 实例
     */
    public static RepositoryApiWrapper repo(String owner, String repo) {
        return new RepositoryApiWrapper(github(), owner, repo);
    }

    // ---- Issue ----

    /**
     * 获取 Issue API 封装（不指定仓库）
     *
     * @return IssueApiWrapper 实例
     */
    public static IssueApiWrapper issue() {
        return new IssueApiWrapper(github());
    }

    /**
     * 获取 Issue API 封装（指定仓库）
     *
     * @param owner 仓库所有者
     * @param repo  仓库名称
     * @return IssueApiWrapper 实例
     */
    public static IssueApiWrapper issue(String owner, String repo) {
        return new IssueApiWrapper(github(), owner, repo);
    }

    // ---- Pull Request ----

    /**
     * 获取 Pull Request API 封装（不指定仓库）
     *
     * @return PullRequestApiWrapper 实例
     */
    public static PullRequestApiWrapper pr() {
        return new PullRequestApiWrapper(github());
    }

    /**
     * 获取 Pull Request API 封装（指定仓库）
     *
     * @param owner 仓库所有者
     * @param repo  仓库名称
     * @return PullRequestApiWrapper 实例
     */
    public static PullRequestApiWrapper pr(String owner, String repo) {
        return new PullRequestApiWrapper(github(), owner, repo);
    }

    // ---- Release ----

    /**
     * 获取 Release API 封装（不指定仓库）
     *
     * @return ReleaseApiWrapper 实例
     */
    public static ReleaseApiWrapper release() {
        return new ReleaseApiWrapper(github());
    }

    /**
     * 获取 Release API 封装（指定仓库）
     *
     * @param owner 仓库所有者
     * @param repo  仓库名称
     * @return ReleaseApiWrapper 实例
     */
    public static ReleaseApiWrapper release(String owner, String repo) {
        return new ReleaseApiWrapper(github(), owner, repo);
    }

    // ---- GitHub Actions ----

    /**
     * 获取 GitHub Actions API 封装（不指定仓库）
     *
     * @return ActionApiWrapper 实例
     */
    public static ActionApiWrapper action() {
        return new ActionApiWrapper(github());
    }

    /**
     * 获取 GitHub Actions API 封装（指定仓库）
     *
     * @param owner 仓库所有者
     * @param repo  仓库名称
     * @return ActionApiWrapper 实例
     */
    public static ActionApiWrapper action(String owner, String repo) {
        return new ActionApiWrapper(github(), owner, repo);
    }

    // ---- User ----

    /**
     * 获取用户 API 封装
     *
     * @return UserApiWrapper 实例
     */
    public static UserApiWrapper user() {
        return new UserApiWrapper(github());
    }

    // ---- Organization ----

    /**
     * 获取组织 API 封装（不指定组织）
     *
     * @return OrganizationApiWrapper 实例
     */
    public static OrganizationApiWrapper org() {
        return new OrganizationApiWrapper(github());
    }

    /**
     * 获取组织 API 封装（指定组织）
     *
     * @param orgName 组织名称
     * @return OrganizationApiWrapper 实例
     */
    public static OrganizationApiWrapper org(String orgName) {
        return new OrganizationApiWrapper(github(), orgName);
    }
}
