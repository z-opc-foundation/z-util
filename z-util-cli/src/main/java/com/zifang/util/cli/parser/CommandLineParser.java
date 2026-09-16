package com.zifang.util.cli.parser;

import com.zifang.util.cli.exception.ParseException;
import com.zifang.util.cli.model.CommandLine;
import com.zifang.util.cli.model.Options;

import java.util.Properties;

/**
 * Interface for command-line parsers.
 */
public interface CommandLineParser {

    /**
     * Parse the command-line arguments.
     *
     * @param options   the Options to parse
     * @param arguments the command-line arguments
     * @return the parsed CommandLine
     * @throws ParseException if parsing fails
     */
    CommandLine parse(Options options, String[] arguments) throws ParseException;

    /**
     * Parse the command-line arguments with stop-at-non-option behavior.
     *
     * @param options         the Options to parse
     * @param arguments       the command-line arguments
     * @param stopAtNonOption if true, stop parsing at first non-option argument
     * @return the parsed CommandLine
     * @throws ParseException if parsing fails
     */
    CommandLine parse(Options options, String[] arguments, boolean stopAtNonOption) throws ParseException;

    /**
     * Parse the command-line arguments with default properties.
     *
     * @param options    the Options to parse
     * @param arguments  the command-line arguments
     * @param properties default option values
     * @return the parsed CommandLine
     * @throws ParseException if parsing fails
     */
    CommandLine parse(Options options, String[] arguments, Properties properties) throws ParseException;

    /**
     * Parse the command-line arguments with all options.
     *
     * @param options         the Options to parse
     * @param arguments       the command-line arguments
     * @param properties      default option values
     * @param stopAtNonOption if true, stop parsing at first non-option argument
     * @return the parsed CommandLine
     * @throws ParseException if parsing fails
     */
    CommandLine parse(Options options, String[] arguments, Properties properties, boolean stopAtNonOption) throws ParseException;
}
