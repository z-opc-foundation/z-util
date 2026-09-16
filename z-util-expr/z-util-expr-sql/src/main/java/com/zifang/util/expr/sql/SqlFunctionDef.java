package com.zifang.util.expr.sql;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * SQL 函数定义（元数据）。
 */
public class SqlFunctionDef {
    private final String name;
    private final Method method;
    private final boolean isVarargs;

    public SqlFunctionDef(String name, Method method) {
        this.name = name != null ? name.toUpperCase() : null;
        this.method = method;
        this.isVarargs = (method != null && method.getParameterCount() > 0
                && method.getParameterTypes()[method.getParameterCount() - 1].isArray());
    }

    public String getName() {
        return name;
    }

    public Method getMethod() {
        return method;
    }

    public Object exec(java.util.Map<String, Object> row, Object... args) {
        try {
            if (isVarargs) {
                int paramCount = method.getParameterCount();
                int fixedCount = paramCount - 2;
                if (paramCount == 2) {
                    return method.invoke(null, row, args);
                }
                Object[] invokeArgs = new Object[paramCount];
                invokeArgs[0] = row;
                for (int i = 0; i < fixedCount; i++) {
                    invokeArgs[i + 1] = i < args.length ? args[i] : null;
                }
                Object[] varargs = new Object[Math.max(0, args.length - fixedCount)];
                for (int i = 0; i < varargs.length; i++) {
                    varargs[i] = args[fixedCount + i];
                }
                invokeArgs[paramCount - 1] = varargs;
                return method.invoke(null, invokeArgs);
            }
            // method is never null here (UDF path goes through SqlUdfDef which overrides exec)
            // 非 varargs 方法：动态计算所需的实参数组大小，向缺失槽填 null 后反射调用
            int paramCount = method.getParameterCount();
            Object[] invokeArgs = new Object[paramCount];
            invokeArgs[0] = row;
            for (int i = 1; i < paramCount; i++) {
                invokeArgs[i] = i - 1 < args.length ? args[i - 1] : null;
            }
            return method.invoke(null, invokeArgs);
        } catch (Exception e) {
            throw new SqlException("Failed to execute function: " + name, e);
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SqlFunctionDef && name.equals(((SqlFunctionDef) o).name) && method.equals(((SqlFunctionDef) o).method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, method);
    }

    @Override
    public String toString() {
        return "SqlFunctionDef{name='" + name + "', method=" + method.getName() + "}";
    }
}