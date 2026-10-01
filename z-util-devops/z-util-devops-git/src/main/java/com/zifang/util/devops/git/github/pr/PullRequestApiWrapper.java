package com.zifang.util.devops.git.github.pr;

import com.zifang.util.devops.git.github.http.GithubHttpClient;

import java.io.IOException;
import java.util.List;

/**
 * GitHub Pull Request API 包装（v0 全未实跑；续接清单见
 * {@code _doc/001_arch/github-api-migration.md} §pr）。
 */
public class PullRequestApiWrapper {

    private final GithubHttpClient client;
    private final String owner;
    private final String repo;

    public PullRequestApiWrapper(GithubHttpClient client) {
        this(client, null, null);
    }

    public PullRequestApiWrapper(GithubHttpClient client, String owner, String repo) {
        this.client = client;
        this.owner = owner;
        this.repo = repo;
    }

    public List<String> list(String state) throws IOException {
        throw notImpl("pr.list");
    }

    public String get(int number) throws IOException {
        throw notImpl("pr.get");
    }

    public String create(String title, String head, String base) throws IOException {
        throw notImpl("pr.create");
    }

    public String merge(int number, String commitMessage) throws IOException {
        throw notImpl("pr.merge");
    }

    public void close(int number) throws IOException {
        throw notImpl("pr.close");
    }

    public List<String> listReviews(int number) throws IOException {
        throw notImpl("pr.listReviews");
    }

    public String submitReview(int number, String body, String state) throws IOException {
        throw notImpl("pr.submitReview");
    }

    public List<String> listFiles(int number) throws IOException {
        throw notImpl("pr.listFiles");
    }

    private static UnsupportedOperationException notImpl(String key) {
        return new UnsupportedOperationException(
                "PullRequestApiWrapper." + key + " 未迁移实现，详见 _doc/001_arch/github-api-migration.md");
    }
}
