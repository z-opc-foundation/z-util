package com.zifang.util.cli.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a group of options where only one option in the group may be selected.
 */
public class OptionGroup implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Option> options = new ArrayList<>();
    private String description;
    private boolean required = false;
    private Option selected;

    /**
     * OptionGroup方法。
     */
    public OptionGroup() {
    }

    /**
     * OptionGroup方法。
     * * @param description final类型参数
     */
    public OptionGroup(final String description) {
        this.description = description;
    }

    /**
     * addOption方法。
     * * @param option final类型参数
     *
     * @return OptionGroup类型返回值
     */
    public OptionGroup addOption(final Option option) {
        options.add(option);
        return this;
    }

    /**
     * getOptions方法。
     *
     * @return List<Option>类型返回值
     */
    public List<Option> getOptions() {
        return options;
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
     * setDescription方法。
     * * @param description final类型参数
     */
    public void setDescription(final String description) {
        this.description = description;
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
     * setRequired方法。
     * * @param required final类型参数
     */
    public void setRequired(final boolean required) {
        this.required = required;
    }

    /**
     * getSelected方法。
     *
     * @return Option类型返回值
     */
    public Option getSelected() {
        return selected;
    }

    /**
     * setSelected方法。
     * * @param option final类型参数
     */
    public void setSelected(final Option option) {
        this.selected = option;
    }

    /**
     * toString方法。
     *
     * @return String类型返回值
     */
    public String toString() {
        return options.toString();
    }
}
