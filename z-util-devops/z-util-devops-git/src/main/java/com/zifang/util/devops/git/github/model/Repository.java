package com.zifang.util.devops.git.github.model;

import com.zifang.util.json.model.JsonObject;

/**
 * GitHub 仓库（v0 切片，仅覆盖已实跑端点所需字段）。
 *
 * <p>完整字段集见 {@code _doc/001_arch/github-api-migration.md}。
 */
public class Repository {

    private final String fullName;
    private final String description;
    private final String defaultBranch;
    private final String language;
    private final int stargazersCount;
    private final int forksCount;
    private final boolean isPrivate;
    private final String htmlUrl;

    public Repository(String fullName, String description, String defaultBranch,
                      String language, int stargazersCount, int forksCount,
                      boolean isPrivate, String htmlUrl) {
        this.fullName = fullName;
        this.description = description;
        this.defaultBranch = defaultBranch;
        this.language = language;
        this.stargazersCount = stargazersCount;
        this.forksCount = forksCount;
        this.isPrivate = isPrivate;
        this.htmlUrl = htmlUrl;
    }

    /**
     * 从 GitHub /repos/{owner}/{repo} 响应构造。
     */
    public static Repository fromJson(JsonObject o) {
        return new Repository(
                o.getString("full_name"),
                o.getString("description"),
                o.getString("default_branch"),
                o.getString("language"),
                o.getInt("stargazers_count"),
                o.getInt("forks_count"),
                o.getBoolean("private"),
                o.getString("html_url"));
    }

    public String getFullName() { return fullName; }
    public String getDescription() { return description; }
    public String getDefaultBranch() { return defaultBranch; }
    public String getLanguage() { return language; }
    public int getStargazersCount() { return stargazersCount; }
    public int getForksCount() { return forksCount; }
    public boolean isPrivate() { return isPrivate; }
    public String getHtmlUrl() { return htmlUrl; }
}
