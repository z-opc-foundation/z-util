package com.zifang.util.cli.parser;

import com.zifang.util.cli.exception.*;
import com.zifang.util.cli.model.CommandLine;
import com.zifang.util.cli.model.Option;
import com.zifang.util.cli.model.OptionGroup;
import com.zifang.util.cli.model.Options;

import java.util.*;

/**
 * Default parser implementation for command-line arguments.
 * Supports both short (-s) and long (--long) options, option groups,
 * required options, and argument value parsing.
 */
public class DefaultParser implements CommandLineParser {

    /**
     * Start of current option token (e.g., "-" or "--")
     */
    private static final String OPT_PREFIX = "-";
    private static final String LONG_OPT_PREFIX = "--";
    /**
     * Current Options
     */
    private Options options;
    /**
     * The parsed CommandLine
     */
    private CommandLine cmd;
    /**
     * List of required options
     */
    private List<Option> requiredOptions;
    /**
     * Current option being processed
     */
    private Option currentOption;
    /**
     * Whether to stop at non-option argument
     */
    private boolean stopAtNonOption;
    /**
     * Token list during parsing
     */
    private List<String> tokens = new ArrayList<>();
    /**
     * State: currently parsing option vs argument
     */
    private boolean eatTheRest;
    /**
     * The current token index
     */
    private int currentTokenIndex;

    /**
     * Find the index of '=' in the token (for --foo=value parsing).
     */
    static int indexOfEqual(final String token) {
        for (int i = 0; i < token.length(); i++) {
            if (token.charAt(i) == '=') {
                return i;
            }
        }
        return -1;
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
        // reset state
        options.helpOptions().forEach(Option::clearValues);
        for (OptionGroup optionGroup : options.getOptionGroups()) {
            optionGroup.setSelected(null);
        }

        this.options = options;
        this.cmd = CommandLine.builder().get();
        this.stopAtNonOption = stopAtNonOption;
        this.requiredOptions = new ArrayList<>(options.getRequiredOptions());
        this.eatTheRest = false;
        this.tokens.clear();

        String[] argumentsToParse = arguments == null ? new String[0] : arguments;

        // Flatten the arguments
        String[] flatArgs = flatten(argumentsToParse, stopAtNonOption);

        // Process flattened tokens
        ListIterator<String> iterator = Arrays.asList(flatArgs).listIterator();
        while (iterator.hasNext()) {
            String token = iterator.next();
            if (token == null) {
                continue;
            }

            if (LONG_OPT_PREFIX.equals(token)) {
                // "--" forces end of options
                eatTheRest = true;
            } else if (OPT_PREFIX.equals(token)) {
                if (stopAtNonOption) {
                    eatTheRest = true;
                } else {
                    cmd.addArg(token);
                }
            } else if (token.startsWith(LONG_OPT_PREFIX)) {
                // Long option: --foo or --foo=bar
                processLongOption(token.substring(2), iterator);
            } else if (token.startsWith(OPT_PREFIX)) {
                // Short option: -f or -fvalue
                processShortOption(token.substring(1), iterator);
            } else {
                // Regular argument
                cmd.addArg(token);
                if (stopAtNonOption) {
                    eatTheRest = true;
                }
            }

            if (eatTheRest) {
                while (iterator.hasNext()) {
                    String remaining = iterator.next();
                    if (remaining != null && !LONG_OPT_PREFIX.equals(remaining)) {
                        cmd.addArg(remaining);
                    }
                }
            }
        }

        // Process properties (default values)
        processProperties(properties);

        // Check required options
        checkRequiredOptions();

        return cmd;
    }

    /**
     * Flattens the arguments array, splitting combined short options like "-abc" into "-a -b -c".
     */
    protected String[] flatten(final String[] arguments, final boolean stopAtNonOption) throws ParseException {
        List<String> result = new ArrayList<>();
        eatTheRest = false;

        for (int i = 0; i < arguments.length; i++) {
            String arg = arguments[i];

            if (arg == null) {
                continue;
            }

            if (LONG_OPT_PREFIX.equals(arg)) {
                eatTheRest = true;
                result.add(arg);
            } else if (OPT_PREFIX.equals(arg)) {
                result.add(arg);
            } else if (arg.startsWith(LONG_OPT_PREFIX)) {
                // Long option
                String longOpt = arg.substring(2);
                int equalIndex = indexOfEqual(longOpt);

                if (equalIndex != -1) {
                    // --foo=value
                    String opt = longOpt.substring(0, equalIndex);
                    String value = longOpt.substring(equalIndex + 1);
                    List<String> matchingOpts = options.getMatchingOptions(opt);
                    if (matchingOpts.isEmpty()) {
                        result.add(arg);
                    } else if (matchingOpts.size() > 1) {
                        throw new AmbiguousOptionException(opt, matchingOpts);
                    } else {
                        result.add(LONG_OPT_PREFIX + matchingOpts.get(0));
                        result.add(value);
                    }
                } else {
                    // --foo
                    result.add(arg);
                }
            } else if (arg.startsWith(OPT_PREFIX) && arg.length() > 2) {
                // Short option cluster: -abc
                // Extract the first char as candidate option
                String cluster = arg.substring(1);
                String firstChar = String.valueOf(cluster.charAt(0));
                Option firstOpt = options.hasShortOption(firstChar) ? options.getOption(firstChar) : null;

                if (firstOpt != null && firstOpt.hasArg()) {
                    // -fvalue: first option takes an argument, rest is the value
                    result.add(OPT_PREFIX + firstChar);
                    result.add(cluster.substring(1));
                } else {
                    // Burst into individual short options
                    for (int j = 0; j < cluster.length(); j++) {
                        String ch = String.valueOf(cluster.charAt(j));
                        if (!options.hasShortOption(ch)) {
                            // Unknown option: treat remainder as positional args and stop
                            result.add(arg.substring(j));
                            break;
                        }
                        result.add(OPT_PREFIX + ch);
                        Option shortOpt = options.getOption(ch);
                        if (shortOpt.hasArg() && j + 1 < cluster.length()) {
                            result.add(cluster.substring(j + 1));
                            break;
                        }
                    }
                }
            } else {
                result.add(arg);
            }
        }

        return result.toArray(new String[0]);
    }

    private void processLongOption(final String token, final ListIterator<String> iterator) throws ParseException {
        int equalIndex = indexOfEqual(token);
        String opt = equalIndex != -1 ? token.substring(0, equalIndex) : token;
        String value = equalIndex != -1 ? token.substring(equalIndex + 1) : null;

        List<String> matchingOpts = options.getMatchingOptions(opt);
        if (matchingOpts.isEmpty()) {
            throw new UnrecognizedOptionException("Unrecognized option: --" + opt, "--" + opt);
        } else if (matchingOpts.size() > 1) {
            throw new AmbiguousOptionException("--" + opt, matchingOpts);
        }

        Option option = options.getOption(matchingOpts.get(0));
        processOption(option, iterator, value);
    }

    private void processShortOption(final String token, final ListIterator<String> iterator) throws ParseException {
        String opt = token.length() > 1 ? String.valueOf(token.charAt(0)) : token;

        if (!options.hasShortOption(opt)) {
            // Treat unknown short option cluster (e.g., "-ab") as positional args
            // if stopAtNonOption, otherwise throw
            if (stopAtNonOption) {
                cmd.addArg(OPT_PREFIX + token);
                return;
            }
            throw new UnrecognizedOptionException("Unrecognized option: -" + token, "-" + token);
        }

        Option option = options.getOption(opt);
        String value = null;

        if (option.hasArg() && token.length() > 1) {
            // -fvalue
            value = token.substring(1);
        }

        processOption(option, iterator, value);
    }

    private void processOption(final Option option, final ListIterator<String> iterator, String value) throws ParseException {
        // Update required options tracking
        if (option.isRequired()) {
            requiredOptions.remove(option);
        }

        // Update option group
        OptionGroup group = options.getOptionGroup(option);
        if (group != null) {
            if (group.getSelected() != null) {
                throw new AlreadySelectedException(group, option);
            }
            group.setSelected(option);
            if (group.isRequired()) {
                for (Option groupOpt : group.getOptions()) {
                    requiredOptions.remove(groupOpt);
                }
            }
        }

        // Handle argument value
        if (option.hasArg()) {
            if (value == null) {
                // Look for next token as value
                if (iterator.hasNext()) {
                    String next = iterator.next();
                    if (!next.startsWith(OPT_PREFIX) || LONG_OPT_PREFIX.equals(next) || OPT_PREFIX.equals(next)) {
                        if (options.hasOption(next) && options.getOption(next).hasArg()) {
                            // Next token is another option, not a value
                            iterator.previous();
                        } else {
                            value = next;
                        }
                    } else if (next.startsWith(LONG_OPT_PREFIX) || next.startsWith(OPT_PREFIX)) {
                        // Could be a short option cluster or long option
                        String shortOpt = next.startsWith(LONG_OPT_PREFIX) ? next.substring(2) : next.substring(1);
                        if (shortOpt.isEmpty() || !options.hasOption(next)) {
                            iterator.previous();
                        } else {
                            Option nextOption = options.getOption(next.startsWith(LONG_OPT_PREFIX) ?
                                    options.getMatchingOptions(shortOpt).get(0) : String.valueOf(shortOpt.charAt(0)));
                            if (nextOption != null && nextOption.hasArg()) {
                                iterator.previous();
                            } else {
                                value = next;
                            }
                        }
                    } else {
                        value = next;
                    }
                }
            }

            if (value == null && !option.hasOptionalArg()) {
                throw new MissingArgumentException(option);
            }
        }

        // Add value to option
        if (value != null) {
            option.addValueForProcessing(value);
        }

        cmd.addOption(option);
    }

    private void processProperties(final Properties properties) throws ParseException {
        if (properties == null) {
            return;
        }

        for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements(); ) {
            String optionStr = e.nextElement().toString();
            Option opt = options.getOption(optionStr);
            if (opt == null) {
                throw new UnrecognizedOptionException("Default option wasn't defined", optionStr);
            }

            OptionGroup optionGroup = options.getOptionGroup(opt);
            boolean selected = optionGroup != null && optionGroup.getSelected() != null;

            if (!cmd.hasOption(optionStr) && !selected) {
                String value = properties.getProperty(optionStr);
                if (opt.hasArg()) {
                    if (opt.isValuesEmpty()) {
                        try {
                            opt.processValue(value);
                        } catch (RuntimeException exp) {
                            // Ignore
                        }
                    }
                } else {
                    if (!"yes".equalsIgnoreCase(value) && !"true".equalsIgnoreCase(value) && !"1".equalsIgnoreCase(value)) {
                        continue;
                    }
                }
                cmd.addOption(opt);
                updateRequiredOptions(opt);
            }
        }
    }

    private void updateRequiredOptions(final Option opt) {
        if (opt.isRequired()) {
            requiredOptions.remove(opt);
        }
        OptionGroup group = options.getOptionGroup(opt);
        if (group != null && group.isRequired()) {
            for (Option groupOpt : group.getOptions()) {
                requiredOptions.remove(groupOpt);
            }
        }
    }

    private void checkRequiredOptions() throws MissingOptionException {
        if (!requiredOptions.isEmpty()) {
            throw new MissingOptionException(requiredOptions);
        }
    }
}
