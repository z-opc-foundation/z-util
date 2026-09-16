package com.zifang.util.expr.sql.engine.ast;

/**
 * JOIN 子句节点。
 */
public class JoinClause {

    public enum JoinType {
        INNER, LEFT, RIGHT, FULL, CROSS
    }

    private final JoinType joinType;
    private final String tableName;
    private final String alias;
    private final Expression onCondition;

    public JoinClause(JoinType joinType, String tableName, String alias, Expression onCondition) {
        this.joinType = joinType;
        this.tableName = tableName;
        this.alias = alias;
        this.onCondition = onCondition;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public String getTableName() {
        return tableName;
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
        sb.append(joinType).append(" JOIN ").append(tableName);
        if (alias != null) sb.append(" AS ").append(alias);
        if (onCondition != null) sb.append(" ON ").append(onCondition);
        return sb.toString();
    }
}
