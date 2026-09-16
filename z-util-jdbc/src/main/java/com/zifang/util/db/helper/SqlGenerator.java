package com.zifang.util.db.helper;

/**
 * SQL生成器
 * <p>
 * 提供从Java对象到SQL语句的自动转换能力。该类主要负责：
 * <ul>
 *   <li>将Java对象属性映射到数据库表列</li>
 *   <li>自动生成INSERT、UPDATE、DELETE语句</li>
 *   <li>处理主键生成策略（如自增、UUID等）</li>
 *   <li>支持批量操作的SQL生成</li>
 *   <li>处理特殊数据类型（如枚举、BLOB等）的转换</li>
 * </ul>
 *
 * <p>该类是ORM框架的核心组件之一，通过元数据描述（如注解）
 * 获取Java类与数据库表之间的映射关系，实现对象的持久化操作。
 *
 * <p>使用示例：
 * <pre>
 * SqlGenerator generator = new SqlGenerator(User.class);
 * String insertSql = generator.generateInsert(user);
 * String updateSql = generator.generateUpdate(user);
 * </pre>
 *
 * @author zifang
 * @see SqlAnalyser
 * @see SqlBuilder
 */
public class SqlGenerator {
}
