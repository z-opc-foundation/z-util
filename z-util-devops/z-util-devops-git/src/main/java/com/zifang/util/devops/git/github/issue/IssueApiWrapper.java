package com.zifang.util.devops.git.github.issue;

import com.zifang.util.devops.git.github.http.GithubHttpClient;

import java.io.IOException;
import java.util.List;

/**
 * GitHub Issue API 包装（v0 全未实跑；续接清单见
 * {@code _doc/001_arch/github-api-migration.md} §issue）。
 */
public class IssueApiWrapper {

    private final GithubHttpClient client;
    private final String owner;
    private final String repo;

    public IssueApiWrapper(GithubHttpClient client) {
        this(client, null, null);
    }

    public IssueApiWrapper(GithubHttpClient client, String owner, String repo) {
        this.client = client;
        this.owner = owner;
        this.repo = repo;
    }

    public List<String> list(String state) throws IOException {
        throw notImpl("issue.list");
    }

    public String get(int number) throws IOException {
        throw notImpl("issue.get");
    }

    public String create(String title, String body) throws IOException {
        throw notImpl("issue.create");
    }

    public String update(int number, String title, String body) throws IOException {
        throw notImpl("issue.update");
    }

    public void close(int number) throws IOException {
        throw notImpl("issue.close");
    }

    public void reopen(int number) throws IOException {
        throw notImpl("issue.reopen");
    }

    public List<String> listComments(int number) throws IOException {
        throw notImpl("issue.listComments");
    }

    public String addComment(int number, String body) throws IOException {
        throw notImpl("issue.addComment");
    }

    private static UnsupportedOperationException notImpl(String key) {
        return new UnsupportedOperationException(
                "IssueApiWrapper." + key + " 未迁移实现，详见 _doc/001_arch/github-api-migration.md");
    }
}
