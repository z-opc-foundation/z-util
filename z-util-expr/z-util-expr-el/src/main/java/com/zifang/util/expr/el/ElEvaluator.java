package com.zifang.util.expr.el;

import java.util.HashMap;
import java.util.Map;

/**
 * EL (Expression Language) evaluator，兼容 SpEL 子集。
 * <p>
 * 自研实现，不依赖 Spring / 任何第三方库。底层是
 * {@link ElExpression} 提供的 recursive-descent 解析器 + 反射求值。
 *
 * <p>支持特性：
 * <ul>
 *     <li>算术：+ - * / %</li>
 *     <li>比较：== != &lt; &gt; &lt;= &gt;=</li>
 *     <li>逻辑：&amp;&amp; || !</li>
 *     <li>三目：? :</li>
 *     <li>属性访问：bean.prop</li>
 *     <li>Map/List 访问：map['key'] / list[0]</li>
 *     <li>方法调用：obj.method(args)</li>
 *     <li>变量：#name（SpEL 风格）</li>
 * </ul>
 */
public class ElEvaluator {

    private final ElExpression.Context context;
    private final Map<String, Object> variableMap;

    public ElEvaluator() {
        this.context = new ElExpression.Context();
        this.variableMap = new HashMap<>();
    }

    /**
     * 评估一个 EL 表达式。
     */
    public Object eval(String expression) {
        try {
            return ElExpression.compile(expression).evaluate(context);
        } catch (ElException e) {
            // 用统一信息包装，保持原异常作为 cause
            throw new ElException("Failed to evaluate EL expression: " + expression, e);
        } catch (Exception e) {
            throw new ElException("Failed to evaluate EL expression: " + expression, e);
        }
    }

    /**
     * 评估一个 EL 表达式，指定 root 对象（用于无前缀的属性访问）。
     */
    public Object eval(String expression, Object rootObject) {
        ElExpression.Context ctx = new ElExpression.Context(variableMap);
        ctx.setRoot(rootObject);
        try {
            return ElExpression.compile(expression).evaluate(ctx);
        } catch (ElException e) {
            throw e;
        } catch (Exception e) {
            throw new ElException("Failed to evaluate EL expression: " + expression, e);
        }
    }

    /**
     * 评估一个 EL 表达式，注入指定变量。
     */
    public Object eval(String expression, Map<String, Object> variables) {
        ElExpression.Context ctx = new ElExpression.Context();
        ctx.putAll(variables);
        try {
            return ElExpression.compile(expression).evaluate(ctx);
        } catch (ElException e) {
            throw e;
        } catch (Exception e) {
            throw new ElException("Failed to evaluate EL expression: " + expression, e);
        }
    }

    public void setVariable(String name, Object value) {
        variableMap.put(name, value);
        context.set(name, value);
    }

    public Object getVariable(String name) {
        return variableMap.get(name);
    }

    public void clearVariables() {
        variableMap.clear();
    }

    public Map<String, Object> getVariables() {
        return new HashMap<>(variableMap);
    }

    public void setVariables(Map<String, Object> variables) {
        if (variables != null) {
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                setVariable(entry.getKey(), entry.getValue());
            }
        }
    }
}
