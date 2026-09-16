package com.zifang.util.db.context;

import com.zifang.util.db.transaction.TransactionManager;
import org.junit.Test;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;

import static org.junit.Assert.*;

/**
 * DataSourceContext 类测试
 */

/**
 * DataSourceContextTest类。
 */
public class DataSourceContextTest {

    /**
     * 创建一个不真正连库的 DataSource 桩对象，供仅测试 DataSourceContext 装配使用。
     */
    private static DataSource mockDataSource() {
        return (DataSource) Proxy.newProxyInstance(
                DataSource.class.getClassLoader(),
                new Class<?>[]{DataSource.class},
                (proxy, method, args) -> null);
    }

    @Test
    /**
     * testDefaultConstructor方法。
     */
    public void testDefaultConstructor() {
        DataSourceContext context = new DataSourceContext();
        assertNotNull(context);
        assertNull(context.getDatasourceFactory());
        assertNull(context.getScanPackageName());
        assertNull(context.getTransactionManager());
    }

    @Test
    /**
     * testChainMethods方法。
     */
    public void testChainMethods() {
        DataSourceContext context = new DataSourceContext()
                .scanPackage("com.example.repository")
                .transationManager(new TransactionManager(mockDataSource()));

        assertEquals("com.example.repository", context.getScanPackageName());
        assertNotNull(context.getTransactionManager());
    }

    @Test
    /**
     * testScanPackageName方法。
     */
    public void testScanPackageName() {
        DataSourceContext context = new DataSourceContext();
        context.setScanPackageName("com.test.pkg");

        assertEquals("com.test.pkg", context.getScanPackageName());
    }

    @Test
    /**
     * testDataSourceFactory方法。
     */
    public void testDataSourceFactory() {
        DataSourceContext context = new DataSourceContext();
        DatasourceFactory factory = new DatasourceFactory() {
            @Override
            /**
             * getDatasource方法。
             * @return DataSource类型返回值
             */
            public DataSource getDatasource() {
                return null;
            }
        };

        context.setDatasourceFactory(factory);
        assertNotNull(context.getDatasourceFactory());
        assertSame(factory, context.getDatasourceFactory());
    }

    @Test
    /**
     * testTransactionManager方法。
     */
    public void testTransactionManager() {
        DataSourceContext context = new DataSourceContext();
        TransactionManager tm = new TransactionManager(mockDataSource());

        context.setTransactionManager(tm);
        assertNotNull(context.getTransactionManager());
        assertSame(tm, context.getTransactionManager());
    }

    @Test
    /**
     * testEquals方法。
     */
    public void testEquals() {
        DataSourceContext ctx1 = new DataSourceContext();
        ctx1.setScanPackageName("com.test");

        DataSourceContext ctx2 = new DataSourceContext();
        ctx2.setScanPackageName("com.test");

        assertEquals(ctx1, ctx2);
    }

    @Test
    /**
     * testEqualsWithDifferentPackages方法。
     */
    public void testEqualsWithDifferentPackages() {
        DataSourceContext ctx1 = new DataSourceContext();
        ctx1.setScanPackageName("com.test1");

        DataSourceContext ctx2 = new DataSourceContext();
        ctx2.setScanPackageName("com.test2");

        assertNotEquals(ctx1, ctx2);
    }

    @Test
    /**
     * testEqualsWithNull方法。
     */
    public void testEqualsWithNull() {
        DataSourceContext ctx = new DataSourceContext();
        assertNotEquals(ctx, null);
    }

    @Test
    /**
     * testHashCode方法。
     */
    public void testHashCode() {
        DataSourceContext ctx1 = new DataSourceContext();
        ctx1.setScanPackageName("com.test");

        DataSourceContext ctx2 = new DataSourceContext();
        ctx2.setScanPackageName("com.test");

        assertEquals(ctx1.hashCode(), ctx2.hashCode());
    }

    @Test
    /**
     * testToString方法。
     */
    public void testToString() {
        DataSourceContext ctx = new DataSourceContext();
        String str = ctx.toString();

        assertNotNull(str);
        assertTrue(str.contains("DataSourceContext"));
    }

    @Test
    /**
     * testFullConfiguration方法。
     */
    public void testFullConfiguration() {
        DatasourceFactory factory = new DatasourceFactory() {
            @Override
            /**
             * getDatasource方法。
             * @return DataSource类型返回值
             */
            public DataSource getDatasource() {
                return null;
            }
        };
        TransactionManager tm = new TransactionManager(mockDataSource());

        DataSourceContext ctx = new DataSourceContext()
                .scanPackage("com.example")
                .dataSourceFactory(factory)
                .transationManager(tm);

        assertEquals("com.example", ctx.getScanPackageName());
        assertNotNull(ctx.getDatasourceFactory());
        assertNotNull(ctx.getTransactionManager());
    }
}