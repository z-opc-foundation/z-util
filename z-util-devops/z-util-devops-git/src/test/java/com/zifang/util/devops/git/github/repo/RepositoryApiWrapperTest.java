package com.zifang.util.devops.git.github.repo;

import com.zifang.util.devops.git.github.model.Repository;
import com.zifang.util.devops.git.github.test.MockGithubServer;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * RepositoryApiWrapper 的 mock-server 驱动测试。
 *
 * <p>覆盖 §repo 表内 11 个方法：get / listBranches / create / createOrgRepo / delete /
 * listUserRepos / listMyRepos / fork / listForks / listStargazers / search(keyword) /
 * search(keyword, lang)。
 */
public class RepositoryApiWrapperTest {

    private static final String REPO_JSON =
            "{\"full_name\":\"octocat/Hello-World\"," +
            "\"description\":\"My first repo on GitHub!\"," +
            "\"default_branch\":\"master\"," +
            "\"language\":\"Java\"," +
            "\"stargazers_count\":80," +
            "\"forks_count\":9," +
            "\"private\":false," +
            "\"html_url\":\"https://github.com/octocat/Hello-World\"}";

    @Test
    public void get_returnsParsedRepository() throws Exception {
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/repos/octocat/Hello-World", REPO_JSON, 200)) {
            Repository r = new RepositoryApiWrapper(mock.newClient(), "octocat", "Hello-World").get();
            assertEquals("octocat/Hello-World", r.getFullName());
            assertEquals("Java", r.getLanguage());
            assertEquals(80, r.getStargazersCount());
        }
    }

    @Test
    public void listBranches_returnsBranchNames() throws Exception {
        String body = "[{\"name\":\"main\"},{\"name\":\"feature/x\"}]";
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/repos/octocat/Hello-World/branches", body, 200)) {
            List<String> names = new RepositoryApiWrapper(mock.newClient(), "octocat", "Hello-World").listBranches();
            assertEquals(2, names.size());
            assertEquals("main", names.get(0));
            assertEquals("feature/x", names.get(1));
        }
    }

    @Test
    public void create_postsJsonAndReturnsRepo() throws Exception {
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "POST", "/user/repos", REPO_JSON, 201)) {
            Repository r = new RepositoryApiWrapper(mock.newClient()).create("Hello-World", "desc", false);
            assertEquals("octocat/Hello-World", r.getFullName());
            assertEquals(1, mock.exchanges().size());
        }
    }

    @Test
    public void createOrgRepo_hitsOrgEndpoint() throws Exception {
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "POST", "/orgs/myorg/repos", REPO_JSON, 201)) {
            Repository r = new RepositoryApiWrapper(mock.newClient()).createOrgRepo("myorg", "Hello", "d", false);
            assertNotNull(r);
        }
    }

    @Test
    public void delete_hitsDeleteEndpoint() throws Exception {
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "DELETE", "/repos/octocat/Hello-World", "", 204)) {
            new RepositoryApiWrapper(mock.newClient(), "octocat", "Hello-World").delete();
            assertEquals(1, mock.exchanges().size());
        }
    }

    @Test
    public void listUserRepos_returnsList() throws Exception {
        String body = "[" + REPO_JSON + "," + REPO_JSON + "]";
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/users/octocat/repos", body, 200)) {
            List<Repository> repos = new RepositoryApiWrapper(mock.newClient()).listUserRepos("octocat");
            assertEquals(2, repos.size());
            assertEquals("octocat/Hello-World", repos.get(0).getFullName());
        }
    }

    @Test
    public void listMyRepos_returnsList() throws Exception {
        String body = "[" + REPO_JSON + "]";
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/user/repos", body, 200)) {
            List<Repository> repos = new RepositoryApiWrapper(mock.newClient()).listMyRepos();
            assertEquals(1, repos.size());
        }
    }

    @Test
    public void fork_returnsRepo() throws Exception {
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "POST", "/repos/octocat/Hello-World/forks", REPO_JSON, 202)) {
            Repository r = new RepositoryApiWrapper(mock.newClient(), "octocat", "Hello-World").fork();
            assertNotNull(r);
        }
    }

    @Test
    public void listForks_returnsList() throws Exception {
        String body = "[" + REPO_JSON + "]";
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/repos/octocat/Hello-World/forks", body, 200)) {
            List<Repository> forks = new RepositoryApiWrapper(mock.newClient(), "octocat", "Hello-World").listForks();
            assertEquals(1, forks.size());
        }
    }

    @Test
    public void listStargazers_returnsUsers() throws Exception {
        String body = "[{\"login\":\"octocat\",\"name\":\"The Octocat\"," +
                "\"email\":\"octocat@github.com\",\"avatar_url\":\"a\",\"html_url\":\"h\"}]";
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/repos/octocat/Hello-World/stargazers", body, 200)) {
            List<com.zifang.util.devops.git.github.model.User> users =
                    new RepositoryApiWrapper(mock.newClient(), "octocat", "Hello-World").listStargazers();
            assertEquals(1, users.size());
            assertEquals("octocat", users.get(0).getLogin());
        }
    }

    @Test
    public void search_keyword_returnsItems() throws Exception {
        String body = "{\"items\":[" + REPO_JSON + "]}";
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/search/repositories?q=hello", body, 200)) {
            List<Repository> repos = new RepositoryApiWrapper(mock.newClient()).search("hello");
            assertEquals(1, repos.size());
            assertTrue(repos.get(0).getFullName().length() > 0);
        }
    }

    @Test
    public void search_withLanguage_appendsLanguage() throws Exception {
        String body = "{\"items\":[" + REPO_JSON + "]}";
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/search/repositories?q=hello+language:Java", body, 200)) {
            List<Repository> repos = new RepositoryApiWrapper(mock.newClient()).search("hello", "Java");
            assertEquals(1, repos.size());
        }
    }
}
