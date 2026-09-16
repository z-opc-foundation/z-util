package com.zifang.util.db.context;

import com.alibaba.druid.pool.DruidDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据源管理器，负责注册和管理多个数据源
 */
public class DataSourceManager {

    private static final String MYSQL8 = "com.mysql.cj.jdbc.Driver";
    private static final String DEFAULT_PROPERTIES = "remarks=true;useInformationSchema=true";
    private static final String defaultJDBCParam = "useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&zeroDateTimeBehavior=CONVERT_TO_NULL";
    private static final String jdbcUrl = "jdbc:mysql://%s:%d/%s?%s";

    private final static int DEFAULT_INITIAL_SIZE = 10;
    private final static int DEFAULT_MAX_ACTIVE_SIZE = 30;
    private static final String USE_INFORMATION_SCHEMA = "&useInformationSchema=true";
    private static final String DATASOURCE_URL_PREFIX = "spring.datasource.";

    private static Map<String, DataSource> dataSourceMap = new HashMap<>();

    /**
     * 注册一个新的数据源
     *
     * @param name          数据源名称，唯一标识，不能为空
     * @param datasourceUrl 数据库地址，不能为空
     * @param port          端口号
     * @param schemaMark    数据库名称
     * @param username      用户名，不能为空
     * @param password      密码
     * @return 创建的数据源对象，注册失败返回null
     * @throws RuntimeException 如果必要参数为空或数据源名称已存在
     */
    public static DataSource registerDataSource(String name, String datasourceUrl, Integer port, String schemaMark, String username, String password) {

        if (name == null || datasourceUrl == null || username == null) {
            throw new RuntimeException("必要参数不能为空,name=" + name + " datasourceUrl=" + datasourceUrl + " username=" + username);
        }
        if (dataSourceMap.get(name) != null) {
            throw new RuntimeException("存在同名数据源");
        }

        String url = String.format(jdbcUrl, datasourceUrl, port, schemaMark, defaultJDBCParam);
        try {
            //1. 构建数据源对象
            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setDriverClassName(MYSQL8);
            dataSource.setUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            dataSource.setConnectionProperties(DEFAULT_PROPERTIES);
            dataSource.setTestOnBorrow(false);//借用连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
            dataSource.setTestOnReturn(false);//归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
            // 空闲连接回收器进行检验
            dataSource.setTestWhileIdle(true);//如果检测失败，则连接将被从池中去除
            dataSource.setTimeBetweenEvictionRunsMillis(60000);//1分钟
            dataSource.setMaxActive(DEFAULT_MAX_ACTIVE_SIZE);
            dataSource.setInitialSize(DEFAULT_INITIAL_SIZE);
            dataSource.setConnectTimeout(50000);
            dataSource.setConnectionErrorRetryAttempts(0);
            dataSource.setBreakAfterAcquireFailure(true);

            //2. 得到连接对象
            Connection connection = dataSource.getConnection();
            connection.close();

            //3. 置入缓存
            dataSourceMap.put(name, dataSource);
            return dataSource;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
