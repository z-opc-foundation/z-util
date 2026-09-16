package com.zifang.util.db.context;

import javax.sql.DataSource;

/**
 * 数据源工厂接口
 * <p>
 * 用于创建和获取数据源实例的工厂接口。该接口是数据源管理的重要组成部分，
 * 提供了数据源对象的统一创建和访问方式。
 *
 * <p>实现该接口的类可以：
 * <ul>
 *   <li>根据配置信息动态创建数据源</li>
 *   <li>管理数据源的生命周期（创建、初始化、销毁）</li>
 *   <li>提供数据源的缓存和复用机制</li>
 *   <li>支持多数据源环境下的数据源切换</li>
 * </ul>
 *
 * <p>典型实现包括：
 * <ul>
 *   <li>基于Druid连接池的数据源工厂</li>
 *   <li>基于HikariCP的数据源工厂</li>
 *   <li>基于C3P0的数据源工厂</li>
 *   <li>支持动态配置的数据源工厂</li>
 * </ul>
 *
 * @author zifang
 * @see DataSourceManager
 * @see javax.sql.DataSource
 */
public interface DatasourceFactory {

    /**
     * 获取数据源实例
     *
     * @return 数据源对象
     */
    DataSource getDatasource();

}
