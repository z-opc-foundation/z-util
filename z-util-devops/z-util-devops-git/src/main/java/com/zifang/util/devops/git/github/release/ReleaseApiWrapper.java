package com.zifang.util.devops.git.github.release;

import com.zifang.util.devops.git.github.http.GithubHttpClient;

import java.io.IOException;
import java.util.List;

/**
 * GitHub Release API 包装（v0 全未实跑；续接清单见
 * {@code _doc/001_arch/github-api-migration.md} §release）。
 */
public class ReleaseApiWrapper {

    private final GithubHttpClient client;
    private final String owner;
    private final String repo;

    public ReleaseApiWrapper(GithubHttpClient client) {
        this(client, null, null);
    }

    public ReleaseApiWrapper(GithubHttpClient client, String owner, String repo) {
        this.client = client;
        this.owner = owner;
        this.repo = repo;
    }

    public List<String> list() throws IOException {
        throw notImpl("release.list");
    }

    public String getLatest() throws IOException {
        throw notImpl("release.getLatest");
    }

    public String getByTag(String tag) throws IOException {
        throw notImpl("release.getByTag");
    }

    public String create(String tag, String name, String body) throws IOException {
        throw notImpl("release.create");
    }

    public void delete(long id) throws IOException {
        throw notImpl("release.delete");
    }

    private static UnsupportedOperationException notImpl(String key) {
        return new UnsupportedOperationException(
                "ReleaseApiWrapper." + key + " 未迁移实现，详见 _doc/001_arch/github-api-migration.md");
    }
}
