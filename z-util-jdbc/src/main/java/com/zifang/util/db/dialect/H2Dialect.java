package com.zifang.util.db.dialect;

import com.zifang.util.db.meta.DataSourceDTO;

/**
 * H2 方言。host 留空或写作 {@code mem} 时走内存库，否则按 TCP server 连接。
 * <p>
 * 存在意义是让动态查询与数据源注册能在无外部实例的情况下被测试覆盖。
 *
 * @author zifang
 */
public class H2Dialect extends AbstractDialect {

    public static final String ID = "h2";

    private static final String DRIVER = "org.h2.Driver";

    private static final String MEM = "mem";

    public H2Dialect() {
        super(ID, DRIVER, '"');
    }

    @Override
    public String buildUrl(DataSourceDTO def) {
        String db = def.getSchemaMark() == null ? "test" : def.getSchemaMark().trim();
        String host = def.getDatasourceUrl() == null ? "" : def.getDatasourceUrl().trim();
        if (host.isEmpty() || MEM.equalsIgnoreCase(host)) {
            return "jdbc:h2:mem:" + db + ";DB_CLOSE_DELAY=-1";
        }
        return "jdbc:h2:tcp://" + host + ":" + Check.port(def.getPortNumber(), 9092) + "/" + db;
    }
}
