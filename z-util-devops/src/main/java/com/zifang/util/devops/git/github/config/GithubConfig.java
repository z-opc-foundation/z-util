package com.zifang.util.devops.git.github.config;

import com.zifang.util.core.lang.StringUtil;

/**
 * GitHub API 配置
 * <p>
 * 用于配置GitHub API连接的认证信息和端点地址，
 * 支持标准GitHub.com和企业版GitHub Enterprise。
 *
 * @author zifang
 * @version 1.0.0
 */
public class GithubConfig {

    /**
     * GitHub Personal Access Token
     */
    private final String token;

    /**
     * GitHub Enterprise API URL（可选，默认为 https://api.github.com）
     */
    private final String apiUrl;

    private GithubConfig(String token, String apiUrl) {
        this.token = token;
        this.apiUrl = apiUrl;
    }

    /**
     * of方法。
     * * @param token String类型参数
     *
     * @return static GithubConfig类型返回值
     */
    public static GithubConfig of(String token) {
        return new GithubConfig(token, "https://api.github.com");
    }

    /**
     * 使用指定 token 和 API URL 创建配置
     *
     * @param token  GitHub Personal Access Token
     * @param apiUrl GitHub Enterprise API 地址（可选）
     * @return GithubConfig 实例
     */
    public static GithubConfig of(String token, String apiUrl) {
        return new GithubConfig(token, apiUrl);
    }

    /**
     * 从环境变量 GITHUB_TOKEN 构建配置
     *
     * @return GithubConfig 实例
     * @throws IllegalStateException 当环境变量 GITHUB_TOKEN 未设置时抛出
     */
    public static GithubConfig fromEnv() {
        String token = System.getenv("GITHUB_TOKEN");
        if (token == null || StringUtil.isBlank(token)) {
            throw new IllegalStateException("GITHUB_TOKEN environment variable is not set");
        }
        return of(token);
    }

    /**
     * getToken方法。
     *
     * @return String类型返回值
     */
    public String getToken() {
        return token;
    }

    /**
     * getApiUrl方法。
     *
     * @return String类型返回值
     */
    public String getApiUrl() {
        return apiUrl;
    }
}
