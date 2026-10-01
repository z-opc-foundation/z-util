package com.zifang.util.devops.git.github.model;

import com.zifang.util.json.model.JsonObject;

/**
 * GitHub Organization（v0 切片）。
 */
public class Org {

    private final String login;
    private final String name;
    private final String description;

    public Org(String login, String name, String description) {
        this.login = login;
        this.name = name;
        this.description = description;
    }

    public static Org fromJson(JsonObject o) {
        return new Org(
                o.getString("login"),
                o.getString("name"),
                o.getString("description"));
    }

    public String getLogin() { return login; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}
