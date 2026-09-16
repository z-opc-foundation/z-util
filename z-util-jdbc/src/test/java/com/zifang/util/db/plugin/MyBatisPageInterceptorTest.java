package com.zifang.util.db.plugin;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * MyBatisPageInterceptorTest类。
 * <p>
 * 基于H2内存数据库验证物理分页拦截器的分页改写与count统计。
 */
public class MyBatisPageInterceptorTest {

    /**
     * 测试表总记录数
     */
    private static final int TOTAL_ROWS = 25;

    private static SqlSessionFactory sqlSessionFactory;

    /**
     * 测试用数据源，供多个SqlSessionFactory复用同一个内存库
     */
    private static JdbcDataSource dataSource;

    /**
     * 测试用Mapper，全量查询（分页由拦截器改写）
     */
    public interface ItemMapper {

        @Select("SELECT id, name FROM t_page_item ORDER BY id")
        List<Map<String, Object>> selectAll();

        @Select("SELECT id, name FROM t_page_item WHERE id > #{minId} ORDER BY id")
        List<Map<String, Object>> selectAbove(int minId);
    }

    /**
     * initFixture方法。
     * <p>
     * 初始化H2内存库、建表、插入25条记录并装配带分页拦截器的SqlSessionFactory。
     *
     * @throws SQLException 初始化失败时抛出
     */
    @BeforeClass
    public static void initFixture() throws SQLException {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:pageInterceptorTest;DB_CLOSE_DELAY=-1");

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE t_page_item (id INT PRIMARY KEY, name VARCHAR(50))");
            for (int i = 1; i <= TOTAL_ROWS; i++) {
                statement.execute("INSERT INTO t_page_item VALUES (" + i + ", 'item" + i + "')");
            }
        }

        Configuration configuration = new Configuration();
        configuration.setEnvironment(new Environment("test", new JdbcTransactionFactory(), dataSource));
        configuration.addInterceptor(new MyBatisPageInterceptor());
        configuration.addMapper(ItemMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }
    /**
     * tearDownFixture方法。
     * <p>
     * 清理ThreadLocal残留，避免影响其他测试。
     */
    @AfterClass
    public static void tearDownFixture() {
        PageContext.clear();
    }

    @Test
    /**
     * testQueryWithoutPaging方法。
     * <p>
     * 未开启分页时查询原样执行，返回全部记录。
     */
    public void testQueryWithoutPaging() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            List<Map<String, Object>> rows = session.getMapper(ItemMapper.class).selectAll();
            assertEquals(TOTAL_ROWS, rows.size());
        }
    }

    @Test
    /**
     * testPageQuery方法。
     * <p>
     * 开启分页后返回对应页的记录，并回填总记录数。
     */
    public void testPageQuery() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PageContext.PageParam param = PageContext.start(2, 10);
            try {
                List<Map<String, Object>> rows = session.getMapper(ItemMapper.class).selectAll();
                assertEquals(10, rows.size());
                assertEquals(11, ((Number) rows.get(0).get("ID")).intValue());
                assertEquals(20, ((Number) rows.get(9).get("ID")).intValue());
                assertEquals(Long.valueOf(TOTAL_ROWS), param.getTotal());
            } finally {
                PageContext.clear();
            }
        }
    }

    @Test
    /**
     * testPageQuery_lastPartialPage方法。
     * <p>
     * 末页记录数不足一页时返回剩余记录。
     */
    public void testPageQuery_lastPartialPage() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PageContext.PageParam param = PageContext.start(3, 10);
            try {
                List<Map<String, Object>> rows = session.getMapper(ItemMapper.class).selectAll();
                assertEquals(5, rows.size());
                assertEquals(21, ((Number) rows.get(0).get("ID")).intValue());
                assertEquals(25, ((Number) rows.get(4).get("ID")).intValue());
                assertEquals(Long.valueOf(TOTAL_ROWS), param.getTotal());
            } finally {
                PageContext.clear();
            }
        }
    }

    @Test
    /**
     * testPageQuery_withParameters方法。
     * <p>
     * 带参数的查询同样被分页改写，count统计复用参数绑定。
     */
    public void testPageQuery_withParameters() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            PageContext.PageParam param = PageContext.start(1, 5);
            try {
                List<Map<String, Object>> rows = session.getMapper(ItemMapper.class).selectAbove(20);
                assertEquals(5, rows.size());
                assertEquals(21, ((Number) rows.get(0).get("ID")).intValue());
                assertEquals(Long.valueOf(5), param.getTotal());
            } finally {
                PageContext.clear();
            }
        }
    }

    @Test
    /**
     * testPageQuery_countDisabled方法。
     * <p>
     * 关闭count统计后仅改写分页SQL，不回填总数。
     * 使用独立的SqlSessionFactory，避免向共享配置动态增删拦截器。
     */
    public void testPageQuery_countDisabled() {
        MyBatisPageInterceptor interceptor = new MyBatisPageInterceptor();
        interceptor.setCountEnabled(false);

        Configuration configuration = new Configuration();
        configuration.setEnvironment(new Environment("countDisabled", new JdbcTransactionFactory(), dataSource));
        configuration.addInterceptor(interceptor);
        configuration.addMapper(ItemMapper.class);
        SqlSessionFactory countDisabledFactory = new SqlSessionFactoryBuilder().build(configuration);

        try (SqlSession session = countDisabledFactory.openSession()) {
            PageContext.PageParam param = PageContext.start(1, 3);
            try {
                List<Map<String, Object>> rows = session.getMapper(ItemMapper.class).selectAll();
                assertEquals(3, rows.size());
                assertNull(param.getTotal());
            } finally {
                PageContext.clear();
            }
        }
    }

    @Test
    /**
     * testPageContext_validation方法。
     * <p>
     * 分页参数的合法性校验与偏移量计算。
     */
    public void testPageContext_validation() {
        try {
            PageContext.start(0, 10);
            fail("page < 1 should be rejected");
        } catch (IllegalArgumentException expected) {
            // 预期异常
        }
        try {
            PageContext.start(1, 0);
            fail("size < 1 should be rejected");
        } catch (IllegalArgumentException expected) {
            // 预期异常
        }

        PageContext.PageParam param = PageContext.start(3, 10);
        try {
            assertEquals(20, param.getOffset());
            assertSame(param, PageContext.current());
        } finally {
            PageContext.clear();
        }
        assertNull(PageContext.current());
    }
}
