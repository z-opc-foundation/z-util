package com.zifang.util.devops.git.github.http;

import com.zifang.util.json.JsonUtil;
import com.zifang.util.json.model.JsonArray;
import com.zifang.util.json.model.JsonObject;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * GitHub REST API 客户端（v0 自研骨架，替代 org.kohsuke.github-api）。
 *
 * <p>本类只暴露少量方法作 v0 切片；后续按 {@code _doc/001_arch/github-api-migration.md}
 * 续接清单补全。
 *
 * <p>迁移背景：github-api 引入的传递依赖树（commons-lang3 / okio / commons-codec / commons-io …）
 * 是 Dependabot 高危漏洞的主来源；自实现让我们完全控制鉴权 / 分页 / 错误语义。
 */
public class GithubHttpClient {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient http;
    private final HttpUrl baseUrl;

    /**
     * @param baseUrl 形如 {@code https://api.github.com}；非空
     * @param token   OAuth/PAT token；可空（公开端点可匿名访问）
     */
    public GithubHttpClient(String baseUrl, String token) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new IllegalArgumentException("baseUrl must not be empty");
        }
        HttpUrl parsed = HttpUrl.parse(baseUrl);
        if (parsed == null) {
            throw new IllegalArgumentException("invalid baseUrl: " + baseUrl);
        }
        this.baseUrl = parsed;
        OkHttpClient.Builder b = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS);
        if (token != null && !token.isEmpty()) {
            b.addInterceptor(chain -> {
                Request authed = chain.request().newBuilder()
                        .header("Authorization", "token " + token)
                        .header("Accept", "application/vnd.github+json")
                        .header("X-GitHub-Api-Version", "2022-11-28")
                        .build();
                return chain.proceed(authed);
            });
        }
        this.http = b.build();
    }

    /**
     * GET 一个对象端点。响应体必须是 JSON 对象；否则抛 {@link IOException}。
     *
     * @param path 以 / 开头的路径，如 {@code /repos/owner/repo}
     */
    public JsonObject getJsonObject(String path) throws IOException {
        HttpUrl url = baseUrl.newBuilder().addPathSegments(stripLeadingSlash(path)).build();
        Request req = new Request.Builder().url(url).get().build();
        try (Response resp = http.newCall(req).execute()) {
            String body = readBody(resp);
            ensure2xx(resp, path, "GET", body);
            if (body.isEmpty()) {
                return new JsonObject();
            }
            return JsonUtil.fromJson(body, JsonObject.class);
        }
    }

    /**
     * GET 一个数组端点。响应体必须是 JSON 数组；否则抛 {@link IOException}。
     */
    public JsonArray getJsonArray(String path) throws IOException {
        HttpUrl url = baseUrl.newBuilder().addPathSegments(stripLeadingSlash(path)).build();
        Request req = new Request.Builder().url(url).get().build();
        try (Response resp = http.newCall(req).execute()) {
            String body = readBody(resp);
            ensure2xx(resp, path, "GET", body);
            if (body.isEmpty()) {
                return new JsonArray();
            }
            return JsonUtil.fromJson(body, JsonArray.class);
        }
    }

    /**
     * POST 一个 JSON 体，返回响应对象。
     */
    public JsonObject postJson(String path, String jsonBody) throws IOException {
        HttpUrl url = baseUrl.newBuilder().addPathSegments(stripLeadingSlash(path)).build();
        Request req = new Request.Builder()
                .url(url)
                .post(RequestBody.create(jsonBody == null ? "{}" : jsonBody, JSON))
                .build();
        try (Response resp = http.newCall(req).execute()) {
            String body = readBody(resp);
            ensure2xx(resp, path, "POST", body);
            if (body.isEmpty()) {
                return new JsonObject();
            }
            return JsonUtil.fromJson(body, JsonObject.class);
        }
    }

    private static String readBody(Response resp) throws IOException {
        return resp.body() == null ? "" : resp.body().string();
    }

    private static void ensure2xx(Response resp, String path, String method, String body) throws GithubHttpException {
        if (!resp.isSuccessful()) {
            throw new GithubHttpException(method + " " + path + " -> HTTP " + resp.code() + ": " + body);
        }
    }

    private static String stripLeadingSlash(String path) {
        return path == null ? "" : (path.startsWith("/") ? path.substring(1) : path);
    }
}
