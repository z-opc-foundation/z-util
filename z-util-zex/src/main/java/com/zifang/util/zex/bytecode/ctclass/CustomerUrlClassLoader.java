package com.zifang.util.zex.bytecode.ctclass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义的URL类加载器。
 * <p>
 * 此类继承自URLClassLoader，用于从指定URL路径加载类文件。
 * 内部维护了jar包路径与其中类名列表的映射关系。
 *
 * @author zifang
 * @version 1.0
 */
public class CustomerUrlClassLoader extends URLClassLoader {

    private static final Logger log = LoggerFactory.getLogger(CustomerUrlClassLoader.class);

    /**
     * 扫描过 过程中记录jar名字与下面的类名列表
     */
    private Map<String, List<String>> jarPathClassListMapper = new LinkedHashMap<>();


    /**
     * CustomerUrlClassLoader方法。
     * * @param urls URL[]类型参数
     *
     * @param parent ClassLoader类型参数
     */
    public CustomerUrlClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }
}
