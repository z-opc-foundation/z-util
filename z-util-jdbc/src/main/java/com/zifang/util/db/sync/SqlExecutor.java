package com.zifang.util.db.sync;

import com.zifang.util.core.lang.exception.BusinessException;
import com.zifang.util.core.meta.BaseStatusCode;
import com.zifang.util.db.meta.DataSourceTableColumnDTO;
import com.zifang.util.db.meta.DataSourceTableDTO;
import com.zifang.util.db.support.Identifiers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 表级 DDL 与裸 SQL 执行器（MySQL 语法）。
 * <p>
 * 结构化取数请用 {@link com.zifang.util.db.query.DynamicQuery}；本类保留给
 * 低代码"按定义动态建表/改表"这类必须发 DDL 的场景。表名列名先过
 * {@link Identifiers} 白名单再拼接——DDL 无法参数化，这里是唯一的注入闸口。
 *
 * @author zifang
 */
public class SqlExecutor {

    private static final Logger log = LoggerFactory.getLogger(SqlExecutor.class);

    private final DataSource dataSource;

    /**
     * 使用指定数据源构造SqlExecutor
     *
     * @param dataSource 数据源，不能为null
     */
    public SqlExecutor(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource 不能为空");
        }
        this.dataSource = dataSource;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * 获取指定schema下的所有表信息
     *
     * @param schemaMark 数据库schema名称，用于过滤表
     * @return 表信息列表，包含表名和备注
     * @throws BusinessException 获取表信息失败时抛出
     */
    public List<DataSourceTableDTO> fetchTableInfo(String schemaMark) {
        List<DataSourceTableDTO> tables = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             ResultSet resultSet = conn.getMetaData().getTables(schemaMark, "%", "%", new String[]{"TABLE"})) {
            while (resultSet.next()) {
                DataSourceTableDTO table = new DataSourceTableDTO();
                table.setTableName(resultSet.getString("TABLE_NAME"));
                table.setDescriptions(resultSet.getString("REMARKS"));
                tables.add(table);
            }
            return tables;
        } catch (SQLException e) {
            throw new BusinessException(BaseStatusCode.FAIL, "获取表信息出错: " + e.getMessage());
        }
    }

    /**
     * 获取指定表的字段信息
     *
     * @param schemaMark 数据库schema名称
     * @param tableName  表名称
     * @return 字段信息列表，包含字段名、类型、备注
     * @throws BusinessException 获取失败时抛出
     */
    public List<DataSourceTableColumnDTO> fetchTableColumnInfo(String schemaMark, String tableName) {
        List<DataSourceTableColumnDTO> columns = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             ResultSet resultSet = conn.getMetaData().getColumns(schemaMark, "%", safeTable(tableName), null)) {
            while (resultSet.next()) {
                DataSourceTableColumnDTO column = new DataSourceTableColumnDTO();
                column.setTableName(resultSet.getString("TABLE_NAME"));
                column.setColumnName(resultSet.getString("COLUMN_NAME"));
                column.setColumnType(resultSet.getString("TYPE_NAME").toLowerCase());
                column.setColumnComment(resultSet.getString("REMARKS"));
                columns.add(column);
            }
            return columns;
        } catch (SQLException e) {
            throw new BusinessException(BaseStatusCode.FAIL, "获取字段信息出错: " + e.getMessage());
        }
    }

    /**
     * 获取指定schema下所有表的详细信息，包含每个表的字段列表
     *
     * @param dataSource 数据源
     * @param schemaMark 数据库schema名称
     * @return 表信息列表，每条表信息包含字段详情
     * @throws BusinessException 获取失败时抛出
     */
    public List<DataSourceTableDTO> deepFetchTableInfo(DataSource dataSource, String schemaMark) {
        List<DataSourceTableDTO> tables = new ArrayList<>();
        List<DataSourceTableColumnDTO> columns = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            try (ResultSet tableResultSet = conn.getMetaData().getTables(schemaMark, "%", "%", new String[]{"TABLE"})) {
                while (tableResultSet.next()) {
                    DataSourceTableDTO table = new DataSourceTableDTO();
                    table.setTableName(tableResultSet.getString("TABLE_NAME"));
                    table.setDescriptions(tableResultSet.getString("REMARKS"));
                    tables.add(table);
                }
            }
            try (ResultSet columnResultSet = conn.getMetaData().getColumns(schemaMark, "%", "%", null)) {
                while (columnResultSet.next()) {
                    DataSourceTableColumnDTO column = new DataSourceTableColumnDTO();
                    column.setTableName(columnResultSet.getString("TABLE_NAME"));
                    column.setColumnName(columnResultSet.getString("COLUMN_NAME"));
                    column.setColumnType(columnResultSet.getString("TYPE_NAME"));
                    column.setColumnComment(columnResultSet.getString("REMARKS"));
                    columns.add(column);
                }
            }
        } catch (SQLException e) {
            throw new BusinessException(BaseStatusCode.FAIL, "获取库表信息出错: " + e.getMessage());
        }
        Map<String, List<DataSourceTableColumnDTO>> byTable = columns.stream()
                .collect(Collectors.groupingBy(DataSourceTableColumnDTO::getTableName));
        for (DataSourceTableDTO table : tables) {
            table.setColumns(byTable.get(table.getTableName()));
        }
        return tables;
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
                "create table IF NOT EXISTS %s(id bigint(20) comment '主键') COMMENT='%s' ENGINE=InnoDB DEFAULT CHARSET=utf8",
                safeTable(tableName), safeComment(descriptions));
        executeDML(sql);
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
                safeTable(tableName), safeColumn(columnName), safeType(columnType), safeComment(columnComment));
        executeDML(sql);
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
    public void updateTableColumn(String tableName, String columnName, String targetColumnName,
                                  String targetColumnType, String targetColumnComment) throws RuntimeException {
        String sql = String.format("ALTER TABLE %s change %s %s %s comment '%s'",
                safeTable(tableName), safeColumn(columnName), safeColumn(targetColumnName),
                safeType(targetColumnType), safeComment(targetColumnComment));
        executeDML(sql);
    }

    /**
     * 删除表字段
     *
     * @param tableName  表名
     * @param columnName 要删除的字段名
     * @throws RuntimeException 删除失败时抛出
     */
    public void removeTableColumn(String tableName, String columnName) throws RuntimeException {
        executeDML(String.format("ALTER TABLE %s drop column %s", safeTable(tableName), safeColumn(columnName)));
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
        executeDML(String.format("ALTER TABLE %s rename to %s", safeTable(tableName), safeTable(targetTableName)));
        executeDML(String.format("alter TABLE %s comment '%s'", safeTable(tableName), safeComment(targetTableComments)));
    }

    /**
     * 删除表
     *
     * @param tableName    表名
     * @param descriptions 表描述（目前未使用）
     * @throws RuntimeException 删除失败时抛出
     */
    public void removeTable(String tableName, String descriptions) throws RuntimeException {
        executeDML(String.format("DROP TABLE %s", safeTable(tableName)));
    }

    /**
     * 执行DML语句（INSERT、UPDATE、DELETE）
     *
     * @param dataSource 数据源
     * @param sql        要执行的DML语句
     * @throws BusinessException 执行失败时抛出
     */
    public void executeDml(DataSource dataSource, String sql) {
        log.info("execute dml sql:{}", sql);
        try (Connection connection = dataSource.getConnection();
             Statement smt = connection.createStatement()) {
            smt.executeUpdate(sql);
        } catch (SQLException e) {
            log.error("执行操作数据库失败：执行的sql:{},错误信息 {}", sql, e.getMessage());
            throw new BusinessException(BaseStatusCode.FAIL, "执行操作数据库失败，请通知值班人员");
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
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Map<String, Object>> rows = ResultSetMapper.toMapList(rs);
            return rows == null ? new ArrayList<>() : rows;
        } catch (SQLException e) {
            log.error("数据库查询异常：执行的sql:{},错误信息 {}", sql, e.getMessage());
            throw new BusinessException(BaseStatusCode.FAIL, "数据库查询异常，请通知值班人员");
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
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sqlCnt);
             ResultSet rs = ps.executeQuery()) {
            Object scalar = ResultSetMapper.toScalar(rs);
            return scalar == null ? null : Integer.valueOf(String.valueOf(scalar));
        } catch (SQLException e) {
            log.error("统计失败：执行的sql:{},错误信息 {}", sqlCnt, e.getMessage());
            return null;
        }
    }

    private void executeDML(String sql) throws RuntimeException {
        try (Connection connection = dataSource.getConnection();
             Statement smt = connection.createStatement()) {
            smt.executeUpdate(sql);
        } catch (SQLException e) {
            log.error("操作数据表失败：执行的sql:{},错误信息 {}", sql, e.getMessage());
            throw new RuntimeException("操作数据表失败，请联系管理员！");
        }
    }

    private static String safeTable(String tableName) {
        String[] parts = Identifiers.parts(tableName);
        return String.join(".", parts);
    }

    private static String safeColumn(String columnName) {
        return Identifiers.require(columnName);
    }

    /**
     * 字段类型是"varchar(255)"这类带括号的形式，白名单只放行类型名与长度。
     */
    private static String safeType(String columnType) {
        if (columnType == null || !columnType.matches("[A-Za-z0-9_]{1,32}(\\([0-9]{1,5}(,[0-9]{1,5})?\\))?")) {
            throw new IllegalArgumentException("非法字段类型: " + columnType);
        }
        return columnType;
    }

    /**
     * 注释会被单引号包裹，去掉引号与注释符防止闭合逃逸。
     */
    private static String safeComment(String comment) {
        if (comment == null) {
            return "";
        }
        return comment.replace("'", "").replace("\"", "").replace(";", "").replace("\\", "");
    }

    @Override
    public String toString() {
        return "SqlExecutor{dataSource=" + dataSource + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SqlExecutor that = (SqlExecutor) o;
        return Objects.equals(dataSource, that.dataSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataSource);
    }
}
