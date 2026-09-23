package com.zifang.util.db.query;

import com.zifang.util.core.lang.exception.BusinessException;
import com.zifang.util.core.meta.BaseStatusCode;
import com.zifang.util.db.dialect.Dialect;
import com.zifang.util.db.dialect.Dialects;
import com.zifang.util.db.dialect.SqlType;
import com.zifang.util.db.support.Identifiers;
import com.zifang.util.db.sync.ResultSetMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 动态查询执行器：不依赖实体类与 ORM 映射，输入 {@link Query} 或 {@link SqlSpec}，
 * 输出 {@code List<Map<String, Object>>}，供报表、低代码、导出等场景直接消费。
 * <pre>
 *   DynamicQuery dq = new DynamicQuery(registry.require("z-base-db-report"));
 *   List&lt;Map&lt;String, Object&gt;&gt; rows = dq.list(
 *       Query.select("id", "amount").from("t_order").where(Criteria.ge("amount", 100)));
 *   SqlSpec spec = SqlTemplate.of("SELECT * FROM t_order WHERE biz_date = ${day}")
 *                             .bind(Collections.singletonMap("day", date));
 * </pre>
 *
 * @author zifang
 */
public class DynamicQuery {

    private static final Logger log = LoggerFactory.getLogger(DynamicQuery.class);

    private final DataSource dataSource;

    private volatile Dialect dialect;

    private volatile QueryCompiler compiler;

    private int maxRows;

    /**
     * 方言未知的构造方式：首次使用时从连接元数据识别。
     */
    public DynamicQuery(DataSource dataSource) {
        this(dataSource, null);
    }

    public DynamicQuery(DataSource dataSource, Dialect dialect) {
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource 不能为空");
        }
        this.dataSource = dataSource;
        this.dialect = dialect;
    }

    public DataSource dataSource() {
        return dataSource;
    }

    /**
     * 单次读取的行数上限，0 表示不限制。内存语义的调用方应显式设置。
     */
    public DynamicQuery maxRows(int maxRows) {
        if (maxRows < 0) {
            throw new IllegalArgumentException("maxRows 不能为负");
        }
        this.maxRows = maxRows;
        return this;
    }

    public int maxRows() {
        return maxRows;
    }

    public Dialect dialect() {
        Dialect current = dialect;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            if (dialect == null) {
                try (Connection conn = dataSource.getConnection()) {
                    dialect = Dialects.detect(conn);
                } catch (SQLException e) {
                    throw new BusinessException(BaseStatusCode.FAIL, "识别方言失败: " + e.getMessage());
                }
            }
            current = dialect;
        }
        return current;
    }

    public QueryCompiler compiler() {
        QueryCompiler current = compiler;
        if (current == null) {
            synchronized (this) {
                if (compiler == null) {
                    compiler = new QueryCompiler(dialect());
                }
                current = compiler;
            }
        }
        return current;
    }

    // ---------------------------------------------------------------- 读

    public List<Map<String, Object>> list(Query query) {
        return list(compiler().compile(query));
    }

    public List<Map<String, Object>> list(final SqlSpec spec) {
        return execute(spec, new ResultSetWork<List<Map<String, Object>>>() {
            @Override
            public List<Map<String, Object>> run(ResultSet rs) throws SQLException {
                return ResultSetMapper.toMapList(rs);
            }
        });
    }

    /**
     * 取首行，无数据返回 null；需要"恰好一行"的语义请自行加 limit(1) 并校验。
     */
    public Map<String, Object> one(Query query) {
        List<Map<String, Object>> rows = list(query);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public Map<String, Object> one(SqlSpec spec) {
        List<Map<String, Object>> rows = list(spec);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * 取首行首列，无数据返回 null。
     */
    public Object scalar(SqlSpec spec) {
        return execute(spec, new ResultSetWork<Object>() {
            @Override
            public Object run(ResultSet rs) throws SQLException {
                return ResultSetMapper.toScalar(rs);
            }
        });
    }

    public Object scalar(Query query) {
        return scalar(compiler().compile(query));
    }

    public long count(Query query) {
        Object value = scalar(compiler().compileCount(query));
        if (value == null) {
            return 0L;
        }
        return value instanceof Number ? ((Number) value).longValue() : Long.parseLong(String.valueOf(value));
    }

    /**
     * 分页取数：query 上的 limit/offset 决定窗口，总数按同条件另算。
     */
    public Page<Map<String, Object>> page(Query query) {
        long total = count(query);
        List<Map<String, Object>> rows = total == 0
                ? new ArrayList<Map<String, Object>>()
                : list(query);
        return new Page<>(rows, total, Math.max(query.offset(), 0), query.limit());
    }

    // ---------------------------------------------------------------- 写

    public int insert(String table, Map<String, ?> values) {
        return executeUpdate(compiler().compileInsert(table, values));
    }

    public int update(String table, Map<String, ?> values, Predicate where) {
        return executeUpdate(compiler().compileUpdate(table, values, where));
    }

    public int delete(String table, Predicate where) {
        return executeUpdate(compiler().compileDelete(table, where));
    }

    public int executeUpdate(SqlSpec spec) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(spec.sql())) {
            bind(ps, spec.params());
            long started = System.currentTimeMillis();
            int affected = ps.executeUpdate();
            if (log.isDebugEnabled()) {
                log.debug("写入完成 sql:[{}] params:{} affected:{} cost:{}ms",
                        spec.sql(), spec.params(), affected, System.currentTimeMillis() - started);
            }
            return affected;
        } catch (SQLException e) {
            throw failure("写入", spec, e);
        }
    }

    // ---------------------------------------------------------------- 元数据

    /**
     * 当前库可见的表与视图。视图一并返回：报表和低代码把视图当作一等数据单元。
     */
    public List<String> tables() {
        List<String> names = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(conn.getCatalog(), conn.getSchema(), "%",
                    new String[]{"TABLE", "VIEW"})) {
                while (rs.next()) {
                    names.add(rs.getString("TABLE_NAME"));
                }
            }
            return names;
        } catch (SQLException e) {
            throw new BusinessException(BaseStatusCode.FAIL, "读取表清单失败: " + e.getMessage());
        }
    }

    /**
     * 表结构：列名 → 归一类型，保持数据库中的列序。
     * 传 {@code qualifier.table} 时按该限定名定位。
     */
    public Map<String, SqlType> columns(String table) {
        String[] segments = Identifiers.parts(table);
        String name = segments[segments.length - 1];
        Map<String, SqlType> columns = new LinkedHashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            if (segments.length == 1) {
                readColumns(meta, conn.getCatalog(), conn.getSchema(), name, columns);
            } else {
                // 限定名前缀是 catalog 还是 schema 由库决定: MySQL 把库当 catalog, PG/H2 把库当 schema
                readColumns(meta, segments[0], null, name, columns);
                if (columns.isEmpty()) {
                    readColumns(meta, conn.getCatalog(), segments[0], name, columns);
                }
            }
            return columns;
        } catch (SQLException e) {
            throw new BusinessException(BaseStatusCode.FAIL, "读取表结构失败: " + table + " - " + e.getMessage());
        }
    }

    private static void readColumns(DatabaseMetaData meta, String catalog, String schema,
                                    String table, Map<String, SqlType> into) throws SQLException {
        try (ResultSet rs = meta.getColumns(catalog, schema, table, "%")) {
            while (rs.next()) {
                into.put(rs.getString("COLUMN_NAME"), SqlType.of(rs.getInt("DATA_TYPE")));
            }
        }
    }

    // ---------------------------------------------------------------- 内部

    private interface ResultSetWork<T> {
        T run(ResultSet rs) throws SQLException;
    }

    private <T> T execute(SqlSpec spec, ResultSetWork<T> work) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(spec.sql())) {
            bind(ps, spec.params());
            if (maxRows > 0) {
                ps.setMaxRows(maxRows);
            }
            if (log.isDebugEnabled()) {
                log.debug("查询 sql:[{}] params:{}", spec.sql(), spec.params());
            }
            try (ResultSet rs = ps.executeQuery()) {
                return work.run(rs);
            }
        } catch (SQLException e) {
            throw failure("查询", spec, e);
        }
    }

    private static void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object value = params.get(i);
            if (value == null) {
                ps.setNull(i + 1, Types.NULL);
            } else {
                ps.setObject(i + 1, value);
            }
        }
    }

    /**
     * SQL 进日志、异常只带库侧原因，避免参数值随异常外抛。
     */
    private static BusinessException failure(String action, SqlSpec spec, SQLException cause) {
        log.error("执行{}失败 sql:[{}] params:{} 原因:{}", action, spec.sql(), spec.params(), cause.getMessage());
        return new BusinessException(BaseStatusCode.FAIL, "数据库" + action + "失败: " + cause.getMessage());
    }
}
