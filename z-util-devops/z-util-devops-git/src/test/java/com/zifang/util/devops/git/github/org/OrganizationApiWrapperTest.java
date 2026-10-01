package com.zifang.util.devops.git.github.org;

import com.zifang.util.devops.git.github.model.Org;
import com.zifang.util.devops.git.github.test.MockGithubServer;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * OrganizationApiWrapper 的 mock-server 驱动测试，覆盖 §org 全部 4 方法。
 */
public class OrganizationApiWrapperTest {

    private static final String ORG_JSON =
            "{\"login\":\"myorg\",\"name\":\"My Org\",\"description\":\"x\"}";

    @Test
    public void get_returnsOrg() throws Exception {
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/orgs/myorg", ORG_JSON, 200)) {
            Org o = new OrganizationApiWrapper(mock.newClient()).get("myorg");
            assertEquals("myorg", o.getLogin());
        }
    }

    @Test
    public void listMembers_returnsLogins() throws Exception {
        String body = "[{\"login\":\"a\"},{\"login\":\"b\"}]";
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/orgs/myorg/members", body, 200)) {
            List<String> list = new OrganizationApiWrapper(mock.newClient(), "myorg").listMembers();
            assertEquals(2, list.size());
        }
    }

    @Test
    public void listRepos_returnsNames() throws Exception {
        String body = "[{\"name\":\"r1\"},{\"name\":\"r2\"}]";
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/orgs/myorg/repos", body, 200)) {
            List<String> list = new OrganizationApiWrapper(mock.newClient(), "myorg").listRepos();
            assertEquals(2, list.size());
            assertEquals("r1", list.get(0));
        }
    }

    @Test
    public void listTeams_returnsSlugs() throws Exception {
        String body = "[{\"slug\":\"backend\"},{\"slug\":\"frontend\"}]";
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/orgs/myorg/teams", body, 200)) {
            List<String> list = new OrganizationApiWrapper(mock.newClient(), "myorg").listTeams();
            assertEquals(2, list.size());
            assertEquals("backend", list.get(0));
        }
    }
}
