package com.zifang.util.db.context;

import com.alibaba.druid.pool.DruidDataSource;
import com.zifang.util.core.lang.exception.BusinessException;
import com.zifang.util.db.dialect.H2Dialect;
import com.zifang.util.db.dialect.MySqlDialect;
import com.zifang.util.db.meta.DataSourceDTO;
import org.junit.After;
import org.junit.Test;

import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 数据源注册表：注册、探活、重复、热替换与销毁。
 */
public class DataSourceRegistryTest {

    private final DataSourceRegistry registry = new DataSourceRegistry();

    @After
    public void tearDown() {
        registry.close();
    }

    private static DataSourceDTO h2(String code, String db) {
        DataSourceDTO def = new DataSourceDTO();
        def.setDatasourceCode(code);
        def.setDatasourceName(code);
        def.setDatasourceType(H2Dialect.ID);
        def.setSchemaMark(db);
        def.setUserName("sa");
        def.setPw("");
        return def;
    }

    @Test
    public void registerAndLookup() {
        DataSource created = registry.register(h2("report", "reg_lookup"));
        assertNotNull(created);
        assertTrue(registry.contains("report"));
        assertEquals(1, registry.size());
        assertEquals(1, registry.codes().size());
        assertSame(created, registry.get("report"));
        assertSame(created, registry.require("report"));
        assertEquals("reg_lookup", registry.def("report").getSchemaMark());
        assertEquals(H2Dialect.ID, registry.dialect("report").id());
        assertNull(registry.get("missing"));
        assertNull(registry.def("missing"));
        assertNull(registry.dialect("missing"));
        try {
            registry.require("missing");
            fail("未注册的数据源应抛异常");
        } catch (BusinessException expected) {
            assertTrue(expected.getMessage().contains("数据源未注册"));
        }
    }

    @Test
    public void duplicateCodeRejectedAndFirstSurvives() {
        DataSource first = registry.register(h2("report", "reg_dup"));
        try {
            registry.register(h2("report", "reg_dup"));
            fail("同名数据源应被拒绝");
        } catch (BusinessException expected) {
            assertTrue(expected.getMessage().contains("已存在同名数据源"));
        }
        assertSame(first, registry.require("report"));
    }

    @Test
    public void rebindSwapsPoolAndKeepsDatabase() {
        DruidDataSource first = (DruidDataSource) registry.register(h2("report", "reg_rebind"));
        try (java.sql.Connection conn = first.getConnection();
             java.sql.Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE kept(id INT PRIMARY KEY)");
            st.execute("INSERT INTO kept VALUES (1)");
        } catch (Exception e) {
            fail("准备数据失败: " + e);
        }
        DataSource second = registry.rebind(h2("report", "reg_rebind"));
        assertNotSame(first, second);
        assertTrue("旧池应已关闭", first.isClosed());
        try (java.sql.Connection conn = second.getConnection();
             java.sql.Statement st = conn.createStatement();
             java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM kept")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        } catch (Exception e) {
            fail("重绑后应仍可读到同一内存库: " + e);
        }
        assertEquals(1, registry.size());
    }

    @Test
    public void unregisterClosesAndIsIdempotent() {
        DruidDataSource pool = (DruidDataSource) registry.register(h2("report", "reg_unreg"));
        assertTrue(registry.unregister("report"));
        assertTrue("注销应关闭连接池", pool.isClosed());
        assertFalse(registry.unregister("report"));
        assertEquals(0, registry.size());
    }

    @Test
    public void probeFailureLeavesNoEntry() {
        DataSourceDTO unreachable = h2("report", "reg_fail");
        unreachable.setDatasourceUrl("127.0.0.1");
        unreachable.setPortNumber(1);
        try {
            registry.register(unreachable);
            fail("连不上应抛异常");
        } catch (BusinessException expected) {
            assertTrue(expected.getMessage().contains("数据源连接失败"));
        }
        assertFalse(registry.contains("report"));
        assertEquals(0, registry.size());
    }

    @Test
    public void badDefinitionsRejected() {
        DataSourceDTO noCode = h2(" ", "reg_bad");
        try {
            registry.register(noCode);
            fail("缺 code 应报错");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("datasourceCode"));
        }
        try {
            registry.register(null);
            fail("空定义应报错");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("不能为空"));
        }
        DataSourceDTO noType = h2("report", "reg_bad");
        noType.setDatasourceType(null);
        try {
            registry.register(noType);
            fail("缺类型应报错");
        } catch (BusinessException expected) {
            assertTrue(expected.getMessage().contains("datasourceType"));
        }
        DataSourceDTO unknown = h2("report", "reg_bad");
        unknown.setDatasourceType("oracle");
        try {
            registry.register(unknown);
            fail("未知类型应报错");
        } catch (BusinessException expected) {
            assertTrue(expected.getMessage().contains("不支持的数据源类型"));
        }
        assertEquals(0, registry.size());
    }

    @Test
    public void poolSpecAppliesAndFactoryAdapterResolves() {
        registry.poolSpec(PoolSpec.defaults().initialSize(1).minIdle(1).maxActive(3).keepAlive(false));
        assertEquals(3, registry.poolSpec().getMaxActive());
        DruidDataSource pool = (DruidDataSource) registry.register(h2("report", "reg_spec"));
        assertEquals(3, pool.getMaxActive());
        assertEquals(1, pool.getInitialSize());
        assertFalse(pool.isKeepAlive());
        assertEquals(MySqlDialect.ID, new MySqlDialect().id());
        DataSource viaFactory = registry.factoryFor("report").getDatasource();
        assertSame(pool, viaFactory);
        try {
            registry.factoryFor("missing");
            fail("未注册的 code 不能取工厂");
        } catch (BusinessException expected) {
            assertTrue(expected.getMessage().contains("数据源未注册"));
        }
        registry.poolSpec(null);
        assertEquals(PoolSpec.defaults().getMaxActive(), registry.poolSpec().getMaxActive());
    }

    @Test
    public void jdbcUrlOverridesAssembledAddress() {
        DataSourceDTO def = h2("report", "reg_raw_url");
        def.setDatasourceType(null);
        def.setJdbcUrl("jdbc:h2:mem:reg_raw_url;DB_CLOSE_DELAY=-1");
        def.setDatasourceUrl("10.255.255.1");
        def.setPortNumber(1);
        registry.register(def);
        assertEquals(H2Dialect.ID, registry.dialect("report").id());
        assertEquals("jdbc:h2:mem:reg_raw_url;DB_CLOSE_DELAY=-1", registry.def("report").getJdbcUrl());
    }

    @Test
    public void illegalJdbcUrlRejected() {
        DataSourceDTO def = h2("report", "reg_bad_url");
        def.setJdbcUrl("mysql://127.0.0.1:3306/db");
        try {
            registry.register(def);
            fail("缺 jdbc: 前缀的地址应被拒绝");
        } catch (BusinessException expected) {
            assertTrue(expected.getMessage().contains("非法 JDBC URL"));
        }
        assertFalse(registry.contains("report"));
        assertEquals(0, registry.size());
    }

    @Test
    public void closeReleasesEverything() {
        DruidDataSource a = (DruidDataSource) registry.register(h2("one", "reg_close"));
        DruidDataSource b = (DruidDataSource) registry.register(h2("two", "reg_close2"));
        registry.close();
        assertTrue(a.isClosed());
        assertTrue(b.isClosed());
        assertEquals(0, registry.size());
    }
}
