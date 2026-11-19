package com.zifang.util.codegen.core.processor;

import com.zifang.util.codegen.core.diag.Diagnostics;
import com.zifang.util.codegen.core.emit.FilerSourceEmitter;
import com.zifang.util.codegen.core.inject.AstMemberEmitter;
import com.zifang.util.codegen.core.model.AnnotationSpec;
import com.zifang.util.codegen.core.model.TypeModel;
import com.zifang.util.codegen.core.spi.CodeGenHandler;
import com.zifang.util.codegen.core.spi.HandlerContext;
import com.zifang.util.codegen.core.spi.SourceEmitter;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * 代码生成处理器基类：负责收集被注解的元素、建类型模型、把现场交给 handler。
 *
 * <p>子类通过 {@link #handlers()} 声明自己的 handler；引擎另外会自动并入
 * {@code META-INF/services} 里登记的第三方 handler，供使用者扩自己的特性。</p>
 *
 * @author zifang
 */
public abstract class AbstractCodegenProcessor extends AbstractProcessor {

    private List<CodeGenHandler> handlers = List.of();
    private Diagnostics diagnostics;
    private ModelBuilder modelBuilder;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.diagnostics = new Diagnostics(processingEnv.getMessager());
        this.modelBuilder = new ModelBuilder(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
        this.handlers = resolveHandlers();
    }

    /**
     * 本处理器自带的 handler。
     *
     * <p>默认返回 {@code ServiceLoader} 发现的 handler；内置特性集请在子类里直接返回实例，
     * 这样不依赖类加载器布局。</p>
     */
    protected List<CodeGenHandler> handlers() {
        return List.of();
    }

    /** 编译期诊断出口。 */
    protected Diagnostics diagnostics() {
        return diagnostics;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> names = new LinkedHashSet<>();
        for (CodeGenHandler handler : handlers) {
            names.addAll(handler.supportedAnnotations());
        }
        return names;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver() || handlers.isEmpty()) {
            return false;
        }

        Map<Element, Set<String>> hits = collectHits(roundEnv);
        for (Map.Entry<Element, Set<String>> entry : hits.entrySet()) {
            Element element = entry.getKey();
            Set<String> annotationNames = entry.getValue();
            try {
                dispatch(roundEnv, element, annotationNames);
            } catch (RuntimeException e) {
                diagnostics.error(element, "代码生成失败：" + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
        return true;
    }

    private Map<Element, Set<String>> collectHits(RoundEnvironment roundEnv) {
        Map<Element, Set<String>> hits = new LinkedHashMap<>();
        for (String annotationName : getSupportedAnnotationTypes()) {
            TypeElement annotationType = processingEnv.getElementUtils().getTypeElement(annotationName);
            if (annotationType == null) {
                continue;
            }
            for (Element element : roundEnv.getElementsAnnotatedWith(annotationType)) {
                hits.computeIfAbsent(element, k -> new LinkedHashSet<>()).add(annotationName);
            }
        }
        return hits;
    }

    private void dispatch(RoundEnvironment roundEnv, Element element, Set<String> annotationNames) {
        TypeElement owner = ownerOf(element);
        if (owner == null) {
            return;
        }
        TypeModel type = modelBuilder.build(owner);
        AstMemberEmitter members = AstMemberEmitter.create(processingEnv, owner, diagnostics);
        if (members == null) {
            return;
        }
        SourceEmitter sources = new FilerSourceEmitter(processingEnv.getFiler());

        List<AnnotationSpec> targetAnnotations = modelBuilder.annotationsOf(element);
        List<AnnotationSpec> typeAnnotations = element.equals(owner)
                ? targetAnnotations
                : modelBuilder.annotationsOf(owner);

        HandlerContext context = new HandlerContext(
                processingEnv, roundEnv, type, element, targetAnnotations, typeAnnotations, members, sources);

        for (CodeGenHandler handler : handlers) {
            for (String supported : handler.supportedAnnotations()) {
                if (annotationNames.contains(supported)) {
                    handler.handle(context);
                    break;
                }
            }
        }
    }

    /** 找到被注解元素所属的类型：类型本身，或字段所属的外层类型。 */
    private TypeElement ownerOf(Element element) {
        if (element instanceof TypeElement) {
            return (TypeElement) element;
        }
        if (element instanceof VariableElement && element.getKind() == ElementKind.FIELD) {
            Element enclosing = element.getEnclosingElement();
            if (enclosing instanceof TypeElement) {
                return (TypeElement) enclosing;
            }
        }
        return null;
    }

    private List<CodeGenHandler> resolveHandlers() {
        List<CodeGenHandler> result = new ArrayList<>(handlers());
        Set<Class<?>> seen = new LinkedHashSet<>();
        for (CodeGenHandler handler : result) {
            seen.add(handler.getClass());
        }
        try {
            for (CodeGenHandler handler : ServiceLoader.load(CodeGenHandler.class, getClass().getClassLoader())) {
                if (seen.add(handler.getClass())) {
                    result.add(handler);
                }
            }
        } catch (RuntimeException | ServiceConfigurationError e) {
            diagnostics.note(null, "扩展 handler 加载失败（不影响内置特性）：" + e.getMessage());
        }
        return Collections.unmodifiableList(result);
    }
}
