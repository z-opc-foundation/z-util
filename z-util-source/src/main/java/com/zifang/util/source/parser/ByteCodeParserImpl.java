package com.zifang.util.source.parser;

import com.zifang.util.source.define.ByteCodeParser;
import com.zifang.util.source.generator.info.ClassInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于 ClassReader 的字节码解析实现
 */
public class ByteCodeParserImpl implements ByteCodeParser {

    private static final Logger log = LoggerFactory.getLogger(ByteCodeParserImpl.class);

    /**
     * ByteCodeParserImpl方法。
     */
    public ByteCodeParserImpl() {
    }

    /**
     * parse方法。
     * * @param bytecode byte[]类型参数
     *
     * @return ClassInfo类型返回值
     */
    public ClassInfo parse(byte[] bytecode) {
        if (bytecode == null || bytecode.length == 0) {
            throw new IllegalArgumentException("bytecode 不能为空");
        }
        log.debug("解析字节码，长度={}", bytecode.length);
        throw new UnsupportedOperationException("字节码解析尚未实现，请使用 SourceCodeParser");
    }

    /**
     * parse方法。
     * * @param clazz Class?类型参数
     *
     * @return ClassInfo类型返回值
     */
    public ClassInfo parse(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz 不能为 null");
        }
        log.debug("解析类: {}", clazz.getName());
        throw new UnsupportedOperationException("字节码解析尚未实现，请使用 SourceCodeParser");
    }
}
