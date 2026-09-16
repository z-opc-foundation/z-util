package com.zifang.util.core.system.properties;

import java.util.Map;

/**
 * @author zifang
 */
public class EnvPropertiesUtil {

    /**
     * @return 当前宿主机系统级别的参数
     */
    public static Map<String, String> getEnvProperties() {
        return System.getenv();
    }

    /**
     * 获得到 当前系统环境的参数值
     *
     * @param propertyKey 环境(系统)
     * @return propertyKey 对应的系统参数值
     */
    public static String getEnvProperty(String propertyKey) {
        return getEnvProperties().get(propertyKey);
    }

}
