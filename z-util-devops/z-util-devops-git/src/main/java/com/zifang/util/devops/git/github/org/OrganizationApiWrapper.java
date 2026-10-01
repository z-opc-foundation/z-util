package com.zifang.util.devops.git.github.org;

import com.zifang.util.devops.git.github.http.GithubHttpClient;
import com.zifang.util.devops.git.github.model.Org;
import com.zifang.util.json.model.JsonArray;
import com.zifang.util.json.model.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * GitHub Organization API 包装——§org 表内 4 方法全实跑。
 */
public class OrganizationApiWrapper {

    private final GithubHttpClient client;
    private String org;

    public OrganizationApiWrapper(GithubHttpClient client) {
        this(client, null);
    }

    public OrganizationApiWrapper(GithubHttpClient client, String org) {
        this.client = client;
        this.org = org;
    }

    /** GET /orgs/{org} */
    public Org get(String org) throws IOException {
        JsonObject body = client.getJsonObject("/orgs/" + org);
        return Org.fromJson(body);
    }

    public Org get() throws IOException {
        return get(this.org);
    }

    /** GET /orgs/{org}/members */
    public List<String> listMembers() throws IOException {
        return toStrings(client.getJsonArray("/orgs/" + this.org + "/members"), "login");
    }

    /** GET /orgs/{org}/repos */
    public List<String> listRepos() throws IOException {
        return toStrings(client.getJsonArray("/orgs/" + this.org + "/repos"), "name");
    }

    /** GET /orgs/{org}/teams */
    public List<String> listTeams() throws IOException {
        return toStrings(client.getJsonArray("/orgs/" + this.org + "/teams"), "slug");
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
