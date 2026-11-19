package com.zifang.util.codegen.service;

import com.zifang.util.codegen.core.processor.AbstractCodegenProcessor;

import javax.annotation.processing.RoundEnvironment;
import java.util.List;
import java.util.Set;

/**
 * SPI 注册处理器：驱动 {@link ServiceRegistryHandler}。
 *
 * <p>与内核约定不同的一点：注册文件必须在最后一轮（processingOver）统一落盘，
 * 而内核基类在 processingOver 直接返回，所以这里覆写 {@code process} 补上 flush。</p>
 *
 * @author zifang
 */
public final class ZServiceProcessor extends AbstractCodegenProcessor {

    private final ServiceRegistryHandler registryHandler = new ServiceRegistryHandler();

    @Override
    protected List<com.zifang.util.codegen.core.spi.CodeGenHandler> handlers() {
        return List.of(registryHandler);
    }

    @Override
    public boolean process(Set<? extends javax.lang.model.element.TypeElement> annotations, RoundEnvironment roundEnv) {
        boolean claimed = super.process(annotations, roundEnv);
        if (roundEnv.processingOver()) {
            registryHandler.flush(processingEnv);
        }
        return claimed;
    }
}
