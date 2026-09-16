package com.zifang.util.db.sync;

import com.zifang.util.core.lang.exception.BusinessException;
import com.zifang.util.core.meta.BaseStatusCode;
import com.zifang.util.db.meta.DataSourceTableColumnDTO;
import com.zifang.util.db.meta.DataSourceTableDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SQL执行器，提供数据库表的创建、修改、查询等操作
 *
 * @author zifang
 */
public class SqlExecutor {

    private static final Logger log = LoggerFactory.getLogger(SqlExecutor.class);

    private static Map<String, DataSource> dataSourceCache = new LinkedHashMap<>();

    private final DataSource dataSource;

    /**
     * 使用指定数据源构造SqlExecutor
     *
     * @param dataSource 数据源，不能为null
     */
    public SqlExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 获取指定schema下的所有表信息
     *
     * @param schemaMark 数据库schema名称，用于过滤表
     * @return 表信息列表，包含表名和备注
     * @throws BusinessException 获取表信息失败时抛出
     */
    public List<DataSourceTableDTO> fetchTableInfo(String schemaMark) {
        List<DataSourceTableDTO> dataSourceTableDTOS = new ArrayList<>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            ResultSet resultSet = conn.getMetaData().getTables(schemaMark, "%", "%", new String[]{"TABLE"});
            while (resultSet.next()) {
                DataSourceTableDTO dataSourceTableDTO = new DataSourceTableDTO();
                dataSourceTableDTO.setTableName(resultSet.getString("TABLE_NAME"));
                dataSourceTableDTO.setDescriptions(resultSet.getString("REMARKS"));
                dataSourceTableDTOS.add(dataSourceTableDTO);
            }
            return dataSourceTableDTOS;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new BusinessException(BaseStatusCode.FAIL, "获取表信息出错");
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取指定表的字段信息
     *
     * @param schemaMark 数据库schema名称
     * @param tableName  表名称
     * @return 字段信息列表，包含字段名、类型、备注等
     */
    public List<DataSourceTableColumnDTO> fetchTableColumnInfo(String schemaMark, String tableName) {
        List<DataSourceTableColumnDTO> dataSourceTableColumnDTOS = new ArrayList<>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            ResultSet resultSet = conn.getMetaData().getColumns(schemaMark, "%", tableName, null);
            while (resultSet.next()) {
                DataSourceTableColumnDTO dataSourceTableColumnDTO = new DataSourceTableColumnDTO();
                dataSourceTableColumnDTO.setColumnName(resultSet.getString("COLUMN_NAME"));
                dataSourceTableColumnDTO.setColumnType(resultSet.getString("TYPE_NAME").toLowerCase());
                dataSourceTableColumnDTO.setColumnComment(resultSet.getString("REMARKS"));
                dataSourceTableColumnDTOS.add(dataSourceTableColumnDTO);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return dataSourceTableColumnDTOS;
    }

    /**
     * 创建表，默认包含一个id主键字段
     *
     * @param tableName    表名，不能为空
     * @param descriptions 表描述/备注
     * @throws RuntimeException 创建失败时抛出
     */
    public void createTable(String tableName, String descriptions) throws RuntimeException {
        String sql = String.format(
                "create table IF NOT EXISTS %s(id bigint(20) comment '主键') COMMENT='%s' ENGINE=InnoDB DEFAULT CHARSET=utf8;",
                tableName,
                descriptions
        );
        executeDML(dataSource, sql);
    }

    /**
     * 为表添加新字段
     *
     * @param tableName     表名
     * @param columnName    新字段名
     * @param columnType    字段类型，如varchar(255)、int等
     * @param columnComment 字段备注/注释
     * @throws RuntimeException 添加失败时抛出
     */
    public void createTableColumn(String tableName, String columnName, String columnType, String columnComment) throws RuntimeException {
        String sql = String.format("ALTER TABLE %s ADD %s %s comment '%s'",
                tableName,
                columnName,
                columnType,
                columnComment
        );
        executeDML(dataSource, sql);
    }

    /**
     * 修改表字段（可修改字段名、类型、备注）
     *
     * @param tableName           表名
     * @param columnName          原字段名
     * @param targetColumnName    目标字段名
     * @param targetColumnType    目标字段类型
     * @param targetColumnComment 目标字段备注
     * @throws RuntimeException 修改失败时抛出
     */
    public void updateTableColumn(String tableName, String columnName, String targetColumnName, String targetColumnType, String targetColumnComment) throws RuntimeException {
        String sql = String.format("ALTER TABLE %s change %s %s %s comment '%s'",
                tableName,
                columnName,
                targetColumnName,
                targetColumnType,
                targetColumnComment
        );
        executeDML(dataSource, sql);
    }

    /**
     * 删除表字段
     *
     * @param tableName  表名
     * @param columnName 要删除的字段名
     * @throws RuntimeException 删除失败时抛出
     */
    public void removeTableColumn(String tableName, String columnName) throws RuntimeException {
        String sql = String.format("ALTER TABLE %s drop column %s;", tableName, columnName);
        executeDML(dataSource, sql);
    }

    /**
     * 修改表名和表备注
     *
     * @param tableName           原表名
     * @param targetTableName     目标表名
     * @param targetTableComments 目标表备注
     * @throws RuntimeException 修改失败时抛出
     */
    public void updateTable(String tableName, String targetTableName, String targetTableComments) throws RuntimeException {
        String sql1 = String.format("ALTER TABLE %s rename to %s",
                tableName,
                targetTableName
        );

        String sql2 = String.format("alter TABLE %s comment '%s'",
                tableName,
                targetTableComments
        );
        executeDML(dataSource, sql1);
        executeDML(dataSource, sql2);
    }

    /**
     * 删除表
     *
     * @param tableName    表名
     * @param descriptions 表描述（目前未使用）
     * @throws RuntimeException 删除失败时抛出
     */
    public void removeTable(String tableName, String descriptions) throws RuntimeException {
        String sql = String.format(" DROP TABLE %s", tableName);
        executeDML(dataSource, sql);
    }

    /**
     * 获取指定schema下所有表的详细信息，包含每个表的字段列表
     *
     * @param dataSource 数据源
     * @param schemaMark 数据库schema名称
     * @return 表信息列表，每条表信息包含字段详情
     */
    public List<DataSourceTableDTO> deepFetchTableInfo(DataSource dataSource, String schemaMark) {
        List<DataSourceTableDTO> dataSourceTableDTOS = new ArrayList<>();
        List<DataSourceTableColumnDTO> dataSourceTableColumnDTOS = new ArrayList<>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            ResultSet tableResultSet = conn.getMetaData().getTables(schemaMark, "%", "%", new String[]{"TABLE"});
            while (tableResultSet.next()) {
                DataSourceTableDTO dataSourceTableDTO = new DataSourceTableDTO();
                dataSourceTableDTO.setTableName(tableResultSet.getString("TABLE_NAME"));
                dataSourceTableDTO.setDescriptions(tableResultSet.getString("REMARKS"));
                dataSourceTableDTOS.add(dataSourceTableDTO);
            }

            ResultSet columnResultSet = conn.getMetaData().getColumns(schemaMark, "%", "%", null);
            while (columnResultSet.next()) {
                DataSourceTableColumnDTO dataSourceTableColumnDTO = new DataSourceTableColumnDTO();
//                dataSourceTableColumnDTO.setDatasourceCode();
                dataSourceTableColumnDTO.setTableName(columnResultSet.getString("TABLE_NAME"));
                dataSourceTableColumnDTO.setColumnName(columnResultSet.getString("COLUMN_NAME"));
                dataSourceTableColumnDTO.setColumnType(columnResultSet.getString("TYPE_NAME"));
                dataSourceTableColumnDTO.setColumnComment(columnResultSet.getString("REMARKS"));
                dataSourceTableColumnDTOS.add(dataSourceTableColumnDTO);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        Map<String, List<DataSourceTableColumnDTO>> columnMap = dataSourceTableColumnDTOS.stream().collect(Collectors.groupingBy(DataSourceTableColumnDTO::getTableName));
        dataSourceTableDTOS.forEach(e -> e.setColumns(columnMap.get(e.getTableName())));

        return dataSourceTableDTOS;
    }

    /**
     * 执行DML语句（INSERT、UPDATE、DELETE）
     *
     * @param dataSource 数据源
     * @param sql        要执行的DML语句
     * @throws BusinessException 执行失败时抛出
     */
    public void executeDml(DataSource dataSource, String sql) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            Statement smt = connection.createStatement();
            log.info("execute dml sql:{}", sql);
            smt.executeUpdate(sql);
        } catch (SQLException e) {
            log.error(String.format("执行操作数据库失败：执行的sql:%S,错误信息 %S", sql, e.getMessage()));
            throw new BusinessException(BaseStatusCode.FAIL, "执行操作数据库失败，请通知值班人员");
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 执行SELECT查询语句，返回多条记录
     *
     * @param dataSource 数据源
     * @param sql        SELECT查询语句
     * @return 查询结果列表，每条记录为Map结构
     * @throws BusinessException 查询失败时抛出
     */
    public List<Map<String, Object>> selectList(DataSource dataSource, String sql) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sql);
            rs = ps.executeQuery();
            List<Map<String, Object>> l = ResultSetMapper.toMapList(rs);
            return l == null ? new ArrayList<>() : l;
        } catch (SQLException e) {
            log.error(String.format("数据库查询异常：执行的sql:%S,错误信息 %S", sql, e.getMessage()));
            throw new BusinessException(BaseStatusCode.FAIL, "数据库查询异常，请通知值班人员");
        } finally {
            close(rs);
            close(ps);
            close(connection);
        }
    }

    /**
     * 执行COUNT查询，返回记录数
     *
     * @param dataSource 数据源
     * @param sqlCnt     COUNT查询语句
     * @return 记录数，查询失败返回null
     */
    public Integer count(DataSource dataSource, String sqlCnt) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            ps = connection.prepareStatement(sqlCnt);
            rs = ps.executeQuery();
            Object scalar = ResultSetMapper.toScalar(rs);
            return scalar == null ? null : Integer.valueOf(String.valueOf(scalar));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
            close(ps);
            close(connection);
        }
        return null;
    }

    private void executeDML(DataSource dataSource, String sql) throws RuntimeException {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            Statement smt = connection.createStatement();
            smt.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("操作数据表失败，请联系管理员！");
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 静默关闭 JDBC 资源，封装到处可见的 try/catch 样板。
     */
    private static void close(AutoCloseable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前SqlExecutor使用的数据源
     *
     * @return 数据源对象
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "SqlExecutor{dataSourceCache=" + dataSourceCache + ", dataSource=" + dataSource + "}";
    }

    @Override
    /**
     * equals方法。
     *      * @param o Object类型参数
     * @return boolean类型返回值
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SqlExecutor that = (SqlExecutor) o;
        return Objects.equals(dataSourceCache, that.dataSourceCache) &&
                Objects.equals(dataSource, that.dataSource);
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        return Objects.hash(dataSourceCache, dataSource);
    }
}
