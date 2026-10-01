package com.zifang.util.proxy.weave;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;

/**
 * 字节级入口织入：用 ASM 对既有 class 的每个具体方法入口插入静态钩子调用。
 * <p>
 * 钩子签名固定为 {@code public static void record(String owner, String method)}。
 * 与源码级代理（SourceProxyFactory）互补：不重新编译、final 方法也能织入。
 */
public class EntryWeaver {

    private EntryWeaver() {
    }

    /**
     * @param original      原始 class 字节码
     * @param hookClass     钩子所在类（record 方法定义处）
     * @param hookMethodName 钩子静态方法名
     * @return 织入后的 class 字节码
     */
    public static byte[] weave(byte[] original, Class<?> hookClass, String hookMethodName) throws IOException {
        String hookOwner = Type.getInternalName(hookClass);
        String hookDesc = "(Ljava/lang/String;Ljava/lang/String;)V";

        ClassReader reader = new ClassReader(original);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        reader.accept(new ClassVisitor(Opcodes.ASM9, writer) {

            private String ownerInternalName;

            @Override
            public void visit(int version, int access, String name, String signature,
                              String superName, String[] interfaces) {
                this.ownerInternalName = name;
                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                boolean concrete = (access & Opcodes.ACC_ABSTRACT) == 0 && (access & Opcodes.ACC_NATIVE) == 0;
                boolean weavable = !name.startsWith("<") && (access & Opcodes.ACC_STATIC) == 0 && concrete;
                if (!weavable || mv == null) {
                    return mv;
                }
                String owner = ownerInternalName;
                return new MethodVisitor(Opcodes.ASM9, mv) {
                    @Override
                    public void visitCode() {
                        // 必须先委派给底层 writer 的 visitCode 让写入器进入字节码发射状态，
                        // 再插入钩子；直接 super.visitCode() 是 no-op，会让 INVOKESTATIC
                        // 落在未初始化的 code buffer 里被丢弃
                        mv.visitCode();
                        mv.visitLdcInsn(owner);
                        mv.visitLdcInsn(name);
                        mv.visitMethodInsn(Opcodes.INVOKESTATIC, hookOwner, hookMethodName, hookDesc, false);
                    }
                };
            }
        }, 0);
        return writer.toByteArray();
    }
}
