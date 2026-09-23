package com.zifang.util.db.query;

import com.zifang.util.db.dialect.Dialect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 把 {@link Query} / {@link Predicate} 编译成参数化 SQL。
 * <p>
 * 标识符由 {@link Dialect#quote} 引用，值一律落到 {@code ?} 绑定参数。
 *
 * @author zifang
 */
public final class QueryCompiler {

    private final Dialect dialect;

    public QueryCompiler(Dialect dialect) {
        if (dialect == null) {
            throw new IllegalArgumentException("dialect 不能为空");
        }
        this.dialect = dialect;
    }

    public Dialect dialect() {
        return dialect;
    }

    public SqlSpec compile(Query query) {
        List<Object> params = new ArrayList<>();
        String sql = renderSelect(query, params) + dialect.limitClause(query.offset(), query.limit());
        return new SqlSpec(sql, params);
    }

    /**
     * 计数查询：外层包子查询，因此 distinct 投影能得到正确总数；分页不影响总数，编译时天然忽略。
     */
    public SqlSpec compileCount(Query query) {
        List<Object> params = new ArrayList<>();
        return new SqlSpec(dialect.countSql(renderSelect(query, params)), params);
    }

    public SqlSpec compileInsert(String table, Map<String, ?> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("insert 至少需要一个列值");
        }
        List<Object> params = new ArrayList<>(values.size());
        StringBuilder columns = new StringBuilder();
        StringBuilder holders = new StringBuilder();
        for (Map.Entry<String, ?> entry : values.entrySet()) {
            if (columns.length() > 0) {
                columns.append(", ");
                holders.append(", ");
            }
            columns.append(dialect.quote(entry.getKey()));
            holders.append('?');
            params.add(entry.getValue());
        }
        String sql = "INSERT INTO " + dialect.quote(table)
                + " (" + columns + ") VALUES (" + holders + ")";
        return new SqlSpec(sql, params);
    }

    public SqlSpec compileUpdate(String table, Map<String, ?> values, Predicate where) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("update 至少需要一个列值");
        }
        requireCondition(where, "update");
        List<Object> params = new ArrayList<>(values.size());
        StringBuilder sets = new StringBuilder();
        for (Map.Entry<String, ?> entry : values.entrySet()) {
            if (sets.length() > 0) {
                sets.append(", ");
            }
            sets.append(dialect.quote(entry.getKey())).append(" = ?");
            params.add(entry.getValue());
        }
        String sql = "UPDATE " + dialect.quote(table) + " SET " + sets
                + " WHERE " + render(where, params);
        return new SqlSpec(sql, params);
    }

    public SqlSpec compileDelete(String table, Predicate where) {
        requireCondition(where, "delete");
        List<Object> params = new ArrayList<>();
        String sql = "DELETE FROM " + dialect.quote(table) + " WHERE " + render(where, params);
        return new SqlSpec(sql, params);
    }

    /**
     * 渲染条件树，参数按出现顺序追加。
     */
    public String render(Predicate predicate, List<Object> params) {
        if (predicate == null) {
            throw new IllegalArgumentException("条件不能为空");
        }
        return renderPredicate(predicate, params);
    }

    private String renderSelect(Query query, List<Object> params) {
        if (query.table() == null) {
            throw new IllegalArgumentException("Query 未指定表名");
        }
        StringBuilder sql = new StringBuilder("SELECT ");
        if (query.isDistinct()) {
            sql.append("DISTINCT ");
        }
        sql.append(renderProjection(query.columns())).append(" FROM ").append(dialect.quote(query.table()));
        if (!query.conditions().isEmpty()) {
            sql.append(" WHERE ").append(joinTopLevel(query.conditions(), params));
        }
        if (!query.orders().isEmpty()) {
            StringBuilder order = new StringBuilder();
            for (Query.Order item : query.orders()) {
                if (order.length() > 0) {
                    order.append(", ");
                }
                order.append(dialect.quote(item.column())).append(item.isAsc() ? " ASC" : " DESC");
            }
            sql.append(" ORDER BY ").append(order);
        }
        return sql.toString();
    }

    private String renderProjection(List<String> columns) {
        if (columns.isEmpty()) {
            return "*";
        }
        StringBuilder projected = new StringBuilder();
        for (String column : columns) {
            if (projected.length() > 0) {
                projected.append(", ");
            }
            projected.append("*".equals(column) ? "*" : dialect.quote(column));
        }
        return projected.toString();
    }

    private String joinTopLevel(List<Predicate> conditions, List<Object> params) {
        if (conditions.size() == 1) {
            return renderPredicate(conditions.get(0), params);
        }
        return renderPredicate(new PredicateGroup(Logic.AND, conditions), params);
    }

    private String renderPredicate(Predicate predicate, List<Object> params) {
        if (predicate instanceof ConstPredicate) {
            return ((ConstPredicate) predicate).literal();
        }
        if (predicate instanceof PredicateGroup) {
            return renderGroup((PredicateGroup) predicate, params);
        }
        if (predicate instanceof Criterion) {
            return renderCriterion((Criterion) predicate, params);
        }
        throw new IllegalArgumentException("未知条件节点: " + predicate);
    }

    private String renderGroup(PredicateGroup group, List<Object> params) {
        List<String> parts = new ArrayList<>();
        for (Predicate child : group.children()) {
            String rendered = renderPredicate(child, params);
            // 只有自身是多节点的分组需要括号，叶子不再包一层，生成的 SQL 才可读
            if (child instanceof PredicateGroup
                    && ((PredicateGroup) child).children().size() > 1) {
                rendered = "(" + rendered + ")";
            }
            parts.add(rendered);
        }
        if (parts.isEmpty()) {
            return group.logic() == Logic.AND ? "1 = 1" : "1 = 0";
        }
        if (parts.size() == 1) {
            return parts.get(0);
        }
        StringBuilder joined = new StringBuilder();
        for (String part : parts) {
            if (joined.length() > 0) {
                joined.append(group.logic().keyword());
            }
            joined.append(part);
        }
        return joined.toString();
    }

    private String renderCriterion(Criterion criterion, List<Object> params) {
        String column = dialect.quote(criterion.column());
        Op op = criterion.op();
        switch (op) {
            case IS_NULL:
                return column + " IS NULL";
            case IS_NOT_NULL:
                return column + " IS NOT NULL";
            case BETWEEN:
                params.add(criterion.values().get(0));
                params.add(criterion.values().get(1));
                return column + " BETWEEN ? AND ?";
            case IN:
            case NOT_IN:
                StringBuilder holders = new StringBuilder();
                for (Object value : criterion.values()) {
                    if (holders.length() > 0) {
                        holders.append(", ");
                    }
                    holders.append('?');
                    params.add(value);
                }
                return column + (op == Op.IN ? " IN (" : " NOT IN (") + holders + ")";
            default:
                params.add(criterion.values().get(0));
                return column + ' ' + op.symbol() + " ?";
        }
    }

    private static void requireCondition(Predicate where, String action) {
        if (where == null) {
            throw new IllegalArgumentException("全表 " + action + " 被拒绝, 确有需要请显式传 Criteria.alwaysTrue()");
        }
    }
}
