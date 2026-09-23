package com.zifang.util.db.dialect;

import com.zifang.util.db.meta.DataSourceDTO;

/**
 * MySQL 方言。驱动与连接参数沿用 z-util-jdbc 既有约定。
 *
 * @author zifang
 */
public class MySqlDialect extends AbstractDialect {

    public static final String ID = "mysql";

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    private static final String URL = "jdbc:mysql://%s:%d/%s?%s";

    private static final String PARAMS =
            "useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai"
                    + "&zeroDateTimeBehavior=CONVERT_TO_NULL&allowPublicKeyRetrieval=true";

    public MySqlDialect() {
        super(ID, DRIVER, '`');
    }

    @Override
    public String buildUrl(DataSourceDTO def) {
        String host = Check.text(def.getDatasourceUrl(), "datasourceUrl");
        int port = Check.port(def.getPortNumber(), 3306);
        String db = Check.text(def.getSchemaMark(), "schemaMark");
        return String.format(URL, host, port, db, PARAMS);
    }
}
