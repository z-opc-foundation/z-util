package com.zifang.util.devops.git.github.model;

import com.zifang.util.json.model.JsonObject;

/**
 * GitHub Pull Request（v0 切片）。
 */
public class PullRequest {

    private final int number;
    private final String title;
    private final String state;

    public PullRequest(int number, String title, String state) {
        this.number = number;
        this.title = title;
        this.state = state;
    }

    public static PullRequest fromJson(JsonObject o) {
        return new PullRequest(
                o.getInt("number"),
                o.getString("title"),
                o.getString("state"));
    }

    public int getNumber() { return number; }
    public String getTitle() { return title; }
    public String getState() { return state; }
}
