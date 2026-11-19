package com.zifang.util.codegen.meta;

import com.zifang.util.codegen.core.processor.AbstractCodegenProcessor;
import com.zifang.util.codegen.core.spi.CodeGenHandler;

import java.util.List;

/**
 * 元模型处理器入口。
 *
 * @author zifang
 */
public class ZMetaModelProcessor extends AbstractCodegenProcessor {

    @Override
    protected List<CodeGenHandler> handlers() {
        return List.of(new MetaModelHandler());
    }
}
