package com.zifang.util.devops.git.github.user;

import com.zifang.util.devops.git.github.model.User;
import com.zifang.util.devops.git.github.test.MockGithubServer;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * UserApiWrapper 的 mock-server 驱动测试，覆盖 §user 全部 5 方法。
 */
public class UserApiWrapperTest {

    private static final String USER_JSON =
            "{\"login\":\"octocat\",\"name\":\"The Octocat\"," +
            "\"email\":\"octo@github.com\",\"avatar_url\":\"a\",\"html_url\":\"h\"}";

    @Test
    public void getCurrentUser_returnsUser() throws Exception {
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/user", USER_JSON, 200)) {
            User u = new UserApiWrapper(mock.newClient()).getCurrentUser();
            assertEquals("octocat", u.getLogin());
        }
    }

    @Test
    public void get_returnsUserByLogin() throws Exception {
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/users/octocat", USER_JSON, 200)) {
            User u = new UserApiWrapper(mock.newClient()).get("octocat");
            assertEquals("octocat", u.getLogin());
        }
    }

    @Test
    public void listEmails_returnsEmails() throws Exception {
        String body = "[{\"email\":\"a@x.com\"},{\"email\":\"b@x.com\"}]";
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/user/emails", body, 200)) {
            List<String> emails = new UserApiWrapper(mock.newClient()).listEmails();
            assertEquals(2, emails.size());
            assertEquals("a@x.com", emails.get(0));
        }
    }

    @Test
    public void listFollowers_returnsLogins() throws Exception {
        String body = "[{\"login\":\"f1\"},{\"login\":\"f2\"}]";
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/users/octocat/followers", body, 200)) {
            List<String> followers = new UserApiWrapper(mock.newClient()).listFollowers("octocat");
            assertEquals(2, followers.size());
            assertEquals("f1", followers.get(0));
        }
    }

    @Test
    public void listFollowing_returnsLogins() throws Exception {
        String body = "[{\"login\":\"fg1\"}]";
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/users/octocat/following", body, 200)) {
            List<String> following = new UserApiWrapper(mock.newClient()).listFollowing("octocat");
            assertEquals(1, following.size());
            assertEquals("fg1", following.get(0));
        }
    }
}
