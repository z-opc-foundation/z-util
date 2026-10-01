package com.zifang.util.devops.git.github.model;

import com.zifang.util.json.model.JsonObject;

/**
 * GitHub Issue 评论（v0 切片）。
 */
public class Comment {

    private final long id;
    private final String body;

    public Comment(long id, String body) {
        this.id = id;
        this.body = body;
    }

    public static Comment fromJson(JsonObject o) {
        return new Comment(o.getLong("id"), o.getString("body"));
    }

    public long getId() { return id; }
    public String getBody() { return body; }
}
