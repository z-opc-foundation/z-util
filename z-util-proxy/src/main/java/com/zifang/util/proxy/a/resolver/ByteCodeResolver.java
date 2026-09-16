package com.zifang.util.proxy.a.resolver;

import com.zifang.util.proxy.a.model.ClassFile;
import com.zifang.util.proxy.a.model.constantpool.*;
import com.zifang.util.proxy.a.model.field.FieldTable;
import com.zifang.util.proxy.a.model.inter.InterfaceIndex;
import com.zifang.util.proxy.a.model.method.MethodTable;
import com.zifang.util.proxy.a.model.readtype.U1;
import com.zifang.util.proxy.a.model.readtype.U2;
import com.zifang.util.proxy.a.model.readtype.U4;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Java字节码解析器
 * <p>
 * 用于解析.class文件，提取其中的类信息、常量池、字段、方法、属性等结构化数据。
 * 基于JVM规范实现，支持解析Java 8及以上版本的字节码文件。
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 从文件解析
 * ClassFile classFile = ByteCodeResolver.parseFromFile("MyClass.class");
 *
 * // 从输入流解析
 * InputStream is = ...;
 * ClassFile classFile = ByteCodeResolver.parseFromStream(is);
 *
 * // 获取解析结果
 * System.out.println("类名: " + classFile.getClassName());
 * System.out.println("方法数: " + classFile.getMethods().size());
 * }</pre>
 *
 * @author zifang
 */
public class ByteCodeResolver {

    private final InputStream inputStream;
    private final ClassFile classFile;

    /**
     * ByteCodeResolver方法。
     * * @param inputStream InputStream类型参数
     */
    public ByteCodeResolver(InputStream inputStream) {
        this.inputStream = inputStream;
        this.classFile = new ClassFile();
    }

    /**
     * 从文件路径解析字节码
     *
     * @param filePath .class文件路径
     * @return 解析后的ClassFile对象
     */
    public static ClassFile parseFromFile(String filePath) {
        try (InputStream is = Files.newInputStream(Paths.get(filePath))) {
            return new ByteCodeResolver(is).parse();
        } catch (Exception e) {
            throw new RuntimeException("解析字节码文件失败: " + filePath, e);
        }
    }

    /**
     * 从输入流解析字节码
     *
     * @param inputStream 包含字节码的输入流
     * @return 解析后的ClassFile对象
     */
    public static ClassFile parseFromStream(InputStream inputStream) {
        return new ByteCodeResolver(inputStream).parse();
    }

    /**
     * 主方法，用于命令行测试
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("用法: java ByteCodeResolver <class文件路径>");
            System.out.println("示例: java ByteCodeResolver /path/to/MyClass.class");
            return;
        }

        String filePath = args[0];
        try {
            ClassFile classFile = parseFromFile(filePath);

            System.out.println("========== 字节码解析结果 ==========");
            System.out.println("魔数: " + String.format("0x%08X", classFile.magic.value));
            System.out.println("版本: " + classFile.majorVersion.value + "." + classFile.minorVersion.value);
            System.out.println("常量池大小: " + classFile.constantPoolSize.value);

            // 打印常量池内容
            System.out.println("\n--- 常量池内容 ---");
            for (int i = 0; i < classFile.poolInfo.getPoolList().size(); i++) {
                AbstractConstantPool pool = classFile.poolInfo.getPoolList().get(i);
                System.out.println("[" + (i + 1) + "] " + pool.getClass().getSimpleName() + ": " + pool);
            }

            System.out.println("\n解析完成: " + filePath);
        } catch (Exception e) {
            System.err.println("解析失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 执行解析
     *
     * @return 解析后的ClassFile对象
     */
    public ClassFile parse() {
        try {
            // 1. 解析魔数 magic
            parseMagic();

            // 2. 解析版本号 minor/major version
            parseVersion();

            // 3. 解析常量池 constant pool
            parseConstantPool();

            // 4. 解析访问标志 access flags
            parseAccessFlags();

            // 5. 解析类索引 this class
            parseThisClass();

            // 6. 解析父类索引 super class
            parseSuperClass();

            // 7. 解析接口表 interfaces
            parseInterfaces();

            // 8. 解析字段表 fields
            parseFields();

            // 9. 解析方法表 methods
            parseMethods();

            return classFile;
        } catch (Exception e) {
            throw new RuntimeException("字节码解析失败", e);
        }
    }

    /**
     * 解析魔数 (4字节)
     * 固定为 0xCAFEBABE
     */
    private void parseMagic() {
        classFile.magic = U4.read(inputStream);
        // 验证魔数
        if (classFile.magic.value != 0xCAFEBABE) {
            throw new RuntimeException("无效的字节码文件，魔数应为 0xCAFEBABE");
        }
    }

    /**
     * 解析版本号 (各2字节)
     * minor_version: 次版本号
     * major_version: 主版本号 (52=Java8, 55=Java11, 61=Java17)
     */
    private void parseVersion() {
        classFile.minorVersion = U2.read(inputStream);
        classFile.majorVersion = U2.read(inputStream);
    }

    /**
     * 解析常量池
     * constant_pool_count (2字节): 常量池计数
     * constant_pool[1..count-1]: 常量池内容
     */
    private void parseConstantPool() throws Exception {
        U2 poolCount = U2.read(inputStream);
        classFile.constantPoolSize = poolCount;

        List<AbstractConstantPool> poolList = new ArrayList<>();
        // 循环读取常量池项，注意LONG和DOUBLE类型会占用两个索引
        for (int i = 1; i < poolCount.value; i++) {
            U1 tag = U1.read(inputStream);
            AbstractConstantPool constantPool = createConstantPool(tag.value);
            constantPool.read(inputStream);
            poolList.add(constantPool);
            // LONG和DOUBLE类型占用常量池中两个位置
            if (tag.value == AbstractConstantPool.CONSTANT_LONG_INFO
                    || tag.value == AbstractConstantPool.CONSTANT_DOUBLE_INFO) {
                i++;
            }
        }

        classFile.poolInfo = new ConstantPoolInfo((short) poolCount.value);
        classFile.poolInfo.setPoolList(poolList);
    }

    /**
     * 根据tag创建对应的常量池项
     */
    private AbstractConstantPool createConstantPool(byte tag) {
        switch (tag) {
            case AbstractConstantPool.CONSTANT_UTF8_INFO:
                return new Utf8Info(tag);
            case AbstractConstantPool.CONSTANT_INTEGER_INFO:
                return new ConstantIntegerInfo(tag);
            case AbstractConstantPool.CONSTANT_FLOAT_INFO:
                return new ConstantFloatInfo(tag);
            case AbstractConstantPool.CONSTANT_LONG_INFO:
                return new ConstantLongInfo(tag);
            case AbstractConstantPool.CONSTANT_DOUBLE_INFO:
                return new ConstantDoubleInfo(tag);
            case AbstractConstantPool.CONSTANT_CLASS_INFO:
                return new ClassInfo(tag);
            case AbstractConstantPool.CONSTANT_STRING_INFO:
                return new ConstantClassInfo(tag);
            case AbstractConstantPool.CONSTANT_FIELDREF_INFO:
                return new FieldRefInfo(tag);
            case AbstractConstantPool.CONSTANT_METHODREF_INFO:
                return new MethodRefInfo(tag);
            case AbstractConstantPool.CONSTANT_INTERFACEMETHODREF_INFO:
                return new ConstantInterfaceMethodRefInfo(tag);
            case AbstractConstantPool.CONSTANT_NAMEANDTYPE_INFO:
                return new ConstantNameAndTypeInfo(tag);
            case AbstractConstantPool.CONSTANT_METHODHANDLE_INFO:
                return new ConstantMethodHandleInfo(tag);
            case AbstractConstantPool.CONSTANT_METHODTYPE_INFO:
                return new ConstantMethodTypeInfo(tag);
            case AbstractConstantPool.CONSTANT_INVOKEDYNAMIC_INFO:
                return new ConstantInvokeDynamicInfo(tag);
            default:
                throw new RuntimeException("未知的常量池类型: " + tag);
        }
    }

    /**
     * 解析访问标志 (2字节)
     * 包括: ACC_PUBLIC, ACC_FINAL, ACC_SUPER, ACC_INTERFACE, ACC_ABSTRACT 等
     */
    private void parseAccessFlags() {
        classFile.accessFlag = U2.read(inputStream);
    }

    /**
     * 解析类索引 (2字节)
     * 指向常量池中的CONSTANT_Class_info
     */
    private void parseThisClass() {
        classFile.classIndex = U2.read(inputStream);
    }

    /**
     * 解析父类索引 (2字节)
     * 指向常量池中的CONSTANT_Class_info
     * Object类的父类索引为0
     */
    private void parseSuperClass() {
        classFile.superClassIndex = U2.read(inputStream);
    }

    /**
     * 解析接口表
     * interfaces_count (2字节): 接口数量
     * interfaces[count]: 每个接口的常量池索引
     */
    private void parseInterfaces() {
        U2 interfacesCount = U2.read(inputStream);
        InterfaceIndex interfaceIndex = new InterfaceIndex();
        for (int i = 0; i < interfacesCount.value; i++) {
            interfaceIndex.addIndex(U2.read(inputStream));
        }
        classFile.interfaceIndex = interfaceIndex;
    }

    /**
     * 解析字段表
     * fields_count (2字节): 字段数量
     * fields[count]: 每个字段的结构
     */
    private void parseFields() {
        // 先读取字段数量
        U2 fieldsCount = U2.read(inputStream);
        // 字段表解析器会读取各个字段
        classFile.fieldInfo = new FieldTable(inputStream, classFile.poolInfo.getPoolList(), fieldsCount.value);
    }

    /**
     * 解析方法表
     * methods_count (2字节): 方法数量
     * methods[count]: 每个方法的结构
     */
    private void parseMethods() {
        // 先读取方法数量
        U2 methodsCount = U2.read(inputStream);
        // 方法表解析器会读取各个方法
        MethodTable methodInfo = new MethodTable(inputStream, classFile.poolInfo.getPoolList(), methodsCount.value);
        classFile.setMethodInfo(methodInfo);
    }

    /**
     * 获取解析后的ClassFile对象
     *
     * @return ClassFile对象
     */
    public ClassFile getClassFile() {
        return classFile;
    }
}
