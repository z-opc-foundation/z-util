package com.zifang.util.cli.parser;

import com.zifang.util.cli.exception.ParseException;
import com.zifang.util.cli.model.CommandLine;
import com.zifang.util.cli.model.Option;
import com.zifang.util.cli.model.OptionGroup;
import com.zifang.util.cli.model.Options;

import java.util.Properties;

/**
 * Abstract base parser providing common parsing framework.
 *
 * @deprecated since 1.3, use {@link DefaultParser} instead.
 */
@Deprecated
public abstract class Parser implements CommandLineParser {

    protected CommandLine cmd;
    private Options options;
    private java.util.List requiredOptions;

    /**
     * Parser方法。
     */
    protected Parser() {
    }

    /**
     * checkRequiredOptions方法。
     */
    protected void checkRequiredOptions() throws com.zifang.util.cli.exception.MissingOptionException {
        if (!getRequiredOptions().isEmpty()) {
            throw new com.zifang.util.cli.exception.MissingOptionException(getRequiredOptions());
        }
    }

    /**
     * flatten方法。
     * * @param opts Options类型参数
     *
     * @param arguments       String[]类型参数
     * @param stopAtNonOption boolean类型参数
     * @return abstract String[]类型返回值
     */
    protected abstract String[] flatten(Options opts, String[] arguments, boolean stopAtNonOption) throws ParseException;

    /**
     * getOptions方法。
     *
     * @return Options类型返回值
     */
    protected Options getOptions() {
        return options;
    }

    /**
     * setOptions方法。
     * * @param options final类型参数
     */
    protected void setOptions(final Options options) {
        this.options = options;
        this.requiredOptions = new java.util.ArrayList<>(options.getRequiredOptions());
    }

    /**
     * getRequiredOptions方法。
     *
     * @return java.util.List类型返回值
     */
    protected java.util.List getRequiredOptions() {
        return requiredOptions;
    }

    @Override
    /**
     * parse方法。
     *      * @param options final类型参数
     * @param arguments final类型参数
     * @return CommandLine类型返回值
     */
    public CommandLine parse(final Options options, final String[] arguments) throws ParseException {
        return parse(options, arguments, null, false);
    }

    @Override
    /**
     * parse方法。
     *      * @param options final类型参数
     * @param arguments final类型参数
     * @param stopAtNonOption final类型参数
     * @return CommandLine类型返回值
     */
    public CommandLine parse(final Options options, final String[] arguments, final boolean stopAtNonOption) throws ParseException {
        return parse(options, arguments, null, stopAtNonOption);
    }

    @Override
    /**
     * parse方法。
     *      * @param options final类型参数
     * @param arguments final类型参数
     * @param properties final类型参数
     * @return CommandLine类型返回值
     */
    public CommandLine parse(final Options options, final String[] arguments, final Properties properties) throws ParseException {
        return parse(options, arguments, properties, false);
    }

    @Override
    /**
     * parse方法。
     *      * @param options final类型参数
     * @param arguments final类型参数
     * @param properties final类型参数
     * @param stopAtNonOption final类型参数
     * @return CommandLine类型返回值
     */
    public CommandLine parse(final Options options, final String[] arguments, final Properties properties, final boolean stopAtNonOption)
            throws ParseException {
        options.helpOptions().forEach(opt -> opt.clearValues());
        for (OptionGroup group : options.getOptionGroups()) {
            group.setSelected(null);
        }

        setOptions(options);
        cmd = CommandLine.builder().get();

        String[] flatArgs = flatten(getOptions(), arguments == null ? new String[0] : arguments, stopAtNonOption);
        java.util.List<String> tokenList = java.util.Arrays.asList(flatArgs);
        java.util.ListIterator<String> iterator = tokenList.listIterator();

        boolean eatTheRest = false;
        while (iterator.hasNext()) {
            String token = iterator.next();
            if (token == null) continue;

            if ("--".equals(token)) {
                eatTheRest = true;
            } else if ("-".equals(token)) {
                if (stopAtNonOption) {
                    eatTheRest = true;
                } else {
                    cmd.addArg(token);
                }
            } else if (token.startsWith("--")) {
                processOption(token, iterator);
            } else if (token.startsWith("-")) {
                processOption(token, iterator);
            } else {
                cmd.addArg(token);
                if (stopAtNonOption) {
                    eatTheRest = true;
                }
            }

            if (eatTheRest) {
                while (iterator.hasNext()) {
                    String remaining = iterator.next();
                    if (remaining != null && !".".equals(remaining)) {
                        cmd.addArg(remaining);
                    }
                }
            }
        }

        processProperties(properties);
        checkRequiredOptions();
        return cmd;
    }

    /**
     * processOption方法。
     * * @param arg final类型参数
     *
     * @param iter final类型参数
     */
    protected void processOption(final String arg, final java.util.ListIterator<String> iter) throws ParseException {
        if (!getOptions().hasOption(arg)) {
            throw new com.zifang.util.cli.exception.UnrecognizedOptionException("Unrecognized option: " + arg, arg);
        }

        Option opt = getOptions().getOption(arg);
        if (opt.isRequired()) {
            getRequiredOptions().remove(opt.getKey());
        }

        OptionGroup group = getOptions().getOptionGroup(opt);
        if (group != null) {
            if (group.getSelected() != null) {
                throw new com.zifang.util.cli.exception.AlreadySelectedException(group, opt);
            }
            group.setSelected(opt);
        }

        if (opt.hasArg()) {
            processArgs(opt, iter);
        }

        cmd.addOption(opt);
    }

    /**
     * processArgs方法。
     * * @param opt final类型参数
     *
     * @param iter final类型参数
     */
    public void processArgs(final Option opt, final java.util.ListIterator<String> iter) throws ParseException {
        while (iter.hasNext()) {
            String str = iter.next();
            if (getOptions().hasOption(str) && str.startsWith("-")) {
                iter.previous();
                break;
            }
            try {
                opt.processValue(str);
            } catch (RuntimeException exp) {
                iter.previous();
                break;
            }
        }
        if (opt.isValuesEmpty() && !opt.hasOptionalArg()) {
            throw new com.zifang.util.cli.exception.MissingArgumentException(opt);
        }
    }

    /**
     * processProperties方法。
     * * @param properties final类型参数
     */
    protected void processProperties(final Properties properties) throws ParseException {
        if (properties == null) return;
        for (java.util.Enumeration<?> e = properties.propertyNames(); e.hasMoreElements(); ) {
            String option = e.nextElement().toString();
            Option opt = options.getOption(option);
            if (opt == null) {
                throw new com.zifang.util.cli.exception.UnrecognizedOptionException("Default option wasn't defined", option);
            }
            OptionGroup optionGroup = options.getOptionGroup(opt);
            boolean selected = optionGroup != null && optionGroup.getSelected() != null;
            if (!cmd.hasOption(option) && !selected) {
                String value = properties.getProperty(option);
                if (opt.hasArg()) {
                    if (opt.isValuesEmpty()) {
                        try {
                            opt.processValue(value);
                        } catch (RuntimeException exp) {
                        }
                    }
                } else if (!"yes".equalsIgnoreCase(value) && !"true".equalsIgnoreCase(value) && !"1".equalsIgnoreCase(value)) {
                    continue;
                }
                cmd.addOption(opt);
                updateRequiredOptions(opt);
            }
        }
    }

    private void updateRequiredOptions(final Option opt) {
        if (opt.isRequired()) {
            getRequiredOptions().remove(opt.getKey());
        }
        OptionGroup group = getOptions().getOptionGroup(opt);
        if (group != null && group.isRequired()) {
            getRequiredOptions().remove(group);
        }
    }
}
