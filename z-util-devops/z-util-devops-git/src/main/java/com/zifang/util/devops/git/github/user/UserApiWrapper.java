package com.zifang.util.devops.git.github.user;

import com.zifang.util.devops.git.github.http.GithubHttpClient;
import com.zifang.util.devops.git.github.model.User;
import com.zifang.util.json.model.JsonObject;

import java.io.IOException;
import java.util.List;

/**
 * GitHub User API 包装。
 *
 * <p>v0：仅 {@link #getCurrentUser()} 实跑；其余方法抛 {@link UnsupportedOperationException}，
 * 续接清单见 {@code _doc/001_arch/github-api-migration.md} §user。
 */
public class UserApiWrapper {

    private final GithubHttpClient client;

    public UserApiWrapper(GithubHttpClient client) {
        this.client = client;
    }

    /** GET /user → 当前认证用户。 */
    public User getCurrentUser() throws IOException {
        JsonObject body = client.getJsonObject("/user");
        return User.fromJson(body);
    }

    public User get(String login) throws IOException {
        throw notImpl("user.get(login)");
    }

    public List<String> listEmails() throws IOException {
        throw notImpl("user.listEmails");
    }

    public List<String> listFollowers(String login) throws IOException {
        throw notImpl("user.listFollowers");
    }

    public List<String> listFollowing(String login) throws IOException {
        throw notImpl("user.listFollowing");
    }

    private static UnsupportedOperationException notImpl(String key) {
        return new UnsupportedOperationException(
                "UserApiWrapper." + key + " 未迁移实现，详见 _doc/001_arch/github-api-migration.md");
    }
}
