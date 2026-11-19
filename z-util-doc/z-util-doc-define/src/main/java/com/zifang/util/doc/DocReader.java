package com.zifang.util.doc;

import java.io.IOException;

/**
 * 格式读取器 SPI。实现类放各自的 z-util-doc-{format} 模块，零第三方依赖。
 * supports 拿到完整字节，允许做深度嗅探（zip 容器要看内部条目）。
 */
public interface DocReader {

    DocFormat format();

    /** 该数据是否为本格式。 */
    boolean supports(byte[] data);

    DocDocument read(byte[] data) throws IOException;
}
