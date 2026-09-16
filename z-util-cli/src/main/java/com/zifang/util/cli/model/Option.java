package com.zifang.util.cli.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a command-line option. Supports short opt (-s), long opt (--long),
 * argument requirements, value conversion, and deprecation metadata.
 */
public class Option implements Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    private final String opt;
    private final String longOpt;
    private final boolean hasArg;
    private final String description;
    private final boolean required;
    private final String argName;
    private final boolean optionalArg;
    private final int maxArgs;
    private final List<String> values = new ArrayList<>();
    private DeprecatedAttributes deprecated;

    /**
     * Option方法。
     * * @param opt final类型参数
     *
     * @param hasArg      final类型参数
     * @param description final类型参数
     */
    protected Option(final String opt, final boolean hasArg, final String description) {
        this(opt, null, hasArg, null, description, false, false, 1, null);
    }

    /**
     * Option方法。
     * * @param opt final类型参数
     *
     * @param longOpt     final类型参数
     * @param hasArg      final类型参数
     * @param description final类型参数
     */
    protected Option(final String opt, final String longOpt, final boolean hasArg, final String description) {
        this(opt, longOpt, hasArg, null, description, false, false, 1, null);
    }

    /**
     * Option方法。
     * * @param opt final类型参数
     *
     * @param longOpt     final类型参数
     * @param hasArg      final类型参数
     * @param argName     final类型参数
     * @param description final类型参数
     * @param required    final类型参数
     * @param optionalArg final类型参数
     * @param maxArgs     final类型参数
     */
    protected Option(final String opt, final String longOpt, final boolean hasArg,
                     final String argName, final String description, final boolean required,
                     final boolean optionalArg, final int maxArgs) {
        this(opt, longOpt, hasArg, argName, description, required, optionalArg, maxArgs, null);
    }

    /**
     * Option方法。
     * * @param opt final类型参数
     *
     * @param longOpt     final类型参数
     * @param hasArg      final类型参数
     * @param argName     final类型参数
     * @param description final类型参数
     * @param required    final类型参数
     * @param optionalArg final类型参数
     * @param maxArgs     final类型参数
     * @param deprecated  final类型参数
     */
    protected Option(final String opt, final String longOpt, final boolean hasArg,
                     final String argName, final String description, final boolean required,
                     final boolean optionalArg, final int maxArgs, final DeprecatedAttributes deprecated) {
        if (opt == null && longOpt == null) {
            throw new IllegalArgumentException("opt and longOpt cannot both be null");
        }
        this.opt = opt;
        this.longOpt = longOpt;
        this.hasArg = hasArg;
        this.argName = argName;
        this.description = description;
        this.required = required;
        this.optionalArg = optionalArg;
        this.maxArgs = maxArgs;
        this.deprecated = deprecated;
    }

    // Builder pattern support

    /**
     * Option方法。
     * * @param builder final类型参数
     */
    protected Option(final Builder builder) {
        this(builder.opt, builder.longOpt, builder.hasArg, builder.argName,
                builder.description, builder.required, builder.optionalArg,
                builder.maxArgs, builder.deprecated);
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
     * getKey方法。
     *
     * @return String类型返回值
     */
    public String getKey() {
        return opt != null ? opt : longOpt;
    }

    /**
     * getOpt方法。
     *
     * @return String类型返回值
     */
    public String getOpt() {
        return opt;
    }

    /**
     * getLongOpt方法。
     *
     * @return String类型返回值
     */
    public String getLongOpt() {
        return longOpt;
    }

    /**
     * hasArg方法。
     *
     * @return boolean类型返回值
     */
    public boolean hasArg() {
        return hasArg;
    }

    /**
     * getDescription方法。
     *
     * @return String类型返回值
     */
    public String getDescription() {
        return description;
    }

    /**
     * isRequired方法。
     *
     * @return boolean类型返回值
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * getArgName方法。
     *
     * @return String类型返回值
     */
    public String getArgName() {
        return argName;
    }

    /**
     * hasOptionalArg方法。
     *
     * @return boolean类型返回值
     */
    public boolean hasOptionalArg() {
        return optionalArg;
    }

    /**
     * getMaxArgs方法。
     *
     * @return int类型返回值
     */
    public int getMaxArgs() {
        return maxArgs;
    }

    /**
     * getDeprecated方法。
     *
     * @return DeprecatedAttributes类型返回值
     */
    public DeprecatedAttributes getDeprecated() {
        return deprecated;
    }

    /**
     * isDeprecated方法。
     *
     * @return boolean类型返回值
     */
    public boolean isDeprecated() {
        return deprecated != null && deprecated.isDeprecated();
    }

    /**
     * setDeprecated方法。
     * * @param deprecated final类型参数
     */
    public void setDeprecated(final DeprecatedAttributes deprecated) {
        this.deprecated = deprecated;
    }

    /**
     * getSince方法。
     *
     * @return String类型返回值
     */
    public String getSince() {
        return deprecated != null ? deprecated.getSince() : null;
    }

    /**
     * getValues方法。
     *
     * @return List<String>类型返回值
     */
    public List<String> getValues() {
        return values;
    }

    /**
     * getValue方法。
     *
     * @return String类型返回值
     */
    public String getValue() {
        return values.isEmpty() ? null : values.get(0);
    }

    /**
     * getValue方法。
     * * @param index final类型参数
     *
     * @return String类型返回值
     */
    public String getValue(final int index) {
        return values.get(index);
    }

    /**
     * getValue方法。
     * * @param defaultValue final类型参数
     *
     * @return String类型返回值
     */
    public String getValue(final String defaultValue) {
        return values.isEmpty() ? defaultValue : values.get(0);
    }

    /**
     * hasValue方法。
     *
     * @return boolean类型返回值
     */
    public boolean hasValue() {
        return !values.isEmpty();
    }

    /**
     * addValueForProcessing方法。
     * * @param value final类型参数
     */
    public void addValueForProcessing(final String value) {
        if (!hasArg) {
            throw new IllegalStateException("Cannot add value to non-argument option");
        }
        values.add(value);
    }

    /**
     * processValue方法。
     * * @param value final类型参数
     */
    public void processValue(final String value) {
        addValueForProcessing(value);
    }

    /**
     * isValuesEmpty方法。
     *
     * @return boolean类型返回值
     */
    public boolean isValuesEmpty() {
        return values.isEmpty();
    }

    /**
     * clearValues方法。
     */
    public void clearValues() {
        values.clear();
    }

    /**
     * acceptsArg方法。
     *
     * @return boolean类型返回值
     */
    public boolean acceptsArg() {
        return hasArg && (values.size() < maxArgs || maxArgs < 1);
    }

    @Override
    /**
     * clone方法。
     * @return Object类型返回值
     */
    public Object clone() {
        try {
            Option copy = (Option) super.clone();
            copy.values.clear();
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.getMessage());
        }
    }

    /**
     * toString方法。
     *
     * @return String类型返回值
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (opt != null) {
            sb.append("-").append(opt);
        }
        if (longOpt != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append("--").append(longOpt);
        }
        if (hasArg) {
            sb.append(" <").append(argName != null ? argName : "arg").append(">");
        }
        return sb.toString();
    }

    public static class Builder {
        private String opt;
        private String longOpt;
        private boolean hasArg = false;
        private String argName;
        private String description;
        private boolean required = false;
        private boolean optionalArg = false;
        private int maxArgs = 1;
        private DeprecatedAttributes deprecated;

        /**
         * Builder方法。
         */
        public Builder() {
        }

        /**
         * opt方法。
         * * @param opt final类型参数
         *
         * @return Builder类型返回值
         */
        public Builder opt(final String opt) {
            this.opt = opt;
            return this;
        }

        /**
         * longOpt方法。
         * * @param longOpt final类型参数
         *
         * @return Builder类型返回值
         */
        public Builder longOpt(final String longOpt) {
            this.longOpt = longOpt;
            return this;
        }

        /**
         * hasArg方法。
         * * @param hasArg final类型参数
         *
         * @return Builder类型返回值
         */
        public Builder hasArg(final boolean hasArg) {
            this.hasArg = hasArg;
            return this;
        }

        /**
         * hasArg方法。
         * * @param argName final类型参数
         *
         * @return Builder类型返回值
         */
        public Builder hasArg(final String argName) {
            this.hasArg = true;
            this.argName = argName;
            return this;
        }

        /**
         * argName方法。
         * * @param argName final类型参数
         *
         * @return Builder类型返回值
         */
        public Builder argName(final String argName) {
            this.argName = argName;
            return this;
        }

        /**
         * description方法。
         * * @param description final类型参数
         *
         * @return Builder类型返回值
         */
        public Builder description(final String description) {
            this.description = description;
            return this;
        }

        /**
         * required方法。
         * * @param required final类型参数
         *
         * @return Builder类型返回值
         */
        public Builder required(final boolean required) {
            this.required = required;
            return this;
        }

        /**
         * optionalArg方法。
         * * @param optionalArg final类型参数
         *
         * @return Builder类型返回值
         */
        public Builder optionalArg(final boolean optionalArg) {
            this.optionalArg = optionalArg;
            return this;
        }

        /**
         * maxArgs方法。
         * * @param maxArgs final类型参数
         *
         * @return Builder类型返回值
         */
        public Builder maxArgs(final int maxArgs) {
            this.maxArgs = maxArgs;
            return this;
        }

        /**
         * deprecated方法。
         * * @param deprecated final类型参数
         *
         * @return Builder类型返回值
         */
        public Builder deprecated(final DeprecatedAttributes deprecated) {
            this.deprecated = deprecated;
            return this;
        }

        /**
         * deprecated方法。
         * * @param since final类型参数
         *
         * @param description final类型参数
         * @return Builder类型返回值
         */
        public Builder deprecated(final String since, final String description) {
            this.deprecated = new DeprecatedAttributes(since, description);
            return this;
        }

        /**
         * build方法。
         *
         * @return Option类型返回值
         */
        public Option build() {
            if (opt == null && longOpt == null) {
                throw new IllegalArgumentException("opt or longOpt must be specified");
            }
            return new Option(this);
        }
    }
}
