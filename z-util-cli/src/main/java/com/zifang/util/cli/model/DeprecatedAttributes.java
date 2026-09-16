package com.zifang.util.cli.model;

import java.io.Serializable;

/**
 * Represents deprecation metadata for an option.
 */
public class DeprecatedAttributes implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String since;
    private final String description;
    private final boolean forRemoval;

    /**
     * DeprecatedAttributes方法。
     */
    public DeprecatedAttributes() {
        this(null, null);
    }

    /**
     * DeprecatedAttributes方法。
     * * @param since final类型参数
     *
     * @param description final类型参数
     */
    public DeprecatedAttributes(final String since, final String description) {
        this(since, description, false);
    }

    /**
     * DeprecatedAttributes方法。
     * * @param since final类型参数
     *
     * @param description final类型参数
     * @param forRemoval  final类型参数
     */
    public DeprecatedAttributes(final String since, final String description, final boolean forRemoval) {
        this.since = since;
        this.description = description;
        this.forRemoval = forRemoval;
    }

    /**
     * getSince方法。
     *
     * @return String类型返回值
     */
    public String getSince() {
        return since;
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
     * isForRemoval方法。
     *
     * @return boolean类型返回值
     */
    public boolean isForRemoval() {
        return forRemoval;
    }

    /**
     * isDeprecated方法。
     *
     * @return boolean类型返回值
     */
    public boolean isDeprecated() {
        return true;
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        StringBuilder sb = new StringBuilder("[Deprecated");
        if (forRemoval) {
            sb.append(" for removal");
        }
        if (since != null && !since.isEmpty()) {
            sb.append(" since ").append(since);
        }
        if (description != null && !description.isEmpty()) {
            sb.append(". ").append(description);
        }
        sb.append("]");
        return sb.toString();
    }
}
