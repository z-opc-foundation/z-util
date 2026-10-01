package com.zifang.util.devops.git.github.repo;

import com.zifang.util.devops.git.github.http.GithubHttpClient;
import com.zifang.util.devops.git.github.model.Branch;
import com.zifang.util.devops.git.github.model.Repository;
import com.zifang.util.json.model.JsonArray;
import com.zifang.util.json.model.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * GitHub Repository API 包装。
 *
 * <p>v0 切片：仅 {@link #get(String, String)} 与 {@link #listBranches()} 真跑 okhttp REST，
 * 其余方法抛 {@link UnsupportedOperationException}，续接清单见
 * {@code _doc/001_arch/github-api-migration.md} 的 §repo 表格。
 */
public class RepositoryApiWrapper {

    private final GithubHttpClient client;
    private String owner;
    private String repo;

    public RepositoryApiWrapper(GithubHttpClient client) {
        this.client = client;
    }

    public RepositoryApiWrapper(GithubHttpClient client, String owner, String repo) {
        this.client = client;
        this.owner = owner;
        this.repo = repo;
    }

    public RepositoryApiWrapper withRepo(String owner, String repo) {
        this.owner = owner;
        this.repo = repo;
        return this;
    }

    // ==================== 实跑 v0 ====================

    /** GET /repos/{owner}/{repo} → Repository POJO。 */
    public Repository get(String owner, String repo) throws IOException {
        JsonObject body = client.getJsonObject("/repos/" + owner + "/" + repo);
        return Repository.fromJson(body);
    }

    public Repository get() throws IOException {
        return get(this.owner, this.repo);
    }

    /** GET /repos/{owner}/{repo}/branches → 分支名列表。 */
    public List<String> listBranches() throws IOException {
        JsonArray arr = client.getJsonArray("/repos/" + this.owner + "/" + this.repo + "/branches");
        List<String> names = new ArrayList<>(arr.size());
        for (int i = 0; i < arr.size(); i++) {
            names.add(Branch.fromJson(arr.getJsonObject(i)).getName());
        }
        return names;
    }

    // ==================== TODO：见迁移 doc §repo ====================

    public Repository create(String name, String description, boolean isPrivate) throws IOException {
        throw notImpl("repo.create");
    }

    public Repository createOrgRepo(String org, String name, String description, boolean isPrivate) throws IOException {
        throw notImpl("repo.createOrgRepo");
    }

    public void delete(String owner, String repo) throws IOException {
        throw notImpl("repo.delete");
    }

    public void delete() throws IOException {
        throw notImpl("repo.delete");
    }

    public String getDescription() throws IOException {
        throw notImpl("repo.getDescription");
    }

    public String getDefaultBranch() throws IOException {
        throw notImpl("repo.getDefaultBranch");
    }

    public String getLanguage() throws IOException {
        throw notImpl("repo.getLanguage");
    }

    public int getStargazersCount() throws IOException {
        throw notImpl("repo.getStargazersCount");
    }

    public int getForksCount() throws IOException {
        throw notImpl("repo.getForksCount");
    }

    public List<String> listUserRepos(String username) throws IOException {
        throw notImpl("repo.listUserRepos");
    }

    public List<String> listMyRepos() throws IOException {
        throw notImpl("repo.listMyRepos");
    }

    public Repository fork() throws IOException {
        throw notImpl("repo.fork");
    }

    public List<String> listForks() throws IOException {
        throw notImpl("repo.listForks");
    }

    public List<String> listStargazers() throws IOException {
        throw notImpl("repo.listStargazers");
    }

    public List<String> search(String keyword) throws IOException {
        throw notImpl("repo.search");
    }

    public List<String> search(String keyword, String language) throws IOException {
        throw notImpl("repo.search(lang)");
    }

    /** v0 兼容：原 {@code info()} 走 {@code Repository POJO}。 */
    public Repository info() throws IOException {
        return get();
    }

    private static UnsupportedOperationException notImpl(String key) {
        return new UnsupportedOperationException(
                "RepositoryApiWrapper." + key + " 未迁移实现，详见 _doc/001_arch/github-api-migration.md");
    }
}
