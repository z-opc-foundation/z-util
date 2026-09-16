package com.zifang.util.zex.bytecode.asm;

import org.objectweb.asm.*;

import java.io.File;
import java.io.FileOutputStream;

/**
 * ASM字节码操作示例类。
 * <p>
 * 此类演示了如何使用ASM框架读取、修改和生成字节码。
 * 通过ClassReader读取类信息，ClassWriter生成新的字节码，
 * ClassVisitor和MethodVisitor进行字节码的访问和修改。
 *
 * @author zifang
 * @version 1.0
 */
public class A {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) throws Exception {
        //读取
        ClassReader classReader = new ClassReader("com/zifang/util/zex/bytecode/asm/Base");
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        //处理
        ClassVisitor classVisitor = new MyClassVisitor(classWriter);
        classReader.accept(classVisitor, ClassReader.SKIP_DEBUG);
        byte[] data = classWriter.toByteArray();
        //输出
        File f = new File("/Users/zifang/workplace/idea_workplace/components/util-zex/src/main/resources/Base.class");
        FileOutputStream fout = new FileOutputStream(f);
        fout.write(data);
        fout.close();
        System.out.println("now generator cc success!!!!!");
    }
}


class MyClassVisitor extends ClassVisitor implements Opcodes {

    /**
     * MyClassVisitor方法。
     * * @param cv ClassVisitor类型参数
     */
    public MyClassVisitor(ClassVisitor cv) {
        super(ASM5, cv);
    }

    @Override
    /**
     * visit方法。
     *      * @param version int类型参数
     * @param access int类型参数
     * @param name String类型参数
     * @param signature String类型参数
     * @param superName String类型参数
     * @param interfaces String[]类型参数
     */
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        cv.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    /**
     * visitMethod方法。
     *      * @param access int类型参数
     * @param name String类型参数
     * @param desc String类型参数
     * @param signature String类型参数
     * @param exceptions String[]类型参数
     * @return MethodVisitor类型返回值
     */
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        //Base类中有两个方法：无参构造以及process方法，这里不增强构造方法
        if (!name.equals("<init>") && mv != null) {
            mv = new MyMethodVisitor(mv);
        }
        return mv;
    }

    class MyMethodVisitor extends MethodVisitor implements Opcodes {
        /**
         * MyMethodVisitor方法。
         * * @param mv MethodVisitor类型参数
         */
        public MyMethodVisitor(MethodVisitor mv) {
            super(Opcodes.ASM5, mv);
        }

        @Override
        /**
         * visitCode方法。
         */
        public void visitCode() {
            super.visitCode();
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitLdcInsn("start");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        }

        @Override
        /**
         * visitInsn方法。
         *      * @param opcode int类型参数
         */
        public void visitInsn(int opcode) {
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) || opcode == Opcodes.ATHROW) {
                //方法在返回之前，打印"end"
                mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                mv.visitLdcInsn("end");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            }
            mv.visitInsn(opcode);
        }
    }
}