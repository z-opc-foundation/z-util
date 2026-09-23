package com.zifang.util.db.dialect;

import com.zifang.util.core.lang.exception.BusinessException;
import com.zifang.util.core.meta.BaseStatusCode;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 方言注册表：按 id、JDBC URL 或连接元数据定位方言，并允许注册自定义实现。
 *
 * @author zifang
 */
public final class Dialects {

    private static final Map<String, Dialect> REGISTRY = new ConcurrentHashMap<>();

    private static final Map<String, String> ALIASES = new LinkedHashMap<>();

    static {
        ALIASES.put("mariadb", MySqlDialect.ID);
        ALIASES.put("postgresql", PostgresDialect.ID);
        ALIASES.put("pg", PostgresDialect.ID);
        register(new MySqlDialect());
        register(new PostgresDialect());
        register(new H2Dialect());
    }

    private Dialects() {
    }

    /**
     * 注册（或覆盖）一个方言实现。
     */
    public static void register(Dialect dialect) {
        if (dialect == null || dialect.id() == null || dialect.id().isEmpty()) {
            throw new IllegalArgumentException("方言及其 id 不能为空");
        }
        REGISTRY.put(normalize(dialect.id()), dialect);
    }

    /**
     * 按 id 取方言，未知 id 抛异常。
     */
    public static Dialect resolve(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new BusinessException(BaseStatusCode.FAIL, "数据源未指定类型 (datasourceType)");
        }
        String key = normalize(id);
        String alias = ALIASES.get(key);
        if (alias != null) {
            key = alias;
        }
        Dialect dialect = REGISTRY.get(key);
        if (dialect == null) {
            throw new BusinessException(BaseStatusCode.FAIL,
                    "不支持的数据源类型: " + id + ", 已注册: " + ids());
        }
        return dialect;
    }

    /**
     * 按 JDBC URL 前缀取方言，用于 URL 直连场景。
     */
    public static Dialect forJdbcUrl(String jdbcUrl) {
        if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:")) {
            throw new BusinessException(BaseStatusCode.FAIL, "非法 JDBC URL: " + jdbcUrl);
        }
        String body = jdbcUrl.substring("jdbc:".length());
        int cut = body.indexOf(':');
        String head = (cut < 0 ? body : body.substring(0, cut)).toLowerCase(Locale.ROOT);
        String alias = ALIASES.get(head);
        Dialect dialect = REGISTRY.get(alias != null ? alias : head);
        if (dialect == null) {
            throw new BusinessException(BaseStatusCode.FAIL, "无法从 URL 识别方言: " + jdbcUrl);
        }
        return dialect;
    }

    /**
     * 从已有连接的元数据识别方言。
     */
    public static Dialect detect(Connection conn) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            String product = meta.getDatabaseProductName();
            return resolve(product);
        } catch (SQLException e) {
            throw new BusinessException(BaseStatusCode.FAIL, "识别数据库方言失败: " + e.getMessage());
        }
    }

    public static Set<String> ids() {
        return Collections.unmodifiableSet(REGISTRY.keySet());
    }

    private static String normalize(String id) {
        return id.trim().toLowerCase(Locale.ROOT);
    }
}
