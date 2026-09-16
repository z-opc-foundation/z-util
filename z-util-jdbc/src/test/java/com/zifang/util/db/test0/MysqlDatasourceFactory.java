package com.zifang.util.db.test0;

import com.alibaba.druid.pool.DruidDataSource;
import com.zifang.util.db.context.DatasourceFactory;

import javax.sql.DataSource;

/**
 * MysqlDatasourceFactory类。
 */
public class MysqlDatasourceFactory implements DatasourceFactory {

    private DruidDataSource dataSource = null;

    @Override
    /**
     * getDatasource方法。
     * @return DataSource类型返回值
     */
    public DataSource getDatasource() {

        try {
            if (dataSource == null) {
                dataSource = new DruidDataSource();
                //设置连接参数
                dataSource.setUrl("jdbc:mysql://47.104.173.209:3308/micro?useSSL=false&autoReconnect=true");
                dataSource.setDriverClassName("com.mysql.jdbc.Driver");
                dataSource.setUsername("micro");
                dataSource.setPassword("123456");
                //配置初始化大小、最小、最大
                dataSource.setInitialSize(20);
                dataSource.setMinIdle(10);
                dataSource.setMaxActive(50);
                //连接泄漏监测
                dataSource.setRemoveAbandoned(true);
                dataSource.setRemoveAbandonedTimeout(30);
                //配置获取连接等待超时的时间
                dataSource.setMaxWait(20000);
                //配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
                dataSource.setTimeBetweenEvictionRunsMillis(20000);
                //防止过期
                dataSource.setValidationQuery("select 1 from dual");
                dataSource.setTestWhileIdle(true);
                dataSource.setTestOnBorrow(true);
                dataSource.setLogAbandoned(true);
                dataSource.setKeepAlive(true);

                dataSource.setFilters("stat");
            } else {
                return dataSource;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataSource;
    }
}
