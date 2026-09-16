package com.zifang.util.expr.sql;

import com.zifang.util.expr.sql.annotation.SqlFunction;
import com.zifang.util.expr.sql.function.SqlFunctions;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * SQL 函数注册表。
 * 扫描内置函数 + 用户自定义类，统一的函数查找入口。
 * <p>
 * 用法：
 * <pre>
 * SqlFunctionRegistry reg = SqlFunctionRegistry.get();
 * reg.registerBuiltin();                      // 注册内置函数
 * reg.register(MyFunctions.class);            // 注册自定义类
 * </pre>
 */
public final class SqlFunctionRegistry {

    private static final SqlFunctionRegistry INSTANCE = new SqlFunctionRegistry();

    /**
     * 函数名 → 函数定义（不区分大小写）
     */
    private final Map<String, SqlFunctionDef> functions = new HashMap<>();

    private SqlFunctionRegistry() {
    }

    public static SqlFunctionRegistry get() {
        return INSTANCE;
    }

    /**
     * 注册内置函数。
     * 自动扫描 SqlFunctions 类中所有带 @SqlFunction 注解的方法。
     */
    public SqlFunctionRegistry registerBuiltin() {
        register(SqlFunctions.class);
        return this;
    }

    /**
     * 注册一个类中所有带 @SqlFunction 注解的方法。
     * 跳过已存在的同名函数（idempotent）。
     */
    public SqlFunctionRegistry register(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            SqlFunction ann = method.getAnnotation(SqlFunction.class);
            if (ann != null) {
                String name = ann.value().toUpperCase();
                if (functions.containsKey(name)) {
                    // Skip duplicate — idempotent
                    continue;
                }
                // 必须是 static 方法
                if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                    throw new SqlException("SqlFunction method must be static: " + method);
                }
                functions.put(name, new SqlFunctionDef(name, method));
            }
        }
        return this;
    }

    /**
     * 注册用户自定义函数（通过 SqlUdf 接口实现）。
     *
     * @param name 函数名
     * @param udf  函数实现
     */
    public SqlFunctionRegistry register(String name, SqlUdf udf) {
        functions.put(name.toUpperCase(), new SqlUdfDef(name, udf));
        return this;
    }

    /**
     * 查找函数。
     *
     * @return null if not found
     */
    public SqlFunctionDef find(String name) {
        return functions.get(name.toUpperCase());
    }

    public boolean contains(String name) {
        return functions.containsKey(name.toUpperCase());
    }

    public Collection<SqlFunctionDef> all() {
        return functions.values();
    }

    /**
     * 返回所有已注册函数名
     */
    public Set<String> names() {
        return functions.keySet();
    }

    /**
     * 清空所有函数（测试用）
     */
    public void clear() {
        functions.clear();
    }

    // -------------------------------------------------------------------------
    // 内置函数封装（用于 register(name, udf) 场景）
    // -------------------------------------------------------------------------

    /**
     * 用户自定义 UDF 接口。
     * 用于 register(name, udf) 方式注册。
     */
    public interface SqlUdf {
        Object exec(Map<String, Object> row, Object... args);
    }

    /**
     * 包装 SqlUdf 为 SqlFunctionDef
     */
    private static class SqlUdfDef extends SqlFunctionDef {
        private final SqlUdf udf;

        SqlUdfDef(String name, SqlUdf udf) {
            super(name, null);
            this.udf = udf;
        }

        @Override
        public Object exec(Map<String, Object> row, Object... args) {
            return udf.exec(row, args);
        }
    }
}