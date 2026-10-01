package com.zifang.util.devops.git.github.user;

import com.zifang.util.devops.git.github.http.GithubHttpClient;
import com.zifang.util.devops.git.github.model.User;
import com.zifang.util.json.model.JsonArray;
import com.zifang.util.json.model.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * GitHub User API 包装——§user 表内 5 方法全实跑。
 */
public class UserApiWrapper {

    private final GithubHttpClient client;

    public UserApiWrapper(GithubHttpClient client) {
        this.client = client;
    }

    /** GET /user */
    public User getCurrentUser() throws IOException {
        JsonObject body = client.getJsonObject("/user");
        return User.fromJson(body);
    }

    /** GET /users/{login} */
    public User get(String login) throws IOException {
        JsonObject body = client.getJsonObject("/users/" + login);
        return User.fromJson(body);
    }

    /** GET /user/emails —— 返回用户邮箱列表 */
    public List<String> listEmails() throws IOException {
        JsonArray arr = client.getJsonArray("/user/emails");
        return toStrings(arr, "email");
    }

    /** GET /users/{login}/followers */
    public List<String> listFollowers(String login) throws IOException {
        JsonArray arr = client.getJsonArray("/users/" + login + "/followers");
        return toStrings(arr, "login");
    }

    /** GET /users/{login}/following */
    public List<String> listFollowing(String login) throws IOException {
        JsonArray arr = client.getJsonArray("/users/" + login + "/following");
        return toStrings(arr, "login");
    }

    private static List<String> toStrings(JsonArray arr, String field) {
        if (arr == null) {
            return new ArrayList<>();
        }
        List<String> out = new ArrayList<>(arr.size());
        for (int i = 0; i < arr.size(); i++) {
            out.add(arr.getJsonObject(i).getString(field));
        }
        return out;
    }
}
