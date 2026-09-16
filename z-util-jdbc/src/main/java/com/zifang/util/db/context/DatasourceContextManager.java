package com.zifang.util.db.context;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 数据源上下文管理器，用于管理多个数据源上下文
 */
public class DatasourceContextManager {

    public static final String DEFAULT = "default";

    /**
     * 持久层上下文 内部的所有的数据源上下文
     */
    private static final Map<String, DataSourceContext> dataSourceContextMap = new LinkedHashMap<>();

    /**
     * 注册一个数据源上下文
     *
     * @param dataSourceContextName 上下文名称，唯一标识，不能为空
     * @param dataSourceFactory     数据源上下文实例
     * @throws RuntimeException 如果名称已存在
     */
    public static void register(String dataSourceContextName, DataSourceContext dataSourceFactory) {
        assert dataSourceContextName != null && !"".equals(dataSourceContextName);
        if (dataSourceContextMap.get(dataSourceContextName) != null) {
            throw new RuntimeException("重复注册数据源上下文:key=" + dataSourceContextName);
        }
        dataSourceContextMap.put(dataSourceContextName, dataSourceFactory);
    }

    /**
     * 获取指定名称的数据源上下文
     *
     * @param dataSourceContextName 上下文名称
     * @return 数据源上下文，不存在返回null
     */
    public static DataSourceContext fetchContext(String dataSourceContextName) {
        return dataSourceContextMap.get(dataSourceContextName);
    }
}
