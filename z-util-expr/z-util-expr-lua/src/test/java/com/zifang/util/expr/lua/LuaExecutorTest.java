package com.zifang.util.expr.lua;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * LuaExecutor 测试类。
 *
 * @author zifang
 */
public class LuaExecutorTest {

    @Test
    public void testArithmetic() {
        LuaExecutor executor = new LuaExecutor();

        // 加法
        Object result = executor.eval("return 1 + 2");
        assertEquals(3.0, result);

        // 减法
        result = executor.eval("return 10 - 3");
        assertEquals(7.0, result);

        // 乘法
        result = executor.eval("return 4 * 5");
        assertEquals(20.0, result);

        // 除法
        result = executor.eval("return 20 / 4");
        assertEquals(5.0, result);

        // 浮点除法
        result = executor.eval("return 7 / 2");
        assertEquals(3.5, result);

        // 取模
        result = executor.eval("return 17 % 5");
        assertEquals(2.0, result);

        // 幂运算
        result = executor.eval("return 2 ^ 3");
        assertEquals(8.0, result);

        // 复杂表达式
        result = executor.eval("return (10 + 5) * 2 - 8 / 4");
        assertEquals(28.0, result);
    }

    @Test
    public void testVariables() {
        LuaExecutor executor = new LuaExecutor();

        // 变量赋值和访问
        Object result = executor.eval("a = 10; return a");
        assertEquals(10.0, result);

        // 多个变量
        result = executor.eval("x, y = 5, 8; return x + y");
        assertEquals(13.0, result);

        // 字符串变量
        result = executor.eval("name = 'hello'; return name");
        assertEquals("hello", result);

        // 绑定变量并使用
        executor.bind("globalVar", 100);
        result = executor.eval("return globalVar");
        assertEquals(100.0, result);

        // 绑定字符串变量
        executor.bind("greeting", "world");
        result = executor.eval("return greeting");
        assertEquals("world", result);

        // 绑定布尔值
        executor.bind("flag", true);
        result = executor.eval("return flag");
        assertEquals(true, result);

        // 获取已绑定的变量
        executor.eval("myVar = 42");
        Object varValue = executor.get("myVar");
        assertEquals(42.0, varValue);
    }

    @Test
    public void testFunctions() {
        LuaExecutor executor = new LuaExecutor();

        // 定义并调用函数
        executor.eval("function add(a, b) return a + b end");
        Object result = executor.call("add", 3, 5);
        assertEquals(8.0, result);

        // 调用带浮点参数的函数
        result = executor.call("add", 2.5, 3.5);
        assertEquals(6.0, result);

        // 定义并调用返回字符串的函数
        executor.eval("function greet(name) return 'Hello, ' .. name end");
        result = executor.call("greet", "Lua");
        assertEquals("Hello, Lua", result);

        // 调用多返回值函数（只取第一个）
        executor.eval("function multi() return 1, 2, 3 end");
        result = executor.call("multi");
        assertEquals(1.0, result);

        // 嵌套函数调用
        executor.eval("function square(x) return x * x end");
        executor.eval("function double(x) return x + x end");
        result = executor.eval("return double(square(3))");
        assertEquals(18.0, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testTable() {
        LuaExecutor executor = new LuaExecutor();

        // 创建和访问表
        Object result = executor.eval("t = {a = 1, b = 2}; return t.a");
        assertEquals(1.0, result);

        // 表的数组部分
        result = executor.eval("arr = {10, 20, 30}; return arr[1]");
        assertEquals(10.0, result);

        result = executor.eval("arr = {10, 20, 30}; return arr[2]");
        assertEquals(20.0, result);

        // 表作为返回值（转换为 Map）
        result = executor.eval("return {x = 5, y = 10}");
        assertTrue(result instanceof Map);
        Map<String, Object> map = (Map<String, Object>) result;
        assertEquals(5.0, map.get("x"));
        assertEquals(10.0, map.get("y"));

        // 嵌套表
        result = executor.eval("nested = {inner = {value = 100}}; return nested.inner.value");
        assertEquals(100.0, result);

        // 表的长度
        result = executor.eval("t = {1, 2, 3, 4, 5}; return #t");
        assertEquals(5.0, result);

        // 表遍历
        executor.bind("sum", 0);
        executor.eval("for i = 1, 5 do sum = sum + i end");
        result = executor.get("sum");
        assertEquals(15.0, result);

        // 绑定 Map 到 Lua 表
        Map<String, Object> javaMap = new HashMap<>();
        javaMap.put("name", "test");
        javaMap.put("count", 42);
        executor.bind("javamap", javaMap);
        result = executor.eval("return javamap.name");
        assertEquals("test", result);

        result = executor.eval("return javamap.count");
        assertEquals(42.0, result);
    }

    @Test
    public void testExceptions() {
        LuaExecutor executor = new LuaExecutor();

        // 语法错误
        try {
            executor.eval("function bad() return }");
            fail("Expected LuaException");
        } catch (LuaException e) {
            assertTrue(e.getMessage().contains("Lua script execution failed"));
        }

        // 调用不存在的函数
        try {
            executor.call("nonExistentFunction", 1, 2);
            fail("Expected LuaException");
        } catch (LuaException e) {
            assertTrue(e.getMessage().contains("Function not found"));
        }

        // 运行时错误
        try {
            executor.eval("local t = nil; return t.somefield");
            fail("Expected LuaException");
        } catch (LuaException e) {
            assertTrue(e.getMessage().contains("Lua script execution failed"));
        }

        // 算术错误（除零）
        try {
            executor.eval("return 1 / 0");
            // Lua 不会抛出除零错误，而是返回 inf
            Object result = executor.eval("return 1 / 0");
            assertTrue(Double.isInfinite((Double) result));
        } catch (LuaException e) {
            // 有些 Lua 实现会抛出错误
            assertTrue(e.getMessage().contains("Lua script execution failed"));
        }

        // 无效的参数类型 - Lua 抛出错误而不是自动转换
        try {
            executor.eval("function expectNumber(n) return n + 1 end");
            executor.call("expectNumber", "not a number");
            fail("Expected LuaException");
        } catch (LuaException e) {
            assertTrue(e.getMessage().contains("attempt to perform arithmetic"));
        }
    }
}
