package com.zifang.util.core.pattern.command;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 命令解析器
 * <p>
 * 解析文本或列表格式的命令描述并执行
 *
 * @author zifang
 */
public class CommandParser<C extends CommandContext> {

    private final CommandRegistry<C> registry;
    private final CommandExecutor<C> executor;
    private final Pattern paramPattern = Pattern.compile("(\\w+)(?:\\((.*)\\))?");
    private final Pattern kvPattern = Pattern.compile("(\\w+)=([^,]+)");

    /**
     * CommandParser方法。
     * * @param registry CommandRegistryC类型参数
     */
    public CommandParser(CommandRegistry<C> registry) {
        this.registry = registry;
        this.executor = new CommandExecutor<>(registry);
    }

    /**
     * 解析并执行命令脚本
     * <p>
     * 格式: command1; command2; command3
     * 带参数: command1(p1=val1,p2=val2); command2
     * 带条件: command1?condition
     *
     * @param script  命令脚本
     * @param context 执行上下文
     */
    public void parseAndExecute(String script, C context) {
        if (script == null || script.trim().isEmpty()) {
            return;
        }

        String[] commands = script.split(";");
        for (String cmd : commands) {
            cmd = cmd.trim();
            if (cmd.isEmpty()) {
                continue;
            }

            parseAndExecuteSingle(cmd, context);
        }
    }

    /**
     * 解析并执行命令列表
     */
    public void parseAndExecute(List<String> commandLines, C context) {
        for (String line : commandLines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue; // 跳过空行和注释
            }
            parseAndExecuteSingle(line, context);
        }
    }

    /**
     * 解析并执行单个命令
     */
    private void parseAndExecuteSingle(String cmd, C context) {
        // 解析条件表达式
        String condition = null;
        if (cmd.contains("?")) {
            String[] parts = cmd.split("\\?", 2);
            cmd = parts[0].trim();
            condition = parts[1].trim();
        }

        // 检查条件
        if (condition != null && !evaluateCondition(condition, context)) {
            return;
        }

        // 解析参数
        Map<String, String> params = new HashMap<>();
        Matcher paramMatcher = paramPattern.matcher(cmd);
        String commandName = null;

        if (paramMatcher.matches()) {
            commandName = paramMatcher.group(1);
            String paramStr = paramMatcher.group(2);
            if (paramStr != null && !paramStr.isEmpty()) {
                parseParameters(paramStr, params);
            }
        } else {
            commandName = cmd;
        }

        // 设置参数到上下文
        for (Map.Entry<String, String> entry : params.entrySet()) {
            context.put(entry.getKey(), parseValue(entry.getValue()));
        }

        // 执行命令
        executor.execute(context, commandName);
    }

    /**
     * 解析参数字符串 key=value,key2=value2
     */
    private void parseParameters(String paramStr, Map<String, String> params) {
        String[] pairs = paramStr.split(",");
        for (String pair : pairs) {
            Matcher matcher = kvPattern.matcher(pair.trim());
            if (matcher.matches()) {
                params.put(matcher.group(1), matcher.group(2));
            }
        }
    }

    /**
     * 解析参数值
     */
    private Object parseValue(String value) {
        if (value == null) {
            return null;
        }
        value = value.trim();

        // 字符串
        if ((value.startsWith("\"") && value.endsWith("\"")) ||
                (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }

        // 布尔值
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }

        // 数字
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                return Long.parseLong(value);
            }
        } catch (NumberFormatException ignored) {
        }

        // 列表格式 [a,b,c]
        if (value.startsWith("[") && value.endsWith("]")) {
            String inner = value.substring(1, value.length() - 1);
            String[] items = inner.split(",");
            return Arrays.stream(items)
                    .map(String::trim)
                    .map(this::parseValue)
                    .collect(Collectors.toList());
        }

        return value;
    }

    /**
     * 评估条件
     */
    private boolean evaluateCondition(String condition, C context) {
        condition = condition.trim();

        // 支持 && 和 ||
        if (condition.contains("&&")) {
            String[] parts = condition.split("&&");
            for (String part : parts) {
                if (!evaluateSingleCondition(part.trim(), context)) {
                    return false;
                }
            }
            return true;
        }

        if (condition.contains("||")) {
            String[] parts = condition.split("\\|\\|");
            for (String part : parts) {
                if (evaluateSingleCondition(part.trim(), context)) {
                    return true;
                }
            }
            return false;
        }

        return evaluateSingleCondition(condition, context);
    }

    /**
     * 评估单个条件
     */
    private boolean evaluateSingleCondition(String condition, C context) {
        // 支持 context.key == value 格式
        if (condition.contains("==")) {
            String[] parts = condition.split("==");
            String key = parts[0].trim();
            String expected = parts[1].trim();
            Object actual = context.get(key);
            return Objects.equals(String.valueOf(actual), expected);
        }

        // 支持 context.key != value 格式
        if (condition.contains("!=")) {
            String[] parts = condition.split("!=");
            String key = parts[0].trim();
            String expected = parts[1].trim();
            Object actual = context.get(key);
            return !Objects.equals(String.valueOf(actual), expected);
        }

        // 支持 exists(key) 格式
        if (condition.startsWith("exists(") && condition.endsWith(")")) {
            String key = condition.substring(7, condition.length() - 1);
            return context.containsKey(key);
        }

        // 默认作为布尔值处理
        return Boolean.parseBoolean(condition);
    }

    /**
     * 获取命令执行器
     */
    public CommandExecutor<C> getExecutor() {
        return executor;
    }

    /**
     * 获取注册表
     */
    public CommandRegistry<C> getRegistry() {
        return registry;
    }
}
