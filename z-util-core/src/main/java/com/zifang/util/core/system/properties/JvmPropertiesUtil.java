package com.zifang.util.core.system.properties;

import java.util.Properties;

/**
 * java环境的参数获取
 *
 * @author zifang
 */
public class JvmPropertiesUtil {

    /**
     * @return java环境下参数
     */
    public static JvmProperties getJvmProperties() {
        JvmProperties jvmProperties = new JvmProperties();
        jvmProperties.init();
        return jvmProperties;
    }

    /**
     * 通过jvm环境参数key 获得数据
     *
     * @param jvmPropertyKey java环境的参数key
     * @return java环境的数据值
     */
    public static String getJvmProperty(String jvmPropertyKey) {
        return getJvmProperty(jvmPropertyKey, null);
    }

    /**
     * 通过key值获得到对应的参数value
     *
     * @param jvmPropertyKey  jvm环境参数key值
     * @param propertyDefault jvm环境参数value默认值
     * @return
     */
    public static String getJvmProperty(String jvmPropertyKey, String propertyDefault) {
        Properties properties = System.getProperties();
        return properties.getProperty(jvmPropertyKey, propertyDefault);
    }

    /**
     * 动态替换java环境的参数 如果成功替换则把旧值返回
     *
     * @param jvmPropertyKey   java环境的key值
     * @param jvmPropertyValue java环境的value值
     * @return 返回成功替换之后的就值
     */
    public static String setJvmProperty(String jvmPropertyKey, String jvmPropertyValue) {
        Properties properties = System.getProperties();
        Object value = properties.get(jvmPropertyKey);
        if (value == null) {
            properties.setProperty(jvmPropertyKey, jvmPropertyValue);
            return null;
        } else {
            properties.setProperty(jvmPropertyKey, jvmPropertyValue);
            return String.valueOf(value);
        }
    }
}
