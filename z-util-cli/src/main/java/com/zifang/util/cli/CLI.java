package com.zifang.util.cli;

import com.zifang.util.cli.help.HelpFormatter;
import com.zifang.util.cli.model.CommandLine;
import com.zifang.util.cli.model.Option;
import com.zifang.util.cli.model.OptionGroup;
import com.zifang.util.cli.model.Options;
import com.zifang.util.cli.parser.CommandLineParser;
import com.zifang.util.cli.parser.DefaultParser;

import java.util.Properties;

/**
 * Command-line interface utility providing a fluent API for building
 * options, parsing arguments, and formatting help text.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Options options = CLI.builder()
 *     .withOption(Option.builder("h").longOpt("help").hasArg(false).description("Display help").build())
 *     .withOption(Option.builder("f").longOpt("file").hasArg(true).argName("FILE").description("Input file").required(true).build())
 *     .build();
 *
 * CommandLineParser parser = new DefaultParser();
 * CommandLine cmd = parser.parse(options, args);
 *
 * if (cmd.hasOption("h")) {
 *     new HelpFormatter().printHelp("myapp", options);
 * }
 * }</pre>
 */
public final class CLI {

    private CLI() {
    }

    /**
     * Create a new Options builder.
     */
    public static OptionsBuilder builder() {
        return new OptionsBuilder();
    }

    /**
     * Create a new Options builder with an existing Options instance.
     */
    public static OptionsBuilder builder(final Options options) {
        return new OptionsBuilder(options);
    }

    /**
     * Create a new CommandLineParser.
     */
    public static CommandLineParser parser() {
        return new DefaultParser();
    }

    /**
     * Create a new HelpFormatter.
     */
    public static HelpFormatter helpFormatter() {
        return new HelpFormatter();
    }

    /**
     * Parse arguments with default options.
     */
    public static CommandLine parse(final Options options, final String[] args) throws com.zifang.util.cli.exception.ParseException {
        return parser().parse(options, args);
    }

    /**
     * Parse arguments with default options and properties.
     */
    public static CommandLine parse(final Options options, final String[] args, final Properties properties)
            throws com.zifang.util.cli.exception.ParseException {
        return parser().parse(options, args, properties);
    }

    /**
     * Fluent builder for Options.
     */
    public static class OptionsBuilder {
        private final Options options;

        /**
         * OptionsBuilder方法。
         */
        public OptionsBuilder() {
            this.options = new Options();
        }

        /**
         * OptionsBuilder方法。
         * * @param options final类型参数
         */
        public OptionsBuilder(final Options options) {
            this.options = options;
        }

        /**
         * withOption方法。
         * * @param option final类型参数
         *
         * @return OptionsBuilder类型返回值
         */
        public OptionsBuilder withOption(final Option option) {
            options.addOption(option);
            return this;
        }

        /**
         * withRequiredOption方法。
         * * @param option final类型参数
         *
         * @return OptionsBuilder类型返回值
         */
        public OptionsBuilder withRequiredOption(final Option option) {
            Option required = Option.builder()
                    .opt(option.getOpt())
                    .longOpt(option.getLongOpt())
                    .hasArg(option.hasArg())
                    .argName(option.getArgName())
                    .description(option.getDescription())
                    .required(true)
                    .build();
            options.addOption(required);
            return this;
        }

        /**
         * withOptionGroup方法。
         * * @param group final类型参数
         *
         * @return OptionsBuilder类型返回值
         */
        public OptionsBuilder withOptionGroup(final OptionGroup group) {
            options.addOptionGroup(group);
            return this;
        }

        /**
         * withOption方法。
         * * @param opt final类型参数
         *
         * @param hasArg      final类型参数
         * @param description final类型参数
         * @return OptionsBuilder类型返回值
         */
        public OptionsBuilder withOption(final String opt, final boolean hasArg, final String description) {
            options.addOption(opt, hasArg, description);
            return this;
        }

        /**
         * withOption方法。
         * * @param opt final类型参数
         *
         * @param longOpt     final类型参数
         * @param hasArg      final类型参数
         * @param description final类型参数
         * @return OptionsBuilder类型返回值
         */
        public OptionsBuilder withOption(final String opt, final String longOpt, final boolean hasArg, final String description) {
            options.addOption(opt, longOpt, hasArg, description);
            return this;
        }

        /**
         * build方法。
         *
         * @return Options类型返回值
         */
        public Options build() {
            return options;
        }
    }
}
