package com.zifang.util.db.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 编译产物：带 {@code ?} 占位的 SQL 文本与顺序绑定的参数。
 * <p>
 * 参数一律走 PreparedStatement 绑定，不存在拼进 SQL 的字符串值。
 *
 * @author zifang
 */
public final class SqlSpec {

    private final String sql;

    private final List<Object> params;

    public SqlSpec(String sql, List<Object> params) {
        this.sql = sql;
        this.params = params == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(params));
    }

    public static SqlSpec of(String sql, Object... values) {
        List<Object> list = new ArrayList<>();
        Collections.addAll(list, values);
        return new SqlSpec(sql, list);
    }

    public String sql() {
        return sql;
    }

    public List<Object> params() {
        return params;
    }

    public int paramCount() {
        return params.size();
    }

    @Override
    public String toString() {
        return "SqlSpec{sql=" + sql + ", params=" + params + "}";
    }
}
