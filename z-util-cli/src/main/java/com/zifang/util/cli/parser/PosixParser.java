package com.zifang.util.cli.parser;

import com.zifang.util.cli.exception.AmbiguousOptionException;
import com.zifang.util.cli.exception.ParseException;
import com.zifang.util.cli.model.Option;
import com.zifang.util.cli.model.Options;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Parser for POSIX-style command-line arguments.
 *
 * @deprecated since 1.3, use {@link DefaultParser} instead.
 */
@Deprecated
/**
 * PosixParser类。
 */
public class PosixParser extends Parser {

    private final List<String> tokens = new ArrayList<>();
    private boolean eatTheRest;
    private Option currentOption;
    private Options options;

    /**
     * PosixParser方法。
     */
    public PosixParser() {
    }

    @Override
    /**
     * flatten方法。
     *      * @param options final类型参数
     * @param arguments final类型参数
     * @param stopAtNonOption final类型参数
     * @return String[]类型返回值
     */
    protected String[] flatten(final Options options, final String[] arguments, final boolean stopAtNonOption) throws ParseException {
        init();
        this.options = options;
        Iterator<String> iter = Arrays.asList(arguments).iterator();

        while (iter.hasNext()) {
            String token = iter.next();
            if (token == null) continue;

            if ("--".equals(token) || "-".equals(token)) {
                tokens.add(token);
            } else if (token.startsWith("--")) {
                handleLongOption(token, stopAtNonOption);
            } else if (token.startsWith("-")) {
                if (token.length() == 2 || options.hasOption(token)) {
                    handleShortOption(token, stopAtNonOption);
                } else if (!options.getMatchingOptions(token).isEmpty()) {
                    List<String> matchingOpts = options.getMatchingOptions(token);
                    if (matchingOpts.size() > 1) {
                        throw new AmbiguousOptionException(token, matchingOpts);
                    }
                    Option opt = options.getOption(matchingOpts.get(0));
                    handleShortOption("-" + opt.getLongOpt(), stopAtNonOption);
                } else {
                    burstToken(token, stopAtNonOption);
                }
            } else {
                processNonOptionToken(token, stopAtNonOption);
            }

            if (eatTheRest) {
                while (iter.hasNext()) {
                    String remaining = iter.next();
                    if (remaining != null) {
                        tokens.add(remaining);
                    }
                }
            }
        }

        return tokens.toArray(new String[0]);
    }

    private void init() {
        eatTheRest = false;
        tokens.clear();
        currentOption = null;
    }

    private void handleLongOption(final String token, final boolean stopAtNonOption) throws AmbiguousOptionException {
        String optPart = token.substring(2);
        int equalIndex = DefaultParser.indexOfEqual(optPart);
        String opt = equalIndex == -1 ? optPart : optPart.substring(0, equalIndex);
        String value = equalIndex == -1 ? null : optPart.substring(equalIndex + 1);

        List<String> matchingOpts = options.getMatchingOptions(opt);
        if (matchingOpts.isEmpty()) {
            processNonOptionToken(token, stopAtNonOption);
        } else if (matchingOpts.size() > 1) {
            throw new AmbiguousOptionException(opt, matchingOpts);
        } else {
            currentOption = options.getOption(matchingOpts.get(0));
            tokens.add("--" + currentOption.getLongOpt());
            if (value != null) {
                tokens.add(value);
            }
        }
    }

    private void handleShortOption(final String token, final boolean stopAtNonOption) {
        if (stopAtNonOption && !options.hasOption(token)) {
            eatTheRest = true;
        }
        if (options.hasOption(token)) {
            currentOption = options.getOption(token);
        }
        tokens.add(token);
    }

    private void burstToken(final String token, final boolean stopAtNonOption) {
        for (int i = 1; i < token.length(); i++) {
            String ch = String.valueOf(token.charAt(i));
            if (!options.hasOption(ch)) {
                if (stopAtNonOption) {
                    processNonOptionToken(token.substring(i), true);
                } else {
                    tokens.add(token);
                }
                break;
            }
            tokens.add("-" + ch);
            currentOption = options.getOption(ch);
            if (currentOption.hasArg() && token.length() != i + 1) {
                tokens.add(token.substring(i + 1));
                break;
            }
        }
    }

    private void processNonOptionToken(final String value, final boolean stopAtNonOption) {
        if (stopAtNonOption && (currentOption == null || !currentOption.hasArg())) {
            eatTheRest = true;
            tokens.add("--");
        }
        tokens.add(value);
    }
}
