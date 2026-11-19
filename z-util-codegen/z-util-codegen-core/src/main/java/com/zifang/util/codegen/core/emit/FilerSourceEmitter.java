package com.zifang.util.codegen.core.emit;

import com.zifang.util.codegen.core.spi.SourceEmitter;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 基于 {@link Filer} 的源码生成实现。
 *
 * @author zifang
 */
public final class FilerSourceEmitter implements SourceEmitter {

    private final Filer filer;

    public FilerSourceEmitter(Filer filer) {
        this.filer = filer;
    }

    @Override
    public void generate(String qualifiedName, JavaSource source) {
        try {
            JavaFileObject file = filer.createSourceFile(qualifiedName);
            try (PrintWriter writer = new PrintWriter(file.openWriter())) {
                writer.print(source.render());
            }
        } catch (IOException e) {
            throw new IllegalStateException("failed to generate source: " + qualifiedName, e);
        }
    }
}
