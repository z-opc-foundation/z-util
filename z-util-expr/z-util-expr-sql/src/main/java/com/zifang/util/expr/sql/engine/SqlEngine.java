package com.zifang.util.expr.sql.engine;

import com.zifang.util.expr.sql.SqlException;
import com.zifang.util.expr.sql.SqlFunctionDef;
import com.zifang.util.expr.sql.SqlFunctionRegistry;
import com.zifang.util.expr.sql.engine.ast.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * 内存 SQL 执行引擎。
 * <p>
 * 执行流程：JOIN → WHERE → GROUP BY → SELECT → HAVING → ORDER BY → DISTINCT → LIMIT/OFFSET
 * <p>
 * 支持索引加速：WHERE 等值条件利用列索引，JOIN ON 等值条件利用哈希连接。
 */
public class SqlEngine {

    private final TableRegistry registry;

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

        // 5. GROUP BY
        boolean hasGroupBy = !stmt.getGroupBy().isEmpty();
        if (hasGroupBy) {
            rows = applyGroupBy(rows, stmt.getGroupBy(), stmt.getSelectItems());
        }

        // 6. HAVING
        if (stmt.getHavingClause() != null) {
            rows = applyWhere(rows, stmt.getHavingClause(), null);
        }

        // 7. SELECT (投影)
        if (hasGroupBy) {
            // GROUP BY 已经计算了 SELECT 列，跳过投影
        } else if (hasAggregateFunction(stmt.getSelectItems())) {
            rows = applyImplicitAggregate(rows, stmt.getSelectItems());
        } else {
            rows = applySelect(rows, stmt.getSelectItems(), stmt.isDistinct());
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
                                                   boolean distinct) {
        if (selectItems.size() == 1 && selectItems.get(0) instanceof ColumnRef
                && "*".equals(((ColumnRef) selectItems.get(0)).getColumn())) {
            return rows;
        }

        List<Map<String, Object>> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
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
                projected.put(outputName, evaluate(expr, row));
            }
            result.add(projected);
        }
        return result;
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

    private boolean hasAggregateFunction(List<Expression> selectItems) {
        for (Expression item : selectItems) {
            Expression expr = item instanceof AliasedExpr ? ((AliasedExpr) item).getExpression() : item;
            if (expr instanceof FunctionCall) {
                String name = ((FunctionCall) expr).getName();
                if ("COUNT".equals(name) || "SUM".equals(name) || "AVG".equals(name)
                        || "MAX".equals(name) || "MIN".equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Map<String, Object>> applyImplicitAggregate(List<Map<String, Object>> rows,
                                                              List<Expression> selectItems) {
        LinkedHashMap<String, Object> resultRow = new LinkedHashMap<>();
        for (Expression item : selectItems) {
            String outputName = getExprOutputName(item);
            Expression expr = item instanceof AliasedExpr ? ((AliasedExpr) item).getExpression() : item;
            if (expr instanceof FunctionCall) {
                resultRow.put(outputName, evaluateAggregateFunction((FunctionCall) expr, rows));
            } else {
                resultRow.put(outputName, evaluate(expr, rows.isEmpty() ? Collections.emptyMap() : rows.get(0)));
            }
        }
        return Collections.singletonList(resultRow);
    }

    // ==================== GROUP BY ====================

    private List<Map<String, Object>> applyGroupBy(List<Map<String, Object>> rows,
                                                    List<Expression> groupByExprs,
                                                    List<Expression> selectItems) {
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

            result.add(resultRow);
        }
        return result;
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

        if (expr instanceof FunctionCall) {
            return evaluateAggregateFunction((FunctionCall) expr, groupRows);
        }
        return evaluate(expr, firstRow);
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
                BigDecimal sum = BigDecimal.ZERO;
                for (Map<String, Object> row : rows) {
                    Object val = evaluate(args.get(0), row);
                    if (val != null) {
                        sum = sum.add(toBigDecimal(val));
                    }
                }
                return sum;
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
                return sum.divide(BigDecimal.valueOf(count), 10, BigDecimal.ROUND_HALF_UP).stripTrailingZeros();
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
            boolean found = false;
            for (Expression v : inExpr.getValues()) {
                Object vVal = evaluate(v, row);
                if (Objects.equals(val, vVal)) {
                    found = true;
                    break;
                }
            }
            return inExpr.isNegated() ? !found : found;
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

        throw new SqlException("未知的函数: " + funcName);
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
}
