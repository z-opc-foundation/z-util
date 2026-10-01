package com.zifang.util.devops.git.github.action;

import com.zifang.util.devops.git.github.http.GithubHttpClient;

import java.io.IOException;
import java.util.List;

/**
 * GitHub Actions API 包装（v0 全未实跑；续接清单见
 * {@code _doc/001_arch/github-api-migration.md} §action）。
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

    public List<String> listWorkflows() throws IOException {
        throw notImpl("action.listWorkflows");
    }

    public List<String> listWorkflowRuns(long workflowId) throws IOException {
        throw notImpl("action.listWorkflowRuns");
    }

    public List<String> listArtifacts(long runId) throws IOException {
        throw notImpl("action.listArtifacts");
    }

    public List<String> listJobs(long runId) throws IOException {
        throw notImpl("action.listJobs");
    }

    private static UnsupportedOperationException notImpl(String key) {
        return new UnsupportedOperationException(
                "ActionApiWrapper." + key + " 未迁移实现，详见 _doc/001_arch/github-api-migration.md");
    }
}
