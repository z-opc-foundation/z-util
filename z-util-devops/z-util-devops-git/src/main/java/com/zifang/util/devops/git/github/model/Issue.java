package com.zifang.util.devops.git.github.model;

import com.zifang.util.json.model.JsonObject;

/**
 * GitHub Issue（v0 切片，仅覆盖已实跑端点字段）。
 */
public class Issue {

    private final int number;
    private final String title;
    private final String state;
    private final String body;

    public Issue(int number, String title, String state, String body) {
        this.number = number;
        this.title = title;
        this.state = state;
        this.body = body;
    }

    public static Issue fromJson(JsonObject o) {
        return new Issue(
                o.getInt("number"),
                o.getString("title"),
                o.getString("state"),
                o.getString("body"));
    }

    public int getNumber() { return number; }
    public String getTitle() { return title; }
    public String getState() { return state; }
    public String getBody() { return body; }
}
