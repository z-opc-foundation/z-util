package com.zifang.util.source.compiler;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * 字符序列Java文件对象
 * <p>
 * 自定义的JavaFileObject实现，用于在内存中存储Java源代码和编译后的字节码。
 * 支持通过CharSequence存储源代码，通过ByteArrayOutputStream存储字节码，
 * 整个编译过程无需访问文件系统。
 *
 * @author zifang
 * @version 1.0.0
 */
public class CharSequenceJavaFileObject extends SimpleJavaFileObject {

    /**
     * 源代码内容
     */
    private final CharSequence sourceCode;
    /**
     * 用于存储编译后字节码的输出流
     */
    private ByteArrayOutputStream byteCode = new ByteArrayOutputStream();

    /**
     * 使用类名和源代码构造文件对象（作为源代码存储）
     *
     * @param className  类名
     * @param sourceCode 源代码内容
     */
    public CharSequenceJavaFileObject(String className, CharSequence sourceCode) {
        super(URI.create(className + Kind.SOURCE.extension), Kind.SOURCE);
        this.sourceCode = sourceCode;
    }

    /**
     * 使用全限定类名和文件类型构造文件对象（作为字节码存储）
     *
     * @param fullClassName 全限定类名
     * @param kind          文件类型
     */
    public CharSequenceJavaFileObject(String fullClassName, Kind kind) {
        super(URI.create(fullClassName), kind);
        this.sourceCode = null;
    }

    /**
     * 获取源代码内容
     *
     * @param ignoreEncodingErrors 是否忽略编码错误
     * @return 源代码内容
     */
    @Override
    /**
     * getCharContent方法。
     *      * @param ignoreEncodingErrors boolean类型参数
     * @return CharSequence类型返回值
     */
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return sourceCode;
    }

    /**
     * 打开输入流读取字节码
     *
     * @return 字节码输入流
     */
    @Override
    /**
     * openInputStream方法。
     * @return InputStream类型返回值
     */
    public InputStream openInputStream() {
        return new ByteArrayInputStream(getByteCode());
    }

    /**
     * 打开输出流写入字节码
     * <p>
     * 编译结果回调的OutputStream，回调成功后就能通过getByteCode()方法获取编译后的字节码
     *
     * @return 字节码输出流
     */
    @Override
    /**
     * openOutputStream方法。
     * @return OutputStream类型返回值
     */
    public OutputStream openOutputStream() {
        return byteCode;
    }

    /**
     * 获取编译后的字节码
     *
     * @return 字节码数组
     */
    public byte[] getByteCode() {
        return byteCode.toByteArray();
    }
}