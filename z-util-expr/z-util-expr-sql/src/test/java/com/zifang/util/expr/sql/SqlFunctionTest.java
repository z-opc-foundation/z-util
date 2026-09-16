package com.zifang.util.expr.sql;

import com.zifang.util.expr.sql.expression.ExpressionEvaluator;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * SQL 函数 + 表达式求值测试
 */
public class SqlFunctionTest {

    private final ExpressionEvaluator eval = new ExpressionEvaluator().registerBuiltin();

    private Map<String, Object> row(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put(kv[i].toString(), kv[i + 1]);
        }
        return m;
    }

    // ===================== 常量折叠测试 =====================

    @Test
    public void testConstantAbs() {
        Object r = eval.optimize("ABS(-5)");
        assertEquals(5.0, r);
    }

    @Test
    public void testConstantMath() {
        Object r = eval.optimize("ROUND(3.14159, 2)");
        assertEquals(3.14, r);
    }

    @Test
    public void testConstantString() {
        Object r = eval.optimize("UPPER('hello')");
        assertEquals("HELLO", r);
    }

    @Test
    public void testConstantNested() {
        Object r = eval.optimize("SUBSTRING('HelloWorld', 1, 5)");
        assertEquals("Hello", r);
    }

    // ===================== 行级求值测试 =====================

    @Test
    public void testRowAbs() {
        Map<String, Object> r = row("v", -10);
        assertEquals(10.0, eval.evaluate("ABS(v)", r));
    }

    @Test
    public void testRowUpper() {
        Map<String, Object> r = row("name", "alice");
        assertEquals("ALICE", eval.evaluate("UPPER(name)", r));
    }

    @Test
    public void testRowArithmetic() {
        Map<String, Object> r = row("salary", 5000, "bonus", 500);
        Object result = eval.evaluate("salary + bonus * 1.1", r);
        assertTrue(result instanceof Number);
    }

    @Test
    public void testRowConcat() {
        Map<String, Object> r = row("a", "Hello", "b", "World");
        assertEquals("Hello World", eval.evaluate("CONCAT(a, ' ', b)", r));
    }

    @Test
    public void testRowSubstring() {
        Map<String, Object> r = row("s", "HelloWorld");
        assertEquals("World", eval.evaluate("SUBSTRING(s, 6, 5)", r));
    }

    @Test
    public void testRowDateFunctions() {
        Map<String, Object> r = row();
        Object now = eval.evaluate("NOW()", r);
        assertNotNull(now);
        assertTrue(now instanceof LocalDate || now instanceof LocalDateTime);
    }

    @Test
    public void testRowIf() {
        Map<String, Object> r = row("score", 85);
        assertEquals("PASS", eval.evaluate("IF(score >= 60, 'PASS', 'FAIL')", r));
    }

    @Test
    public void testRowIfFalse() {
        Map<String, Object> r = row("score", 45);
        assertEquals("FAIL", eval.evaluate("IF(score >= 60, 'PASS', 'FAIL')", r));
    }

    @Test
    public void testRowCoalesce() {
        Map<String, Object> r = row("a", null, "b", "default");
        assertEquals("default", eval.evaluate("COALESCE(a, b, 'none')", r));
    }

    @Test
    public void testRowNullIf() {
        Map<String, Object> r = row("v", 0);
        assertNull(eval.evaluate("NULLIF(v, 0)", r));
    }

    @Test
    public void testRowDecode() {
        Map<String, Object> r = row("status", 1);
        assertEquals("active", eval.evaluate("DECODE(status, 0, 'inactive', 1, 'active', 'unknown')", r));
    }

    @Test
    public void testRowTrim() {
        Map<String, Object> r = row("s", "  hello  ");
        assertEquals("hello", eval.evaluate("TRIM(s)", r));
    }

    @Test
    public void testRowReplace() {
        Map<String, Object> r = row("s", "hello world");
        assertEquals("hi world", eval.evaluate("REPLACE(s, 'hello', 'hi')", r));
    }

    @Test
    public void testRowLength() {
        Map<String, Object> r = row("s", "hello");
        assertEquals(5, eval.evaluate("LENGTH(s)", r));
    }

    @Test
    public void testRowInstr() {
        Map<String, Object> r = row("s", "hello world");
        assertEquals(4, eval.evaluate("INSTR(s, 'lo')", r));
    }

    @Test
    public void testRowYear() {
        Map<String, Object> r = row();
        Object y = eval.evaluate("YEAR(NOW())", r);
        assertTrue(y instanceof Number);
    }

    @Test
    public void testRowRound() {
        Map<String, Object> r = row("v", 3.14159);
        assertEquals(3.14, eval.evaluate("ROUND(v, 2)", r));
    }

    @Test
    public void testRowFloorCeil() {
        Map<String, Object> r = row("v", 3.7);
        assertEquals(3.0, eval.evaluate("FLOOR(v)", r));
        assertEquals(4.0, eval.evaluate("CEIL(v)", r));
    }

    @Test
    public void testRowMd5() {
        Map<String, Object> r = row("s", "hello");
        Object result = eval.evaluate("MD5(s)", r);
        assertEquals("5d41402abc4b2a76b9719d911017c592", result);
    }

    @Test
    public void testRowCast() {
        Map<String, Object> r = row("s", "123");
        assertEquals(123, eval.evaluate("CAST(s AS INTEGER)", r));
    }

    @Test
    public void testRowToNumber() {
        Map<String, Object> r = row("s", "456.78");
        assertEquals(456.78, eval.evaluate("TO_NUMBER(s)", r));
    }

    @Test
    public void testRowToChar() {
        Map<String, Object> r = row("v", 123);
        Object result = eval.evaluate("TO_CHAR(v)", r);
        assertEquals("123", result);
    }

    @Test
    public void testRowDateDiff() {
        Map<String, Object> r = row();
        Object result = eval.evaluate("DATEDIFF(NOW(), DATE('2024-01-01'))", r);
        assertTrue(result instanceof Long || result instanceof Integer);
    }

    @Test
    public void testRowLpad() {
        Map<String, Object> r = row("s", "hi");
        assertEquals("****hi", eval.evaluate("LPAD(s, 6, '*')", r));
    }

    @Test
    public void testRowRpad() {
        Map<String, Object> r = row("s", "hi");
        assertEquals("hi****", eval.evaluate("RPAD(s, 6, '*')", r));
    }

    @Test
    public void testRowIfnull() {
        Map<String, Object> r = row("v", null);
        assertEquals("fallback", eval.evaluate("IFNULL(v, 'fallback')", r));
    }

    @Test
    public void testRowNvl() {
        Map<String, Object> r = row("v", null);
        assertEquals("fallback", eval.evaluate("NVL(v, 'fallback')", r));
    }

    @Test
    public void testRowNvl2() {
        Map<String, Object> r = row("v", "exists");
        assertEquals("yes", eval.evaluate("NVL2(v, 'yes', 'no')", r));
    }

    @Test
    public void testRowCaseWhen() {
        Map<String, Object> r = row("v", 10);
        // IF supports nested evaluation
        assertEquals("big", eval.evaluate("IF(v > 5, IF(v > 8, 'big', 'medium'), 'small')", r));
    }

    @Test
    public void testRowMod() {
        Map<String, Object> r = row("v", 10);
        assertEquals(1.0, eval.evaluate("MOD(v, 3)", r));
    }

    @Test
    public void testRowPower() {
        Map<String, Object> r = row("v", 2);
        assertEquals(8.0, eval.evaluate("POWER(v, 3)", r));
    }

    @Test
    public void testRowSqrt() {
        Map<String, Object> r = row("v", 9);
        assertEquals(3.0, eval.evaluate("SQRT(v)", r));
    }

    @Test
    public void testRowLog() {
        Map<String, Object> r = row("v", Math.E);
        assertEquals(1.0, eval.evaluate("LOG(v)", r));
    }

    @Test
    public void testRowExp() {
        Map<String, Object> r = row("v", 1);
        assertEquals(Math.exp(1), ((Number) eval.evaluate("EXP(v)", r)).doubleValue(), 1e-10);
    }

    @Test
    public void testRowLog10() {
        Map<String, Object> r = row("v", 100);
        assertEquals(2.0, eval.evaluate("LOG10(v)", r));
    }

    @Test
    public void testRowSign() {
        Map<String, Object> r = row("v", -5);
        assertEquals(-1, eval.evaluate("SIGN(v)", r));
        r.put("v", 5);
        assertEquals(1, eval.evaluate("SIGN(v)", r));
        r.put("v", 0);
        assertEquals(0, eval.evaluate("SIGN(v)", r));
    }

    @Test
    public void testRowGreatest() {
        Map<String, Object> r = row("a", 5, "b", 3, "c", 9);
        assertEquals(9.0, eval.evaluate("GREATEST(a, b, c)", r));
    }

    @Test
    public void testRowLeast() {
        Map<String, Object> r = row("a", 5, "b", 3, "c", 9);
        assertEquals(3.0, eval.evaluate("LEAST(a, b, c)", r));
    }

    @Test
    public void testRowConcatWs() {
        Map<String, Object> r = row("a", "hello", "b", "world");
        assertEquals("hello-world-123", eval.evaluate("CONCAT_WS('-', a, b, 123)", r));
    }

    @Test
    public void testRowInitcap() {
        Map<String, Object> r = row("s", "hello world foo-bar");
        assertEquals("Hello World Foo-Bar", eval.evaluate("INITCAP(s)", r));
    }

    @Test
    public void testRowLeftRight() {
        Map<String, Object> r = row("s", "HelloWorld");
        assertEquals("Hello", eval.evaluate("LEFT(s, 5)", r));
        assertEquals("World", eval.evaluate("RIGHT(s, 5)", r));
    }

    @Test
    public void testRowDayOfWeek() {
        Map<String, Object> r = row();
        Object result = eval.evaluate("DAYOFWEEK(NOW())", r);
        assertTrue(result instanceof Integer);
    }

    @Test
    public void testRowMonthName() {
        Map<String, Object> r = row();
        Object result = eval.evaluate("MONTHNAME(NOW())", r);
        assertNotNull(result);
    }

    @Test
    public void testRowIsnull() {
        Map<String, Object> r = row("v", null);
        assertEquals(true, eval.evaluate("ISNULL(v)", r));
        r.put("v", 1);
        assertEquals(false, eval.evaluate("ISNULL(v)", r));
    }

    // ===================== 用户自定义 UDF 测试 =====================

    @Test
    public void testCustomUdf() {
        eval.register("my_double", (row, args) -> {
            Object v = args.length > 0 ? args[0] : 0;
            if (v instanceof Number) {
                return ((Number) v).doubleValue() * 2;
            }
            return null;
        });
        Map<String, Object> r = row("v", 5);
        assertEquals(10.0, eval.evaluate("MY_DOUBLE(v)", r));
    }

    @Test
    public void testCustomUdfClass() {
        eval.register("custom_func", (row, args) -> "CUSTOM:" + (args.length > 0 ? args[0] : ""));
        Map<String, Object> r = row("name", "Alice");
        assertEquals("CUSTOM:Alice", eval.evaluate("CUSTOM_FUNC(name)", r));
    }

    // ===================== 比较/逻辑运算 =====================

    @Test
    public void testComparison() {
        Map<String, Object> r = row("salary", 5000);
        assertEquals(true, eval.evaluate("salary > 4000", r));
        assertEquals(false, eval.evaluate("salary < 4000", r));
    }

    @Test
    public void testLogicalAndOr() {
        Map<String, Object> r = row("a", 1, "b", 2);
        // Basic boolean expression evaluation with AND/OR
        Boolean result1 = (Boolean) eval.evaluate("a > 0 AND b > 1", r);
        assertEquals(true, result1);
        Boolean result2 = (Boolean) eval.evaluate("a > 0 OR c > 0", r);
        assertEquals(true, result2);
        Boolean result3 = (Boolean) eval.evaluate("a < 0 AND b > 1", r);
        assertEquals(false, result3);
    }

    // ===================== 注册表测试 =====================

    @Test
    public void testRegistryHasBuiltinFunctions() {
        SqlFunctionRegistry reg = SqlFunctionRegistry.get();
        assertTrue(reg.contains("ABS"));
        assertTrue(reg.contains("UPPER"));
        assertTrue(reg.contains("CONCAT"));
        assertTrue(reg.contains("NOW"));
        assertTrue(reg.contains("IF"));
        assertTrue(reg.contains("COALESCE"));
        assertTrue(reg.contains("DECODE"));
        assertTrue(reg.contains("MD5"));
    }

    @Test
    public void testRegistryNames() {
        SqlFunctionRegistry reg = SqlFunctionRegistry.get();
        Set<String> names = reg.names();
        assertTrue(names.size() > 50);
    }

    @Test
    public void testRegistryFind() {
        SqlFunctionRegistry reg = SqlFunctionRegistry.get();
        SqlFunctionDef def = reg.find("ABS");
        assertNotNull(def);
    }

    @Test
    public void testRegistryClearAndReregister() {
        SqlFunctionRegistry reg = SqlFunctionRegistry.get();
        reg.clear();
        assertFalse(reg.contains("ABS"));
        reg.registerBuiltin();
        assertTrue(reg.contains("ABS"));
    }
}