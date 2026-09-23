package com.zifang.util.db.dialect;

import com.zifang.util.db.meta.DataSourceDTO;

/**
 * PostgreSQL 方言。
 * <p>
 * PG 会把未加引号的标识符折叠成小写，本类所有表名列名都带双引号，
 * 因此大小写敏感的建表方式（如 z-lc 动态建表）不会被静默改写。
 *
 * @author zifang
 */
public class PostgresDialect extends AbstractDialect {

    public static final String ID = "postgres";

    private static final String DRIVER = "org.postgresql.Driver";

    private static final String URL = "jdbc:postgresql://%s:%d/%s";

    public PostgresDialect() {
        super(ID, DRIVER, '"');
    }

    @Override
    public String buildUrl(DataSourceDTO def) {
        String host = Check.text(def.getDatasourceUrl(), "datasourceUrl");
        int port = Check.port(def.getPortNumber(), 5432);
        String db = Check.text(def.getSchemaMark(), "schemaMark");
        return String.format(URL, host, port, db);
    }
}
