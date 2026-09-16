package com.zifang.util.db.plugin;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * MyBatis物理分页拦截器
 * <p>
 * 拦截StatementHandler的prepare阶段，在SQL发送到数据库之前完成两件事：
 * <ul>
 *   <li>执行count查询统计总记录数，并回填到{@link PageContext.PageParam}</li>
 *   <li>按数据库方言将原SQL改写为物理分页SQL（LIMIT/OFFSET、ROWNUM或FETCH）</li>
 * </ul>
 * <p>
 * 是否分页由{@link PageContext}决定：调用方通过{@link PageContext#start(long, long)}
 * 开启分页后，当前线程内的下一次查询会被改写；未开启分页时查询原样执行，零侵入。
 * <p>
 * 配置方式（mybatis-config.xml）：
 * <pre>
 * &lt;plugins&gt;
 *     &lt;plugin interceptor="com.zifang.util.db.plugin.MyBatisPageInterceptor"&gt;
 *         &lt;property name="countEnabled" value="true"/&gt;
 *     &lt;/plugin&gt;
 * &lt;/plugins&gt;
 * </pre>
 * <p>
 * 使用方式：
 * <pre>
 * PageContext.PageParam param = PageContext.start(2, 10);
 * try {
 *     List&lt;User&gt; users = userMapper.selectAll(); // 物理分页结果
 *     Long total = param.getTotal();               // 总记录数
 * } finally {
 *     PageContext.clear();
 * }
 * </pre>
 *
 * @author zifang
 * @see PageContext
 * @see PageDialect
 */
@Intercepts(@Signature(type = StatementHandler.class, method = "prepare",
        args = {Connection.class, Integer.class}))
public class MyBatisPageInterceptor implements Interceptor {

    /**
     * count查询结果的列别名
     */
    private static final String COUNT_ALIAS = "page_count_";

    /**
     * 是否在分页查询前执行count查询统计总记录数，默认开启
     */
    private boolean countEnabled = true;

    /**
     * intercept方法。
     * <p>
     * 拦截SQL预编译阶段：当前线程存在分页参数时执行count统计并改写分页SQL，
     * 否则直接放行。
     *
     * @param invocation 拦截到的调用
     * @return 原调用结果
     * @throws Throwable 执行过程中出现异常时抛出
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        PageContext.PageParam param = PageContext.current();
        if (param == null) {
            // 未开启分页，原样执行
            return invocation.proceed();
        }

        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        Connection connection = (Connection) invocation.getArgs()[0];

        try {
            if (countEnabled && param.getTotal() == null) {
                param.setTotal(queryTotal(connection, statementHandler));
            }
            PageDialect dialect = PageDialect.resolve(connection);
            BoundSql boundSql = statementHandler.getBoundSql();
            String pageSql = dialect.buildPageSql(boundSql.getSql(), param.getOffset(), param.getSize());
            replaceBoundSql(invocation.getTarget(), pageSql);
            return invocation.proceed();
        } finally {
            PageContext.clear();
        }
    }

    /**
     * plugin方法。
     * <p>
     * 仅对StatementHandler类型的目标对象生成代理。
     *
     * @param target 待包装的目标对象
     * @return 代理对象
     */
    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    /**
     * setProperties方法。
     * <p>
     * 支持的配置项：
     * <ul>
     *   <li>countEnabled：是否开启count统计，默认true</li>
     * </ul>
     *
     * @param properties 插件配置
     */
    @Override
    public void setProperties(Properties properties) {
        if (properties == null) {
            return;
        }
        String countEnabledValue = properties.getProperty("countEnabled");
        if (countEnabledValue != null) {
            this.countEnabled = Boolean.parseBoolean(countEnabledValue.trim());
        }
    }

    /**
     * 用分页SQL替换语句处理器中的BoundSql
     * <p>
     * 通过替换整个BoundSql对象（而非反射修改final字段）保持对新版JDK的兼容。
     * 替换时完整复制原参数映射与附加参数，保证后续参数绑定不受影响。
     *
     * @param target  被拦截的语句处理器（可能是被其他插件包装的代理）
     * @param pageSql 分页SQL
     */
    private void replaceBoundSql(Object target, String pageSql) {
        MetaObject metaObject = SystemMetaObject.forObject(target);
        // 剥离其他插件产生的JDK代理层，定位到真实的StatementHandler
        while (metaObject.hasGetter("h.target")) {
            metaObject = SystemMetaObject.forObject(metaObject.getValue("h.target"));
        }
        // RoutingStatementHandler内部委托delegate；其他实现直接持有boundSql
        String basePath = metaObject.hasGetter("delegate") ? "delegate." : "";

        BoundSql boundSql = (BoundSql) metaObject.getValue(basePath + "boundSql");
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue(basePath + "mappedStatement");

        BoundSql newBoundSql = new BoundSql(mappedStatement.getConfiguration(), pageSql,
                boundSql.getParameterMappings(), boundSql.getParameterObject());
        copyAdditionalParameters(boundSql, newBoundSql);
        metaObject.setValue(basePath + "boundSql", newBoundSql);
    }

    /**
     * 复制原BoundSql的附加参数到新BoundSql
     *
     * @param source 原BoundSql
     * @param target 新BoundSql
     */
    @SuppressWarnings("unchecked")
    private void copyAdditionalParameters(BoundSql source, BoundSql target) {
        Map<String, Object> additionalParameters = (Map<String, Object>) SystemMetaObject.forObject(source)
                .getValue("additionalParameters");
        if (additionalParameters == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : additionalParameters.entrySet()) {
            target.setAdditionalParameter(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 执行count查询统计总记录数
     * <p>
     * 将原SQL包裹为 SELECT COUNT(*) FROM ( 原SQL ) 子查询，并复用原SQL的参数绑定。
     *
     * @param connection       数据库连接
     * @param statementHandler 原语句处理器
     * @return 总记录数
     * @throws SQLException count查询执行失败时抛出
     */
    private long queryTotal(Connection connection, StatementHandler statementHandler) throws SQLException {
        String originalSql = statementHandler.getBoundSql().getSql();
        String countSql = "SELECT COUNT(*) FROM ( " + originalSql + " ) " + COUNT_ALIAS;
        try (PreparedStatement statement = connection.prepareStatement(countSql)) {
            statementHandler.getParameterHandler().setParameters(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getLong(1) : 0;
            }
        }
    }

    /**
     * 是否开启count统计
     *
     * @return 开启返回true，否则返回false
     */
    public boolean isCountEnabled() {
        return countEnabled;
    }

    /**
     * 设置是否开启count统计
     *
     * @param countEnabled 是否开启
     */
    public void setCountEnabled(boolean countEnabled) {
        this.countEnabled = countEnabled;
    }
}
