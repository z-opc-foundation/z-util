package com.zifang.util.expr.sql.engine.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SELECT 语句 AST 根节点。
 */
public class SelectStmt {

    private boolean distinct;
    private List<Expression> selectItems;     // SELECT 列表（AliasedExpr 或 ColumnRef 等）
    private String tableName;                 // 主表名
    private String tableAlias;                // 主表别名
    private Expression whereClause;           // WHERE 条件
    private List<Expression> groupBy;         // GROUP BY 列表
    private Expression havingClause;          // HAVING 条件
    private List<SortKey> orderBy;            // ORDER BY 列表
    private Integer limit;                    // LIMIT
    private Integer offset;                   // OFFSET
    private List<JoinClause> joins;           // JOIN 列表

    public SelectStmt() {
        this.selectItems = new ArrayList<>();
        this.groupBy = new ArrayList<>();
        this.orderBy = new ArrayList<>();
        this.joins = new ArrayList<>();
    }

    // ==================== getters / setters ====================

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public List<Expression> getSelectItems() {
        return selectItems;
    }

    public void setSelectItems(List<Expression> selectItems) {
        this.selectItems = selectItems;
    }

    public void addSelectItem(Expression item) {
        this.selectItems.add(item);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public void setTableAlias(String tableAlias) {
        this.tableAlias = tableAlias;
    }

    /**
     * 获取表的上下文名（别名优先）。
     */
    public String getTableContextName() {
        return tableAlias != null ? tableAlias : tableName;
    }

    public Expression getWhereClause() {
        return whereClause;
    }

    public void setWhereClause(Expression whereClause) {
        this.whereClause = whereClause;
    }

    public List<Expression> getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(List<Expression> groupBy) {
        this.groupBy = groupBy;
    }

    public Expression getHavingClause() {
        return havingClause;
    }

    public void setHavingClause(Expression havingClause) {
        this.havingClause = havingClause;
    }

    public List<SortKey> getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(List<SortKey> orderBy) {
        this.orderBy = orderBy;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public List<JoinClause> getJoins() {
        return joins;
    }

    public void setJoins(List<JoinClause> joins) {
        this.joins = joins;
    }

    public void addJoin(JoinClause join) {
        this.joins.add(join);
    }

    /**
     * 检查 SELECT 列表是否包含 *（全列选择）。
     */
    public boolean isSelectAll() {
        return selectItems.size() == 1 && selectItems.get(0) instanceof ColumnRef
                && "*".equals(((ColumnRef) selectItems.get(0)).getColumn());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SELECT ");
        if (distinct) sb.append("DISTINCT ");
        for (int i = 0; i < selectItems.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(selectItems.get(i));
        }
        sb.append(" FROM ").append(tableName);
        if (tableAlias != null) sb.append(" AS ").append(tableAlias);
        for (JoinClause join : joins) {
            sb.append(" ").append(join);
        }
        if (whereClause != null) sb.append(" WHERE ").append(whereClause);
        if (!groupBy.isEmpty()) {
            sb.append(" GROUP BY ");
            for (int i = 0; i < groupBy.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(groupBy.get(i));
            }
        }
        if (havingClause != null) sb.append(" HAVING ").append(havingClause);
        if (!orderBy.isEmpty()) {
            sb.append(" ORDER BY ");
            for (int i = 0; i < orderBy.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(orderBy.get(i));
            }
        }
        if (limit != null) sb.append(" LIMIT ").append(limit);
        if (offset != null) sb.append(" OFFSET ").append(offset);
        return sb.toString();
    }
}
