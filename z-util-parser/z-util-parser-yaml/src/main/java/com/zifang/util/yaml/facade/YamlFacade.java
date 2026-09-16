package com.zifang.util.yaml.facade;

/**
 * YAML 序列化/反序列化统一门面接口。
 */
/**
 * YamlFacade接口。
 */

/**
 * YamlFacade接口。
 */
public interface YamlFacade {

    String toYaml(Object obj);

    String toPrettyYaml(Object obj);

    <T> T fromYaml(String yaml, Class<T> clazz);

    <T> T fromYaml(String yaml, YamlTypeBinding<T> binding);

    Object fromYamlFlat(String yaml);

    String engine();

    default boolean isValidYaml(String yaml) {
        if (yaml == null || yaml.trim().isEmpty()) return false;
        try {
            fromYamlFlat(yaml);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}