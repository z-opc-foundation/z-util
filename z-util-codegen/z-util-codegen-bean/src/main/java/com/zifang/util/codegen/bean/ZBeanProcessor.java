package com.zifang.util.codegen.bean;

import com.zifang.util.codegen.core.processor.AbstractCodegenProcessor;
import com.zifang.util.codegen.core.spi.CodeGenHandler;

import java.util.List;

/**
 * Lombok 性质特性集的注解处理器入口。
 *
 * @author zifang
 */
public class ZBeanProcessor extends AbstractCodegenProcessor {

    @Override
    protected List<CodeGenHandler> handlers() {
        return List.of(
                new AccessorHandler(),
                new ToStringHandler(),
                new EqualsHashCodeHandler(),
                new ConstructorHandler(),
                new BuilderHandler(),
                new LoggerHandler(),
                new DataHandler());
    }
}
