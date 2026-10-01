package com.zifang.util.devops.git.github.pr;

import com.zifang.util.devops.git.github.model.PullRequest;
import com.zifang.util.devops.git.github.test.MockGithubServer;
import com.zifang.util.json.model.JsonObject;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * PullRequestApiWrapper 的 mock-server 驱动测试，覆盖 §pr 全部 8 方法。
 */
public class PullRequestApiWrapperTest {

    private static final String PR_JSON =
            "{\"number\":5,\"title\":\"feat x\",\"state\":\"open\"}";

    @Test
    public void list_returnsPRs() throws Exception {
        String body = "[" + PR_JSON + "," + PR_JSON.replace("5", "6") + "]";
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/repos/o/r/pulls", body, 200)) {
            List<PullRequest> list = new PullRequestApiWrapper(mock.newClient(), "o", "r").list(null);
            assertEquals(2, list.size());
        }
    }

    @Test
    public void get_returnsPR() throws Exception {
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/repos/o/r/pulls/5", PR_JSON, 200)) {
            PullRequest pr = new PullRequestApiWrapper(mock.newClient(), "o", "r").get(5);
            assertEquals(5, pr.getNumber());
        }
    }

    @Test
    public void create_returnsPR() throws Exception {
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "POST", "/repos/o/r/pulls", PR_JSON, 201)) {
            PullRequest pr = new PullRequestApiWrapper(mock.newClient(), "o", "r").create("feat x", "feat-x", "main");
            assertEquals("feat x", pr.getTitle());
        }
    }

    @Test
    public void merge_returnsMergeResult() throws Exception {
        String body = "{\"sha\":\"abc123\",\"merged\":true}";
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "PUT", "/repos/o/r/pulls/5/merge", body, 200)) {
            JsonObject result = new PullRequestApiWrapper(mock.newClient(), "o", "r").merge(5, "msg");
            assertEquals(true, result.getBoolean("merged"));
        }
    }

    @Test
    public void close_patchesState() throws Exception {
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "PATCH", "/repos/o/r/pulls/5",
                "{\"number\":5,\"title\":\"feat x\",\"state\":\"closed\"}", 200)) {
            PullRequest pr = new PullRequestApiWrapper(mock.newClient(), "o", "r").close(5);
            assertEquals("closed", pr.getState());
        }
    }

    @Test
    public void listReviews_returnsList() throws Exception {
        String body = "[{\"id\":1,\"state\":\"APPROVED\"}]";
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/repos/o/r/pulls/5/reviews", body, 200)) {
            List<JsonObject> reviews = new PullRequestApiWrapper(mock.newClient(), "o", "r").listReviews(5);
            assertEquals(1, reviews.size());
        }
    }

    @Test
    public void submitReview_postsReview() throws Exception {
        String body = "{\"id\":42,\"state\":\"APPROVED\"}";
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "POST", "/repos/o/r/pulls/5/reviews", body, 200)) {
            JsonObject r = new PullRequestApiWrapper(mock.newClient(), "o", "r")
                    .submitReview(5, "LGTM", "APPROVED");
            assertEquals("APPROVED", r.getString("state"));
        }
    }

    @Test
    public void listFiles_returnsFiles() throws Exception {
        String body = "[{\"filename\":\"x.java\"}]";
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/repos/o/r/pulls/5/files", body, 200)) {
            List<JsonObject> files = new PullRequestApiWrapper(mock.newClient(), "o", "r").listFiles(5);
            assertEquals(1, files.size());
        }
    }
}
