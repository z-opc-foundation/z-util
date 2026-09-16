package com.zifang.util.workflow.engine.runtime;

import com.zifang.util.expr.el.ElEvaluator;
import com.zifang.util.expr.el.ElException;

import java.util.Map;

/**
 * 网关条件评估器。
 * <p>
 * 自研实现：基于 z-util-expr-el 的 EL 引擎（不再依赖 Spring SpEL）。
 * <p>
 * 支持的语法：
 * <ul>
 *   <li>比较运算符: ==, !=, &lt;, &gt;, &lt;=, &gt;=</li>
 *   <li>逻辑运算符: &amp;&amp;, ||, !</li>
 *   <li>字符串匹配: 'text', "text"</li>
 *   <li>括号分组: (expr)</li>
 *   <li>方法调用: object.method()</li>
 *   <li>三目运算符: condition ? trueVal : falseVal</li>
 * </ul>
 * <p>
 * 输入格式: ${expression}，例如:
 * <ul>
 *   <li>${level >= 1}</li>
 *   <li>${amount > 1000 && amount <= 5000}</li>
 *   <li>${status == 'approved'}</li>
 *   <li>${!isDisabled}</li>
 *   <li>${email.contains('@')}</li>
 * </ul>
 *
 * <p>实现策略：
 * <ol>
 *   <li>把 ${...} 包装去掉</li>
 *   <li>把表达式中"非方法调用、非字符串字面量"的标识符前加 #（SpEL 风格）</li>
 *   <li>复用自研 EL 解析器评估，返回布尔结果</li>
 * </ol>
 */
public class GatewayEvaluator {

    /**
     * Evaluate a gateway condition expression against variables.
     *
     * @param expression the expression to evaluate (e.g., "${approved == true}" or "${amount > 1000 && amount <= 5000}")
     * @param variables  the runtime variables available in the expression context
     * @return true if the condition is satisfied, false otherwise
     * @throws IllegalArgumentException if the expression format is invalid
     */
    public boolean evaluate(String expression, Map<String, Object> variables) {
        if (expression == null || expression.trim().isEmpty()) {
            return true;
        }

        String innerExpression = stripBraces(expression.trim());
        String spelExpression = prefixVariables(innerExpression);

        ElEvaluator evaluator = new ElEvaluator();
        if (variables != null) {
            evaluator.setVariables(variables);
        }
        try {
            Object result = evaluator.eval(spelExpression);
            return toBoolean(result);
        } catch (ElException e) {
            throw new IllegalArgumentException("Failed to evaluate expression: " + expression, e);
        }
    }

    /**
     * 去除 ${...} 包装。
     */
    private String stripBraces(String expression) {
        if (expression.startsWith("${") && expression.endsWith("}")) {
            return expression.substring(2, expression.length() - 1).trim();
        }
        return expression;
    }

    /**
     * 把表达式中"非方法调用、非字符串字面量"的标识符前加 # 前缀。
     * <p>
     * 跳过字符串字面量（单/双引号包裹）和方法调用（identifier 后紧跟 '('）。
     */
    private String prefixVariables(String expression) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        int len = expression.length();

        while (i < len) {
            char ch = expression.charAt(i);

            if (ch == '\'' || ch == '"') {
                char quote = ch;
                result.append(ch);
                i++;
                while (i < len) {
                    char c = expression.charAt(i);
                    result.append(c);
                    if (c == quote && (i == 0 || expression.charAt(i - 1) != '\\')) {
                        i++;
                        break;
                    }
                    i++;
                }
                continue;
            }

            if (Character.isLetter(ch) || ch == '_' || ch == '$') {
                StringBuilder identifier = new StringBuilder();
                int start = i;
                while (i < len && (Character.isLetterOrDigit(expression.charAt(i)) || expression.charAt(i) == '_' || expression.charAt(i) == '$')) {
                    identifier.append(expression.charAt(i));
                    i++;
                }
                String ident = identifier.toString();

                // 标识符后紧跟 '(' 说明是方法调用，不加 # 前缀
                if (i < len && expression.charAt(i) == '(') {
                    result.append(ident);
                    continue;
                }

                if (isElKeyword(ident)) {
                    result.append(ident);
                } else {
                    result.append('#').append(ident);
                }
                continue;
            }

            result.append(ch);
            i++;
        }

        return result.toString();
    }

    private boolean isElKeyword(String word) {
        return word.equals("true") || word.equals("false") || word.equals("null")
                || word.equals("and") || word.equals("or") || word.equals("not")
                || word.equals("div") || word.equals("mod")
                || word.equals("instanceof") || word.equals("matches")
                || word.equals("between");
    }

    private boolean toBoolean(Object result) {
        if (result instanceof Boolean) return (Boolean) result;
        if (result == null) return false;
        if (result instanceof Number) return ((Number) result).doubleValue() != 0.0;
        if (result instanceof String) return !((String) result).isEmpty();
        return Boolean.parseBoolean(result.toString());
    }
}
