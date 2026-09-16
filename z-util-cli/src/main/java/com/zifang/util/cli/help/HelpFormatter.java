package com.zifang.util.cli.help;

import com.zifang.util.cli.model.Option;
import com.zifang.util.cli.model.Options;

import java.io.PrintWriter;
import java.util.*;

/**
 * Formats help text for command-line options.
 * Supports custom left padding, description width, option headers, and footers.
 */
public class HelpFormatter {

    /**
     * Default output width
     */
    public static final int DEFAULT_WIDTH = 74;

    /**
     * Default left padding
     */
    public static final int DEFAULT_LEFT_PAD = 1;

    /**
     * Default description indentation
     */
    public static final int DEFAULT_DESC_INDENT = 3;

    /**
     * Default number of characters per line
     */
    public static final int DEFAULT_SYNTAX_INDENT = 3;

    /**
     * Default option prefix
     */
    public static final String DEFAULT_OPT_PREFIX = "-";

    /**
     * Default long option prefix
     */
    public static final String DEFAULT_LONG_OPT_PREFIX = "--";

    /**
     * Default argument name delimiter start
     */
    public static final String DEFAULT_ARG_NAME_OPEN = "<";

    /**
     * Default argument name delimiter end
     */
    public static final String DEFAULT_ARG_NAME_CLOSE = ">";

    /**
     * Default syntax prefix
     */
    public static final String DEFAULT_SYNTAX_PREFIX = "usage: ";

    /**
     * Default separator between option and argument
     */
    public static final String DEFAULT_SEPARATOR = " ";

    protected int width = DEFAULT_WIDTH;
    protected int leftPad = DEFAULT_LEFT_PAD;
    protected int descPad = DEFAULT_DESC_INDENT;
    protected int syntaxIndent = DEFAULT_SYNTAX_INDENT;
    protected String optPrefix = DEFAULT_OPT_PREFIX;
    protected String longOptPrefix = DEFAULT_LONG_OPT_PREFIX;
    protected String syntaxPrefix = DEFAULT_SYNTAX_PREFIX;
    protected String argNameOpen = DEFAULT_ARG_NAME_OPEN;
    protected String argNameClose = DEFAULT_ARG_NAME_CLOSE;
    protected String separator = DEFAULT_SEPARATOR;

    /**
     * HelpFormatter方法。
     */
    public HelpFormatter() {
    }

    /**
     * getWidth方法。
     *
     * @return int类型返回值
     */
    public int getWidth() {
        return width;
    }

    /**
     * setWidth方法。
     * * @param width final类型参数
     */
    public void setWidth(final int width) {
        this.width = width;
    }

    /**
     * getLeftPad方法。
     *
     * @return int类型返回值
     */
    public int getLeftPad() {
        return leftPad;
    }

    /**
     * setLeftPad方法。
     * * @param leftPad final类型参数
     */
    public void setLeftPad(final int leftPad) {
        this.leftPad = leftPad;
    }

    /**
     * getDescPad方法。
     *
     * @return int类型返回值
     */
    public int getDescPad() {
        return descPad;
    }

    /**
     * setDescPad方法。
     * * @param descPad final类型参数
     */
    public void setDescPad(final int descPad) {
        this.descPad = descPad;
    }

    /**
     * getSyntaxIndent方法。
     *
     * @return int类型返回值
     */
    public int getSyntaxIndent() {
        return syntaxIndent;
    }

    /**
     * setSyntaxIndent方法。
     * * @param syntaxIndent final类型参数
     */
    public void setSyntaxIndent(final int syntaxIndent) {
        this.syntaxIndent = syntaxIndent;
    }

    /**
     * getOptPrefix方法。
     *
     * @return String类型返回值
     */
    public String getOptPrefix() {
        return optPrefix;
    }

    /**
     * setOptPrefix方法。
     * * @param optPrefix final类型参数
     */
    public void setOptPrefix(final String optPrefix) {
        this.optPrefix = optPrefix;
    }

    /**
     * getLongOptPrefix方法。
     *
     * @return String类型返回值
     */
    public String getLongOptPrefix() {
        return longOptPrefix;
    }

    /**
     * setLongOptPrefix方法。
     * * @param longOptPrefix final类型参数
     */
    public void setLongOptPrefix(final String longOptPrefix) {
        this.longOptPrefix = longOptPrefix;
    }

    /**
     * getSyntaxPrefix方法。
     *
     * @return String类型返回值
     */
    public String getSyntaxPrefix() {
        return syntaxPrefix;
    }

    /**
     * setSyntaxPrefix方法。
     * * @param syntaxPrefix final类型参数
     */
    public void setSyntaxPrefix(final String syntaxPrefix) {
        this.syntaxPrefix = syntaxPrefix;
    }

    /**
     * getArgNameOpen方法。
     *
     * @return String类型返回值
     */
    public String getArgNameOpen() {
        return argNameOpen;
    }

    /**
     * setArgNameOpen方法。
     * * @param argNameOpen final类型参数
     */
    public void setArgNameOpen(final String argNameOpen) {
        this.argNameOpen = argNameOpen;
    }

    /**
     * getArgNameClose方法。
     *
     * @return String类型返回值
     */
    public String getArgNameClose() {
        return argNameClose;
    }

    /**
     * setArgNameClose方法。
     * * @param argNameClose final类型参数
     */
    public void setArgNameClose(final String argNameClose) {
        this.argNameClose = argNameClose;
    }

    /**
     * getSeparator方法。
     *
     * @return String类型返回值
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * setSeparator方法。
     * * @param separator final类型参数
     */
    public void setSeparator(final String separator) {
        this.separator = separator;
    }

    /**
     * Print help text to System.out.
     */
    public void printHelp(final String cmdLineSyntax, final Options options) {
        printHelp(width, cmdLineSyntax, null, options, null, false);
    }

    /**
     * Print help text to System.out with auto-generated usage.
     */
    public void printHelp(final String cmdLineSyntax, final Options options, final boolean autoUsage) {
        printHelp(width, cmdLineSyntax, null, options, null, autoUsage);
    }

    /**
     * Print help text to System.out with header and footer.
     */
    public void printHelp(final String cmdLineSyntax, final String header, final Options options, final String footer) {
        printHelp(width, cmdLineSyntax, header, options, footer, false);
    }

    /**
     * Print help text to System.out with header, footer, and auto usage.
     */
    public void printHelp(final String cmdLineSyntax, final String header, final Options options, final String footer, final boolean autoUsage) {
        printHelp(width, cmdLineSyntax, header, options, footer, autoUsage);
    }

    /**
     * Print help text with custom width.
     */
    public void printHelp(final int width, final String cmdLineSyntax, final String header, final Options options, final String footer, final boolean autoUsage) {
        PrintWriter pw = new PrintWriter(System.out);
        printHelp(pw, width, cmdLineSyntax, header, options, footer, autoUsage);
        pw.flush();
    }

    /**
     * Print help text to a PrintWriter.
     */
    public void printHelp(final PrintWriter pw, final int width, final String cmdLineSyntax,
                          final String header, final Options options, final String footer, final boolean autoUsage) {
        if (cmdLineSyntax == null || cmdLineSyntax.isEmpty()) {
            throw new IllegalArgumentException("cmdLineSyntax cannot be null or empty");
        }

        StringBuilder sb = new StringBuilder();

        // Syntax line
        sb.append(syntaxPrefix).append(cmdLineSyntax);
        if (autoUsage) {
            sb.append(" ").append(createSyntaxOptions(options));
        }
        pw.println(sb.toString());
        pw.println();

        // Header
        if (header != null && !header.isEmpty()) {
            printWrappedText(pw, header);
            pw.println();
        }

        // Options
        printOptions(pw, options);

        // Footer
        if (footer != null && !footer.isEmpty()) {
            pw.println();
            printWrappedText(pw, footer);
        }
    }

    /**
     * Print the options table.
     */
    public void printOptions(final PrintWriter pw, final Options options) {
        printOptions(pw, options, leftPad, descPad);
    }

    /**
     * Print the options table with custom padding.
     */
    public void printOptions(final PrintWriter pw, final Options options, final int leftPad, final int descPad) {
        String lpad = createPadding(leftPad);
        String dpad = createPadding(descPad);

        Collection<Option> opts = sortedOptions(options);

        // Calculate option and description widths
        int optionWidth = 0;
        for (Option opt : opts) {
            String optText = createOptionText(opt);
            optionWidth = Math.max(optionWidth, optText.length());
        }

        int descWidth = width - leftPad - optionWidth - descPad - 2;
        if (descWidth < 10) {
            descWidth = 20;
            optionWidth = width - leftPad - descPad - descWidth - 4;
        }

        // Print each option
        for (Option opt : opts) {
            StringBuilder sb = new StringBuilder(lpad);

            String optText = createOptionText(opt);
            sb.append(optText);

            if (optText.length() < optionWidth) {
                sb.append(createPadding(optionWidth - optText.length()));
            }

            sb.append(dpad);

            String desc = opt.getDescription();
            if (desc != null && !desc.isEmpty()) {
                List<String> descLines = wrapText(desc, descWidth);
                sb.append(descLines.get(0));
                for (int i = 1; i < descLines.size(); i++) {
                    pw.println(sb.toString());
                    sb = new StringBuilder(createPadding(leftPad + optionWidth + descPad));
                    sb.append(descLines.get(i));
                }
            }

            pw.println(sb.toString());
        }
    }

    /**
     * Create the text representation of an option.
     */
    public String createOptionText(final Option option) {
        StringBuilder sb = new StringBuilder();

        if (option.getOpt() != null) {
            sb.append(optPrefix).append(option.getOpt());
            if (option.hasArg()) {
                sb.append(createArgName(option));
            }
        }

        if (option.getOpt() != null && option.getLongOpt() != null) {
            sb.append(", ");
        } else if (option.getOpt() == null && option.getLongOpt() != null) {
            sb.append("  ");
        }

        if (option.getLongOpt() != null) {
            sb.append(longOptPrefix).append(option.getLongOpt());
            if (option.hasArg()) {
                sb.append(createArgName(option));
            }
        }

        return sb.toString();
    }

    /**
     * Create the argument name string for an option.
     */
    public String createArgName(final Option option) {
        String argName = option.getArgName();
        if (argName == null || argName.isEmpty()) {
            argName = "arg";
        }
        return separator + argNameOpen + argName + argNameClose;
    }

    /**
     * Create padding string of specified length.
     */
    public String createPadding(final int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * Wrap text to fit within specified width.
     */
    public List<String> wrapText(final String text, final int width) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            lines.add("");
            return lines;
        }

        int pos = 0;
        while (pos < text.length()) {
            int end = Math.min(pos + width, text.length());
            if (end < text.length()) {
                // Find last whitespace
                int lastSpace = -1;
                for (int i = pos; i < end; i++) {
                    if (Character.isWhitespace(text.charAt(i))) {
                        lastSpace = i;
                    }
                }
                if (lastSpace > pos) {
                    end = lastSpace;
                }
            }

            String line = text.substring(pos, end).trim();
            lines.add(line);
            pos = end;
            // Skip any whitespace
            while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
                pos++;
            }
        }

        return lines;
    }

    /**
     * Print wrapped text.
     */
    public void printWrappedText(final PrintWriter pw, final String text) {
        List<String> lines = wrapText(text, width);
        for (String line : lines) {
            pw.println(line);
        }
    }

    /**
     * Print wrapped text with leading padding.
     */
    public void printWrappedText(final PrintWriter pw, final int width, final String text) {
        List<String> lines = wrapText(text, width);
        for (String line : lines) {
            pw.println(line);
        }
    }

    /**
     * Create the syntax string for all options.
     */
    public String createSyntaxOptions(final Options options) {
        StringBuilder sb = new StringBuilder();
        Collection<Option> opts = sortedOptions(options);

        for (Option opt : opts) {
            if (sb.length() > 0) {
                sb.append(" ");
            }

            if (!opt.isRequired()) {
                sb.append("[");
            }

            sb.append(optPrefix);
            if (opt.getOpt() != null) {
                sb.append(opt.getOpt());
            } else {
                sb.append(longOptPrefix).append(opt.getLongOpt());
            }

            if (opt.hasArg()) {
                sb.append(" ").append(argNameOpen).append(opt.getArgName() != null ? opt.getArgName() : "arg").append(argNameClose);
            }

            if (!opt.isRequired()) {
                sb.append("]");
            }
        }

        return sb.toString();
    }

    /**
     * Sort options alphabetically.
     */
    private Collection<Option> sortedOptions(final Options options) {
        List<Option> opts = new ArrayList<>(options.getOptions());
        Collections.sort(opts, new Comparator<Option>() {
            @Override
            /**
             * compare方法。
             *      * @param o1 final类型参数
             * @param o2 final类型参数
             * @return int类型返回值
             */
            public int compare(final Option o1, final Option o2) {
                String k1 = o1.getKey();
                String k2 = o2.getKey();
                return k1.compareToIgnoreCase(k2);
            }
        });
        return opts;
    }

    /**
     * Print usage to System.out.
     */
    public void printUsage(final String cmdLineSyntax, final Options options) {
        printUsage(new PrintWriter(System.out), cmdLineSyntax, options);
    }

    /**
     * Print usage to a PrintWriter.
     */
    public void printUsage(final PrintWriter pw, final String cmdLineSyntax, final Options options) {
        StringBuilder sb = new StringBuilder(syntaxPrefix);
        sb.append(cmdLineSyntax);
        sb.append(" ");
        sb.append(createSyntaxOptions(options));
        pw.println(sb.toString());
    }
}
