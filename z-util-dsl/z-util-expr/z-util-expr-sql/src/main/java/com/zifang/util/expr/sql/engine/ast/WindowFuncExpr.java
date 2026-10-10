package com.zifang.util.expr.sql.engine.ast;

import java.util.Collections;
import java.util.List;

/**
 * 窗口函数节点，如 {@code ROW_NUMBER() OVER (PARTITION BY dept ORDER BY amount DESC)}。
 * <p>
 * 目前支持 ROW_NUMBER / RANK / DENSE_RANK；求值在执行引擎里对 WHERE/GROUP 之后
 * 的行集整体预计算（见 SqlEngine#computeWindowValues），不支持在 WHERE/HAVING 中使用。
 */
public class WindowFuncExpr implements Expression {

    private final String name;
    private final List<Expression> arguments;
    private final List<Expression> partitionBy;
    private final List<SortKey> orderBy;

    public WindowFuncExpr(String name, List<Expression> arguments,
                          List<Expression> partitionBy, List<SortKey> orderBy) {
        this.name = name;
        this.arguments = arguments != null ? arguments : Collections.<Expression>emptyList();
        this.partitionBy = partitionBy != null ? partitionBy : Collections.<Expression>emptyList();
        this.orderBy = orderBy != null ? orderBy : Collections.<SortKey>emptyList();
    }

    public String getName() {
        return name;
    }

    public List<Expression> getArguments() {
        return arguments;
    }

    public List<Expression> getPartitionBy() {
        return partitionBy;
    }

    public List<SortKey> getOrderBy() {
        return orderBy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name).append("(");
        for (int i = 0; i < arguments.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(arguments.get(i));
        }
        sb.append(") OVER (");
        boolean first = true;
        if (!partitionBy.isEmpty()) {
            sb.append("PARTITION BY ");
            for (int i = 0; i < partitionBy.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(partitionBy.get(i));
            }
            first = false;
        }
        if (!orderBy.isEmpty()) {
            if (!first) sb.append(' ');
            sb.append("ORDER BY ");
            for (int i = 0; i < orderBy.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(orderBy.get(i));
            }
        }
        return sb.append(")").toString();
    }
}
