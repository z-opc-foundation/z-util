package com.zifang.util.core.pattern.command;

import java.util.ArrayList;
import java.util.List;

/**
 * 命令执行器
 * <p>
 * 负责从注册表执行命令，支持命令链、日志、异常处理
 *
 * @param <C> 上下文类型
 * @author zifang
 */
public class CommandExecutor<C extends CommandContext> {

    private final CommandRegistry<C> registry;
    private final List<CommandListener<C>> listeners;
    private Command<C> lastExecuted;

    /**
     * CommandExecutor方法。
     * * @param registry CommandRegistryC类型参数
     */
    public CommandExecutor(CommandRegistry<C> registry) {
        this.registry = registry;
        this.listeners = new ArrayList<>();
    }

    /**
     * 执行单个命令
     */
    public void execute(C context, String commandName) {
        Command<C> command = registry.get(commandName);
        if (command == null) {
            throw new IllegalArgumentException("Command not found: " + commandName);
        }
        executeCommand(context, command);
    }

    /**
     * 执行命令链（按顺序）
     */
    public void executeChain(C context, String... commandNames) {
        for (String name : commandNames) {
            if (context.isInterrupted()) {
                break;
            }
            execute(context, name);
        }
    }

    /**
     * 执行命令链（列表）
     */
    public void executeChain(C context, List<String> commandNames) {
        executeChain(context, commandNames.toArray(new String[0]));
    }

    /**
     * 执行命令
     */
    private void executeCommand(C context, Command<C> command) {
        notifyBeforeExecution(command, context);

        long startTime = System.currentTimeMillis();
        try {
            command.execute(context);
            context.addExecutedCommand(command);
            lastExecuted = command;

            long duration = System.currentTimeMillis() - startTime;
            notifyAfterExecution(command, context, duration, null);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            notifyOnError(command, context, e, duration);
            throw e;
        }
    }

    /**
     * 获取最后执行的命令
     */
    public Command<C> getLastExecuted() {
        return lastExecuted;
    }

    /**
     * 添加监听器
     */
    public CommandExecutor<C> addListener(CommandListener<C> listener) {
        if (listener != null) {
            listeners.add(listener);
        }
        return this;
    }

    /**
     * 移除监听器
     */
    public CommandExecutor<C> removeListener(CommandListener<C> listener) {
        listeners.remove(listener);
        return this;
    }

    private void notifyBeforeExecution(Command<C> command, C context) {
        for (CommandListener<C> listener : listeners) {
            try {
                listener.onBeforeExecute(command, context);
            } catch (Exception ignored) {
            }
        }
    }

    private void notifyAfterExecution(Command<C> command, C context, long duration, Exception error) {
        for (CommandListener<C> listener : listeners) {
            try {
                listener.onAfterExecute(command, context, duration, error);
            } catch (Exception ignored) {
            }
        }
    }

    private void notifyOnError(Command<C> command, C context, Exception error, long duration) {
        for (CommandListener<C> listener : listeners) {
            try {
                listener.onError(command, context, error, duration);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 获取注册表
     */
    public CommandRegistry<C> getRegistry() {
        return registry;
    }

    /**
     * 命令监听器接口
     */
    public interface CommandListener<C extends CommandContext> {
        default void onBeforeExecute(Command<C> command, C context) {
        }

        default void onAfterExecute(Command<C> command, C context, long duration, Exception error) {
        }

        default void onError(Command<C> command, C context, Exception error, long duration) {
        }
    }

    /**
     * 日志监听器
     */
    public static class LoggingListener<C extends CommandContext> implements CommandListener<C> {
        private final java.util.logging.Logger logger;

        /**
         * LoggingListener方法。
         */
        public LoggingListener() {
            this.logger = java.util.logging.Logger.getLogger(CommandExecutor.class.getName());
        }

        /**
         * LoggingListener方法。
         * * @param logger java.util.logging.Logger类型参数
         */
        public LoggingListener(java.util.logging.Logger logger) {
            this.logger = logger;
        }

        @Override
        /**
         * onBeforeExecute方法。
         *      * @param command CommandC类型参数
         * @param context C类型参数
         */
        public void onBeforeExecute(Command<C> command, C context) {
            logger.info("Executing command: " + command.getName());
        }

        @Override
        /**
         * onAfterExecute方法。
         *      * @param command CommandC类型参数
         * @param context C类型参数
         * @param duration long类型参数
         * @param error Exception类型参数
         */
        public void onAfterExecute(Command<C> command, C context, long duration, Exception error) {
            if (error != null) {
                logger.warning("Command '" + command.getName() + "' failed after " + duration + "ms: " + error.getMessage());
            } else {
                logger.info("Command '" + command.getName() + "' completed in " + duration + "ms");
            }
        }

        @Override
        /**
         * onError方法。
         *      * @param command CommandC类型参数
         * @param context C类型参数
         * @param error Exception类型参数
         * @param duration long类型参数
         */
        public void onError(Command<C> command, C context, Exception error, long duration) {
            logger.severe("Command '" + command.getName() + "' error after " + duration + "ms: " + error.getMessage());
        }
    }
}
