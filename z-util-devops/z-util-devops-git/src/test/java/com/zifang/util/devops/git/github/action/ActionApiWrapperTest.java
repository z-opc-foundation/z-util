package com.zifang.util.devops.git.github.action;

import com.zifang.util.devops.git.github.test.MockGithubServer;
import com.zifang.util.json.model.JsonObject;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * ActionApiWrapper 的 mock-server 驱动测试，覆盖 §action 全部 4 方法。
 *
 * <p>所有 GitHub Actions 列表端点都返回 {@code {"total_count":N, "<key>":[...]}} 包裹，
 * 测试中我们构造这种形状。
 */
public class ActionApiWrapperTest {

    private static final String WORKFLOWS_BODY =
            "{\"total_count\":2,\"workflows\":[{\"id\":1,\"name\":\"ci\"},{\"id\":2,\"name\":\"cd\"}]}";

    private static final String RUNS_BODY =
            "{\"total_count\":1,\"workflow_runs\":[{\"id\":100,\"status\":\"completed\"}]}";

    private static final String ARTIFACTS_BODY =
            "{\"total_count\":1,\"artifacts\":[{\"id\":200,\"name\":\"build-output\"}]}";

    private static final String JOBS_BODY =
            "{\"total_count\":1,\"jobs\":[{\"id\":300,\"name\":\"build\"}]}";

    @Test
    public void listWorkflows_returnsWorkflows() throws Exception {
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/repos/o/r/actions/workflows", WORKFLOWS_BODY, 200)) {
            List<JsonObject> list = new ActionApiWrapper(mock.newClient(), "o", "r").listWorkflows();
            assertEquals(2, list.size());
            assertEquals(Long.valueOf(1L), list.get(0).getLong("id"));
        }
    }

    @Test
    public void listWorkflowRuns_returnsRuns() throws Exception {
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/repos/o/r/actions/workflows/1/runs", RUNS_BODY, 200)) {
            List<JsonObject> list = new ActionApiWrapper(mock.newClient(), "o", "r")
                    .listWorkflowRuns(1L);
            assertEquals(1, list.size());
            assertEquals("completed", list.get(0).getString("status"));
        }
    }

    @Test
    public void listArtifacts_returnsArtifacts() throws Exception {
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/repos/o/r/actions/runs/100/artifacts", ARTIFACTS_BODY, 200)) {
            List<JsonObject> list = new ActionApiWrapper(mock.newClient(), "o", "r")
                    .listArtifacts(100L);
            assertEquals(1, list.size());
            assertEquals("build-output", list.get(0).getString("name"));
        }
    }

    @Test
    public void listJobs_returnsJobs() throws Exception {
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/repos/o/r/actions/runs/100/jobs", JOBS_BODY, 200)) {
            List<JsonObject> list = new ActionApiWrapper(mock.newClient(), "o", "r")
                    .listJobs(100L);
            assertEquals(1, list.size());
            assertEquals("build", list.get(0).getString("name"));
        }
    }
}
