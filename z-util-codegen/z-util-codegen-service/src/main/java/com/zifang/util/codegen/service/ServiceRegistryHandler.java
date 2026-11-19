package com.zifang.util.codegen.service;

import com.zifang.util.codegen.annotation.ZAutoService;
import com.zifang.util.codegen.core.model.AnnotationSpec;
import com.zifang.util.codegen.core.spi.CodeGenHandler;
import com.zifang.util.codegen.core.spi.HandlerContext;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SPI 注册 handler：收集 {@link ZAutoService} 命中的 provider → service 映射，
 * 在最后一轮合并写出 {@code META-INF/services/{service}} 文件。
 *
 * <p>已存在的注册文件会被读回来合并去重，所以增量编译和多次构建不会产生重复行。</p>
 *
 * @author zifang
 */
public final class ServiceRegistryHandler implements CodeGenHandler {

    private static final String Z_AUTO_SERVICE = "com.zifang.util.codegen.annotation.ZAutoService";

    /** service 全限定名 → provider 全限定名集合，跨轮累积、processingOver 时统一落盘。 */
    private final Map<String, Set<String>> registry = new LinkedHashMap<>();

    @Override
    public Set<String> supportedAnnotations() {
        return Set.of(Z_AUTO_SERVICE);
    }

    @Override
    public void handle(HandlerContext context) {
        AnnotationSpec spec = context.annotation(Z_AUTO_SERVICE);
        if (spec == null) {
            return;
        }
        List<String> services = spec.strings("value");
        if (services.isEmpty()) {
            context.environment().getMessager().printMessage(
                    javax.tools.Diagnostic.Kind.WARNING,
                    "@ZAutoService 未声明任何 service 接口，跳过", context.target());
            return;
        }
        for (String service : services) {
            validate(context, service);
            registry.computeIfAbsent(service, k -> new LinkedHashSet<>())
                    .add(context.type().qualifiedName());
        }
    }

    /** provider 必须真的实现（或继承）声明的 service，否则这是用户代码错误，编译就该挂。 */
    private void validate(HandlerContext context, String service) {
        TypeElement serviceType = context.environment().getElementUtils().getTypeElement(service);
        if (serviceType == null) {
            context.environment().getMessager().printMessage(
                    javax.tools.Diagnostic.Kind.ERROR,
                    "@ZAutoService 的 service 找不到类型：" + service, context.target());
            return;
        }
        if (!(context.target() instanceof TypeElement provider)
                || !context.environment().getTypeUtils().isAssignable(provider.asType(), serviceType.asType())) {
            context.environment().getMessager().printMessage(
                    javax.tools.Diagnostic.Kind.ERROR,
                    "@ZAutoService 的提供者 " + context.type().qualifiedName()
                            + " 未实现 service " + service, context.target());
        }
    }

    /** 最后一轮调用：把累积的注册表合并写出。由 {@link ZServiceProcessor} 在 processingOver 时触发。 */
    void flush(ProcessingEnvironment environment) {
        if (registry.isEmpty()) {
            return;
        }
        Filer filer = environment.getFiler();
        for (Map.Entry<String, Set<String>> entry : registry.entrySet()) {
            String path = "META-INF/services/" + entry.getKey();
            Set<String> lines = existingLines(filer, path);
            lines.addAll(entry.getValue());
            try {
                FileObject out = filer.createResource(StandardLocation.CLASS_OUTPUT, "", path);
                try (PrintWriter writer = new PrintWriter(out.openWriter())) {
                    for (String line : lines) {
                        writer.println(line);
                    }
                }
            } catch (IOException e) {
                environment.getMessager().printMessage(
                        javax.tools.Diagnostic.Kind.WARNING,
                        "SPI 注册文件写失败：" + path + "（" + e + "）");
            }
        }
        registry.clear();
    }

    private Set<String> existingLines(Filer filer, String path) {
        try {
            FileObject existing = filer.getResource(StandardLocation.CLASS_OUTPUT, "", path);
            Set<String> lines = new LinkedHashSet<>();
            try (BufferedReader reader = new BufferedReader(
                    new java.io.InputStreamReader(existing.openInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        lines.add(line);
                    }
                }
            }
            return lines;
        } catch (IOException | IllegalArgumentException notFoundYet) {
            return new LinkedHashSet<>();
        }
    }
}
