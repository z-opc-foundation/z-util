package com.zifang.util.yaml.facade;

import org.yaml.snakeyaml.Yaml;

/**
 * SnakeYAML 后端实现。
 */
/**
 * SnakeYamlBackend类。
 */

/**
 * SnakeYamlBackend类。
 */
public class SnakeYamlBackend implements YamlFacade {

    private final Yaml yaml;

    /**
     * SnakeYamlBackend方法。
     */
    /**
     * SnakeYamlBackend方法。
     */
    public SnakeYamlBackend() {
        this.yaml = new Yaml();
    }

    @Override
    /**
     * toYaml方法。
     *      * @param obj Object类型参数
     * @return String类型返回值
     */
    /**
     * toYaml方法。
     *      * @param obj Object类型参数
     * @return String类型返回值
     */
    public String toYaml(Object obj) {
        if (obj == null) return "null";
        return yaml.dumpAsMap(obj);
    }

    @Override
    /**
     * toPrettyYaml方法。
     *      * @param obj Object类型参数
     * @return String类型返回值
     */
    /**
     * toPrettyYaml方法。
     *      * @param obj Object类型参数
     * @return String类型返回值
     */
    public String toPrettyYaml(Object obj) {
        if (obj == null) return "null";
        return yaml.dumpAsMap(obj);
    }

    @Override
    @SuppressWarnings("unchecked")
    /**
     * fromYaml方法。
     *      * @param yamlStr String类型参数
     * @param clazz ClassT类型参数
     * @return <T> T类型返回值
     */
    /**
     * fromYaml方法。
     *      * @param yamlStr String类型参数
     * @param clazz ClassT类型参数
     * @return <T> T类型返回值
     */
    public <T> T fromYaml(String yamlStr, Class<T> clazz) {
        if (yamlStr == null || yamlStr.trim().isEmpty()) return null;
        Object result = yaml.load(yamlStr);
        return clazz.isInstance(result) ? clazz.cast(result) : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    /**
     * fromYaml方法。
     *      * @param yamlStr String类型参数
     * @param binding YamlTypeBindingT类型参数
     * @return <T> T类型返回值
     */
    /**
     * fromYaml方法。
     *      * @param yamlStr String类型参数
     * @param binding YamlTypeBindingT类型参数
     * @return <T> T类型返回值
     */
    public <T> T fromYaml(String yamlStr, YamlTypeBinding<T> binding) {
        if (yamlStr == null || yamlStr.trim().isEmpty()) return null;
        return (T) yaml.load(yamlStr);
    }

    @Override
    @SuppressWarnings("unchecked")
    /**
     * fromYamlFlat方法。
     *      * @param yamlStr String类型参数
     * @return Object类型返回值
     */
    /**
     * fromYamlFlat方法。
     *      * @param yamlStr String类型参数
     * @return Object类型返回值
     */
    public Object fromYamlFlat(String yamlStr) {
        if (yamlStr == null || yamlStr.trim().isEmpty()) return null;
        return yaml.load(yamlStr);
    }

    @Override
    /**
     * engine方法。
     * @return String类型返回值
     */
    /**
     * engine方法。
     * @return String类型返回值
     */
    public String engine() {
        return "SnakeYAML";
    }

    @Override
    /**
     * isValidYaml方法。
     *      * @param yamlStr String类型参数
     * @return boolean类型返回值
     */
    /**
     * isValidYaml方法。
     *      * @param yamlStr String类型参数
     * @return boolean类型返回值
     */
    public boolean isValidYaml(String yamlStr) {
        if (yamlStr == null || yamlStr.trim().isEmpty()) return false;
        try {
            yaml.load(yamlStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}