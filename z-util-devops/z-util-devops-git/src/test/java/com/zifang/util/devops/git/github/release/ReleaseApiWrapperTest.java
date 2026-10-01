package com.zifang.util.devops.git.github.release;

import com.zifang.util.devops.git.github.model.Release;
import com.zifang.util.devops.git.github.test.MockGithubServer;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * ReleaseApiWrapper 的 mock-server 驱动测试，覆盖 §release 全部 5 方法。
 */
public class ReleaseApiWrapperTest {

    private static final String REL_JSON =
            "{\"id\":1,\"tag_name\":\"v1.0\",\"name\":\"v1.0\",\"body\":\"notes\"}";

    @Test
    public void list_returnsReleases() throws Exception {
        String body = "[" + REL_JSON + "]";
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/repos/o/r/releases", body, 200)) {
            List<Release> list = new ReleaseApiWrapper(mock.newClient(), "o", "r").list();
            assertEquals(1, list.size());
            assertEquals("v1.0", list.get(0).getTagName());
        }
    }

    @Test
    public void getLatest_returnsRelease() throws Exception {
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/repos/o/r/releases/latest", REL_JSON, 200)) {
            Release r = new ReleaseApiWrapper(mock.newClient(), "o", "r").getLatest();
            assertEquals(1L, r.getId());
        }
    }

    @Test
    public void getByTag_returnsRelease() throws Exception {
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "GET", "/repos/o/r/releases/tags/v1.0", REL_JSON, 200)) {
            Release r = new ReleaseApiWrapper(mock.newClient(), "o", "r").getByTag("v1.0");
            assertEquals("v1.0", r.getTagName());
        }
    }

    @Test
    public void create_returnsRelease() throws Exception {
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "POST", "/repos/o/r/releases", REL_JSON, 201)) {
            Release r = new ReleaseApiWrapper(mock.newClient(), "o", "r").create("v1.0", "v1.0", "notes");
            assertEquals("v1.0", r.getTagName());
        }
    }

    @Test
    public void delete_hitsDeleteEndpoint() throws Exception {
        try (MockGithubServer mock = MockGithubServer.respondTo(
                "DELETE", "/repos/o/r/releases/1", "", 204)) {
            new ReleaseApiWrapper(mock.newClient(), "o", "r").delete(1L);
            assertEquals(1, mock.exchanges().size());
        }
    }
}
