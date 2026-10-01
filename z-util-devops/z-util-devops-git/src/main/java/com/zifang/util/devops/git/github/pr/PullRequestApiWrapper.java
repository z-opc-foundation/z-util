package com.zifang.util.devops.git.github.pr;

import com.zifang.util.devops.git.github.http.GithubHttpClient;
import com.zifang.util.devops.git.github.model.PullRequest;
import com.zifang.util.json.model.JsonArray;
import com.zifang.util.json.model.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * GitHub Pull Request API 包装——§pr 表内 8 方法全实跑。
 */
public class PullRequestApiWrapper {

    private final GithubHttpClient client;
    private String owner;
    private String repo;

    public PullRequestApiWrapper(GithubHttpClient client) {
        this(client, null, null);
    }

    public PullRequestApiWrapper(GithubHttpClient client, String owner, String repo) {
        this.client = client;
        this.owner = owner;
        this.repo = repo;
    }

    /** GET /repos/{owner}/{repo}/pulls?state=... */
    public List<PullRequest> list(String state) throws IOException {
        String path = "/repos/" + this.owner + "/" + this.repo + "/pulls"
                + (state == null || state.isEmpty() ? "" : "?state=" + state);
        JsonArray arr = client.getJsonArray(path);
        return toPRs(arr);
    }

    /** GET /repos/{owner}/{repo}/pulls/{number} */
    public PullRequest get(int number) throws IOException {
        return PullRequest.fromJson(client.getJsonObject(
                "/repos/" + this.owner + "/" + this.repo + "/pulls/" + number));
    }

    /** POST /repos/{owner}/{repo}/pulls */
    public PullRequest create(String title, String head, String base) throws IOException {
        JsonObject payload = new JsonObject();
        payload.put("title", title);
        payload.put("head", head);
        payload.put("base", base);
        return PullRequest.fromJson(client.postJson(
                "/repos/" + this.owner + "/" + this.repo + "/pulls", payload.toString()));
    }

    /** PUT /repos/{owner}/{repo}/pulls/{number}/merge —— 返回 merge commit 信息 */
    public JsonObject merge(int number, String commitMessage) throws IOException {
        JsonObject payload = new JsonObject();
        payload.put("commit_message", commitMessage == null ? "" : commitMessage);
        return client.putJson(
                "/repos/" + this.owner + "/" + this.repo + "/pulls/" + number + "/merge",
                payload.toString());
    }

    /** PATCH /repos/{owner}/{repo}/pulls/{number} with state: closed */
    public PullRequest close(int number) throws IOException {
        JsonObject payload = new JsonObject();
        payload.put("state", "closed");
        return PullRequest.fromJson(client.patchJson(
                "/repos/" + this.owner + "/" + this.repo + "/pulls/" + number, payload.toString()));
    }

    /** GET /repos/{owner}/{repo}/pulls/{number}/reviews */
    public List<JsonObject> listReviews(int number) throws IOException {
        JsonArray arr = client.getJsonArray(
                "/repos/" + this.owner + "/" + this.repo + "/pulls/" + number + "/reviews");
        List<JsonObject> out = new ArrayList<>(arr.size());
        for (int i = 0; i < arr.size(); i++) {
            out.add(arr.getJsonObject(i));
        }
        return out;
    }

    /** POST /repos/{owner}/{repo}/pulls/{number}/reviews */
    public JsonObject submitReview(int number, String body, String state) throws IOException {
        JsonObject payload = new JsonObject();
        payload.put("body", body);
        payload.put("state", state);
        return client.postJson(
                "/repos/" + this.owner + "/" + this.repo + "/pulls/" + number + "/reviews",
                payload.toString());
    }

    /** GET /repos/{owner}/{repo}/pulls/{number}/files */
    public List<JsonObject> listFiles(int number) throws IOException {
        JsonArray arr = client.getJsonArray(
                "/repos/" + this.owner + "/" + this.repo + "/pulls/" + number + "/files");
        List<JsonObject> out = new ArrayList<>(arr.size());
        for (int i = 0; i < arr.size(); i++) {
            out.add(arr.getJsonObject(i));
        }
        return out;
    }

    private static List<PullRequest> toPRs(JsonArray arr) {
        if (arr == null) {
            return new ArrayList<>();
        }
        List<PullRequest> out = new ArrayList<>(arr.size());
        for (int i = 0; i < arr.size(); i++) {
            out.add(PullRequest.fromJson(arr.getJsonObject(i)));
        }
        return out;
    }
}
