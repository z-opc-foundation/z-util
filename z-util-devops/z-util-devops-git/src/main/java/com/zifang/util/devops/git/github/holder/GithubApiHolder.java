package com.zifang.util.devops.git.github.holder;

import com.zifang.util.devops.git.github.config.GithubConfig;
import com.zifang.util.devops.git.github.http.GithubHttpClient;

/**
 * GitHub 客户端单例持有者。
 *
 * <p>v0：客户端类型从原 github-api 的 {@code GitHub} 切换为自研 {@link GithubHttpClient}
 * （见 {@code _doc/001_arch/github-api-migration.md}）。测试用
 * {@code assertNotNull(holder.getGithub())} 仍兼容。
 */
public class GithubApiHolder {

    public static GithubApiHolder INSTANCE = new GithubApiHolder(null);

    private final GithubHttpClient github;
    private final GithubConfig config;

    private GithubApiHolder(GithubConfig config) {
        this.config = config;
        this.github = config == null ? null : build(config);
    }

    private GithubApiHolder() {
        this(GithubConfig.fromEnv());
    }

    public static GithubApiHolder getInstance() {
        return INSTANCE;
    }

    public static void init(GithubConfig config) {
        if (INSTANCE.config != null) {
            throw new IllegalStateException("GithubApiHolder has already been initialized");
        }
        INSTANCE = new GithubApiHolder(config);
    }

    public static void reset() {
        INSTANCE = new GithubApiHolder(null);
    }

    private GithubHttpClient build(GithubConfig config) {
        return new GithubHttpClient(config.getApiUrl(), config.getToken());
    }

    /**
     * 获取 GitHub 客户端实例，可能为 null（未初始化时）。
     */
    public GithubHttpClient getGithub() {
        return github;
    }

    public GithubConfig getConfig() {
        return config;
    }
}
