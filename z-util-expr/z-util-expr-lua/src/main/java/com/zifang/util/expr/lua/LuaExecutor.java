package com.zifang.util.expr.lua;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.util.HashMap;
import java.util.Map;

/**
 * Lua 脚本执行器，基于纯 Java Luaj 实现。
 * <p>
 * 支持功能：
 * <ul>
 *     <li>执行 Lua 脚本并返回结果</li>
 *     <li>调用 Lua 函数并传递参数</li>
 *     <li>变量绑定（全局变量）</li>
 * </ul>
 *
 * @author zifang
 */
public class LuaExecutor {

    private final Globals globals;

    /**
     * 创建一个新的 Lua 执行器。
     */
    public LuaExecutor() {
        this.globals = JsePlatform.standardGlobals();
    }

    /**
     * 执行 Lua 脚本。
     *
     * @param luaScript Lua 脚本内容
     * @return 执行结果，如果是 nil 则返回 null
     */
    public Object eval(String luaScript) {
        try {
            LuaValue chunk = globals.load(luaScript);
            LuaValue result = chunk.call();
            return toJavaObject(result);
        } catch (Exception e) {
            throw new LuaException("Lua script execution failed: " + e.getMessage(), e);
        }
    }

    /**
     * 调用指定的 Lua 函数。
     *
     * @param functionName 函数名称
     * @param args         函数参数
     * @return 函数返回值，如果是 nil 则返回 null
     */
    public Object call(String functionName, Object... args) {
        try {
            LuaValue func = globals.get(functionName);
            if (func.isnil()) {
                throw new LuaException("Function not found: " + functionName);
            }
            LuaValue[] luaArgs = new LuaValue[args.length];
            for (int i = 0; i < args.length; i++) {
                luaArgs[i] = toLuaValue(args[i]);
            }
            LuaValue result = func.invoke(luaArgs).arg1();
            return toJavaObject(result);
        } catch (LuaException e) {
            throw e;
        } catch (Exception e) {
            throw new LuaException("Failed to call function " + functionName + ": " + e.getMessage(), e);
        }
    }

    /**
     * 绑定一个全局变量。
     *
     * @param name  变量名
     * @param value 变量值
     */
    public void bind(String name, Object value) {
        globals.set(name, toLuaValue(value));
    }

    /**
     * 获取全局变量的值。
     *
     * @param name 变量名
     * @return 变量值，如果是 nil 则返回 null
     */
    public Object get(String name) {
        LuaValue value = globals.get(name);
        return value.isnil() ? null : toJavaObject(value);
    }

    /**
     * 将 Java 对象转换为 LuaValue。
     */
    private LuaValue toLuaValue(Object obj) {
        if (obj == null) {
            return LuaValue.NIL;
        }
        if (obj instanceof Boolean) {
            return LuaValue.valueOf((Boolean) obj);
        }
        if (obj instanceof Number) {
            return LuaValue.valueOf(((Number) obj).doubleValue());
        }
        if (obj instanceof String) {
            return LuaValue.valueOf((String) obj);
        }
        if (obj instanceof Map) {
            LuaValue table = LuaValue.tableOf();
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) obj;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                table.set(entry.getKey(), toLuaValue(entry.getValue()));
            }
            return table;
        }
        if (obj instanceof Object[]) {
            LuaValue table = LuaValue.tableOf();
            Object[] arr = (Object[]) obj;
            for (int i = 0; i < arr.length; i++) {
                table.set(i + 1, toLuaValue(arr[i]));
            }
            return table;
        }
        return LuaValue.valueOf(obj.toString());
    }

    /**
     * 将 LuaValue 转换为 Java 对象。
     */
    private Object toJavaObject(LuaValue value) {
        if (value.isnil()) {
            return null;
        }
        if (value.isboolean()) {
            return value.toboolean();
        }
        if (value.isnumber()) {
            return value.todouble();
        }
        if (value.isstring()) {
            return value.tojstring();
        }
        if (value.istable()) {
            Map<String, Object> map = new HashMap<>();
            LuaValue key = LuaValue.NIL;
            while (true) {
                Varargs next = value.next(key);
                if ((key = next.arg1()).isnil()) {
                    break;
                }
                LuaValue val = next.arg(2);
                map.put(key.tojstring(), toJavaObject(val));
            }
            return map;
        }
        return value.tojstring();
    }
}
