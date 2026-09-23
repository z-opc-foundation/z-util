package com.zifang.util.db.context;

import com.alibaba.druid.pool.DruidDataSource;
import com.zifang.util.core.lang.exception.BusinessException;
import com.zifang.util.core.meta.BaseStatusCode;
import com.zifang.util.db.dialect.Dialect;
import com.zifang.util.db.dialect.Dialects;
import com.zifang.util.db.meta.DataSourceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源注册表：运行期按 code 管理多个数据源，负责建池、探活与销毁。
 * <p>
 * 与旧的静态工具类不同，它是实例级容器——多应用、多租户、测试沙箱可各持一份，
 * {@link #close()} 时池随之释放；{@link #rebind} 支持凭证或地址变更后的热替换。
 * <p>
 * 定义的 {@code datasourceType} 决定方言（mysql / postgres / h2），
 * 凭证由部署方注入，注册表只在内存中持有。
 *
 * @author zifang
 */
public class DataSourceRegistry implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(DataSourceRegistry.class);

    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    private volatile PoolSpec poolSpec = PoolSpec.defaults();

    /**
     * 设置后续注册使用的连接池参数。
     */
    public DataSourceRegistry poolSpec(PoolSpec poolSpec) {
        this.poolSpec = poolSpec == null ? PoolSpec.defaults() : poolSpec;
        return this;
    }

    public PoolSpec poolSpec() {
        return poolSpec;
    }

    /**
     * 注册数据源并探活。
     *
     * @throws BusinessException code 重复、定义不完整或连不上
     */
    public DataSource register(DataSourceDTO def) {
        String code = requireCode(def);
        Entry entry = open(def, code, poolSpec);
        if (entries.putIfAbsent(code, entry) != null) {
            closeQuietly(entry.dataSource);
            throw new BusinessException(BaseStatusCode.FAIL, "已存在同名数据源: " + code);
        }
        log.info("数据源已注册 [{}] type={} url={}", code, entry.dialect.id(), entry.dialect.buildUrl(def));
        return entry.dataSource;
    }

    /**
     * 用新定义替换同 code 的数据源；探活成功后才切换，旧池立即关闭。
     */
    public DataSource rebind(DataSourceDTO def) {
        String code = requireCode(def);
        Entry entry = open(def, code, poolSpec);
        Entry previous = entries.put(code, entry);
        if (previous != null) {
            closeQuietly(previous.dataSource);
        }
        log.info("数据源已重绑 [{}]", code);
        return entry.dataSource;
    }

    /**
     * 注销并关闭数据源。
     *
     * @return 此前是否存在
     */
    public boolean unregister(String code) {
        Entry removed = entries.remove(code);
        if (removed == null) {
            return false;
        }
        closeQuietly(removed.dataSource);
        log.info("数据源已注销 [{}]", code);
        return true;
    }

    public DataSource get(String code) {
        Entry entry = entries.get(code);
        return entry == null ? null : entry.dataSource;
    }

    /**
     * 取数据源，不存在时抛异常而非返回 null。
     */
    public DataSource require(String code) {
        DataSource dataSource = get(code);
        if (dataSource == null) {
            throw new BusinessException(BaseStatusCode.FAIL,
                    "数据源未注册: " + code + ", 已注册: " + codes());
        }
        return dataSource;
    }

    public DataSourceDTO def(String code) {
        Entry entry = entries.get(code);
        return entry == null ? null : entry.def;
    }

    public Dialect dialect(String code) {
        Entry entry = entries.get(code);
        return entry == null ? null : entry.dialect;
    }

    public boolean contains(String code) {
        return entries.containsKey(code);
    }

    public Set<String> codes() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(entries.keySet()));
    }

    public int size() {
        return entries.size();
    }

    /**
     * 适配既有的 {@link DatasourceFactory} 消费方（Repository 上下文等）。
     */
    public DatasourceFactory factoryFor(String code) {
        if (!contains(code)) {
            throw new BusinessException(BaseStatusCode.FAIL, "数据源未注册: " + code);
        }
        return new DatasourceFactory() {
            @Override
            public DataSource getDatasource() {
                return require(code);
            }

            @Override
            public String toString() {
                return "DatasourceFactory(" + code + ")";
            }
        };
    }

    @Override
    public void close() {
        for (String code : codes()) {
            unregister(code);
        }
    }

    private Entry open(DataSourceDTO def, String code, PoolSpec spec) {
        Dialect dialect = Dialects.resolve(def.getDatasourceType());
        String url = dialect.buildUrl(def);
        DruidDataSource pool = buildPool(dialect, def, url, spec == null ? poolSpec : spec);
        try (Connection conn = pool.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(dialect.validationQuery());
        } catch (SQLException e) {
            closeQuietly(pool);
            throw new BusinessException(BaseStatusCode.FAIL,
                    "数据源连接失败 [" + code + "] " + url + " - " + e.getMessage());
        }
        return new Entry(def, dialect, pool);
    }

    private static String requireCode(DataSourceDTO def) {
        if (def == null) {
            throw new IllegalArgumentException("数据源定义不能为空");
        }
        String code = def.getDatasourceCode();
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("数据源必须指定 datasourceCode");
        }
        return code.trim();
    }

    private static DruidDataSource buildPool(Dialect dialect, DataSourceDTO def, String url, PoolSpec spec) {
        DruidDataSource pool = new DruidDataSource();
        pool.setName(def.getDatasourceCode());
        pool.setDriverClassName(dialect.driverClassName());
        pool.setUrl(url);
        pool.setUsername(def.getUserName());
        pool.setPassword(def.getPw());
        pool.setInitialSize(spec.getInitialSize());
        pool.setMinIdle(spec.getMinIdle());
        pool.setMaxActive(spec.getMaxActive());
        pool.setMaxWait(spec.getMaxWaitMillis());
        pool.setValidationQuery(dialect.validationQuery());
        pool.setValidationQueryTimeout(spec.getValidationTimeoutMillis() / 1000);
        pool.setTestOnBorrow(false);
        pool.setTestOnReturn(false);
        pool.setTestWhileIdle(true);
        pool.setTimeBetweenEvictionRunsMillis(spec.getIdleCheckMillis());
        pool.setMinEvictableIdleTimeMillis(spec.getMinEvictableMillis());
        pool.setKeepAlive(spec.isKeepAlive());
        return pool;
    }

    private static void closeQuietly(DataSource dataSource) {
        if (dataSource instanceof DruidDataSource) {
            ((DruidDataSource) dataSource).close();
        } else if (dataSource instanceof AutoCloseable) {
            try {
                ((AutoCloseable) dataSource).close();
            } catch (Exception e) {
                log.warn("关闭数据源失败: {}", e.getMessage());
            }
        }
    }

    private static final class Entry {

        private final DataSourceDTO def;

        private final Dialect dialect;

        private final DataSource dataSource;

        private Entry(DataSourceDTO def, Dialect dialect, DataSource dataSource) {
            this.def = def;
            this.dialect = dialect;
            this.dataSource = dataSource;
        }
    }
}
