package com.zifang.util.cli.model;

import java.io.Serializable;
import java.util.*;

/**
 * Represents a parsed command line. Contains all options and their values
 * found during parsing.
 */
public class CommandLine implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<String, Option> options = new HashMap<>();
    private final List<String> argList = new ArrayList<>();

    /**
     * CommandLine方法。
     */
    protected CommandLine() {
    }

    /**
     * builder方法。
     *
     * @return static Builder类型返回值
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * addOption方法。
     * * @param option final类型参数
     */
    public void addOption(final Option option) {
        options.put(option.getKey(), option);
    }

    // package-private for parser access

    /**
     * addArg方法。
     * * @param arg final类型参数
     */
    public void addArg(final String arg) {
        argList.add(arg);
    }

    // package-private for parser access

    /**
     * hasOption方法。
     * * @param opt final类型参数
     *
     * @return boolean类型返回值
     */
    public boolean hasOption(final String opt) {
        Option option = getOptionObject(opt);
        return option != null;
    }

    /**
     * hasOption方法。
     * * @param opt final类型参数
     *
     * @return boolean类型返回值
     */
    public boolean hasOption(final Option opt) {
        return options.containsKey(opt.getKey());
    }

    /**
     * getOptionObject方法。
     * * @param opt final类型参数
     *
     * @return Option类型返回值
     */
    public Option getOptionObject(final String opt) {
        return options.get(opt);
    }

    /**
     * getOptionValue方法。
     * * @param opt final类型参数
     *
     * @return String类型返回值
     */
    public String getOptionValue(final String opt) {
        Option option = getOptionObject(opt);
        return option != null ? option.getValue() : null;
    }

    /**
     * getOptionValue方法。
     * * @param opt final类型参数
     *
     * @param defaultValue final类型参数
     * @return String类型返回值
     */
    public String getOptionValue(final String opt, final String defaultValue) {
        String value = getOptionValue(opt);
        return value != null ? value : defaultValue;
    }

    /**
     * getOptionValue方法。
     * * @param option final类型参数
     *
     * @return String类型返回值
     */
    public String getOptionValue(final Option option) {
        return hasOption(option) ? option.getValue() : null;
    }

    /**
     * getOptionValue方法。
     * * @param option final类型参数
     *
     * @param defaultValue final类型参数
     * @return String类型返回值
     */
    public String getOptionValue(final Option option, final String defaultValue) {
        String value = getOptionValue(option);
        return value != null ? value : defaultValue;
    }

    /**
     * getOptionValues方法。
     * * @param opt final类型参数
     *
     * @return String[]类型返回值
     */
    public String[] getOptionValues(final String opt) {
        Option option = getOptionObject(opt);
        if (option == null) {
            return null;
        }
        List<String> values = option.getValues();
        return values.toArray(new String[0]);
    }

    /**
     * getArgList方法。
     *
     * @return List<String>类型返回值
     */
    public List<String> getArgList() {
        return argList;
    }

    /**
     * getArgs方法。
     *
     * @return String[]类型返回值
     */
    public String[] getArgs() {
        return argList.toArray(new String[0]);
    }

    /**
     * getOptions方法。
     *
     * @return Collection<Option>类型返回值
     */
    public Collection<Option> getOptions() {
        return options.values();
    }

    /**
     * hasOptions方法。
     *
     * @return boolean类型返回值
     */
    public boolean hasOptions() {
        return !options.isEmpty();
    }

    /**
     * hasArgs方法。
     *
     * @return boolean类型返回值
     */
    public boolean hasArgs() {
        return !argList.isEmpty();
    }

    public static class Builder {
        private final CommandLine cmd = new CommandLine();

        /**
         * get方法。
         *
         * @return CommandLine类型返回值
         */
        public CommandLine get() {
            return cmd;
        }
    }
}
