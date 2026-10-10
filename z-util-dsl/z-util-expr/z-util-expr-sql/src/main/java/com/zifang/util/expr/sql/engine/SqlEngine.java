package com.zifang.util.expr.sql.engine;

import com.zifang.util.expr.sql.SqlException;
import com.zifang.util.expr.sql.SqlFunctionDef;
import com.zifang.util.expr.sql.SqlFunctionRegistry;
import com.zifang.util.expr.sql.engine.ast.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 内存 SQL 执行引擎。
 * <p>
 * 执行流程：JOIN → WHERE → GROUP BY → SELECT → HAVING → ORDER BY → DISTINCT → LIMIT/OFFSET
 * <p>
 * 支持索引加速：WHERE 等值条件利用列索引，JOIN ON 等值条件利用哈希连接。
 */
public class SqlEngine {

    /**
     * 聚合函数名（与 evaluateAggregateFunction 支持面一致）。
     * GROUP BY / 隐式聚合场景下，只有命中这个名字面的函数才走聚合求值，
     * 其余一律按标量表达式对组代表行求值（MySQL 宽松语义）。
     */
    private static final Set<String> AGGREGATE_FUNCTIONS = new HashSet<>(
            Arrays.asList("COUNT", "SUM", "AVG", "MAX", "MIN", "GROUP_CONCAT"));

    private final TableRegistry registry;
    private final AtomicLong derivedTableCounter = new AtomicLong();

    public SqlEngine(TableRegistry registry) {
        this.registry = registry;
    }

    /**
     * 执行 SELECT 语句。
     *
     * @param stmt SELECT AST
     * @return 结果行列表
     */
    public List<Map<String, Object>> execute(SelectStmt stmt) {
        List<String> derivedTables = new ArrayList<>();
        try {
            stmt = materializeSubqueries(stmt, derivedTables);
            stmt = materializeInSubqueries(stmt);
            return executePlan(stmt);
        } finally {
            for (String name : derivedTables) {
                registry.unregister(name);
            }
        }
    }

    /**
     * 把 WHERE/HAVING/SELECT 里的 {@code IN (SELECT …)} 子查询各执行一次，
     * 物化成字面量列表（每查询一次，不做逐行重放）。
     */
    private SelectStmt materializeInSubqueries(SelectStmt stmt) {
        boolean changed = false;
        List<Expression> newSelectItems = stmt.getSelectItems();
        for (int i = 0; i < newSelectItems.size(); i++) {
            Expression replaced = replaceInSubqueries(newSelectItems.get(i));
            if (replaced != newSelectItems.get(i)) {
                if (newSelectItems == stmt.getSelectItems()) {
                    newSelectItems = new ArrayList<>(newSelectItems);
                }
                newSelectItems.set(i, replaced);
                changed = true;
            }
        }
        Expression newWhere = replaceInSubqueries(stmt.getWhereClause());
        Expression newHaving = replaceInSubqueries(stmt.getHavingClause());
        changed |= newWhere != stmt.getWhereClause() || newHaving != stmt.getHavingClause();
        if (!changed) {
            return stmt;
        }
        SelectStmt copy = new SelectStmt();
        copy.setDistinct(stmt.isDistinct());
        copy.setSelectItems(newSelectItems);
        copy.setTableName(stmt.getTableName());
        copy.setTableAlias(stmt.getTableAlias());
        copy.setWhereClause(newWhere);
        copy.setGroupBy(stmt.getGroupBy());
        copy.setHavingClause(newHaving);
        copy.setOrderBy(stmt.getOrderBy());
        copy.setLimit(stmt.getLimit());
        copy.setOffset(stmt.getOffset());
        copy.setJoins(stmt.getJoins());
        return copy;
    }

    private Expression replaceInSubqueries(Expression expr) {
        if (expr == null) {
            return null;
        }
        if (expr instanceof InExpr) {
            InExpr in = (InExpr) expr;
            if (in.getSubquery() == null) {
                return in;
            }
            List<Map<String, Object>> rows = execute(in.getSubquery());
            List<Expression> values = new ArrayList<>(rows.size());
            for (Map<String, Object> subRow : rows) {
                Object v = subRow.isEmpty() ? null : subRow.values().iterator().next();
                values.add(new Literal(v));
            }
            return new InExpr(in.getExpression(), values, in.isNegated());
        }
        if (expr instanceof AliasedExpr) {
            AliasedExpr a = (AliasedExpr) expr;
            Expression inner = replaceInSubqueries(a.getExpression());
            return inner == a.getExpression() ? a : new AliasedExpr(inner, a.getAlias());
        }
        if (expr instanceof BinaryExpr) {
            BinaryExpr b = (BinaryExpr) expr;
            Expression l = replaceInSubqueries(b.getLeft());
            Expression r = replaceInSubqueries(b.getRight());
            return (l == b.getLeft() && r == b.getRight()) ? b : new BinaryExpr(l, b.getOperator(), r);
        }
        if (expr instanceof UnaryExpr) {
            UnaryExpr u = (UnaryExpr) expr;
            Expression inner = replaceInSubqueries(u.getOperand());
            return inner == u.getOperand() ? u : new UnaryExpr(u.getOperator(), inner);
        }
        if (expr instanceof CastExpr) {
            CastExpr c = (CastExpr) expr;
            Expression inner = replaceInSubqueries(c.getExpression());
            return inner == c.getExpression() ? c : new CastExpr(inner, c.getTargetType());
        }
        if (expr instanceof IsNullExpr) {
            IsNullExpr c = (IsNullExpr) expr;
            Expression inner = replaceInSubqueries(c.getExpression());
            return inner == c.getExpression() ? c : new IsNullExpr(inner, c.isNegated());
        }
        if (expr instanceof BetweenExpr) {
            BetweenExpr b = (BetweenExpr) expr;
            Expression e1 = replaceInSubqueries(b.getExpression());
            Expression e2 = replaceInSubqueries(b.getLow());
            Expression e3 = replaceInSubqueries(b.getHigh());
            return (e1 == b.getExpression() && e2 == b.getLow() && e3 == b.getHigh())
                    ? b : new BetweenExpr(e1, e2, e3, b.isNegated());
        }
        if (expr instanceof CaseExpr) {
            CaseExpr c = (CaseExpr) expr;
            boolean same = true;
            Expression operand = replaceInSubqueries(c.getOperand());
            same &= operand == c.getOperand();
            List<Expression> conds = new ArrayList<>(c.getConditions().size());
            List<Expression> results = new ArrayList<>(c.getResults().size());
            for (Expression cond : c.getConditions()) {
                Expression r = replaceInSubqueries(cond);
                same &= r == cond;
                conds.add(r);
            }
            for (Expression res : c.getResults()) {
                Expression r = replaceInSubqueries(res);
                same &= r == res;
                results.add(r);
            }
            Expression elseR = replaceInSubqueries(c.getElseResult());
            same &= elseR == c.getElseResult();
            return same ? c : new CaseExpr(operand, conds, results, elseR);
        }
        if (expr instanceof FunctionCall) {
            FunctionCall f = (FunctionCall) expr;
            boolean same = true;
            List<Expression> args = new ArrayList<>(f.getArguments().size());
            for (Expression arg : f.getArguments()) {
                Expression r = replaceInSubqueries(arg);
                same &= r == arg;
                args.add(r);
            }
            return same ? f : new FunctionCall(f.getName(), args, f.isDistinct());
        }
        return expr;
    }

    /**
     * 把 FROM / JOIN 里的派生表（子查询）递归执行后物化成临时虚拟表，
     * 并返回表名被改写为该临时表的语句副本。物化名在查询结束后统一注销。
     */
    private SelectStmt materializeSubqueries(SelectStmt stmt, List<String> derivedTables) {
        String mainName = stmt.getTableName();
        boolean changed = false;

        if (stmt.getFromSubquery() != null) {
            mainName = materialize(stmt.getFromSubquery(), derivedTables);
            changed = true;
        }

        List<JoinClause> joins = stmt.getJoins();
        List<JoinClause> newJoins = joins;
        for (int i = 0; i < joins.size(); i++) {
            JoinClause join = joins.get(i);
            if (join.getSubquery() != null) {
                if (newJoins == joins) {
                    newJoins = new ArrayList<>(joins);
                }
                String name = materialize(join.getSubquery(), derivedTables);
                newJoins.set(i, new JoinClause(join.getJoinType(), name, join.getAlias(), join.getOnCondition()));
                changed = true;
            }
        }

        if (!changed) {
            return stmt;
        }

        SelectStmt copy = new SelectStmt();
        copy.setDistinct(stmt.isDistinct());
        copy.setSelectItems(stmt.getSelectItems());
        copy.setTableName(mainName);
        copy.setTableAlias(stmt.getTableAlias());
        copy.setWhereClause(stmt.getWhereClause());
        copy.setGroupBy(stmt.getGroupBy());
        copy.setHavingClause(stmt.getHavingClause());
        copy.setOrderBy(stmt.getOrderBy());
        copy.setLimit(stmt.getLimit());
        copy.setOffset(stmt.getOffset());
        copy.setJoins(newJoins);
        return copy;
    }

    private String materialize(SelectStmt subquery, List<String> derivedTables) {
        List<Map<String, Object>> rows = execute(subquery);
        String name = "__derived_" + derivedTableCounter.incrementAndGet() + "__";
        registry.register(name, rows);
        derivedTables.add(name);
        return name;
    }

    private List<Map<String, Object>> executePlan(SelectStmt stmt) {
        // 1. 加载主表
        VirtualTable mainTable = registry.getTable(stmt.getTableName());
        List<Map<String, Object>> rows = new ArrayList<>(mainTable.getRows());

        // 2. 表别名映射：为每行添加 table context
        String mainContext = stmt.getTableContextName();
        rows = addContext(rows, mainContext, mainTable.getColumns());

        // 3. JOIN（利用索引优化）
        for (JoinClause join : stmt.getJoins()) {
            rows = executeJoin(rows, join, stmt.getTableName(), mainContext);
        }

        // 4. WHERE（利用索引优化）
        if (stmt.getWhereClause() != null) {
            rows = applyWhere(rows, stmt.getWhereClause(), mainTable);
        }

        // 5. GROUP BY（HAVING 在分组结果行上过滤，聚合调用先物化进行内）
        boolean hasGroupBy = !stmt.getGroupBy().isEmpty();
        boolean aggregated = false;
        if (hasGroupBy) {
            rows = applyGroupBy(rows, stmt.getGroupBy(), stmt.getSelectItems(), stmt.getHavingClause());
            aggregated = true;
        } else if (hasAggregateFunction(stmt.getSelectItems())) {
            // 6. HAVING（隐式聚合：聚合调用对全集求值）
            rows = applyImplicitAggregate(rows, stmt.getSelectItems());
            if (stmt.getHavingClause() != null && !rows.isEmpty()) {
                if (!toBool(evaluateForImplicitAggregate(stmt.getHavingClause(), rows))) {
                    rows = Collections.emptyList();
                }
            }
            aggregated = true;
        } else if (stmt.getHavingClause() != null) {
            rows = applyWhere(rows, stmt.getHavingClause(), null);
        }

        // 7. SELECT (投影)，窗口函数列先整体预计算
        if (!aggregated) {
            Map<WindowFuncExpr, List<Object>> windowValues = computeWindowValues(rows, stmt.getSelectItems());
            rows = applySelect(rows, stmt.getSelectItems(), stmt.isDistinct(), windowValues);
        }

        // 8. ORDER BY
        if (!stmt.getOrderBy().isEmpty()) {
            rows = applyOrderBy(rows, stmt.getOrderBy());
        }

        // 9. DISTINCT
        if (stmt.isDistinct() && stmt.getGroupBy().isEmpty()) {
            rows = applyDistinct(rows);
        }

        // 10. LIMIT / OFFSET
        if (stmt.getOffset() != null || stmt.getLimit() != null) {
            rows = applyLimitOffset(rows, stmt.getOffset(), stmt.getLimit());
        }

        return rows;
    }

    // ==================== 上下文添加 ====================

    private List<Map<String, Object>> addContext(List<Map<String, Object>> rows, String context, Set<String> columns) {
        List<Map<String, Object>> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            LinkedHashMap<String, Object> ctxRow = new LinkedHashMap<>(row);
            for (String col : columns) {
                ctxRow.put(context + "." + col, row.get(col));
            }
            result.add(ctxRow);
        }
        return result;
    }

    // ==================== JOIN（索引优化） ====================

    private List<Map<String, Object>> executeJoin(List<Map<String, Object>> leftRows,
                                                  JoinClause join,
                                                  String mainTableName,
                                                  String mainContext) {
        VirtualTable rightTable = registry.getTable(join.getTableName());
        String rightContext = join.getTableKey();

        EquiJoinCondition equiJoin = extractEquiJoin(join.getOnCondition(), mainContext, rightContext);

        if (equiJoin != null) {
            return executeHashJoin(leftRows, rightTable, rightContext, equiJoin, join.getJoinType() == JoinClause.JoinType.LEFT);
        } else {
            List<Map<String, Object>> rightRows = addContext(rightTable.getRows(), rightContext, rightTable.getColumns());
            return executeNestedLoopJoin(leftRows, rightRows, join, rightTable);
        }
    }

    private EquiJoinCondition extractEquiJoin(Expression onCondition, String leftContext, String rightContext) {
        if (onCondition == null) return null;
        if (!(onCondition instanceof BinaryExpr)) return null;

        BinaryExpr bin = (BinaryExpr) onCondition;
        if (!"=".equals(bin.getOperator()) && !"==".equals(bin.getOperator())) return null;

        Expression left = bin.getLeft();
        Expression right = bin.getRight();

        if (!(left instanceof ColumnRef) || !(right instanceof ColumnRef)) return null;

        ColumnRef leftCol = (ColumnRef) left;
        ColumnRef rightCol = (ColumnRef) right;

        String leftTable = leftCol.getTable() != null ? leftCol.getTable() : leftContext;
        String leftColName = leftCol.getColumn();
        String rightTable = rightCol.getTable() != null ? rightCol.getTable() : rightContext;
        String rightColName = rightCol.getColumn();

        if (leftTable.equals(leftContext) && rightTable.equals(rightContext)) {
            return new EquiJoinCondition(leftColName, rightColName);
        } else if (rightTable.equals(leftContext) && leftTable.equals(rightContext)) {
            return new EquiJoinCondition(rightColName, leftColName);
        }

        return null;
    }

    private static class EquiJoinCondition {
        final String leftColumn;
        final String rightColumn;

        EquiJoinCondition(String leftColumn, String rightColumn) {
            this.leftColumn = leftColumn;
            this.rightColumn = rightColumn;
        }
    }

    private List<Map<String, Object>> executeHashJoin(List<Map<String, Object>> leftRows,
                                                      VirtualTable rightTable,
                                                      String rightContext,
                                                      EquiJoinCondition condition,
                                                      boolean isLeftJoin) {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Map<String, Object>> rightRows = addContext(rightTable.getRows(), rightContext, rightTable.getColumns());

        // 从 context-enhanced 行构建哈希索引，确保与左表查找使用相同的列名
        Map<Object, List<Map<String, Object>>> rightIndex = new HashMap<>();
        for (Map<String, Object> rightRow : rightRows) {
            Object rightVal = getColumnValue(rightRow, condition.rightColumn, rightContext);
            if (rightVal == null) continue;
            rightIndex.computeIfAbsent(rightVal, k -> new ArrayList<>()).add(rightRow);
        }

        // 遍历左表，用哈希索引查找匹配（类型一致：都使用 context-enhanced 列名）
        for (Map<String, Object> leftRow : leftRows) {
            Object leftVal = getColumnValue(leftRow, condition.leftColumn, null);
            List<Map<String, Object>> matchedRightRows = leftVal != null ? rightIndex.get(leftVal) : null;

            if (matchedRightRows != null && !matchedRightRows.isEmpty()) {
                for (Map<String, Object> rightRow : matchedRightRows) {
                    LinkedHashMap<String, Object> merged = new LinkedHashMap<>(leftRow);
                    merged.putAll(rightRow);
                    result.add(merged);
                }
            } else if (isLeftJoin) {
                LinkedHashMap<String, Object> merged = new LinkedHashMap<>(leftRow);
                for (String col : rightTable.getColumnNames()) {
                    merged.putIfAbsent(rightContext + "." + col, null);
                    merged.putIfAbsent(col, null);
                }
                result.add(merged);
            }
        }

        return result;
    }

    /**
     * 从行中提取列值，优先使用 context-enhanced 列名（table.column），
     * 回退到原始列名。确保 JOIN 两侧使用一致的列名。
     */
    private Object getColumnValue(Map<String, Object> row, String columnName, String context) {
        if (context != null) {
            Object val = row.get(context + "." + columnName);
            if (val != null || row.containsKey(context + "." + columnName)) return val;
        }
        Object val = VirtualTable.getCellValue(row, columnName);
        return val;
    }

    private List<Map<String, Object>> executeNestedLoopJoin(List<Map<String, Object>> leftRows,
                                                            List<Map<String, Object>> rightRows,
                                                            JoinClause join,
                                                            VirtualTable rightTable) {
        String rightContext = join.getTableKey();
        boolean isLeftJoin = join.getJoinType() == JoinClause.JoinType.LEFT;

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> leftRow : leftRows) {
            boolean matched = false;
            for (Map<String, Object> rightRow : rightRows) {
                LinkedHashMap<String, Object> merged = new LinkedHashMap<>(leftRow);
                merged.putAll(rightRow);

                if (join.getOnCondition() == null || evaluateBool(join.getOnCondition(), merged)) {
                    result.add(merged);
                    matched = true;
                }
            }
            if (!matched && isLeftJoin) {
                LinkedHashMap<String, Object> merged = new LinkedHashMap<>(leftRow);
                for (String col : rightTable.getColumnNames()) {
                    merged.putIfAbsent(rightContext + "." + col, null);
                    merged.putIfAbsent(col, null);
                }
                result.add(merged);
            }
        }
        return result;
    }

    // ==================== WHERE / HAVING（索引优化） ====================

    private List<Map<String, Object>> applyWhere(List<Map<String, Object>> rows, Expression condition, VirtualTable table) {
        // 直接全表扫描求值。
        // 索引优化仅适用于单表查询（无 JOIN），但为简化执行流程，
        // 统一使用全表扫描。hash join 已提供主要性能优化。
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            if (evaluateBool(condition, row)) {
                result.add(row);
            }
        }
        return result;
    }

    private EquiCondition extractEquiCondition(Expression condition) {
        if (!(condition instanceof BinaryExpr)) return null;
        BinaryExpr bin = (BinaryExpr) condition;
        if (!"=".equals(bin.getOperator()) && !"==".equals(bin.getOperator())) return null;

        Expression left = bin.getLeft();
        Expression right = bin.getRight();

        if (left instanceof ColumnRef && right instanceof Literal) {
            ColumnRef col = (ColumnRef) left;
            String colName = col.getTable() != null ? col.getQualifiedName() : col.getColumn();
            return new EquiCondition(colName, ((Literal) right).getValue());
        }
        if (left instanceof Literal && right instanceof ColumnRef) {
            ColumnRef col = (ColumnRef) right;
            String colName = col.getTable() != null ? col.getQualifiedName() : col.getColumn();
            return new EquiCondition(colName, ((Literal) left).getValue());
        }

        return null;
    }

    private static class EquiCondition {
        final String column;
        final Object value;

        EquiCondition(String column, Object value) {
            this.column = column;
            this.value = value;
        }
    }

    // ==================== SELECT 投影 ====================

    private List<Map<String, Object>> applySelect(List<Map<String, Object>> rows,
                                                   List<Expression> selectItems,
                                                   boolean distinct,
                                                   Map<WindowFuncExpr, List<Object>> windowValues) {
        if (selectItems.size() == 1 && selectItems.get(0) instanceof ColumnRef
                && "*".equals(((ColumnRef) selectItems.get(0)).getColumn())) {
            return rows;
        }

        List<Map<String, Object>> result = new ArrayList<>(rows.size());
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            Map<String, Object> row = rows.get(rowIndex);
            LinkedHashMap<String, Object> projected = new LinkedHashMap<>();
            for (Expression item : selectItems) {
                String outputName;
                Expression expr;
                if (item instanceof AliasedExpr) {
                    AliasedExpr aliased = (AliasedExpr) item;
                    outputName = aliased.getAlias();
                    expr = aliased.getExpression();
                } else if (item instanceof ColumnRef) {
                    ColumnRef col = (ColumnRef) item;
                    outputName = col.getColumn();
                    expr = col;
                } else {
                    outputName = item.toString();
                    expr = item;
                }
                if (expr instanceof WindowFuncExpr) {
                    List<Object> vals = windowValues.get(expr);
                    projected.put(outputName, vals != null ? vals.get(rowIndex) : null);
                } else {
                    projected.put(outputName, evaluate(expr, row));
                }
            }
            result.add(projected);
        }
        return result;
    }

    // ==================== 窗口函数 ====================

    /**
     * 对 SELECT 列表里的窗口函数做整体预计算，返回每个窗口调用对应当前行序的取值列。
     * 窗口在 WHERE/GROUP 之后、投影之前的行集上求值；行序保持原样，
     * 窗口内部的 ORDER BY 只影响编号分配，不改变输出行序（同 SQL 语义）。
     */
    private Map<WindowFuncExpr, List<Object>> computeWindowValues(List<Map<String, Object>> rows,
                                                                  List<Expression> selectItems) {
        List<WindowFuncExpr> windows = new ArrayList<>();
        for (Expression item : selectItems) {
            Expression expr = item instanceof AliasedExpr ? ((AliasedExpr) item).getExpression() : item;
            if (expr instanceof WindowFuncExpr) {
                windows.add((WindowFuncExpr) expr);
            }
        }
        if (windows.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<WindowFuncExpr, List<Object>> out = new IdentityHashMap<>();
        for (WindowFuncExpr w : windows) {
            out.put(w, evaluateWindow(w, rows));
        }
        return out;
    }

    private List<Object> evaluateWindow(WindowFuncExpr w, List<Map<String, Object>> rows) {
        int n = rows.size();
        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) order[i] = i;
        final List<Map<String, Object>> finalRows = rows;
        final List<Expression> partitionBy = w.getPartitionBy();
        final List<SortKey> orderByKeys = w.getOrderBy();
        Arrays.sort(order, (a, b) -> {
            for (Expression p : partitionBy) {
                int cmp = compareValues(evaluate(p, finalRows.get(a)), evaluate(p, finalRows.get(b)));
                if (cmp != 0) return cmp;
            }
            for (SortKey key : orderByKeys) {
                int cmp = compareValues(evaluate(key.getExpression(), finalRows.get(a)),
                        evaluate(key.getExpression(), finalRows.get(b)));
                if (cmp != 0) return key.isDescending() ? -cmp : cmp;
            }
            return a - b; // 稳定：同键保持原行序
        });

        String funcName = w.getName().toUpperCase();
        List<Object> values = new ArrayList<>(Collections.nCopies(n, null));
        int pos = 0;
        while (pos < n) {
            // 逐分区处理
            int start = pos;
            while (pos < n && !partitionChanged(partitionBy, finalRows.get(order[start]), finalRows.get(order[pos]))) {
                pos++;
            }
            int end = pos;
            long rank = 0;
            long denseRank = 0;
            for (int i = start; i < end; i++) {
                int rowIdx = order[i];
                if (i == start) {
                    rank = 1;
                    denseRank = 1;
                } else {
                    int prev = order[i - 1];
                    boolean tied = orderKeysEqual(orderByKeys, finalRows.get(prev), finalRows.get(rowIdx));
                    if (!tied) {
                        rank = i - start + 1;
                        denseRank++;
                    }
                }
                switch (funcName) {
                    case "ROW_NUMBER":
                        values.set(rowIdx, (long) (i - start + 1));
                        break;
                    case "RANK":
                        values.set(rowIdx, rank);
                        break;
                    case "DENSE_RANK":
                        values.set(rowIdx, denseRank);
                        break;
                    default:
                        throw new SqlException("不支持的窗口函数: " + w.getName()
                                + "（当前支持 ROW_NUMBER / RANK / DENSE_RANK）");
                }
            }
        }
        return values;
    }

    private boolean partitionChanged(List<Expression> partitionBy, Map<String, Object> a, Map<String, Object> b) {
        for (Expression p : partitionBy) {
            Object va = evaluate(p, a);
            Object vb = evaluate(p, b);
            if (!numericEquals(va, vb)) {
                return true;
            }
        }
        return false;
    }

    private boolean orderKeysEqual(List<SortKey> orderByKeys, Map<String, Object> a, Map<String, Object> b) {
        for (SortKey key : orderByKeys) {
            Object va = evaluate(key.getExpression(), a);
            Object vb = evaluate(key.getExpression(), b);
            if (compareValues(va, vb) != 0) {
                return false;
            }
        }
        return true;
    }

    private List<Map<String, Object>> applyOrderBy(List<Map<String, Object>> rows, List<SortKey> sortKeys) {
        List<Map<String, Object>> result = new ArrayList<>(rows);
        result.sort((a, b) -> {
            for (SortKey key : sortKeys) {
                Object va = evaluate(key.getExpression(), a);
                Object vb = evaluate(key.getExpression(), b);
                int cmp = compareValues(va, vb);
                if (cmp != 0) {
                    return key.isDescending() ? -cmp : cmp;
                }
            }
            return 0;
        });
        return result;
    }

    // ==================== 隐式聚合 ====================

    /**
     * 递归扫描一个表达式树里是否出现聚合函数。
     * <p>
     * 顶层只是 {@code SUM(x)*1.0/COUNT(*)} 这种 BinaryExpr 时，原来的"只看顶层
     * {@code FunctionCall}"会把隐式聚合漏判，逐行退化返回 N 行；这里改成深度优先。
     */
    private boolean hasAggregateFunction(List<Expression> selectItems) {
        for (Expression item : selectItems) {
            if (containsAggregate(item)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsAggregate(Expression expr) {
        if (expr == null) {
            return false;
        }
        if (expr instanceof FunctionCall) {
            return AGGREGATE_FUNCTIONS.contains(((FunctionCall) expr).getName());
        }
        if (expr instanceof AliasedExpr) {
            return containsAggregate(((AliasedExpr) expr).getExpression());
        }
        if (expr instanceof BinaryExpr) {
            return containsAggregate(((BinaryExpr) expr).getLeft())
                    || containsAggregate(((BinaryExpr) expr).getRight());
        }
        if (expr instanceof UnaryExpr) {
            return containsAggregate(((UnaryExpr) expr).getOperand());
        }
        if (expr instanceof CastExpr) {
            return containsAggregate(((CastExpr) expr).getExpression());
        }
        if (expr instanceof BetweenExpr) {
            return containsAggregate(((BetweenExpr) expr).getExpression())
                    || containsAggregate(((BetweenExpr) expr).getLow())
                    || containsAggregate(((BetweenExpr) expr).getHigh());
        }
        if (expr instanceof InExpr) {
            return containsAggregate(((InExpr) expr).getExpression());
        }
        if (expr instanceof CaseExpr) {
            CaseExpr c = (CaseExpr) expr;
            if (containsAggregate(c.getOperand()) || containsAggregate(c.getElseResult())) {
                return true;
            }
            for (Expression cond : c.getConditions()) {
                if (containsAggregate(cond)) return true;
            }
            for (Expression res : c.getResults()) {
                if (containsAggregate(res)) return true;
            }
            return false;
        }
        return false;
    }

    private List<Map<String, Object>> applyImplicitAggregate(List<Map<String, Object>> rows,
                                                              List<Expression> selectItems) {
        LinkedHashMap<String, Object> resultRow = new LinkedHashMap<>();
        for (Expression item : selectItems) {
            String outputName = getExprOutputName(item);
            Expression expr = item instanceof AliasedExpr ? ((AliasedExpr) item).getExpression() : item;
            resultRow.put(outputName, evaluateForImplicitAggregate(expr, rows));
        }
        return Collections.singletonList(resultRow);
    }

    /**
     * 隐式聚合求值：表达式树里聚合函数走全集，非聚合节点走首行（同 SQL 同义）。
     * 让 {@code SUM(x)*1.0/COUNT(*)} 这种包裹聚合整棵子树都能在隐式聚合模式下拿到单行结果。
     */
    private Object evaluateForImplicitAggregate(Expression expr, List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return null;
        }
        if (expr instanceof FunctionCall && isAggregateCall((FunctionCall) expr)) {
            return evaluateAggregateFunction((FunctionCall) expr, rows);
        }
        if (expr instanceof AliasedExpr) {
            return evaluateForImplicitAggregate(((AliasedExpr) expr).getExpression(), rows);
        }
        if (expr instanceof BinaryExpr) {
            BinaryExpr bin = (BinaryExpr) expr;
            Object l = evaluateForImplicitAggregate(bin.getLeft(), rows);
            Object r = evaluateForImplicitAggregate(bin.getRight(), rows);
            switch (bin.getOperator()) {
                case "=": case "==": return numericEquals(l, r);
                case "<>": case "!=": return !numericEquals(l, r);
                case "<": return compareValues(l, r) < 0;
                case ">": return compareValues(l, r) > 0;
                case "<=": return compareValues(l, r) <= 0;
                case ">=": return compareValues(l, r) >= 0;
                case "AND": return toBool(l) && toBool(r);
                case "OR": return toBool(l) || toBool(r);
                case "+": case "-": case "*": case "/": case "%":
                    return doArithmetic(l, bin.getOperator(), r);
            }
            throw new SqlException("未知的二元运算符: " + bin.getOperator());
        }
        if (expr instanceof UnaryExpr) {
            UnaryExpr unary = (UnaryExpr) expr;
            Object operand = evaluateForImplicitAggregate(unary.getOperand(), rows);
            if ("NOT".equals(unary.getOperator())) {
                return !toBool(operand);
            }
            if ("-".equals(unary.getOperator())) {
                if (operand == null) return null;
                if (operand instanceof Number) return -((Number) operand).doubleValue();
                return null;
            }
            throw new SqlException("未知的一元运算符: " + unary.getOperator());
        }
        if (expr instanceof CastExpr) {
            return castValue(evaluateForImplicitAggregate(((CastExpr) expr).getExpression(), rows),
                    ((CastExpr) expr).getTargetType());
        }
        if (expr instanceof IsNullExpr) {
            IsNullExpr isNull = (IsNullExpr) expr;
            Object val = evaluateForImplicitAggregate(isNull.getExpression(), rows);
            return isNull.isNegated() ? val != null : val == null;
        }
        if (expr instanceof BetweenExpr) {
            BetweenExpr between = (BetweenExpr) expr;
            Object val = evaluateForImplicitAggregate(between.getExpression(), rows);
            Object low = evaluateForImplicitAggregate(between.getLow(), rows);
            Object high = evaluateForImplicitAggregate(between.getHigh(), rows);
            boolean inRange = compareValues(val, low) >= 0 && compareValues(val, high) <= 0;
            return between.isNegated() ? !inRange : inRange;
        }
        if (expr instanceof InExpr) {
            InExpr inExpr = (InExpr) expr;
            Object val = evaluateForImplicitAggregate(inExpr.getExpression(), rows);
            boolean found = false;
            for (Expression v : inExpr.getValues()) {
                if (Objects.equals(val, evaluateForImplicitAggregate(v, rows))) {
                    found = true;
                    break;
                }
            }
            return inExpr.isNegated() ? !found : found;
        }
        // 非聚合叶子（列名 / 字面量 / 普通函数）：按首行求值（同 SQL 同义）。
        Map<String, Object> firstRow = rows.get(0);
        if (expr instanceof Literal) {
            return ((Literal) expr).getValue();
        }
        if (expr instanceof ColumnRef) {
            ColumnRef col = (ColumnRef) expr;
            if ("*".equals(col.getColumn())) return null;
            if (col.getTable() != null) {
                Object val = VirtualTable.getCellValue(firstRow, col.getQualifiedName());
                if (val != null || firstRow.containsKey(col.getQualifiedName())) return val;
            }
            return VirtualTable.getCellValue(firstRow, col.getColumn());
        }
        // 普通函数：以首行为上下文求值（与 evaluate 一致；无副作用参与聚合）
        if (expr instanceof FunctionCall) {
            return evaluateFunction((FunctionCall) expr, firstRow);
        }
        return evaluate(expr, firstRow);
    }

    // ==================== GROUP BY ====================

    private List<Map<String, Object>> applyGroupBy(List<Map<String, Object>> rows,
                                                    List<Expression> groupByExprs,
                                                    List<Expression> selectItems,
                                                    Expression havingClause) {
        Map<String, List<Map<String, Object>>> groups = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String key = computeGroupKey(groupByExprs, row);
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : groups.entrySet()) {
            List<Map<String, Object>> groupRows = entry.getValue();
            Map<String, Object> firstRow = groupRows.get(0);
            LinkedHashMap<String, Object> resultRow = new LinkedHashMap<>();

            for (Expression expr : groupByExprs) {
                String name = getExprOutputName(expr);
                resultRow.put(name, evaluate(expr, firstRow));
            }

            for (Expression item : selectItems) {
                String outputName = getExprOutputName(item);
                if (!resultRow.containsKey(outputName)) {
                    resultRow.put(outputName, evaluateSelectItemForGroup(item, groupRows, firstRow));
                }
            }

            // HAVING 里的聚合调用（含 SELECT 未出现的）先物化到组结果行，
            // 再走普通行级求值——HAVING SUM(x) 直接引用与别名引用都可达。
            if (havingClause != null) {
                for (FunctionCall agg : collectAggregateCalls(havingClause)) {
                    String key = getExprOutputName(agg);
                    if (!resultRow.containsKey(key)) {
                        resultRow.put(key, evaluateAggregateFunction(agg, groupRows));
                    }
                }
                if (!evaluateBool(havingClause, resultRow)) {
                    continue;
                }
            }

            result.add(resultRow);
        }
        return result;
    }

    /**
     * 收集表达式树里所有的聚合函数调用（去重按实例）。
     */
    private List<FunctionCall> collectAggregateCalls(Expression expr) {
        List<FunctionCall> out = new ArrayList<>();
        collectAggregateCalls(expr, out);
        return out;
    }

    private void collectAggregateCalls(Expression expr, List<FunctionCall> out) {
        if (expr == null) {
            return;
        }
        if (expr instanceof FunctionCall) {
            FunctionCall f = (FunctionCall) expr;
            if (isAggregateCall(f)) {
                out.add(f);
                return;
            }
            for (Expression arg : f.getArguments()) {
                collectAggregateCalls(arg, out);
            }
            return;
        }
        if (expr instanceof AliasedExpr) {
            collectAggregateCalls(((AliasedExpr) expr).getExpression(), out);
        } else if (expr instanceof BinaryExpr) {
            collectAggregateCalls(((BinaryExpr) expr).getLeft(), out);
            collectAggregateCalls(((BinaryExpr) expr).getRight(), out);
        } else if (expr instanceof UnaryExpr) {
            collectAggregateCalls(((UnaryExpr) expr).getOperand(), out);
        } else if (expr instanceof CastExpr) {
            collectAggregateCalls(((CastExpr) expr).getExpression(), out);
        } else if (expr instanceof IsNullExpr) {
            collectAggregateCalls(((IsNullExpr) expr).getExpression(), out);
        } else if (expr instanceof BetweenExpr) {
            collectAggregateCalls(((BetweenExpr) expr).getExpression(), out);
            collectAggregateCalls(((BetweenExpr) expr).getLow(), out);
            collectAggregateCalls(((BetweenExpr) expr).getHigh(), out);
        } else if (expr instanceof InExpr) {
            collectAggregateCalls(((InExpr) expr).getExpression(), out);
            for (Expression v : ((InExpr) expr).getValues()) {
                if (v != null) collectAggregateCalls(v, out);
            }
        } else if (expr instanceof CaseExpr) {
            CaseExpr c = (CaseExpr) expr;
            collectAggregateCalls(c.getOperand(), out);
            for (Expression cond : c.getConditions()) collectAggregateCalls(cond, out);
            for (Expression res : c.getResults()) collectAggregateCalls(res, out);
            collectAggregateCalls(c.getElseResult(), out);
        }
    }

    private String computeGroupKey(List<Expression> groupByExprs, Map<String, Object> row) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < groupByExprs.size(); i++) {
            if (i > 0) sb.append("\0");
            Object val = evaluate(groupByExprs.get(i), row);
            sb.append(val);
        }
        return sb.toString();
    }

    private Object evaluateSelectItemForGroup(Expression item, List<Map<String, Object>> groupRows, Map<String, Object> firstRow) {
        Expression expr;
        if (item instanceof AliasedExpr) {
            expr = ((AliasedExpr) item).getExpression();
        } else {
            expr = item;
        }

        if (expr instanceof FunctionCall && isAggregateCall((FunctionCall) expr)) {
            return evaluateAggregateFunction((FunctionCall) expr, groupRows);
        }
        // 非聚合函数（DATE/IF/INSTR/JSON_* 等）按标量对组代表行求值（MySQL 宽松语义）
        return evaluate(expr, firstRow);
    }

    private boolean isAggregateCall(FunctionCall func) {
        return AGGREGATE_FUNCTIONS.contains(func.getName());
    }

    private Object evaluateAggregateFunction(FunctionCall func, List<Map<String, Object>> rows) {
        String funcName = func.getName();
        List<Expression> args = func.getArguments();

        switch (funcName) {
            case "COUNT": {
                if (args.size() == 1 && args.get(0) instanceof ColumnRef
                        && "*".equals(((ColumnRef) args.get(0)).getColumn())) {
                    return (long) rows.size();
                }
                long count = 0;
                for (Map<String, Object> row : rows) {
                    Object val = evaluate(args.get(0), row);
                    if (val != null) count++;
                }
                return count;
            }
            case "SUM": {
                // 类型口径与 z-util-expr-obj 的 Aggregates 对齐：
                // 全整型列给 Long，掺了浮点才给 Double（前端拿到的 1000 不该渲染成 1000.0）
                BigDecimal sum = BigDecimal.ZERO;
                boolean integral = true;
                for (Map<String, Object> row : rows) {
                    Object val = evaluate(args.get(0), row);
                    if (val != null) {
                        sum = sum.add(toBigDecimal(val));
                        integral &= isIntegralValue(val);
                    }
                }
                if (integral) {
                    try {
                        return sum.longValueExact();
                    } catch (ArithmeticException e) {
                        return sum; // 超出 long 的整型和，保精度降级为 BigDecimal
                    }
                }
                return sum.doubleValue();
            }
            case "AVG": {
                BigDecimal sum = BigDecimal.ZERO;
                long count = 0;
                for (Map<String, Object> row : rows) {
                    Object val = evaluate(args.get(0), row);
                    if (val != null) {
                        sum = sum.add(toBigDecimal(val));
                        count++;
                    }
                }
                if (count == 0) return null;
                BigDecimal avg = sum.divide(BigDecimal.valueOf(count), 10, BigDecimal.ROUND_HALF_UP).stripTrailingZeros();
                // stripTrailingZeros 会把 100 变成 1E+2（scale<0），Jackson 会原样序列化成科学计数法
                if (avg.scale() < 0) {
                    avg = avg.setScale(0);
                }
                return avg;
            }
            case "MAX": {
                Object max = null;
                for (Map<String, Object> row : rows) {
                    Object val = evaluate(args.get(0), row);
                    if (val != null && (max == null || compareValues(val, max) > 0)) {
                        max = val;
                    }
                }
                return max;
            }
            case "MIN": {
                Object min = null;
                for (Map<String, Object> row : rows) {
                    Object val = evaluate(args.get(0), row);
                    if (val != null && (min == null || compareValues(val, min) < 0)) {
                        min = val;
                    }
                }
                return min;
            }
            case "GROUP_CONCAT": {
                List<Expression> gArgs = func.getArguments();
                if (gArgs.isEmpty()) {
                    return null;
                }
                Expression valueExpr = gArgs.get(0);
                String separator = ",";
                if (gArgs.size() == 3 && gArgs.get(1) instanceof ColumnRef
                        && "SEPARATOR".equalsIgnoreCase(((ColumnRef) gArgs.get(1)).getColumn())) {
                    Object sepVal = rows.isEmpty() ? null : evaluate(gArgs.get(2), rows.get(0));
                    separator = sepVal != null ? sepVal.toString() : ",";
                }
                Set<Object> seen = func.isDistinct() ? new LinkedHashSet<>() : null;
                StringBuilder joined = new StringBuilder();
                for (Map<String, Object> row : rows) {
                    Object val = evaluate(valueExpr, row);
                    if (val == null) {
                        continue;
                    }
                    if (seen != null && !seen.add(val)) {
                        continue;
                    }
                    if (joined.length() > 0) {
                        joined.append(separator);
                    }
                    joined.append(val);
                }
                return joined.length() == 0 ? null : joined.toString();
            }
            default:
                throw new SqlException("未知的聚合函数: " + funcName);
        }
    }

    // ==================== DISTINCT ====================

    private List<Map<String, Object>> applyDistinct(List<Map<String, Object>> rows) {
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String key = rowtoString(row);
            if (seen.add(key)) {
                result.add(row);
            }
        }
        return result;
    }

    // ==================== LIMIT / OFFSET ====================

    private List<Map<String, Object>> applyLimitOffset(List<Map<String, Object>> rows, Integer offset, Integer limit) {
        int start = offset != null ? Math.max(0, offset) : 0;
        int end = limit != null ? Math.min(rows.size(), start + limit) : rows.size();
        if (start >= rows.size()) return Collections.emptyList();
        return new ArrayList<>(rows.subList(start, end));
    }

    // ==================== 表达式求值 ====================

    public Object evaluate(Expression expr, Map<String, Object> row) {
        if (expr == null) return null;

        if (expr instanceof Literal) {
            return ((Literal) expr).getValue();
        }

        if (expr instanceof ColumnRef) {
            ColumnRef col = (ColumnRef) expr;
            if ("*".equals(col.getColumn())) return null;
            if (col.getTable() != null) {
                Object val = VirtualTable.getCellValue(row, col.getQualifiedName());
                if (val != null || row.containsKey(col.getQualifiedName())) return val;
            }
            return VirtualTable.getCellValue(row, col.getColumn());
        }

        if (expr instanceof BinaryExpr) {
            BinaryExpr bin = (BinaryExpr) expr;
            String op = bin.getOperator();

            if ("AND".equals(op)) {
                return toBool(evaluate(bin.getLeft(), row)) && toBool(evaluate(bin.getRight(), row));
            }
            if ("OR".equals(op)) {
                return toBool(evaluate(bin.getLeft(), row)) || toBool(evaluate(bin.getRight(), row));
            }

            Object left = evaluate(bin.getLeft(), row);
            Object right = evaluate(bin.getRight(), row);

            switch (op) {
                case "=":
                case "==": return numericEquals(left, right);
                case "<>":
                case "!=": return !numericEquals(left, right);
                case "<": return compareValues(left, right) < 0;
                case ">": return compareValues(left, right) > 0;
                case "<=": return compareValues(left, right) <= 0;
                case ">=": return compareValues(left, right) >= 0;
                case "LIKE": return matchLike(left != null ? left.toString() : "", right != null ? right.toString() : "");
                case "+": case "-": case "*": case "/": case "%":
                    return doArithmetic(left, op, right);
            }
            throw new SqlException("未知的二元运算符: " + op);
        }

        if (expr instanceof UnaryExpr) {
            UnaryExpr unary = (UnaryExpr) expr;
            Object operand = evaluate(unary.getOperand(), row);
            switch (unary.getOperator()) {
                case "NOT": return !toBool(operand);
                case "-":
                    if (operand == null) return null;
                    if (operand instanceof Number) return -((Number) operand).doubleValue();
                    return null;
                default:
                    throw new SqlException("未知的一元运算符: " + unary.getOperator());
            }
        }

        if (expr instanceof FunctionCall) {
            return evaluateFunction((FunctionCall) expr, row);
        }

        if (expr instanceof CastExpr) {
            CastExpr cast = (CastExpr) expr;
            Object val = evaluate(cast.getExpression(), row);
            return castValue(val, cast.getTargetType());
        }

        if (expr instanceof IsNullExpr) {
            IsNullExpr isNull = (IsNullExpr) expr;
            Object val = evaluate(isNull.getExpression(), row);
            return isNull.isNegated() ? val != null : val == null;
        }

        if (expr instanceof BetweenExpr) {
            BetweenExpr between = (BetweenExpr) expr;
            Object val = evaluate(between.getExpression(), row);
            Object low = evaluate(between.getLow(), row);
            Object high = evaluate(between.getHigh(), row);
            boolean inRange = compareValues(val, low) >= 0 && compareValues(val, high) <= 0;
            return between.isNegated() ? !inRange : inRange;
        }

        if (expr instanceof InExpr) {
            InExpr inExpr = (InExpr) expr;
            Object val = evaluate(inExpr.getExpression(), row);
            // 未预物化的子查询兜底（理论上 execute 入口已统一物化）
            List<Object> candidates;
            if (inExpr.getSubquery() != null) {
                List<Map<String, Object>> subRows = execute(inExpr.getSubquery());
                candidates = new ArrayList<>(subRows.size());
                for (Map<String, Object> subRow : subRows) {
                    candidates.add(subRow.isEmpty() ? null : subRow.values().iterator().next());
                }
            } else {
                candidates = new ArrayList<>(inExpr.getValues().size());
                for (Expression v : inExpr.getValues()) {
                    candidates.add(evaluate(v, row));
                }
            }
            boolean found = false;
            for (Object vVal : candidates) {
                if (numericEquals(val, vVal)) {
                    found = true;
                    break;
                }
            }
            return inExpr.isNegated() ? !found : found;
        }

        if (expr instanceof CaseExpr) {
            CaseExpr caseExpr = (CaseExpr) expr;
            Object operand = caseExpr.getOperand() != null ? evaluate(caseExpr.getOperand(), row) : null;
            for (int i = 0; i < caseExpr.getConditions().size(); i++) {
                Expression cond = caseExpr.getConditions().get(i);
                Expression result = caseExpr.getResults().get(i);
                if (caseExpr.getOperand() != null) {
                    if (numericEquals(operand, evaluate(cond, row))) {
                        return evaluate(result, row);
                    }
                } else if (toBool(evaluate(cond, row))) {
                    return evaluate(result, row);
                }
            }
            return caseExpr.getElseResult() != null ? evaluate(caseExpr.getElseResult(), row) : null;
        }

        if (expr instanceof WindowFuncExpr) {
            throw new SqlException("窗口函数 " + ((WindowFuncExpr) expr).getName()
                    + " 仅支持在 SELECT 列表中使用（不支持 WHERE/HAVING/GROUP BY）");
        }

        throw new SqlException("不支持的表达式类型: " + expr.getClass().getSimpleName());
    }

    public boolean evaluateBool(Expression expr, Map<String, Object> row) {
        Object result = evaluate(expr, row);
        return toBool(result);
    }

    // ==================== 函数调用 ====================

    private Object evaluateFunction(FunctionCall func, Map<String, Object> row) {
        String funcName = func.getName();
        List<Expression> args = func.getArguments();

        // 组结果行上已物化的聚合值直接取（HAVING/外层包裹表达式场景）
        if (isAggregateCall(func)) {
            String stashKey = getExprOutputName(func);
            if (row.containsKey(stashKey)) {
                return row.get(stashKey);
            }
        }

        switch (funcName) {
            case "COUNT":
                if (args.size() == 1 && args.get(0) instanceof ColumnRef
                        && "*".equals(((ColumnRef) args.get(0)).getColumn())) {
                    return 1L;
                }
                Object countVal = evaluate(args.get(0), row);
                return countVal != null ? 1L : 0L;
            case "SUM":
            case "AVG":
            case "GROUP_CONCAT":
                return evaluate(args.get(0), row);
            case "MAX":
            case "MIN":
                return evaluate(args.get(0), row);
        }

        SqlFunctionDef def = SqlFunctionRegistry.get().find(funcName);
        if (def != null) {
            Object[] resolvedArgs = new Object[args.size()];
            for (int i = 0; i < args.size(); i++) {
                resolvedArgs[i] = evaluate(args.get(i), row);
            }
            return def.exec(row, resolvedArgs);
        }

        throw new SqlException("未知的函数: " + funcName
                + "（内置函数已随注册表自动注册，请检查拼写；自定义函数需先 SqlFunctionRegistry.get().register(...)）");
    }

    // ==================== 工具方法 ====================

    private Object doArithmetic(Object a, String op, Object b) {
        if (a == null || b == null) return null;
        double av = toDouble(a), bv = toDouble(b);
        switch (op) {
            case "+": return av + bv;
            case "-": return av - bv;
            case "*": return av * bv;
            case "/": return bv == 0 ? null : av / bv;
            case "%": return bv == 0 ? null : av % bv;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private int compareValues(Object a, Object b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;
        if (a instanceof Comparable && b instanceof Comparable) {
            if (a.getClass().isAssignableFrom(b.getClass()) || b.getClass().isAssignableFrom(a.getClass())) {
                return ((Comparable<Object>) a).compareTo(b);
            }
        }
        if (a instanceof Number && b instanceof Number) {
            return Double.compare(((Number) a).doubleValue(), ((Number) b).doubleValue());
        }
        return a.toString().compareTo(b.toString());
    }

    private boolean toBool(Object val) {
        if (val == null) return false;
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof Number) return ((Number) val).doubleValue() != 0;
        return Boolean.parseBoolean(val.toString());
    }

    private double toDouble(Object val) {
        if (val instanceof Number) return ((Number) val).doubleValue();
        return Double.parseDouble(val.toString());
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val instanceof BigDecimal) return (BigDecimal) val;
        if (val instanceof Number) return BigDecimal.valueOf(((Number) val).doubleValue());
        return new BigDecimal(val.toString());
    }

    /**
     * 与 z-util-expr-obj Aggregates#isIntegral 同一口径：
     * 整型包装类恒真；BigDecimal 看 scale<=0；浮点看是否整值。
     */
    private boolean isIntegralValue(Object v) {
        if (v instanceof Integer || v instanceof Long || v instanceof Short || v instanceof Byte) {
            return true;
        }
        if (v instanceof BigDecimal) {
            return ((BigDecimal) v).scale() <= 0;
        }
        if (v instanceof Number) {
            double d = ((Number) v).doubleValue();
            return d == Math.floor(d) && !Double.isInfinite(d);
        }
        return false;
    }

    private boolean matchLike(String text, String pattern) {
        String regex = "^" + pattern.replace(".", "\\.").replace("%", ".*").replace("_", ".") + "$";
        return text.matches(regex);
    }

    private boolean numericEquals(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        if (a instanceof Number && b instanceof Number) {
            return Double.compare(((Number) a).doubleValue(), ((Number) b).doubleValue()) == 0;
        }
        return Objects.equals(a, b);
    }

    private Object castValue(Object val, String targetType) {
        if (val == null) return null;
        String type = targetType.toUpperCase().trim();
        switch (type) {
            case "INTEGER": case "INT":
                if (val instanceof Number) return ((Number) val).intValue();
                return Integer.parseInt(val.toString());
            case "LONG": case "BIGINT":
                if (val instanceof Number) return ((Number) val).longValue();
                return Long.parseLong(val.toString());
            case "DOUBLE":
                if (val instanceof Number) return ((Number) val).doubleValue();
                return Double.parseDouble(val.toString());
            case "FLOAT":
                if (val instanceof Number) return ((Number) val).floatValue();
                return Float.parseFloat(val.toString());
            case "STRING": case "VARCHAR": case "TEXT":
                return val.toString();
            case "BOOLEAN": case "BOOL":
                return toBool(val);
            case "DECIMAL": case "NUMERIC":
                return toBigDecimal(val);
            default:
                return val;
        }
    }

    private String getExprOutputName(Expression expr) {
        if (expr instanceof AliasedExpr) {
            return ((AliasedExpr) expr).getAlias();
        }
        if (expr instanceof ColumnRef) {
            return ((ColumnRef) expr).getColumn();
        }
        if (expr instanceof FunctionCall) {
            FunctionCall fc = (FunctionCall) expr;
            StringBuilder sb = new StringBuilder(fc.getName()).append("(");
            for (int i = 0; i < fc.getArguments().size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(getExprOutputName(fc.getArguments().get(i)));
            }
            sb.append(")");
            return sb.toString();
        }
        return expr.toString();
    }

    private String rowtoString(Map<String, Object> row) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("\0");
        }
        return sb.toString();
    }

    // ==================== Schema 推断 ====================

    String explainName(Expression item) {
        if (item instanceof AliasedExpr) {
            return ((AliasedExpr) item).getAlias();
        }
        if (item instanceof ColumnRef) {
            return ((ColumnRef) item).getColumn();
        }
        if (item instanceof FunctionCall) {
            return getExprOutputName(item);
        }
        return item.toString();
    }

    String explainType(Expression item, VirtualTable main) {
        return inferType(item, main);
    }

    @SuppressWarnings("unchecked")
    private String inferType(Expression expr, VirtualTable main) {
        if (expr == null) {
            return "UNKNOWN";
        }
        if (expr instanceof AliasedExpr) {
            return inferType(((AliasedExpr) expr).getExpression(), main);
        }
        if (expr instanceof Literal) {
            Object v = ((Literal) expr).getValue();
            if (v == null) {
                return "NULL";
            }
            if (v instanceof Integer) return "INTEGER";
            if (v instanceof Long) return "LONG";
            if (v instanceof Number) return "DOUBLE";
            if (v instanceof Boolean) return "BOOLEAN";
            if (v instanceof java.time.LocalDate) return "DATE";
            if (v instanceof java.time.LocalDateTime) return "DATETIME";
            return "STRING";
        }
        if (expr instanceof ColumnRef) {
            ColumnRef col = (ColumnRef) expr;
            if ("*".equals(col.getColumn())) {
                return "ROW";
            }
            // 没有列元数据时从首行样本里拿：「dry-run 不取数」是底线，但列名已知时
            // 直接用样本碰一下类是便宜且 100% 准的；纯内存引擎下没有这一笔就 UNKNOWN。
            if (main != null) {
                Map<String, Object> sample = main.getRow(0);
                if (sample != null) {
                    Object v = VirtualTable.getCellValue(sample, col.getColumn());
                    if (v == null) {
                        return "UNKNOWN";
                    }
                    if (v instanceof Integer) return "INTEGER";
                    if (v instanceof Long) return "LONG";
                    if (v instanceof Number) return "DOUBLE";
                    if (v instanceof Boolean) return "BOOLEAN";
                    if (v instanceof java.time.LocalDate) return "DATE";
                    if (v instanceof java.time.LocalDateTime) return "DATETIME";
                    return "STRING";
                }
            }
            return "UNKNOWN";
        }
        if (expr instanceof CastExpr) {
            String t = ((CastExpr) expr).getTargetType().toUpperCase();
            switch (t) {
                case "INTEGER": case "INT": return "INTEGER";
                case "BIGINT": case "LONG": return "LONG";
                case "DOUBLE": case "FLOAT": case "REAL": return "DOUBLE";
                case "DECIMAL": case "NUMERIC": return "DECIMAL";
                case "BOOLEAN": case "BOOL": return "BOOLEAN";
                case "DATE": return "DATE";
                case "DATETIME": case "TIMESTAMP": return "DATETIME";
                case "STRING": case "VARCHAR": case "CHAR": return "STRING";
                default: return t;
            }
        }
        if (expr instanceof FunctionCall) {
            String fname = ((FunctionCall) expr).getName();
            if ("COUNT".equalsIgnoreCase(fname)) return "LONG";
            if ("SUM".equalsIgnoreCase(fname) || "AVG".equalsIgnoreCase(fname)) return "DECIMAL";
            if ("MAX".equalsIgnoreCase(fname) || "MIN".equalsIgnoreCase(fname)) {
                if (!((FunctionCall) expr).getArguments().isEmpty()) {
                    String child = inferType(((FunctionCall) expr).getArguments().get(0), main);
                    if (!"UNKNOWN".equals(child)) return child;
                }
                return "UNKNOWN";
            }
            if (((FunctionCall) expr).getArguments().isEmpty()) {
                return "UNKNOWN";
            }
            return inferType(((FunctionCall) expr).getArguments().get(0), main);
        }
        if (expr instanceof BinaryExpr) {
            String op = ((BinaryExpr) expr).getOperator();
            switch (op) {
                case "=": case "==": case "<>": case "!=":
                case "<": case ">": case "<=": case ">=":
                case "AND": case "OR":
                case "LIKE": case "IN": case "BETWEEN":
                    return "BOOLEAN";
                case "+": case "-": case "*": case "/": case "%":
                    return "DOUBLE";
                default:
                    return "UNKNOWN";
            }
        }
        if (expr instanceof IsNullExpr || expr instanceof BetweenExpr || expr instanceof InExpr) {
            return "BOOLEAN";
        }
        if (expr instanceof UnaryExpr) {
            return "DOUBLE";
        }
        return "UNKNOWN";
    }
}
