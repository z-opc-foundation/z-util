package com.zifang.util.devops.git.github.model;

import com.zifang.util.json.model.JsonObject;

/**
 * GitHub 分支（v0 切片，/repos/{owner}/{repo}/branches 响应）。
 */
public class Branch {

    private final String name;

    public Branch(String name) {
        this.name = name;
    }

    public static Branch fromJson(JsonObject o) {
        return new Branch(o.getString("name"));
    }

    public String getName() { return name; }
}
