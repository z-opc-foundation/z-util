package com.zifang.util.devops.git.github;

import com.zifang.util.devops.git.github.action.ActionApiWrapper;
import com.zifang.util.devops.git.github.holder.GithubApiHolder;
import com.zifang.util.devops.git.github.http.GithubHttpClient;
import com.zifang.util.devops.git.github.issue.IssueApiWrapper;
import com.zifang.util.devops.git.github.org.OrganizationApiWrapper;
import com.zifang.util.devops.git.github.pr.PullRequestApiWrapper;
import com.zifang.util.devops.git.github.release.ReleaseApiWrapper;
import com.zifang.util.devops.git.github.repo.RepositoryApiWrapper;
import com.zifang.util.devops.git.github.user.UserApiWrapper;

/**
 * GitHub API 总入口，静态工厂方法返回各子模块 wrapper。
 *
 * <p>签名与 v0 切前一致：tests 只做 {@code assertNotNull(factory())}，所有现有
 * {@code GithubApiWrapperTest} 保持绿。
 */
public class GithubApiWrapper {

    private GithubApiWrapper() {
    }

    private static GithubHttpClient github() {
        return GithubApiHolder.getInstance().getGithub();
    }

    public static RepositoryApiWrapper repo() {
        return new RepositoryApiWrapper(github());
    }

    public static RepositoryApiWrapper repo(String owner, String repo) {
        return new RepositoryApiWrapper(github(), owner, repo);
    }

    public static IssueApiWrapper issue() {
        return new IssueApiWrapper(github());
    }

    public static IssueApiWrapper issue(String owner, String repo) {
        return new IssueApiWrapper(github(), owner, repo);
    }

    public static PullRequestApiWrapper pr() {
        return new PullRequestApiWrapper(github());
    }

    public static PullRequestApiWrapper pr(String owner, String repo) {
        return new PullRequestApiWrapper(github(), owner, repo);
    }

    public static ReleaseApiWrapper release() {
        return new ReleaseApiWrapper(github());
    }

    public static ReleaseApiWrapper release(String owner, String repo) {
        return new ReleaseApiWrapper(github(), owner, repo);
    }

    public static ActionApiWrapper action() {
        return new ActionApiWrapper(github());
    }

    public static ActionApiWrapper action(String owner, String repo) {
        return new ActionApiWrapper(github(), owner, repo);
    }

    public static UserApiWrapper user() {
        return new UserApiWrapper(github());
    }

    public static OrganizationApiWrapper org() {
        return new OrganizationApiWrapper(github());
    }

    public static OrganizationApiWrapper org(String orgName) {
        return new OrganizationApiWrapper(github(), orgName);
    }
}
