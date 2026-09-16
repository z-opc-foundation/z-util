package com.zifang.util.workflow.conponents;

/**
 * 工作流应用程序构建器。
 * <p>
 * 该类负责构建和配置工作流应用程序实例，
 * 提供链式API来组装工作流的各种组件。
 * <p>
 * 使用示例：
 * <pre>
 * WorkFlowApplicationBuilder builder = new WorkFlowApplicationBuilder();
 * builder.withEngineType("spark")
 *        .withCacheType("redis")
 *        .build();
 * </pre>
 *
 * @see WorkFlowApplication
 * @see WorkFlowApplicationContext
 */
public class WorkFlowApplicationBuilder {

    /**
     * 使用指定的引擎类型构建工作流应用程序。
     *
     * @param engineType 引擎类型，如 "spark"、"java"、"python"
     * @return 构建器实例，支持链式调用
     */
    public WorkFlowApplicationBuilder withEngineType(String engineType) {
        return this;
    }

    /**
     * 使用指定的缓存类型构建工作流应用程序。
     *
     * @param cacheType 缓存类型，如 "memory"、"redis"
     * @return 构建器实例，支持链式调用
     */
    public WorkFlowApplicationBuilder withCacheType(String cacheType) {
        return this;
    }

    /**
     * 构建工作流应用程序实例。
     *
     * @return 工作流应用程序实例
     */
    public WorkFlowApplication build() {
        return new WorkFlowApplication();
    }
}
