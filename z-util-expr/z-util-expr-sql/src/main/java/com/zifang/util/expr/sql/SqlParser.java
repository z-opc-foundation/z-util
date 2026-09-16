package com.zifang.util.expr.sql;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL 语句解析器（增强版）
 * <p>
 * 支持：SELECT/INSERT/UPDATE/DELETE
 * 支持：列名、表名、WHERE 条件提取
 * 支持：? 和 :name 占位符识别
 * 支持：简单 WHERE 条件（= > < >= <= LIKE AND OR IN BETWEEN IS NULL）
 * 支持：DISTINCT, ORDER BY, LIMIT/OFFSET, JOIN, GROUP BY, AS别名
 * 支持：子查询，注释，多值插入
 */
public class SqlParser {

    // 占位符模式
    private static final Pattern QUESTION_MARK_PATTERN = Pattern.compile("\\?");
    private static final Pattern NAMED_PLACEHOLDER_PATTERN = Pattern.compile(":(\\w+)");

    // 注释模式
    private static final Pattern SINGLE_LINE_COMMENT_PATTERN = Pattern.compile("--.*$", Pattern.MULTILINE);
    private static final Pattern MULTI_LINE_COMMENT_PATTERN = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);

    /**
     * 解析 SQL 语句
     *
     * @param sql SQL 语句
     * @return SqlStatement 解析结果
     */
    public SqlStatement parse(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new SqlException("SQL 语句不能为空");
        }

        String trimmedSql = sql.trim();

        // 移除注释
        trimmedSql = removeComments(trimmedSql);

        SqlStatement statement = new SqlStatement();
        statement.setRawSql(sql.trim());

        // 提取占位符
        extractPlaceholders(trimmedSql, statement);

        // 判断 SQL 类型并解析
        if (trimmedSql.toUpperCase().startsWith("SELECT")) {
            parseSelect(trimmedSql, statement);
        } else if (trimmedSql.toUpperCase().startsWith("INSERT")) {
            parseInsert(trimmedSql, statement);
        } else if (trimmedSql.toUpperCase().startsWith("UPDATE")) {
            parseUpdate(trimmedSql, statement);
        } else if (trimmedSql.toUpperCase().startsWith("DELETE")) {
            parseDelete(trimmedSql, statement);
        } else {
            statement.setType(SqlStatement.Type.UNKNOWN);
            throw new SqlException("不支持的 SQL 类型: " + trimmedSql);
        }

        return statement;
    }

    /**
     * 移除 SQL 注释
     */
    private String removeComments(String sql) {
        sql = SINGLE_LINE_COMMENT_PATTERN.matcher(sql).replaceAll("");
        sql = MULTI_LINE_COMMENT_PATTERN.matcher(sql).replaceAll("");
        return sql;
    }

    /**
     * 解析 SELECT 语句
     */
    private void parseSelect(String sql, SqlStatement statement) {
        statement.setType(SqlStatement.Type.SELECT);

        // 检查 DISTINCT
        if (Pattern.compile("\\bDISTINCT\\b", Pattern.CASE_INSENSITIVE).matcher(sql).find()) {
            statement.setDistinct(true);
        }

        // 提取 ORDER BY 子句
        Pattern orderByPattern = Pattern.compile("\\bORDER\\s+BY\\s+(.+?)(?:\\s+LIMIT|\\s+OFFSET|\\s*$)", Pattern.CASE_INSENSITIVE);
        Matcher orderByMatcher = orderByPattern.matcher(sql);
        if (orderByMatcher.find()) {
            parseOrderBy(orderByMatcher.group(1).trim(), statement);
        }

        // 提取 LIMIT/OFFSET
        Pattern limitPattern = Pattern.compile("\\bLIMIT\\s+(\\d+)(?:\\s+OFFSET\\s+(\\d+))?", Pattern.CASE_INSENSITIVE);
        Matcher limitMatcher = limitPattern.matcher(sql);
        if (limitMatcher.find()) {
            statement.setLimit(Integer.parseInt(limitMatcher.group(1)));
            if (limitMatcher.group(2) != null) {
                statement.setOffset(Integer.parseInt(limitMatcher.group(2)));
            }
        }

        // 提取 OFFSET (standalone)
        if (statement.getOffset() == null) {
            Pattern offsetPattern = Pattern.compile("\\bOFFSET\\s+(\\d+)\\b", Pattern.CASE_INSENSITIVE);
            Matcher offsetMatcher = offsetPattern.matcher(sql);
            if (offsetMatcher.find()) {
                statement.setOffset(Integer.parseInt(offsetMatcher.group(1)));
            }
        }

        // 提取 GROUP BY 子句
        Pattern groupByPattern = Pattern.compile("\\bGROUP\\s+BY\\s+(.+?)(?:\\s+HAVING|\\s+ORDER|\\s+LIMIT|\\s*$)", Pattern.CASE_INSENSITIVE);
        Matcher groupByMatcher = groupByPattern.matcher(sql);
        if (groupByMatcher.find()) {
            parseGroupBy(groupByMatcher.group(1).trim(), statement);
        }

        // 提取 JOIN 子句
        parseJoins(sql, statement);

        // 解析列名和表名（需要先移除子句）
        String sqlWithoutClauses = sql;
        // 移除 ORDER BY
        sqlWithoutClauses = sqlWithoutClauses.replaceAll("(?i)\\bORDER\\s+BY\\s+.+?(?=LIMIT|OFFSET|\\s+GROUP|\\s+WHERE|\\s*$)", "");
        // 移除 LIMIT/OFFSET
        sqlWithoutClauses = sqlWithoutClauses.replaceAll("(?i)\\bLIMIT\\s+\\d+(?:\\s+OFFSET\\s+\\d+)?", "");
        sqlWithoutClauses = sqlWithoutClauses.replaceAll("(?i)\\bOFFSET\\s+\\d+", "");
        // 移除 GROUP BY
        sqlWithoutClauses = sqlWithoutClauses.replaceAll("(?i)\\bGROUP\\s+BY\\s+.+?(?=HAVING|ORDER|LIMIT|\\s+WHERE|\\s*$)", "");

        // 解析 SELECT 子句
        Pattern selectPattern = Pattern.compile("^\\s*SELECT\\s+(.+?)\\s+FROM\\s+(\\w+)(?:\\s+(?:AS\\s+)?(\\w+))?", Pattern.CASE_INSENSITIVE);
        Matcher selectMatcher = selectPattern.matcher(sqlWithoutClauses);
        if (!selectMatcher.find()) {
            throw new SqlException("无效的 SELECT 语句: " + sql);
        }

        // 解析列名
        String columnsStr = selectMatcher.group(1).trim();
        if (!"*".equals(columnsStr)) {
            parseColumnList(columnsStr, statement);
            // 解析列别名
            parseColumnAliases(columnsStr, statement);
        } else {
            statement.addColumn("*");
        }

        // 解析表名和别名
        statement.setTableName(selectMatcher.group(2).trim());
        if (selectMatcher.group(3) != null) {
            statement.setTableAlias(selectMatcher.group(3).trim());
        }

        // 解析 WHERE 条件
        Pattern wherePattern = Pattern.compile("\\bWHERE\\s+(.+?)(?:\\s+ORDER|\\s+GROUP|\\s+LIMIT|\\s*$)", Pattern.CASE_INSENSITIVE);
        Matcher whereMatcher = wherePattern.matcher(sql);
        if (whereMatcher.find()) {
            parseWhereConditions(whereMatcher.group(1).trim(), statement);
        }
    }

    /**
     * 解析 ORDER BY 子句
     */
    private void parseOrderBy(String orderByStr, SqlStatement statement) {
        String[] parts = orderByStr.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            boolean descending = trimmed.toUpperCase().endsWith(" DESC");
            if (descending) {
                trimmed = trimmed.substring(0, trimmed.length() - 5).trim();
            }
            boolean ascending = trimmed.toUpperCase().endsWith(" ASC");
            if (ascending) {
                trimmed = trimmed.substring(0, trimmed.length() - 4).trim();
            }
            // 移除表别名前缀
            if (trimmed.contains(".")) {
                trimmed = trimmed.substring(trimmed.lastIndexOf(".") + 1);
            }
            statement.addOrderBy(new SqlStatement.OrderByClause(trimmed, descending));
        }
    }

    /**
     * 解析 GROUP BY 子句
     */
    private void parseGroupBy(String groupByStr, SqlStatement statement) {
        String[] parts = groupByStr.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            // 移除表别名前缀
            if (trimmed.contains(".")) {
                trimmed = trimmed.substring(trimmed.lastIndexOf(".") + 1);
            }
            statement.addGroupBy(trimmed);
        }
    }

    /**
     * 解析 JOIN 子句
     */
    private void parseJoins(String sql, SqlStatement statement) {
        // 匹配各种 JOIN 类型
        Pattern joinPattern = Pattern.compile(
                "(?i)(INNER|LEFT|RIGHT|FULL|CROSS)?\\s*JOIN\\s+(\\w+)(?:\\s+(?:AS\\s+)?(\\w+))?\\s+ON\\s+(.+?)(?=\\s+(?:INNER|LEFT|RIGHT|FULL|CROSS|JOIN)|\\s+(?:WHERE|ORDER|GROUP|LIMIT)|\\s*$)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher joinMatcher = joinPattern.matcher(sql);
        while (joinMatcher.find()) {
            String joinTypeStr = joinMatcher.group(1);
            String tableName = joinMatcher.group(2);
            String alias = joinMatcher.group(3);
            String onCondition = joinMatcher.group(4);

            SqlStatement.JoinType joinType = SqlStatement.JoinType.INNER;
            if (joinTypeStr != null) {
                if (joinTypeStr.equalsIgnoreCase("LEFT")) {
                    joinType = SqlStatement.JoinType.LEFT;
                } else if (joinTypeStr.equalsIgnoreCase("RIGHT")) {
                    joinType = SqlStatement.JoinType.RIGHT;
                } else if (joinTypeStr.equalsIgnoreCase("FULL")) {
                    joinType = SqlStatement.JoinType.FULL;
                } else if (joinTypeStr.equalsIgnoreCase("CROSS")) {
                    joinType = SqlStatement.JoinType.CROSS;
                }
            }

            SqlStatement.JoinClause join = new SqlStatement.JoinClause(joinType, tableName);
            if (alias != null) {
                join.setAlias(alias);
            }
            join.setOnCondition(onCondition.trim());
            statement.addJoin(join);
        }
    }

    /**
     * 解析列别名
     */
    private void parseColumnAliases(String columnsStr, SqlStatement statement) {
        // 匹配 AS 别名模式
        Pattern aliasPattern = Pattern.compile("(?i)\\s+AS\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = aliasPattern.matcher(columnsStr);
        while (matcher.find()) {
            statement.addAlias(matcher.group(1));
        }
    }

    /**
     * 解析 INSERT 语句
     */
    private void parseInsert(String sql, SqlStatement statement) {
        statement.setType(SqlStatement.Type.INSERT);

        // 处理 INSERT SELECT
        if (Pattern.compile("(?i)INSERT\\s+INTO\\s+\\w+\\s+SELECT", Pattern.CASE_INSENSITIVE).matcher(sql).find()) {
            Pattern insertSelectPattern = Pattern.compile("(?i)^\\s*INSERT\\s+INTO\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = insertSelectPattern.matcher(sql);
            if (matcher.find()) {
                statement.setTableName(matcher.group(1).trim());
            }
            return;
        }

        // 解析多值 INSERT
        Pattern insertPattern = Pattern.compile(
                "(?i)^\\s*INSERT\\s+INTO\\s+(\\w+)(?:\\s*\\((.+?)\\))?\\s*VALUES\\s*(.+)$",
                Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = insertPattern.matcher(sql);
        if (!matcher.find()) {
            throw new SqlException("无效的 INSERT 语句: " + sql);
        }

        // 解析表名
        statement.setTableName(matcher.group(1).trim());

        // 解析列名
        String columnsStr = matcher.group(2);
        if (columnsStr != null && !columnsStr.trim().isEmpty()) {
            parseColumnList(columnsStr.trim(), statement);
        }

        // 解析多值
        String valuesStr = matcher.group(3);
        parseMultiValues(valuesStr, statement);
    }

    /**
     * 解析多值插入
     */
    private void parseMultiValues(String valuesStr, SqlStatement statement) {
        // 按 ), 分割不同的值组
        String[] valueGroups = valuesStr.split("\\)\\s*,\\s*\\(");
        for (String group : valueGroups) {
            group = group.trim();
            // 移除首尾括号
            if (group.startsWith("(")) {
                group = group.substring(1);
            }
            if (group.endsWith(")")) {
                group = group.substring(0, group.length() - 1);
            }

            java.util.List<String> values = new java.util.ArrayList<>();
            // 按逗号分割，但要注意引号内的逗号
            String[] parts = group.split(",");
            for (String part : parts) {
                values.add(part.trim());
            }
            statement.addMultiValue(values);
        }
    }

    /**
     * 解析 UPDATE 语句
     */
    private void parseUpdate(String sql, SqlStatement statement) {
        statement.setType(SqlStatement.Type.UPDATE);

        // 解析表名
        Pattern updatePattern = Pattern.compile("(?i)^\\s*UPDATE\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher updateMatcher = updatePattern.matcher(sql);
        if (!updateMatcher.find()) {
            throw new SqlException("无效的 UPDATE 语句: " + sql);
        }
        statement.setTableName(updateMatcher.group(1).trim());

        // 解析 SET 子句中的列名
        Pattern setPattern = Pattern.compile("(?i)SET\\s+(.+?)(?:\\s+WHERE|$)", Pattern.CASE_INSENSITIVE);
        Matcher setMatcher = setPattern.matcher(sql);
        if (setMatcher.find()) {
            String setClause = setMatcher.group(1).trim();
            parseSetClause(setClause, statement);
        }

        // 解析 WHERE 条件
        Pattern wherePattern = Pattern.compile("(?i)\\bWHERE\\s+(.+)$", Pattern.CASE_INSENSITIVE);
        Matcher whereMatcher = wherePattern.matcher(sql);
        if (whereMatcher.find()) {
            parseWhereConditions(whereMatcher.group(1).trim(), statement);
        }
    }

    /**
     * 解析 DELETE 语句
     */
    private void parseDelete(String sql, SqlStatement statement) {
        statement.setType(SqlStatement.Type.DELETE);

        // 解析表名
        Pattern deletePattern = Pattern.compile("(?i)^\\s*DELETE\\s+FROM\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = deletePattern.matcher(sql);
        if (!matcher.find()) {
            throw new SqlException("无效的 DELETE 语句: " + sql);
        }

        statement.setTableName(matcher.group(1).trim());

        // 解析 WHERE 条件
        Pattern wherePattern = Pattern.compile("(?i)\\bWHERE\\s+(.+)$", Pattern.CASE_INSENSITIVE);
        Matcher whereMatcher = wherePattern.matcher(sql);
        if (whereMatcher.find()) {
            parseWhereConditions(whereMatcher.group(1).trim(), statement);
        }
    }

    /**
     * 解析列名列表
     */
    private void parseColumnList(String columnsStr, SqlStatement statement) {
        // 先按逗号分割
        String[] columns = columnsStr.split(",");
        for (String column : columns) {
            String trimmed = column.trim();
            if (!trimmed.isEmpty()) {
                // 移除可能的表别名前缀和别名 (e.g., "t.name AS alias" -> "name")
                trimmed = trimmed.replaceAll("(?i)\\s+AS\\s+\\w+$", "");
                if (trimmed.contains(".")) {
                    trimmed = trimmed.substring(trimmed.lastIndexOf(".") + 1);
                }
                if (!trimmed.equals("*")) {
                    statement.addColumn(trimmed);
                }
            }
        }
    }

    /**
     * 解析 SET 子句
     */
    private void parseSetClause(String setClause, SqlStatement statement) {
        String[] assignments = setClause.split(",");
        for (String assignment : assignments) {
            String trimmed = assignment.trim();
            if (!trimmed.isEmpty()) {
                // 提取列名 (格式: column = value)
                int eqIndex = trimmed.indexOf('=');
                if (eqIndex > 0) {
                    String column = trimmed.substring(0, eqIndex).trim();
                    // 移除可能的表别名前缀
                    if (column.contains(".")) {
                        column = column.substring(column.lastIndexOf(".") + 1);
                    }
                    statement.addColumn(column);
                }
            }
        }
    }

    /**
     * 解析 WHERE 条件
     */
    private void parseWhereConditions(String whereClause, SqlStatement statement) {
        // 按 AND/OR 分割，AND 优先级高于 OR
        SqlStatement.WhereCondition.LogicalOperator pendingOp = SqlStatement.WhereCondition.LogicalOperator.NONE;

        java.util.List<String> segments = splitByLogicalOperators(whereClause);

        for (int i = 0; i < segments.size(); i++) {
            String segment = segments.get(i).trim();
            if (segment.isEmpty()) {
                continue;
            }

            // 检查当前 segment 是否以 AND/OR 开头（这表示上一个逻辑操作符）
            String upper = segment.toUpperCase();
            if (upper.startsWith("AND")) {
                pendingOp = SqlStatement.WhereCondition.LogicalOperator.AND;
                segment = segment.substring(3).trim();
            } else if (upper.startsWith("OR")) {
                pendingOp = SqlStatement.WhereCondition.LogicalOperator.OR;
                segment = segment.substring(2).trim();
            } else {
                pendingOp = SqlStatement.WhereCondition.LogicalOperator.NONE;
            }

            // 解析单个条件
            SqlStatement.WhereCondition condition = parseSingleCondition(segment);
            if (condition != null) {
                condition.setLogicalOperator(pendingOp);
                statement.addWhereCondition(condition);
            }
        }
    }

    /**
     * 按逻辑运算符分割 WHERE 子句
     * 每个 segment 可能以 AND/OR 开头
     */
    private java.util.List<String> splitByLogicalOperators(String whereClause) {
        java.util.List<String> result = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        int parenDepth = 0;
        String pendingOp = "";

        for (int i = 0; i < whereClause.length(); i++) {
            char c = whereClause.charAt(i);
            char upper = Character.toUpperCase(c);

            if (c == '(') {
                parenDepth++;
                current.append(c);
            } else if (c == ')') {
                parenDepth--;
                current.append(c);
            } else if (parenDepth == 0) {
                // 检查 AND（3字符）
                if (upper == 'A' && i + 2 < whereClause.length()
                        && Character.toUpperCase(whereClause.charAt(i + 1)) == 'N'
                        && Character.toUpperCase(whereClause.charAt(i + 2)) == 'D'
                        && (i + 3 == whereClause.length() || !Character.isLetterOrDigit(whereClause.charAt(i + 3)))) {
                    result.add(current.toString());
                    current = new StringBuilder();
                    pendingOp = "AND";
                    i += 2; // 跳到 AND 后的字符，循环末尾会再+1
                    continue;
                }
                // 检查 OR（2字符）
                if (upper == 'O' && i + 1 < whereClause.length()
                        && Character.toUpperCase(whereClause.charAt(i + 1)) == 'R'
                        && (i + 2 == whereClause.length() || !Character.isLetterOrDigit(whereClause.charAt(i + 2)))) {
                    result.add(current.toString());
                    current = new StringBuilder();
                    pendingOp = "OR";
                    i += 1; // 跳到 OR 后的字符，循环末尾会再+1
                    continue;
                }
                // 携带前置操作符
                if (current.length() == 0 && !pendingOp.isEmpty()) {
                    current.append(pendingOp).append(" ");
                    pendingOp = "";
                }
                current.append(c);
            } else {
                current.append(c);
            }
        }

        result.add(current.toString());
        return result;
    }

    /**
     * 解析单个 WHERE 条件
     */
    private SqlStatement.WhereCondition parseSingleCondition(String conditionStr) {
        String trimmed = conditionStr.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        // 处理 IS NULL / IS NOT NULL
        Pattern nullPattern = Pattern.compile("(?i)(\\w+(?:\\.\\w+)?)\\s+IS\\s+(NOT\\s+)?NULL", Pattern.CASE_INSENSITIVE);
        Matcher nullMatcher = nullPattern.matcher(trimmed);
        if (nullMatcher.matches()) {
            SqlStatement.WhereCondition condition = new SqlStatement.WhereCondition();
            condition.setColumn(nullMatcher.group(1).trim());
            condition.setOperator(nullMatcher.group(2) != null ? "IS NOT NULL" : "IS NULL");
            condition.setValue("NULL");
            return condition;
        }

        // 处理 BETWEEN
        Pattern betweenPattern = Pattern.compile("(?i)(\\w+(?:\\.\\w+)?)\\s+BETWEEN\\s+(.+?)\\s+AND\\s+(.+)", Pattern.CASE_INSENSITIVE);
        Matcher betweenMatcher = betweenPattern.matcher(trimmed);
        if (betweenMatcher.matches()) {
            SqlStatement.WhereCondition condition = new SqlStatement.WhereCondition();
            condition.setColumn(betweenMatcher.group(1).trim());
            condition.setOperator("BETWEEN");
            String low = betweenMatcher.group(2).trim();
            String high = betweenMatcher.group(3).trim();
            // 清理引号
            low = cleanQuote(low);
            high = cleanQuote(high);
            condition.setValue(low + " AND " + high);
            return condition;
        }

        // 处理 IN 条件
        if (trimmed.toUpperCase().contains(" IN ")) {
            return parseInCondition(trimmed);
        }

        // 处理普通比较条件
        Pattern condPattern = Pattern.compile("(\\w+(?:\\.\\w+)?)\\s*(>=|<=|>|<|=|LIKE)\\s*(.+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = condPattern.matcher(trimmed);
        if (matcher.matches()) {
            SqlStatement.WhereCondition condition = new SqlStatement.WhereCondition();
            condition.setColumn(matcher.group(1).trim());
            condition.setOperator(matcher.group(2).trim().toUpperCase());
            String value = matcher.group(3).trim();
            value = cleanQuote(value);
            condition.setValue(value);
            return condition;
        }

        return null;
    }

    /**
     * 清理字符串值两端的引号
     */
    private String cleanQuote(String value) {
        value = value.trim();
        if ((value.startsWith("'") && value.endsWith("'")) ||
                (value.startsWith("\"") && value.endsWith("\""))) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }

    /**
     * 解析 IN 条件
     */
    private SqlStatement.WhereCondition parseInCondition(String conditionStr) {
        Pattern inPattern = Pattern.compile("(?i)(\\w+(?:\\.\\w+)?)\\s+IN\\s*\\((.+)\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = inPattern.matcher(conditionStr);
        if (matcher.matches()) {
            SqlStatement.WhereCondition condition = new SqlStatement.WhereCondition();
            condition.setColumn(matcher.group(1).trim());
            condition.setOperator("IN");
            condition.setValue(matcher.group(2).trim());
            return condition;
        }
        return null;
    }

    /**
     * 提取占位符
     */
    private void extractPlaceholders(String sql, SqlStatement statement) {
        // 提取 ? 占位符
        Matcher qMatcher = QUESTION_MARK_PATTERN.matcher(sql);
        while (qMatcher.find()) {
            statement.addPlaceholder("?");
        }

        // 提取 :name 占位符
        Matcher nMatcher = NAMED_PLACEHOLDER_PATTERN.matcher(sql);
        while (nMatcher.find()) {
            statement.addNamedPlaceholder(new SqlStatement.NamedPlaceholder(nMatcher.group(1)));
        }
    }
}
