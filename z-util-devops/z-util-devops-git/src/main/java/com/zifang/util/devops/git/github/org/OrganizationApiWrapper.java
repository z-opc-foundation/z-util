package com.zifang.util.devops.git.github.org;

import com.zifang.util.devops.git.github.http.GithubHttpClient;

import java.io.IOException;
import java.util.List;

/**
 * GitHub Organization API 包装（v0 全未实跑；续接清单见
 * {@code _doc/001_arch/github-api-migration.md} §org）。
 */
public class OrganizationApiWrapper {

    private final GithubHttpClient client;
    private final String org;

    public OrganizationApiWrapper(GithubHttpClient client) {
        this(client, null);
    }

    public OrganizationApiWrapper(GithubHttpClient client, String org) {
        this.client = client;
        this.org = org;
    }

    public String get(String org) throws IOException {
        throw notImpl("org.get");
    }

    public String get() throws IOException {
        throw notImpl("org.get(this)");
    }

    public List<String> listMembers() throws IOException {
        throw notImpl("org.listMembers");
    }

    public List<String> listRepos() throws IOException {
        throw notImpl("org.listRepos");
    }

    public List<String> listTeams() throws IOException {
        throw notImpl("org.listTeams");
    }

    private static UnsupportedOperationException notImpl(String key) {
        return new UnsupportedOperationException(
                "OrganizationApiWrapper." + key + " 未迁移实现，详见 _doc/001_arch/github-api-migration.md");
    }
}
