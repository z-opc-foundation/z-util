package com.zifang.util.codegen.mapper;

import com.zifang.util.codegen.core.processor.AbstractCodegenProcessor;
import com.zifang.util.codegen.core.spi.CodeGenHandler;

import java.util.List;

/**
 * MapStruct 性质转换集的注解处理器入口。
 *
 * @author zifang
 */
public class ZMapperProcessor extends AbstractCodegenProcessor {

    @Override
    protected List<CodeGenHandler> handlers() {
        return List.of(new MapperHandler(diagnostics()));
    }
}
