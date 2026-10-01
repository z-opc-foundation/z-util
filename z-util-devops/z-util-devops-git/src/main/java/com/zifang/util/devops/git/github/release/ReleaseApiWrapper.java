package com.zifang.util.devops.git.github.release;

import com.zifang.util.devops.git.github.http.GithubHttpClient;
import com.zifang.util.devops.git.github.model.Release;
import com.zifang.util.json.model.JsonArray;
import com.zifang.util.json.model.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * GitHub Release API 包装——§release 表内 5 方法全实跑。
 */
public class ReleaseApiWrapper {

    private final GithubHttpClient client;
    private String owner;
    private String repo;

    public ReleaseApiWrapper(GithubHttpClient client) {
        this(client, null, null);
    }

    public ReleaseApiWrapper(GithubHttpClient client, String owner, String repo) {
        this.client = client;
        this.owner = owner;
        this.repo = repo;
    }

    /** GET /repos/{owner}/{repo}/releases */
    public List<Release> list() throws IOException {
        JsonArray arr = client.getJsonArray(
                "/repos/" + this.owner + "/" + this.repo + "/releases");
        return toReleases(arr);
    }

    /** GET /repos/{owner}/{repo}/releases/latest */
    public Release getLatest() throws IOException {
        return Release.fromJson(client.getJsonObject(
                "/repos/" + this.owner + "/" + this.repo + "/releases/latest"));
    }

    /** GET /repos/{owner}/{repo}/releases/tags/{tag} */
    public Release getByTag(String tag) throws IOException {
        return Release.fromJson(client.getJsonObject(
                "/repos/" + this.owner + "/" + this.repo + "/releases/tags/" + tag));
    }

    /** POST /repos/{owner}/{repo}/releases */
    public Release create(String tag, String name, String body) throws IOException {
        JsonObject payload = new JsonObject();
        payload.put("tag_name", tag);
        payload.put("name", name == null ? "" : name);
        payload.put("body", body == null ? "" : body);
        return Release.fromJson(client.postJson(
                "/repos/" + this.owner + "/" + this.repo + "/releases", payload.toString()));
    }

    /** DELETE /repos/{owner}/{repo}/releases/{id} */
    public void delete(long id) throws IOException {
        client.delete("/repos/" + this.owner + "/" + this.repo + "/releases/" + id);
    }

    private static List<Release> toReleases(JsonArray arr) {
        if (arr == null) {
            return new ArrayList<>();
        }
        List<Release> out = new ArrayList<>(arr.size());
        for (int i = 0; i < arr.size(); i++) {
            out.add(Release.fromJson(arr.getJsonObject(i)));
        }
        return out;
    }
}
