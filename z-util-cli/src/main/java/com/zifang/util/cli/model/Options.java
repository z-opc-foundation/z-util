package com.zifang.util.cli.model;

import java.io.Serializable;
import java.util.*;

/**
 * Main container for all command-line options.
 */
public class Options implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<String, Option> shortOpts = new HashMap<>();
    private final Map<String, Option> longOpts = new HashMap<>();
    private final Map<Option, OptionGroup> optionGroups = new HashMap<>();
    private final List<OptionGroup> optionGroupsList = new ArrayList<>();
    private final List<Option> requiredOptions = new ArrayList<>();

    /**
     * Options方法。
     */
    public Options() {
    }

    /**
     * addOption方法。
     * * @param option final类型参数
     *
     * @return Options类型返回值
     */
    public Options addOption(final Option option) {
        String key = option.getKey();
        if (option.getOpt() != null) {
            shortOpts.put(option.getOpt(), option);
        }
        if (option.getLongOpt() != null) {
            longOpts.put(option.getLongOpt(), option);
        }
        if (option.isRequired()) {
            requiredOptions.add(option);
        }
        return this;
    }

    /**
     * addOption方法。
     * * @param opt final类型参数
     *
     * @param hasArg      final类型参数
     * @param description final类型参数
     * @return Options类型返回值
     */
    public Options addOption(final String opt, final boolean hasArg, final String description) {
        return addOption(new Option(opt, hasArg, description));
    }

    /**
     * addOption方法。
     * * @param opt final类型参数
     *
     * @param longOpt     final类型参数
     * @param hasArg      final类型参数
     * @param description final类型参数
     * @return Options类型返回值
     */
    public Options addOption(final String opt, final String longOpt, final boolean hasArg, final String description) {
        return addOption(new Option(opt, longOpt, hasArg, description));
    }

    /**
     * addRequiredOption方法。
     * * @param opt final类型参数
     *
     * @param longOpt     final类型参数
     * @param hasArg      final类型参数
     * @param description final类型参数
     * @return Options类型返回值
     */
    public Options addRequiredOption(final String opt, final String longOpt, final boolean hasArg, final String description) {
        Option option = Option.builder()
                .opt(opt)
                .longOpt(longOpt)
                .hasArg(hasArg)
                .description(description)
                .required(true)
                .build();
        return addOption(option);
    }

    /**
     * addOptionGroup方法。
     * * @param group final类型参数
     *
     * @return Options类型返回值
     */
    public Options addOptionGroup(final OptionGroup group) {
        for (Option option : group.getOptions()) {
            optionGroups.put(option, group);
            addOption(option);
        }
        optionGroupsList.add(group);
        if (group.isRequired()) {
            requiredOptions.addAll(group.getOptions());
        }
        return this;
    }

    /**
     * getOption方法。
     * * @param opt final类型参数
     *
     * @return Option类型返回值
     */
    public Option getOption(final String opt) {
        Option option = shortOpts.get(opt);
        if (option == null) {
            option = longOpts.get(opt);
        }
        return option;
    }

    /**
     * hasOption方法。
     * * @param opt final类型参数
     *
     * @return boolean类型返回值
     */
    public boolean hasOption(final String opt) {
        return shortOpts.containsKey(opt) || longOpts.containsKey(opt);
    }

    /**
     * hasShortOption方法。
     * * @param opt final类型参数
     *
     * @return boolean类型返回值
     */
    public boolean hasShortOption(final String opt) {
        return shortOpts.containsKey(opt);
    }

    /**
     * hasLongOption方法。
     * * @param opt final类型参数
     *
     * @return boolean类型返回值
     */
    public boolean hasLongOption(final String opt) {
        return longOpts.containsKey(opt);
    }

    /**
     * getOptionGroup方法。
     * * @param option final类型参数
     *
     * @return OptionGroup类型返回值
     */
    public OptionGroup getOptionGroup(final Option option) {
        return optionGroups.get(option);
    }

    /**
     * getOptions方法。
     *
     * @return Collection<Option>类型返回值
     */
    public Collection<Option> getOptions() {
        List<Option> opts = new ArrayList<>(shortOpts.values());
        return Collections.unmodifiableCollection(opts);
    }

    /**
     * getOptionGroups方法。
     *
     * @return List<OptionGroup>类型返回值
     */
    public List<OptionGroup> getOptionGroups() {
        return Collections.unmodifiableList(optionGroupsList);
    }

    /**
     * getRequiredOptions方法。
     *
     * @return List<Option>类型返回值
     */
    public List<Option> getRequiredOptions() {
        return Collections.unmodifiableList(requiredOptions);
    }

    /**
     * getOptionsSortedByKey方法。
     *
     * @return List<Option>类型返回值
     */
    public List<Option> getOptionsSortedByKey() {
        List<Option> opts = new ArrayList<>(shortOpts.values());
        Collections.sort(opts, (o1, o2) -> o1.getKey().compareToIgnoreCase(o2.getKey()));
        return opts;
    }

    /**
     * getMatchingOptions方法。
     * * @param opt final类型参数
     *
     * @return List<String>类型返回值
     */
    public List<String> getMatchingOptions(final String opt) {
        List<String> matching = new ArrayList<>();
        for (String longOpt : longOpts.keySet()) {
            if (longOpt.startsWith(opt)) {
                matching.add(longOpt);
            }
        }
        Collections.sort(matching);
        return matching;
    }

    /**
     * helpOptions方法。
     *
     * @return List<Option>类型返回值
     */
    public List<Option> helpOptions() {
        return new ArrayList<>(shortOpts.values());
    }
}
