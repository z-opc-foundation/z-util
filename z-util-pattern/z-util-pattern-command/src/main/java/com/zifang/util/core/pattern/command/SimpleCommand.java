package com.zifang.util.core.pattern.command;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 简单命令实现
 *
 * @param <C> 上下文类型
 * @author zifang
 */
public class SimpleCommand<C extends CommandContext> implements Command<C> {

    private final String name;
    private final String description;
    private final Consumer<C> action;
    private final Predicate<C> canExecute;
    private Consumer<C> undoAction;

    /**
     * SimpleCommand方法。
     * * @param name String类型参数
     *
     * @param action ConsumerC类型参数
     */
    public SimpleCommand(String name, Consumer<C> action) {
        this(name, "", ctx -> true, action, null);
    }

    /**
     * SimpleCommand方法。
     * * @param name String类型参数
     *
     * @param description String类型参数
     * @param action      ConsumerC类型参数
     */
    public SimpleCommand(String name, String description, Consumer<C> action) {
        this(name, description, ctx -> true, action, null);
    }

    /**
     * SimpleCommand方法。
     * * @param name String类型参数
     *
     * @param action     ConsumerC类型参数
     * @param undoAction ConsumerC类型参数
     */
    public SimpleCommand(String name, Consumer<C> action, Consumer<C> undoAction) {
        this(name, "", ctx -> true, action, undoAction);
    }

    /**
     * SimpleCommand方法。
     * * @param name String类型参数
     *
     * @param description String类型参数
     * @param action      ConsumerC类型参数
     * @param undoAction  ConsumerC类型参数
     */
    public SimpleCommand(String name, String description, Consumer<C> action, Consumer<C> undoAction) {
        this.name = name;
        this.description = description;
        this.action = action;
        this.canExecute = ctx -> true;
        this.undoAction = undoAction;
    }

    /**
     * SimpleCommand方法。
     * * @param name String类型参数
     *
     * @param description String类型参数
     * @param canExecute  PredicateC类型参数
     * @param action      ConsumerC类型参数
     * @param undoAction  ConsumerC类型参数
     */
    public SimpleCommand(String name, String description, Predicate<C> canExecute, Consumer<C> action, Consumer<C> undoAction) {
        this.name = name;
        this.description = description;
        this.canExecute = canExecute;
        this.action = action;
        this.undoAction = undoAction;
    }

    /**
     * 创建命令
     */
    public static <C extends CommandContext> SimpleCommand<C> of(String name, Consumer<C> action) {
        return new SimpleCommand<>(name, action);
    }

    /**
     * 创建带撤销的命令
     */
    public static <C extends CommandContext> SimpleCommand<C> of(String name, Consumer<C> action, Consumer<C> undoAction) {
        return new SimpleCommand<>(name, action, undoAction);
    }

    /**
     * 创建命名命令
     */
    public static <C extends CommandContext> SimpleCommand<C> named(String name, String description, Consumer<C> action) {
        return new SimpleCommand<>(name, description, action);
    }

    @Override
    /**
     * execute方法。
     *      * @param context C类型参数
     */
    public void execute(C context) {
        if (!canExecute.test(context)) {
            return;
        }
        action.accept(context);
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
     * getDescription方法。
     * @return String类型返回值
     */
    public String getDescription() {
        return description;
    }

    @Override
    /**
     * supportsUndo方法。
     * @return boolean类型返回值
     */
    public boolean supportsUndo() {
        return undoAction != null;
    }

    @Override
    /**
     * undo方法。
     *      * @param context C类型参数
     */
    public void undo(C context) {
        if (undoAction != null) {
            undoAction.accept(context);
        } else {
            throw new UnsupportedOperationException("Undo not configured");
        }
    }
}
