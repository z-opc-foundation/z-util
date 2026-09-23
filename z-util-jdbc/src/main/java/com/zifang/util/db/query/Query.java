package com.zifang.util.db.query;

import com.zifang.util.db.dialect.Dialect;
import com.zifang.util.db.support.Identifiers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 行级动态查询定义：单表投影 + 条件树 + 排序 + 分页。
 * <p>
 * 刻意不含聚合与 group by——聚合属于上层数据集（内存算或自行写 SqlTemplate），
 * z-jdbc 只负责把结构化的取数意图编译成参数化 SQL。
 *
 * @author zifang
 */
public final class Query {

    static final String ALL_COLUMNS = "*";

    private String table;

    private final List<String> columns = new ArrayList<>();

    private boolean distinct;

    private final List<Predicate> conditions = new ArrayList<>();

    private final List<Order> orders = new ArrayList<>();

    private long offset = -1;

    private long limit = -1;

    private Query() {
    }

    public static Query select(String... columns) {
        Query q = new Query();
        return q.selectColumns(columns);
    }

    /**
     * 表名。不设投影时为 {@code *}；投影与表名的先后顺序随意，
     * 因此 {@code Query.select("id").from("t")} 与 {@code Query.from("t").select("id")} 等价。
     */
    public Query from(String table) {
        Identifiers.parts(table);
        this.table = table;
        return this;
    }

    public Query selectColumns(String... columns) {
        if (columns != null) {
            for (String col : columns) {
                if (ALL_COLUMNS.equals(col)) {
                    this.columns.add(ALL_COLUMNS);
                } else {
                    Identifiers.parts(col);
                    this.columns.add(col);
                }
            }
        }
        return this;
    }

    public Query distinct() {
        this.distinct = true;
        return this;
    }

    /**
     * 顶层条件之间恒为 AND；需要 OR 时显式用 {@link Criteria#or}。
     */
    public Query where(Predicate condition) {
        if (condition != null) {
            this.conditions.add(condition);
        }
        return this;
    }

    public Query orderBy(String column) {
        return orderBy(column, true);
    }

    public Query orderBy(String column, boolean asc) {
        Identifiers.parts(column);
        this.orders.add(new Order(column, asc));
        return this;
    }

    public Query offset(long offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset 不能为负: " + offset);
        }
        this.offset = offset;
        return this;
    }

    public Query limit(long limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("limit 不能为负: " + limit);
        }
        this.limit = limit;
        return this;
    }

    /**
     * 页码从 1 开始。
     */
    public Query page(long pageNo, long pageSize) {
        if (pageNo < 1 || pageSize < 1) {
            throw new IllegalArgumentException("pageNo/pageSize 必须从 1 开始: " + pageNo + "/" + pageSize);
        }
        return limit(pageSize).offset((pageNo - 1) * pageSize);
    }

    public String table() {
        return table;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<String> columns() {
        return Collections.unmodifiableList(columns);
    }

    public List<Predicate> conditions() {
        return Collections.unmodifiableList(conditions);
    }

    public List<Order> orders() {
        return Collections.unmodifiableList(orders);
    }

    public long offset() {
        return offset;
    }

    public long limit() {
        return limit;
    }

    public SqlSpec compile(Dialect dialect) {
        return new QueryCompiler(dialect).compile(this);
    }

    public SqlSpec compileCount(Dialect dialect) {
        return new QueryCompiler(dialect).compileCount(this);
    }

    @Override
    public String toString() {
        return "Query{table=" + table + ", columns=" + columns + ", distinct=" + distinct
                + ", conditions=" + conditions + ", orders=" + orders
                + ", offset=" + offset + ", limit=" + limit + "}";
    }

    /**
     * 排序项。
     */
    public static final class Order {

        private final String column;

        private final boolean asc;

        Order(String column, boolean asc) {
            this.column = column;
            this.asc = asc;
        }

        public String column() {
            return column;
        }

        public boolean isAsc() {
            return asc;
        }
    }
}
