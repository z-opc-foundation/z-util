package com.zifang.util.workflow.engine.runtime;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * GatewayEvaluator 表达式评估器测试。
 * 基于 SpEL 实现，支持完整的表达式语法：
 * - 比较运算符: ==, !=, <, >, <=, >=
 * - 逻辑运算符: &&, ||, !
 * - 字符串匹配: 'text', "text"
 * - 括号分组: (expr)
 * - 三目运算符: condition ? trueVal : falseVal
 */

/**
 * GatewayEvaluatorTest类。
 */
public class GatewayEvaluatorTest {

    private GatewayEvaluator evaluator;

    @Before
    /**
     * setUp方法。
     */
    public void setUp() {
        evaluator = new GatewayEvaluator();
    }

    // ===== 基础比较运算符 =====

    @Test
    /**
     * testEvaluate_Equality_BooleanTrue方法。
     */
    public void testEvaluate_Equality_BooleanTrue() {
        String expression = "${approved == true}";
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", true);

        assertTrue(evaluator.evaluate(expression, variables));
    }

    @Test
    /**
     * testEvaluate_Equality_BooleanFalse方法。
     */
    public void testEvaluate_Equality_BooleanFalse() {
        String expression = "${approved == true}";
        Map<String, Object> variables = new HashMap<>();
        variables.put("approved", false);

        assertFalse(evaluator.evaluate(expression, variables));
    }

    @Test
    /**
     * testEvaluate_Equality_Numeric方法。
     */
    public void testEvaluate_Equality_Numeric() {
        String expression = "${amount == 1000}";

        Map<String, Object> variables = new HashMap<>();

        variables.put("amount", 1000);
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("amount", 999);
        assertFalse(evaluator.evaluate(expression, variables));
    }

    @Test
    /**
     * testEvaluate_NotEqual方法。
     */
    public void testEvaluate_NotEqual() {
        String expression = "${status != 'cancelled'}";

        Map<String, Object> variables = new HashMap<>();

        variables.put("status", "active");
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("status", "cancelled");
        assertFalse(evaluator.evaluate(expression, variables));
    }

    @Test
    /**
     * testEvaluate_GreaterThan方法。
     */
    public void testEvaluate_GreaterThan() {
        String expression = "${amount > 1000}";

        Map<String, Object> variables = new HashMap<>();

        variables.put("amount", 500);
        assertFalse(evaluator.evaluate(expression, variables));

        variables.put("amount", 1001);
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("amount", 1000);
        assertFalse(evaluator.evaluate(expression, variables));
    }

    @Test
    /**
     * testEvaluate_LessThan方法。
     */
    public void testEvaluate_LessThan() {
        String expression = "${temperature < 0}";

        Map<String, Object> variables = new HashMap<>();

        variables.put("temperature", -1);
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("temperature", 0);
        assertFalse(evaluator.evaluate(expression, variables));

        variables.put("temperature", 1);
        assertFalse(evaluator.evaluate(expression, variables));
    }

    @Test
    /**
     * testEvaluate_GreaterThanOrEqual方法。
     */
    public void testEvaluate_GreaterThanOrEqual() {
        String expression = "${score >= 60}";

        Map<String, Object> variables = new HashMap<>();

        variables.put("score", 59);
        assertFalse(evaluator.evaluate(expression, variables));

        variables.put("score", 60);
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("score", 100);
        assertTrue(evaluator.evaluate(expression, variables));
    }

    @Test
    /**
     * testEvaluate_LessThanOrEqual方法。
     */
    public void testEvaluate_LessThanOrEqual() {
        String expression = "${balance <= 0}";

        Map<String, Object> variables = new HashMap<>();

        variables.put("balance", 1);
        assertFalse(evaluator.evaluate(expression, variables));

        variables.put("balance", 0);
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("balance", -100);
        assertTrue(evaluator.evaluate(expression, variables));
    }

    // ===== 逻辑运算符 =====

    @Test
    /**
     * testEvaluate_LogicalAnd方法。
     */
    public void testEvaluate_LogicalAnd() {
        String expression = "${amount > 1000 && amount <= 5000}";

        Map<String, Object> variables = new HashMap<>();

        variables.put("amount", 1500);
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("amount", 500);
        assertFalse(evaluator.evaluate(expression, variables));

        variables.put("amount", 6000);
        assertFalse(evaluator.evaluate(expression, variables));
    }

    @Test
    /**
     * testEvaluate_LogicalOr方法。
     */
    public void testEvaluate_LogicalOr() {
        String expression = "${status == 'vip' || status == 'premium'}";

        Map<String, Object> variables = new HashMap<>();

        variables.put("status", "vip");
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("status", "premium");
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("status", "basic");
        assertFalse(evaluator.evaluate(expression, variables));
    }

    @Test
    /**
     * testEvaluate_LogicalNot方法。
     */
    public void testEvaluate_LogicalNot() {
        String expression = "${!isDisabled}";

        Map<String, Object> variables = new HashMap<>();

        variables.put("isDisabled", false);
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("isDisabled", true);
        assertFalse(evaluator.evaluate(expression, variables));
    }

    @Test
    /**
     * testEvaluate_CompoundAnd方法。
     */
    public void testEvaluate_CompoundAnd() {
        String expression = "${age >= 18 && income > 30000}";

        Map<String, Object> variables = new HashMap<>();

        variables.put("age", 25);
        variables.put("income", 40000);
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("age", 17);
        variables.put("income", 40000);
        assertFalse(evaluator.evaluate(expression, variables));

        variables.put("age", 25);
        variables.put("income", 20000);
        assertFalse(evaluator.evaluate(expression, variables));
    }

    @Test
    /**
     * testEvaluate_CompoundOr方法。
     */
    public void testEvaluate_CompoundOr() {
        String expression = "${level == 1 || level == 2 || level == 3}";

        Map<String, Object> variables = new HashMap<>();

        variables.put("level", 1);
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("level", 2);
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("level", 99);
        assertFalse(evaluator.evaluate(expression, variables));
    }

    // ===== 复杂组合表达式 =====

    @Test
    /**
     * testEvaluate_MixedOperators方法。
     */
    public void testEvaluate_MixedOperators() {
        String expression = "${age >= 18 && age <= 65 && income > 30000}";

        Map<String, Object> variables = new HashMap<>();

        variables.put("age", 25);
        variables.put("income", 40000);
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("age", 70);
        variables.put("income", 40000);
        assertFalse(evaluator.evaluate(expression, variables));

        variables.put("age", 25);
        variables.put("income", 20000);
        assertFalse(evaluator.evaluate(expression, variables));
    }

    @Test
    /**
     * testEvaluate_NegationWithComparison方法。
     */
    public void testEvaluate_NegationWithComparison() {
        String expression = "${!(amount < 1000)}";

        Map<String, Object> variables = new HashMap<>();

        variables.put("amount", 1500);
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("amount", 500);
        assertFalse(evaluator.evaluate(expression, variables));
    }

    @Test
    /**
     * testEvaluate_ParenthesesGrouping方法。
     */
    public void testEvaluate_ParenthesesGrouping() {
        String expression = "${(amount > 1000 || isVIP) && status == 'active'}";

        Map<String, Object> variables = new HashMap<>();

        // 满足两个条件
        variables.put("amount", 1500);
        variables.put("isVIP", false);
        variables.put("status", "active");
        assertTrue(evaluator.evaluate(expression, variables));

        // amount 不满足但 isVIP 满足
        variables.put("amount", 500);
        variables.put("isVIP", true);
        variables.put("status", "active");
        assertTrue(evaluator.evaluate(expression, variables));

        // status 不满足
        variables.put("amount", 1500);
        variables.put("isVIP", false);
        variables.put("status", "suspended");
        assertFalse(evaluator.evaluate(expression, variables));
    }

    // ===== 单变量测试 =====

    @Test
    /**
     * testEvaluate_SingleVariableTrue方法。
     */
    public void testEvaluate_SingleVariableTrue() {
        String expression = "${isActive}";
        Map<String, Object> variables = new HashMap<>();
        variables.put("isActive", true);

        assertTrue(evaluator.evaluate(expression, variables));
    }

    @Test
    /**
     * testEvaluate_SingleVariableFalse方法。
     */
    public void testEvaluate_SingleVariableFalse() {
        String expression = "${isActive}";
        Map<String, Object> variables = new HashMap<>();
        variables.put("isActive", false);

        assertFalse(evaluator.evaluate(expression, variables));
    }

    // ===== 边界条件测试 =====

    @Test
    /**
     * testEvaluate_NullExpression_ReturnsTrue方法。
     */
    public void testEvaluate_NullExpression_ReturnsTrue() {
        Map<String, Object> variables = new HashMap<>();

        assertTrue(evaluator.evaluate(null, variables));
    }

    @Test
    /**
     * testEvaluate_EmptyExpression_ReturnsTrue方法。
     */
    public void testEvaluate_EmptyExpression_ReturnsTrue() {
        Map<String, Object> variables = new HashMap<>();

        assertTrue(evaluator.evaluate("", variables));
    }

    @Test
    /**
     * testEvaluate_WithMissingVariable方法。
     */
    public void testEvaluate_WithMissingVariable() {
        String expression = "${unknownField == true}";
        Map<String, Object> variables = new HashMap<>();

        // 缺失变量返回 null，在布尔上下文中为 false
        assertFalse(evaluator.evaluate(expression, variables));
    }

    @Test
    /**
     * testEvaluate_WithZeroValue方法。
     */
    public void testEvaluate_WithZeroValue() {
        String expression = "${count > 0}";
        Map<String, Object> variables = new HashMap<>();

        variables.put("count", 0);
        assertFalse(evaluator.evaluate(expression, variables));

        variables.put("count", 1);
        assertTrue(evaluator.evaluate(expression, variables));
    }

    @Test
    /**
     * testEvaluate_WithNegativeNumber方法。
     */
    public void testEvaluate_WithNegativeNumber() {
        String expression = "${balance < 0}";
        Map<String, Object> variables = new HashMap<>();

        variables.put("balance", -100);
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("balance", 0);
        assertFalse(evaluator.evaluate(expression, variables));

        variables.put("balance", 100);
        assertFalse(evaluator.evaluate(expression, variables));
    }

    @Test
    /**
     * testEvaluate_WithDecimalNumber方法。
     */
    public void testEvaluate_WithDecimalNumber() {
        String expression = "${price > 9.99}";

        Map<String, Object> variables = new HashMap<>();

        variables.put("price", 9.99);
        assertFalse(evaluator.evaluate(expression, variables));

        variables.put("price", 10.00);
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("price", 10.001);
        assertTrue(evaluator.evaluate(expression, variables));
    }

    // ===== 错误处理测试 =====

    @Test(expected = IllegalArgumentException.class)
    /**
     * testEvaluate_InvalidExpression_ThrowsException方法。
     */
    public void testEvaluate_InvalidExpression_ThrowsException() {
        String expression = "${amount >>> 1000}";  // 无效操作符
        Map<String, Object> variables = new HashMap<>();
        variables.put("amount", 100);

        evaluator.evaluate(expression, variables);
    }

    @Test
    /**
     * testEvaluate_InvalidExpression_NoBraces方法。
     */
    public void testEvaluate_InvalidExpression_NoBraces() {
        // 没有 ${} 包裹的表达式的行为
        String expression = "amount > 1000";
        Map<String, Object> variables = new HashMap<>();
        variables.put("amount", 1500);

        // 应该也能处理（去掉 ${} 包裹）
        assertTrue(evaluator.evaluate(expression, variables));
    }

    // ===== 性能测试 =====

    @Test
    /**
     * testEvaluate_ManyIterations方法。
     */
    public void testEvaluate_ManyIterations() {
        String expression = "${count > 50}";
        Map<String, Object> variables = new HashMap<>();
        variables.put("count", 75);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            evaluator.evaluate(expression, variables);
        }
        long endTime = System.currentTimeMillis();

        // 10000 次评估应该在合理时间内完成（< 2秒）
        assertTrue("Evaluation took too long: " + (endTime - startTime) + "ms",
                (endTime - startTime) < 2000);
    }

    // ===== 业务场景测试 =====

    @Test
    /**
     * testEvaluate_LevelBasedApproval方法。
     */
    public void testEvaluate_LevelBasedApproval() {
        // 多级审批: 1级及以上审批通过
        String expression = "${level >= 1}";

        Map<String, Object> variables = new HashMap<>();

        variables.put("level", 1);
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("level", 0);
        assertFalse(evaluator.evaluate(expression, variables));

        variables.put("level", 5);
        assertTrue(evaluator.evaluate(expression, variables));
    }

    @Test
    /**
     * testEvaluate_AmountRangeBasedRouting方法。
     */
    public void testEvaluate_AmountRangeBasedRouting() {
        // 根据金额范围路由: 1000 以下走快速审批，1000-5000 走标准审批，5000 以上走特殊审批
        String expression = "${amount <= 1000}";

        Map<String, Object> variables = new HashMap<>();

        variables.put("amount", 500);
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("amount", 1001);
        assertFalse(evaluator.evaluate(expression, variables));
    }

    @Test
    /**
     * testEvaluate_CreditScore方法。
     */
    public void testEvaluate_CreditScore() {
        // 信用评分: >= 750 分才能通过
        String expression = "${creditScore >= 750}";

        Map<String, Object> variables = new HashMap<>();

        variables.put("creditScore", 800);
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("creditScore", 749);
        assertFalse(evaluator.evaluate(expression, variables));
    }

    @Test
    /**
     * testEvaluate_InventoryCheck方法。
     */
    public void testEvaluate_InventoryCheck() {
        // 库存检查 + 预留检查
        String expression = "${stock > 0 && reserved < stock}";

        Map<String, Object> variables = new HashMap<>();

        variables.put("stock", 100);
        variables.put("reserved", 50);
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("stock", 100);
        variables.put("reserved", 100);
        assertFalse(evaluator.evaluate(expression, variables));

        variables.put("stock", 0);
        variables.put("reserved", 0);
        assertFalse(evaluator.evaluate(expression, variables));
    }

    @Test
    /**
     * testEvaluate_TimeWindow方法。
     */
    public void testEvaluate_TimeWindow() {
        // 时间窗口判断: 9:00 - 18:00
        String expression = "${hour >= 9 && hour <= 18}";

        Map<String, Object> variables = new HashMap<>();

        variables.put("hour", 12);
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("hour", 8);
        assertFalse(evaluator.evaluate(expression, variables));

        variables.put("hour", 19);
        assertFalse(evaluator.evaluate(expression, variables));
    }

    @Test
    /**
     * testEvaluate_VIPDiscount方法。
     */
    public void testEvaluate_VIPDiscount() {
        // VIP 折扣: VIP 客户或消费满 1000
        String expression = "${isVIP == true || amount >= 1000}";

        Map<String, Object> variables = new HashMap<>();

        variables.put("isVIP", true);
        variables.put("amount", 100);
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("isVIP", false);
        variables.put("amount", 1500);
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("isVIP", false);
        variables.put("amount", 500);
        assertFalse(evaluator.evaluate(expression, variables));
    }

    @Test
    /**
     * testEvaluate_ComplexBusinessLogic方法。
     */
    public void testEvaluate_ComplexBusinessLogic() {
        // 复杂业务逻辑: 年龄 18-60 且收入 > 30000 或为 VIP
        String expression = "${(age >= 18 && age <= 60 && income > 30000) || isVIP == true}";

        Map<String, Object> variables = new HashMap<>();

        // 满足年龄和收入条件
        variables.put("age", 30);
        variables.put("income", 40000);
        variables.put("isVIP", false);
        assertTrue(evaluator.evaluate(expression, variables));

        // 不满足年龄和收入，但满足 VIP
        variables.put("age", 70);
        variables.put("income", 20000);
        variables.put("isVIP", true);
        assertTrue(evaluator.evaluate(expression, variables));

        // 都不满足
        variables.put("age", 30);
        variables.put("income", 20000);
        variables.put("isVIP", false);
        assertFalse(evaluator.evaluate(expression, variables));
    }

    @Test
    /**
     * testEvaluate_EnumLikeComparison方法。
     */
    public void testEvaluate_EnumLikeComparison() {
        // 枚举类型的状态比较
        String expression = "${status == 'APPROVED' && approvedBy == 'MANAGER'}";

        Map<String, Object> variables = new HashMap<>();

        variables.put("status", "APPROVED");
        variables.put("approvedBy", "MANAGER");
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("status", "APPROVED");
        variables.put("approvedBy", "DIRECTOR");
        assertFalse(evaluator.evaluate(expression, variables));
    }

    @Test
    /**
     * testEvaluate_StringMatching方法。
     */
    public void testEvaluate_StringMatching() {
        // 字符串包含判断
        String expression = "${email.contains('@') && email.contains('.')}";

        Map<String, Object> variables = new HashMap<>();

        variables.put("email", "user@example.com");
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("email", "invalid-email");
        assertFalse(evaluator.evaluate(expression, variables));
    }

    @Test
    /**
     * testEvaluate_WithEmptyString方法。
     */
    public void testEvaluate_WithEmptyString() {
        String expression = "${name != ''}";

        Map<String, Object> variables = new HashMap<>();

        variables.put("name", "张三");
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("name", "");
        assertFalse(evaluator.evaluate(expression, variables));

        variables.put("name", null);
        // SpEL 中 null != '' 为 true（null 不等于任何值，包括空字符串）
        assertTrue(evaluator.evaluate(expression, variables));
    }

    @Test
    /**
     * testEvaluate_DecimalComparison方法。
     */
    public void testEvaluate_DecimalComparison() {
        String expression = "${balance >= 1000.50}";

        Map<String, Object> variables = new HashMap<>();

        variables.put("balance", 1000.50);
        assertTrue(evaluator.evaluate(expression, variables));

        variables.put("balance", 1000.49);
        assertFalse(evaluator.evaluate(expression, variables));

        variables.put("balance", 2000.00);
        assertTrue(evaluator.evaluate(expression, variables));
    }
}
