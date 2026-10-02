package com.zifang.util.expr.sql;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL 语句解析结果
 */
public class SqlStatement {

    private Type type;
    private String tableName;
    private String tableAlias;
    private List<String> columns;
    private List<WhereCondition> whereConditions;
    private List<String> placeholders;
    private List<NamedPlaceholder> namedPlaceholders;
    private String rawSql;
    // SELECT 扩展
    private boolean distinct;
    private List<OrderByClause> orderBy;
    private Integer limit;
    private Integer offset;
    private List<JoinClause> joins;
    private List<String> groupBy;
    private List<String> aliases;  // 列别名列表
    // INSERT 扩展
    private List<List<String>> multiValues;  // 多行插入

    public SqlStatement() {
        this.columns = new ArrayList<>();
        this.whereConditions = new ArrayList<>();
        this.placeholders = new ArrayList<>();
        this.namedPlaceholders = new ArrayList<>();
        this.orderBy = new ArrayList<>();
        this.joins = new ArrayList<>();
        this.groupBy = new ArrayList<>();
        this.aliases = new ArrayList<>();
        this.multiValues = new ArrayList<>();
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
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

    public List<String> getColumns() {
        return columns;
    }

    public void addColumn(String column) {
        this.columns.add(column);
    }

    public List<WhereCondition> getWhereConditions() {
        return whereConditions;
    }

    public void addWhereCondition(WhereCondition condition) {
        this.whereConditions.add(condition);
    }

    public List<String> getPlaceholders() {
        return placeholders;
    }

    public void addPlaceholder(String placeholder) {
        this.placeholders.add(placeholder);
    }

    public List<NamedPlaceholder> getNamedPlaceholders() {
        return namedPlaceholders;
    }

    public void addNamedPlaceholder(NamedPlaceholder namedPlaceholder) {
        this.namedPlaceholders.add(namedPlaceholder);
    }

    public String getRawSql() {
        return rawSql;
    }

    public void setRawSql(String rawSql) {
        this.rawSql = rawSql;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public List<OrderByClause> getOrderBy() {
        return orderBy;
    }

    public void addOrderBy(OrderByClause orderByClause) {
        this.orderBy.add(orderByClause);
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

    public void addJoin(JoinClause join) {
        this.joins.add(join);
    }

    public List<String> getGroupBy() {
        return groupBy;
    }

    public void addGroupBy(String column) {
        this.groupBy.add(column);
    }

    public List<String> getAliases() {
        return aliases;
    }

    public void addAlias(String alias) {
        this.aliases.add(alias);
    }

    public List<List<String>> getMultiValues() {
        return multiValues;
    }

    public void addMultiValue(List<String> values) {
        this.multiValues.add(values);
    }

    /**
     * 获取所有表名（包含JOIN的表）
     */
    public List<String> getTableNames() {
        List<String> tables = new ArrayList<>();
        if (tableName != null) {
            tables.add(tableName);
        }
        for (JoinClause join : joins) {
            if (join.getTableName() != null) {
                tables.add(join.getTableName());
            }
        }
        return tables;
    }

    /**
     * 获取列名（从columns列表）
     */
    public List<String> getColumnNames() {
        return new ArrayList<>(columns);
    }

    @Override
    public String toString() {
        return "SqlStatement{" +
                "type=" + type +
                ", tableName='" + tableName + '\'' +
                ", columns=" + columns +
                ", whereConditions=" + whereConditions +
                ", placeholders=" + placeholders +
                ", namedPlaceholders=" + namedPlaceholders +
                '}';
    }

    /**
     * SQL 类型
     */
    public enum Type {
        SELECT,
        INSERT,
        UPDATE,
        DELETE,
        CREATE,
        DROP,
        UNKNOWN
    }

    /**
     * JOIN 类型
     */
    public enum JoinType {
        INNER,
        LEFT,
        RIGHT,
        FULL,
        CROSS
    }

    /**
     * WHERE 条件
     */
    public static class WhereCondition {
        private String column;
        private String operator;
        private String value;
        private LogicalOperator logicalOperator;

        public WhereCondition() {
            this.logicalOperator = LogicalOperator.NONE;
        }

        public WhereCondition(String column, String operator, String value) {
            this.column = column;
            this.operator = operator;
            this.value = value;
            this.logicalOperator = LogicalOperator.NONE;
        }

        public String getColumn() {
            return column;
        }

        public void setColumn(String column) {
            this.column = column;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public LogicalOperator getLogicalOperator() {
            return logicalOperator;
        }

        public void setLogicalOperator(LogicalOperator logicalOperator) {
            this.logicalOperator = logicalOperator;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (logicalOperator != LogicalOperator.NONE) {
                sb.append(logicalOperator).append(" ");
            }
            sb.append(column).append(" ").append(operator).append(" ").append(value);
            return sb.toString();
        }

        public enum LogicalOperator {
            NONE,
            AND,
            OR
        }
    }

    /**
     * 命名占位符
     */
    public static class NamedPlaceholder {
        private String name;

        public NamedPlaceholder(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * ORDER BY 子句
     */
    public static class OrderByClause {
        private String column;
        private boolean descending;

        public OrderByClause(String column) {
            this(column, false);
        }

        public OrderByClause(String column, boolean descending) {
            this.column = column;
            this.descending = descending;
        }

        public String getColumn() {
            return column;
        }

        public void setColumn(String column) {
            this.column = column;
        }

        public boolean isDescending() {
            return descending;
        }

        public void setDescending(boolean descending) {
            this.descending = descending;
        }

        @Override
        public String toString() {
            return column + (descending ? " DESC" : " ASC");
        }
    }

    /**
     * JOIN 子句
     */
    public static class JoinClause {
        private JoinType joinType;
        private String tableName;
        private String alias;
        private String onCondition;

        public JoinClause(JoinType joinType, String tableName) {
            this.joinType = joinType;
            this.tableName = tableName;
        }

        public JoinType getJoinType() {
            return joinType;
        }

        public void setJoinType(JoinType joinType) {
            this.joinType = joinType;
        }

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public String getOnCondition() {
            return onCondition;
        }

        public void setOnCondition(String onCondition) {
            this.onCondition = onCondition;
        }

        @Override
        public String toString() {
            return joinType + " JOIN " + tableName + (alias != null ? " " + alias : "") +
                    (onCondition != null ? " ON " + onCondition : "");
        }
    }
}
