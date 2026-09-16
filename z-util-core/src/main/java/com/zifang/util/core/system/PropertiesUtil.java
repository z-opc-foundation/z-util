package com.zifang.util.core.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class PropertiesUtil {

    private static final Logger log = LoggerFactory.getLogger(PropertiesUtil.class);

    /**
     * 从系统属性文件中获取相应的值
     */
    public static String key(String key) {
        return System.getProperty(key);
    }

    /**
     * 根据Key读取Value
     */
    public static String getValueByKey(String filePath, String key) {
        Properties pps = new Properties();
        try (InputStream in = new BufferedInputStream(new FileInputStream(filePath))) {
            pps.load(in);
            return pps.getProperty(key);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

//    /**
//     * 读取properties文件的内容
//     *
//     * @param fileName 文件名
//     * @return
//     * @throws IOException
//     */
//    public static Properties read(String fileName) throws IOException {
//        try (InputStream inputStream = FileReadUtil.getStreamByFileName(fileName)) {
//            Properties pro = new Properties();
//            pro.load(inputStream);
//            return pro;
//        }
//    }

    /**
     * properties方法。
     * * @param in InputStream类型参数
     *
     * @return static Map<String, String>类型返回值
     */
    public static Map<String, String> properties(InputStream in) {
        Map<String, String> map = new HashMap<>();
        Properties pps = new Properties();
        try {
            pps.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Enumeration en = pps.propertyNames();
        while (en.hasMoreElements()) {
            String strKey = (String) en.nextElement();
            String strValue = pps.getProperty(strKey);
            map.put(strKey, strValue);
        }
        return map;
    }

    /**
     * 读取Properties的全部信息
     */
    public static Map<String, String> GetAllProperties(String filePath) throws IOException {
        Map<String, String> map = new HashMap<>();
        Properties pps = new Properties();
        try (InputStream in = new BufferedInputStream(new FileInputStream(filePath))) {
            return properties(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 写入Properties信息
     */
    public static void WriteProperties(String filePath, String pKey, String pValue) throws IOException {
        Properties props = new Properties();

        props.load(new FileInputStream(filePath));
        // 调用 Hashtable 的方法 put，使用 getProperty 方法提供并行性。
        // 强制要求为属性的键和值使用字符串。返回值是 Hashtable 调用 put 的结果。
        OutputStream fos = new FileOutputStream(filePath);
        props.setProperty(pKey, pValue);
        // 以适合使用 load 方法加载到 Properties 表中的格式，
        // 将此 Properties 表中的属性列表（键和元素对）写入输出流
        props.store(fos, "Update '" + pKey + "' value");

    }

}