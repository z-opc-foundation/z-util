package com.zifang.util.cli.util;

import com.zifang.util.cli.model.Option;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Date;

/**
 * Utility to create Options from pattern strings.
 * <p>
 * Pattern format: [opt][longopt][:value-type][description]
 * </p>
 * <p>
 * Value types: STRING, NUMBER, DATE, FILE, URL, CLASS
 * </p>
 */
public class PatternOptionBuilder {

    public static final Class<String> STRING_VALUE = String.class;
    public static final Class<Number> NUMBER_VALUE = Number.class;
    public static final Class<Date> DATE_VALUE = Date.class;
    public static final Class<File> FILE_VALUE = File.class;
    public static final Class<URL> URL_VALUE = URL.class;
    public static final Class<URI> URI_VALUE = URI.class;
    public static final Class<?> CLASS_VALUE = Object.class;
    public static final Class<?> EXISTING_FILE_VALUE = File.class;
    public static final Class<?> FILES_VALUE = File.class;
    public static final Class<?> OBJECT_VALUE = Object.class;
    public static final Class<?> NUMBER_PATTERN_VALUE = PatternOptionBuilder.class;

    private static final String OPT_CHARS = "-";

    /**
     * Creates an Option using the pattern string.
     */
    public static Option createOption(final String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return null;
        }

        String opt = null;
        String longOpt = null;
        Class<?> type = null;
        String description = null;
        boolean hasArg = false;

        int pos = 0;
        int len = pattern.length();

        // Parse short option (single char not starting with -)
        if (pos < len && !OPT_CHARS.contains(String.valueOf(pattern.charAt(pos)))) {
            opt = String.valueOf(pattern.charAt(pos));
            pos++;
        }

        // Skip dashes
        while (pos < len && OPT_CHARS.contains(String.valueOf(pattern.charAt(pos)))) {
            pos++;
        }

        // Parse long option
        StringBuilder longOptBuf = new StringBuilder();
        while (pos < len && pattern.charAt(pos) != ':' && pattern.charAt(pos) != '%') {
            longOptBuf.append(pattern.charAt(pos));
            pos++;
        }
        if (longOptBuf.length() > 0) {
            longOpt = longOptBuf.toString();
        }

        // Parse type
        if (pos < len && pattern.charAt(pos) == '%') {
            hasArg = true;
            pos++;
            StringBuilder typeBuf = new StringBuilder();
            while (pos < len && pattern.charAt(pos) != ':') {
                typeBuf.append(pattern.charAt(pos));
                pos++;
            }
            type = parseType(typeBuf.toString());
        }

        // Parse description
        if (pos < len && pattern.charAt(pos) == ':') {
            pos++;
            description = pattern.substring(pos);
        }

        // Build option
        Option.Builder builder = Option.builder();
        if (opt != null) builder.opt(opt);
        if (longOpt != null) builder.longOpt(longOpt);
        if (hasArg) {
            builder.hasArg(type != null ? type.getSimpleName().toLowerCase() : "arg");
            if (type != null) {
                builder.description(description != null ? description : "Type: " + type.getSimpleName());
            }
        } else {
            builder.description(description != null ? description : "");
        }

        return builder.build();
    }

    private static Class<?> parseType(final String typeStr) {
        if (typeStr == null || typeStr.isEmpty()) {
            return String.class;
        }
        switch (typeStr.toUpperCase()) {
            case "STRING":
                return String.class;
            case "NUMBER":
            case "NUM":
                return Number.class;
            case "DATE":
                return Date.class;
            case "FILE":
                return File.class;
            case "URL":
                return URL.class;
            case "URI":
                return URI.class;
            case "CLASS":
            case "CLASSNAME":
                return Object.class;
            default:
                return String.class;
        }
    }

    /**
     * Creates Options from a pattern string array.
     */
    public static com.zifang.util.cli.model.Options parsePatterns(final String[] patterns) {
        com.zifang.util.cli.model.Options options = new com.zifang.util.cli.model.Options();
        for (String pattern : patterns) {
            Option opt = createOption(pattern);
            if (opt != null) {
                options.addOption(opt);
            }
        }
        return options;
    }
}
