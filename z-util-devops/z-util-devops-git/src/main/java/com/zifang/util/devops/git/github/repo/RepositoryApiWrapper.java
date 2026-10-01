package com.zifang.util.devops.git.github.repo;

import com.zifang.util.devops.git.github.http.GithubHttpClient;
import com.zifang.util.devops.git.github.model.Branch;
import com.zifang.util.devops.git.github.model.Repository;
import com.zifang.util.devops.git.github.model.User;
import com.zifang.util.json.JsonUtil;
import com.zifang.util.json.model.JsonArray;
import com.zifang.util.json.model.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * GitHub Repository API 包装。
 *
 * <p>v0 切片 → 全量收口：11 个方法全部实跑 okhttp REST。续接进度见
 * {@code _doc/001_arch/github-api-migration.md} §repo。
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

    // ==================== Get / Branches ====================

    /** GET /repos/{owner}/{repo} */
    public Repository get(String owner, String repo) throws IOException {
        JsonObject body = client.getJsonObject("/repos/" + owner + "/" + repo);
        return Repository.fromJson(body);
    }

    public Repository get() throws IOException {
        return get(this.owner, this.repo);
    }

    /** GET /repos/{owner}/{repo}/branches */
    public List<String> listBranches() throws IOException {
        JsonArray arr = client.getJsonArray("/repos/" + this.owner + "/" + this.repo + "/branches");
        List<String> names = new ArrayList<>(arr.size());
        for (int i = 0; i < arr.size(); i++) {
            names.add(Branch.fromJson(arr.getJsonObject(i)).getName());
        }
        return names;
    }

    // ==================== CRUD ====================

    /** POST /user/repos */
    public Repository create(String name, String description, boolean isPrivate) throws IOException {
        JsonObject body = new JsonObject();
        body.put("name", name);
        body.put("description", description);
        body.put("private", isPrivate);
        return Repository.fromJson(client.postJson("/user/repos", body.toString()));
    }

    /** POST /orgs/{org}/repos */
    public Repository createOrgRepo(String org, String name, String description, boolean isPrivate) throws IOException {
        JsonObject body = new JsonObject();
        body.put("name", name);
        body.put("description", description);
        body.put("private", isPrivate);
        return Repository.fromJson(client.postJson("/orgs/" + org + "/repos", body.toString()));
    }

    /** DELETE /repos/{owner}/{repo} */
    public void delete(String owner, String repo) throws IOException {
        client.delete("/repos/" + owner + "/" + repo);
    }

    public void delete() throws IOException {
        delete(this.owner, this.repo);
    }

    // ==================== User Repos ====================

    /** GET /users/{username}/repos?per_page=100 */
    public List<Repository> listUserRepos(String username) throws IOException {
        JsonArray arr = client.getJsonArray("/users/" + username + "/repos?per_page=100");
        return toRepos(arr);
    }

    /** GET /user/repos?per_page=100 */
    public List<Repository> listMyRepos() throws IOException {
        JsonArray arr = client.getJsonArray("/user/repos?per_page=100");
        return toRepos(arr);
    }

    // ==================== Fork / Stargazers ====================

    /** POST /repos/{owner}/{repo}/forks */
    public Repository fork() throws IOException {
        return Repository.fromJson(client.postJson(
                "/repos/" + this.owner + "/" + this.repo + "/forks", "{}"));
    }

    /** GET /repos/{owner}/{repo}/forks */
    public List<Repository> listForks() throws IOException {
        JsonArray arr = client.getJsonArray(
                "/repos/" + this.owner + "/" + this.repo + "/forks");
        return toRepos(arr);
    }

    /** GET /repos/{owner}/{repo}/stargazers */
    public List<User> listStargazers() throws IOException {
        JsonArray arr = client.getJsonArray(
                "/repos/" + this.owner + "/" + this.repo + "/stargazers");
        List<User> users = new ArrayList<>(arr.size());
        for (int i = 0; i < arr.size(); i++) {
            users.add(User.fromJson(arr.getJsonObject(i)));
        }
        return users;
    }

    // ==================== Search ====================

    /** GET /search/repositories?q=... */
    public List<Repository> search(String keyword) throws IOException {
        return search(keyword, null);
    }

    /** GET /search/repositories?q=...+language:... */
    public List<Repository> search(String keyword, String language) throws IOException {
        String q = keyword == null ? "" : keyword;
        if (language != null && !language.isEmpty()) {
            q = q + "+language:" + language;
        }
        JsonObject body = client.getJsonObject(
                "/search/repositories?q=" + q + "&per_page=100");
        JsonArray items = body.getJsonArray("items");
        return toRepos(items);
    }

    // ==================== Info / Description 等便利 getter ====================

    /** v0 兼容：原 {@code info()} 等价于 {@code get()}。 */
    public Repository info() throws IOException {
        return get();
    }

    public String getDescription() throws IOException {
        return get().getDescription();
    }

    public String getDefaultBranch() throws IOException {
        return get().getDefaultBranch();
    }

    public String getLanguage() throws IOException {
        return get().getLanguage();
    }

    public int getStargazersCount() throws IOException {
        return get().getStargazersCount();
    }

    public int getForksCount() throws IOException {
        return get().getForksCount();
    }

    private static List<Repository> toRepos(JsonArray arr) {
        if (arr == null) {
            return new ArrayList<>();
        }
        List<Repository> out = new ArrayList<>(arr.size());
        for (int i = 0; i < arr.size(); i++) {
            out.add(Repository.fromJson(arr.getJsonObject(i)));
        }
        return out;
    }
}
