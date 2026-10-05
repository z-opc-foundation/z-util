package com.zifang.util.expr.sql.engine.ast;

/**
 * JOIN 子句节点。
 */
public class JoinClause {

    public enum JoinType {
        INNER, LEFT, RIGHT, FULL, CROSS
    }

    private final JoinType joinType;
    private final String tableName;   // JOIN 为子查询时为 null，由执行器物化后替换
    private final SelectStmt subquery; // JOIN (SELECT ...) AS t 派生表
    private final String alias;
    private final Expression onCondition;

    public JoinClause(JoinType joinType, String tableName, String alias, Expression onCondition) {
        this(joinType, tableName, null, alias, onCondition);
    }

    public JoinClause(JoinType joinType, SelectStmt subquery, String alias, Expression onCondition) {
        this(joinType, null, subquery, alias, onCondition);
    }

    private JoinClause(JoinType joinType, String tableName, SelectStmt subquery, String alias, Expression onCondition) {
        this.joinType = joinType;
        this.tableName = tableName;
        this.subquery = subquery;
        this.alias = alias;
        this.onCondition = onCondition;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public String getTableName() {
        return tableName;
    }

    public SelectStmt getSubquery() {
        return subquery;
    }

    public String getAlias() {
        return alias;
    }

    /**
     * 获取表名或别名（用于行上下文中的表前缀匹配）。
     */
    public String getTableKey() {
        return alias != null ? alias : tableName;
    }

    public Expression getOnCondition() {
        return onCondition;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(joinType).append(" JOIN ");
        if (subquery != null) {
            sb.append("(").append(subquery).append(")");
        } else {
            sb.append(tableName);
        }
        if (alias != null) sb.append(" AS ").append(alias);
        if (onCondition != null) sb.append(" ON ").append(onCondition);
        return sb.toString();
    }
}
