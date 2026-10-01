package com.zifang.util.devops.git.github.model;

import com.zifang.util.json.model.JsonObject;

/**
 * GitHub Release（v0 切片）。
 */
public class Release {

    private final long id;
    private final String tagName;
    private final String name;
    private final String body;

    public Release(long id, String tagName, String name, String body) {
        this.id = id;
        this.tagName = tagName;
        this.name = name;
        this.body = body;
    }

    public static Release fromJson(JsonObject o) {
        return new Release(
                o.getLong("id"),
                o.getString("tag_name"),
                o.getString("name"),
                o.getString("body"));
    }

    public long getId() { return id; }
    public String getTagName() { return tagName; }
    public String getName() { return name; }
    public String getBody() { return body; }
}
