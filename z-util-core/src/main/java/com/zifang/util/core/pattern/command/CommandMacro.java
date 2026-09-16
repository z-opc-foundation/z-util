package com.zifang.util.core.pattern.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * 命令宏
 * <p>
 * 将多个命令组合成一个可重用的宏命令
 *
 * @param <C> 上下文类型
 * @author zifang
 */
public class CommandMacro<C extends CommandContext> implements Command<C> {

    private final String name;
    private final List<Command<C>> commands;
    private final Predicate<C> canExecute;

    /**
     * CommandMacro方法。
     * * @param name String类型参数
     */
    public CommandMacro(String name) {
        this(name, new ArrayList<>());
    }

    /**
     * CommandMacro方法。
     * * @param name String类型参数
     *
     * @param commands ListCommandC类型参数
     */
    public CommandMacro(String name, List<Command<C>> commands) {
        this.name = name;
        this.commands = new ArrayList<>(commands);
        this.canExecute = null;
    }

    /**
     * CommandMacro方法。
     * * @param name String类型参数
     *
     * @param canExecute PredicateC类型参数
     */
    public CommandMacro(String name, Predicate<C> canExecute) {
        this.name = name;
        this.commands = new ArrayList<>();
        this.canExecute = canExecute;
    }

    /**
     * CommandMacro方法。
     * * @param name String类型参数
     *
     * @param canExecute PredicateC类型参数
     * @param commands   ListCommandC类型参数
     */
    public CommandMacro(String name, Predicate<C> canExecute, List<Command<C>> commands) {
        this.name = name;
        this.commands = new ArrayList<>(commands);
        this.canExecute = canExecute;
    }

    /**
     * 创建宏命令
     */
    public static <C extends CommandContext> CommandMacro<C> of(String name) {
        return new CommandMacro<>(name);
    }

    /**
     * 创建宏命令并添加初始命令
     */
    @SafeVarargs
    /**
     * of方法。
     *      * @param name String类型参数
     * @param commands CommandC...类型参数
     * @return static <C extends CommandContext> CommandMacro<C>类型返回值
     */
    public static <C extends CommandContext> CommandMacro<C> of(String name, Command<C>... commands) {
        CommandMacro<C> macro = new CommandMacro<>(name);
        for (Command<C> cmd : commands) {
            macro.add(cmd);
        }
        return macro;
    }

    /**
     * 添加命令
     */
    public CommandMacro<C> add(Command<C> command) {
        if (command != null) {
            commands.add(command);
        }
        return this;
    }

    /**
     * 添加多个命令
     */
    @SafeVarargs
    /**
     * addAll方法。
     *      * @param commands CommandC...类型参数
     * @return final CommandMacro<C>类型返回值
     */
    public final CommandMacro<C> addAll(Command<C>... commands) {
        for (Command<C> cmd : commands) {
            add(cmd);
        }
        return this;
    }

    /**
     * 在开头添加命令
     */
    public CommandMacro<C> addFirst(Command<C> command) {
        if (command != null) {
            commands.add(0, command);
        }
        return this;
    }

    /**
     * 移除命令
     */
    public CommandMacro<C> remove(Command<C> command) {
        commands.remove(command);
        return this;
    }

    /**
     * 清空命令
     */
    public CommandMacro<C> clear() {
        commands.clear();
        return this;
    }

    /**
     * 获取命令数量
     */
    public int size() {
        return commands.size();
    }

    /**
     * 是否为空
     */
    public boolean isEmpty() {
        return commands.isEmpty();
    }

    /**
     * 获取所有命令
     */
    public List<Command<C>> getCommands() {
        return Collections.unmodifiableList(commands);
    }

    @Override
    /**
     * execute方法。
     *      * @param context C类型参数
     */
    public void execute(C context) {
        if (canExecute != null && !canExecute.test(context)) {
            return;
        }

        for (Command<C> command : commands) {
            if (context.isInterrupted()) {
                break;
            }
            command.execute(context);
        }
    }

    @Override
    /**
     * getName方法。
     * @return String类型返回值
     */
    public String getName() {
        return name;
    }

    @Override
    /**
     * supportsUndo方法。
     * @return boolean类型返回值
     */
    public boolean supportsUndo() {
        // 支持撤销当所有子命令都支持
        for (Command<C> cmd : commands) {
            if (!cmd.supportsUndo()) {
                return false;
            }
        }
        return !commands.isEmpty();
    }

    @Override
    /**
     * undo方法。
     *      * @param context C类型参数
     */
    public void undo(C context) {
        // 逆序撤销所有命令
        List<Command<C>> reversed = new ArrayList<>(commands);
        Collections.reverse(reversed);

        for (Command<C> command : reversed) {
            if (command.supportsUndo()) {
                command.undo(context);
            }
        }
    }
}
