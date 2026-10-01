package com.zifang.util.devops.git.github.issue;

import com.zifang.util.devops.git.github.model.Comment;
import com.zifang.util.devops.git.github.model.Issue;
import com.zifang.util.devops.git.github.test.MockGithubServer;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * IssueApiWrapper 的 mock-server 驱动测试，覆盖 §issue 全部 7 方法。
 */
public class IssueApiWrapperTest {

    private static final String ISSUE_JSON =
            "{\"number\":1,\"title\":\"bug\",\"state\":\"open\",\"body\":\"details\"}";

    private static final String CLOSED_JSON =
            "{\"number\":1,\"title\":\"bug\",\"state\":\"closed\",\"body\":\"details\"}";

    @Test
    public void list_returnsIssues() throws Exception {
        String body = "[" + ISSUE_JSON + "," + ISSUE_JSON.replace("1", "2") + "]";
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/repos/o/r/issues", body, 200)) {
            List<Issue> list = new IssueApiWrapper(mock.newClient(), "o", "r").list(null);
            assertEquals(2, list.size());
            assertEquals("bug", list.get(0).getTitle());
        }
    }

    @Test
    public void get_returnsIssue() throws Exception {
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/repos/o/r/issues/1", ISSUE_JSON, 200)) {
            Issue i = new IssueApiWrapper(mock.newClient(), "o", "r").get(1);
            assertEquals(1, i.getNumber());
        }
    }

    @Test
    public void create_returnsIssue() throws Exception {
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "POST", "/repos/o/r/issues", ISSUE_JSON, 201)) {
            Issue i = new IssueApiWrapper(mock.newClient(), "o", "r").create("bug", "details");
            assertEquals("bug", i.getTitle());
        }
    }

    @Test
    public void update_patchesIssue() throws Exception {
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "PATCH", "/repos/o/r/issues/1", ISSUE_JSON, 200)) {
            Issue i = new IssueApiWrapper(mock.newClient(), "o", "r").update(1, "bug", "more");
            assertEquals(1, i.getNumber());
        }
    }

    @Test
    public void close_setsStateClosed() throws Exception {
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "PATCH", "/repos/o/r/issues/1", CLOSED_JSON, 200)) {
            Issue i = new IssueApiWrapper(mock.newClient(), "o", "r").close(1);
            assertEquals("closed", i.getState());
        }
    }

    @Test
    public void reopen_setsStateOpen() throws Exception {
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "PATCH", "/repos/o/r/issues/1", ISSUE_JSON, 200)) {
            Issue i = new IssueApiWrapper(mock.newClient(), "o", "r").reopen(1);
            assertEquals("open", i.getState());
        }
    }

    @Test
    public void listComments_returnsComments() throws Exception {
        String body = "[{\"id\":100,\"body\":\"hi\"}]";
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/repos/o/r/issues/1/comments", body, 200)) {
            List<Comment> cs = new IssueApiWrapper(mock.newClient(), "o", "r").listComments(1);
            assertEquals(1, cs.size());
            assertEquals("hi", cs.get(0).getBody());
        }
    }

    @Test
    public void addComment_postsComment() throws Exception {
        String body = "{\"id\":101,\"body\":\"thanks\"}";
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "POST", "/repos/o/r/issues/1/comments", body, 201)) {
            Comment c = new IssueApiWrapper(mock.newClient(), "o", "r").addComment(1, "thanks");
            assertEquals(101L, c.getId());
        }
    }
}
