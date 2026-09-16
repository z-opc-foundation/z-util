package com.zifang.util.core.system;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * 系统参数的工具包
 * 命令行加入参数 -Dconf 指定的文件下所有的文件被认为是参数
 *
 * @author zifang
 */
public class PropertyUtil {

    private static final Properties configProperties = new Properties();

    static {
        String conf = System.getProperty("conf");
        if (conf != null) {
            populateCache(new File(conf));
        }
    }

    private static void populateCache(File folder) {
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                populateCache(fileEntry);
            } else {
                if (fileEntry.getName().endsWith(".properties")) {
                    FileReader reader = null;
                    try {
                        reader = new FileReader(fileEntry);
                        configProperties.load(reader);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * getProperty方法。
     * * @param key String类型参数
     *
     * @return static String类型返回值
     */
    public static String getProperty(String key) {
        return configProperties.getProperty(key);
    }
}
