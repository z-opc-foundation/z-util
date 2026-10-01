package com.zifang.util.devops.git.github.holder;

import com.zifang.util.devops.git.github.config.GithubConfig;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;

/**
 * GitHub 客户端单例持有者
 * <p>
 * 负责GitHub API客户端的初始化和单例管理，
 * 支持从环境变量或自定义配置创建客户端实例。
 *
 * @author zifang
 * @version 1.0.0
 */
public class GithubApiHolder {

    /**
     * GithubApiHolder方法。
     * * @param null Object类型参数
     *
     * @return static GithubApiHolder INSTANCE = new类型返回值
     */
    public static GithubApiHolder INSTANCE = new GithubApiHolder(null);

    private final GitHub github;
    private final GithubConfig config;

    private GithubApiHolder(GithubConfig config) {
        this.config = config;
        this.github = config == null ? null : build(config);
    }

    private GithubApiHolder() {
        this(GithubConfig.fromEnv());
    }

    /**
     * 获取 GithubApiHolder 单例实例
     *
     * @return GithubApiHolder 实例
     */
    public static GithubApiHolder getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化 GithubApiHolder（仅可调用一次）
     *
     * @param config GitHub 配置
     * @throws IllegalStateException 当已经初始化过再次调用时抛出
     */
    public static void init(GithubConfig config) {
        if (INSTANCE.config != null) {
            throw new IllegalStateException("GithubApiHolder has already been initialized");
        }
        INSTANCE = new GithubApiHolder(config);
    }

    /**
     * 重置 GithubApiHolder 到未初始化状态
     */
    public static void reset() {
        INSTANCE = new GithubApiHolder(null);
    }

    private GitHub build(GithubConfig config) {
        try {
            GitHubBuilder builder = new GitHubBuilder()
                    .withOAuthToken(config.getToken());
            if (config.getApiUrl() != null && !config.getApiUrl().equals("https://api.github.com")) {
                builder.withEndpoint(config.getApiUrl());
            }
            return builder.build();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to build GitHub client", e);
        }
    }

    /**
     * 获取 GitHub 客户端实例
     *
     * @return GitHub 客户端对象，可能为 null（如果未初始化）
     */
    public GitHub getGithub() {
        return github;
    }

    /**
     * 获取 GitHub 配置
     *
     * @return GithubConfig 配置对象，可能为 null
     */
    public GithubConfig getConfig() {
        return config;
    }
}
