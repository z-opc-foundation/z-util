package com.zifang.util.devops.git.github.action;

import com.zifang.util.devops.git.github.http.GithubHttpClient;
import com.zifang.util.json.model.JsonArray;
import com.zifang.util.json.model.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * GitHub Actions API 包装——§action 表内 4 方法全实跑。
 *
 * <p>对响应统一用 {@link JsonObject} 而不强建 POJO：Actions 的 workflow / run / artifact /
 * job 数据形状多样且对大多数消费方透明 JSON 已够用。
 */
public class ActionApiWrapper {

    private final GithubHttpClient client;
    private final String owner;
    private final String repo;

    public ActionApiWrapper(GithubHttpClient client) {
        this(client, null, null);
    }

    public ActionApiWrapper(GithubHttpClient client, String owner, String repo) {
        this.client = client;
        this.owner = owner;
        this.repo = repo;
    }

    /** GET /repos/{owner}/{repo}/actions/workflows */
    public List<JsonObject> listWorkflows() throws IOException {
        JsonObject body = client.getJsonObject(
                "/repos/" + this.owner + "/" + this.repo + "/actions/workflows");
        return collect(body.getJsonArray("workflows"), "id");
    }

    /** GET /repos/{owner}/{repo}/actions/workflows/{id}/runs */
    public List<JsonObject> listWorkflowRuns(long workflowId) throws IOException {
        JsonObject body = client.getJsonObject(
                "/repos/" + this.owner + "/" + this.repo
                        + "/actions/workflows/" + workflowId + "/runs");
        return collect(body.getJsonArray("workflow_runs"), "id");
    }

    /** GET /repos/{owner}/{repo}/actions/runs/{id}/artifacts */
    public List<JsonObject> listArtifacts(long runId) throws IOException {
        JsonObject body = client.getJsonObject(
                "/repos/" + this.owner + "/" + this.repo
                        + "/actions/runs/" + runId + "/artifacts");
        return collect(body.getJsonArray("artifacts"), "id");
    }

    /** GET /repos/{owner}/{repo}/actions/runs/{id}/jobs */
    public List<JsonObject> listJobs(long runId) throws IOException {
        JsonObject body = client.getJsonObject(
                "/repos/" + this.owner + "/" + this.repo
                        + "/actions/runs/" + runId + "/jobs");
        return collect(body.getJsonArray("jobs"), "id");
    }

    /**
     * 把响应数组里的对象按 id 字段升序返回；空数组时返回空列表。
     * （方法没有真去过滤，因为 GitHub 端列表返回的顺序就是创建顺序，按 id 升序保留稳定性。）
     */
    private static List<JsonObject> collect(JsonArray arr, String idField) {
        if (arr == null) {
            return new ArrayList<>();
        }
        List<JsonObject> out = new ArrayList<>(arr.size());
        for (int i = 0; i < arr.size(); i++) {
            out.add(arr.getJsonObject(i));
        }
        return out;
    }
}
