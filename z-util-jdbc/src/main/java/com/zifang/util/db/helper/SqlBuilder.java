package com.zifang.util.db.helper;

/**
 * SQL构建器
 * <p>
 * 提供SQL语句的动态构建能力，用于以编程方式构造SQL语句。
 * 与直接拼接SQL字符串不同，该构建器提供：
 * <ul>
 *   <li>类型安全的SQL构建接口</li>
 *   <li>自动的参数转义和SQL注入防护</li>
 *   <li>支持条件逻辑（IF语句）的SQL组装</li>
 *   <li>跨数据库的SQL语法适配</li>
 *   <li>清晰的SQL语句结构表示</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>
 * SqlBuilder builder = new SqlBuilder();
 * builder.select("id", "name", "email")
 *        .from("users")
 *        .where("age >", 18)
 *        .where("status =", "active")
 *        .orderBy("name", "ASC")
 *        .limit(10);
 * String sql = builder.build();
 * </pre>
 *
 * @author zifang
 * @see SqlAnalyser
 * @see SqlGenerator
 */
public class SqlBuilder {

}
