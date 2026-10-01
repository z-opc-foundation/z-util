package com.zifang.util.devops.git.github.issue;

import com.zifang.util.devops.git.github.http.GithubHttpClient;
import com.zifang.util.devops.git.github.model.Comment;
import com.zifang.util.devops.git.github.model.Issue;
import com.zifang.util.json.model.JsonArray;
import com.zifang.util.json.model.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * GitHub Issue API 包装——§issue 表内 7 方法全实跑。
 */
public class IssueApiWrapper {

    private final GithubHttpClient client;
    private String owner;
    private String repo;

    public IssueApiWrapper(GithubHttpClient client) {
        this(client, null, null);
    }

    public IssueApiWrapper(GithubHttpClient client, String owner, String repo) {
        this.client = client;
        this.owner = owner;
        this.repo = repo;
    }

    /** GET /repos/{owner}/{repo}/issues?state=... */
    public List<Issue> list(String state) throws IOException {
        String path = "/repos/" + this.owner + "/" + this.repo + "/issues"
                + (state == null || state.isEmpty() ? "" : "?state=" + state);
        JsonArray arr = client.getJsonArray(path);
        return toIssues(arr);
    }

    /** GET /repos/{owner}/{repo}/issues/{number} */
    public Issue get(int number) throws IOException {
        JsonObject body = client.getJsonObject(
                "/repos/" + this.owner + "/" + this.repo + "/issues/" + number);
        return Issue.fromJson(body);
    }

    /** POST /repos/{owner}/{repo}/issues */
    public Issue create(String title, String body) throws IOException {
        JsonObject payload = new JsonObject();
        payload.put("title", title);
        payload.put("body", body == null ? "" : body);
        return Issue.fromJson(client.postJson(
                "/repos/" + this.owner + "/" + this.repo + "/issues", payload.toString()));
    }

    /** PATCH /repos/{owner}/{repo}/issues/{number} */
    public Issue update(int number, String title, String body) throws IOException {
        JsonObject payload = new JsonObject();
        payload.put("title", title);
        payload.put("body", body);
        return Issue.fromJson(client.patchJson(
                "/repos/" + this.owner + "/" + this.repo + "/issues/" + number, payload.toString()));
    }

    /** PATCH /repos/{owner}/{repo}/issues/{number} with state: closed */
    public Issue close(int number) throws IOException {
        return setState(number, "closed");
    }

    /** PATCH /repos/{owner}/{repo}/issues/{number} with state: open */
    public Issue reopen(int number) throws IOException {
        return setState(number, "open");
    }

    /** GET /repos/{owner}/{repo}/issues/{number}/comments */
    public List<Comment> listComments(int number) throws IOException {
        JsonArray arr = client.getJsonArray(
                "/repos/" + this.owner + "/" + this.repo + "/issues/" + number + "/comments");
        List<Comment> out = new ArrayList<>(arr.size());
        for (int i = 0; i < arr.size(); i++) {
            out.add(Comment.fromJson(arr.getJsonObject(i)));
        }
        return out;
    }

    /** POST /repos/{owner}/{repo}/issues/{number}/comments */
    public Comment addComment(int number, String body) throws IOException {
        JsonObject payload = new JsonObject();
        payload.put("body", body);
        return Comment.fromJson(client.postJson(
                "/repos/" + this.owner + "/" + this.repo + "/issues/" + number + "/comments",
                payload.toString()));
    }

    private Issue setState(int number, String state) throws IOException {
        JsonObject payload = new JsonObject();
        payload.put("state", state);
        return Issue.fromJson(client.patchJson(
                "/repos/" + this.owner + "/" + this.repo + "/issues/" + number, payload.toString()));
    }

    private static List<Issue> toIssues(JsonArray arr) {
        if (arr == null) {
            return new ArrayList<>();
        }
        List<Issue> out = new ArrayList<>(arr.size());
        for (int i = 0; i < arr.size(); i++) {
            out.add(Issue.fromJson(arr.getJsonObject(i)));
        }
        return out;
    }
}
