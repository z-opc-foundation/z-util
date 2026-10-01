package com.zifang.util.devops.git.github.model;

import com.zifang.util.json.model.JsonObject;

/**
 * GitHub 用户（v0 切片，仅覆盖 /user 响应字段）。
 */
public class User {

    private final String login;
    private final String name;
    private final String email;
    private final String avatarUrl;
    private final String htmlUrl;

    public User(String login, String name, String email, String avatarUrl, String htmlUrl) {
        this.login = login;
        this.name = name;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.htmlUrl = htmlUrl;
    }

    public static User fromJson(JsonObject o) {
        return new User(
                o.getString("login"),
                o.getString("name"),
                o.getString("email"),
                o.getString("avatar_url"),
                o.getString("html_url"));
    }

    public String getLogin() { return login; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getHtmlUrl() { return htmlUrl; }
}
