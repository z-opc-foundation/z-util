package com.zifang.util.proxy.a.model.attribute;

import com.zifang.util.proxy.a.model.constantpool.AbstractConstantPool;
import com.zifang.util.proxy.a.model.constantpool.Utf8Info;
import com.zifang.util.proxy.a.model.readtype.U2;
import com.zifang.util.proxy.a.model.readtype.U4;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

/**
 * 属性工厂类
 * <p>
 * 根据属性名称创建对应的属性解析对象。
 */
public class AttributeFactory {
    private static final HashMap<String, AbstractAttribute> attrMap = new HashMap<>(29);

    /**
     * 获取属性对象
     *
     * @param inputStream 输入流
     * @param poolList    常量池列表，用于解析属性名称
     * @return 属性对象，如果不支持该属性则返回null
     */
    public static AbstractAttribute getAttributeTable(InputStream inputStream, List<AbstractConstantPool> poolList) {
        U2 attributeNameIndex = U2.read(inputStream);
        U4 attributeLength = U4.read(inputStream);
        short constantIndex = attributeNameIndex.getValue();
        Utf8Info utf8Info = (Utf8Info) poolList.get(constantIndex - 1);
        String key = utf8Info.getValue();

        switch (key) {
            case "ConstantValue":
                //字段表--描述final修饰的属性
                ConstantValue constantValue = new ConstantValue(attributeNameIndex, attributeLength);
                constantValue.read(inputStream);
                return constantValue;
            case "Code":
                //方法表--java代码编译成的字节码指令
                Code code = new Code(attributeNameIndex, attributeLength, poolList);
                code.read(inputStream);
                return code;
            case "Deprecate":
                //类,方法,字段--被声明为deprecate的方法和字段
                break;
            case "Exceptions":
                //方法表--方法抛出的异常列表
                break;
            case "EnclosingMethod":
                //类文件--当一个类为局部类或匿名内部类时才拥有这个属性
                break;
            case "InnerClass":
                //类文件--内部类列表
                break;
            case "LineNumberTable":
                //Code属性--java源码的行号与字节码指令的对应关系
                LineNumberTable lineNumberTable = new LineNumberTable(attributeNameIndex, attributeLength);
                lineNumberTable.read(inputStream);
                return lineNumberTable;
            case "LocalVariableTable":
                //Code属性--方法的局部变量表描述
                LocalVariableTable localVariableTable = new LocalVariableTable(attributeNameIndex, attributeLength);
                localVariableTable.read(inputStream);
                return localVariableTable;
            case "StackMapTable":
                //Code属性--供新的类型检查器检查和处理目标方法的局部变量和操作数栈所需要的的类型是否匹配
                break;
            case "Signature":
                break;
            case "SourceFile":
                //类文件--记录源文件名称
                break;
            case "SourceDebugExtension":
                //类文件--用于存储额外的调试信息
                break;
            case "Synthetic":
                //类,方发表,字段表--表示方法或字段为编译器自动生成的
                break;
            case "LocalVariableTypeTable":
                //类--它使用特征签名代替描述符,是为了引入泛型语法之后能描述泛型参数化类型而添加
                break;
            case "RuntimeVisibleAnnotations":
                //类,方法表,字段表--为动态注解提供支持,该属性用于指明哪些注解是运行时;
                break;
            case "RuntimeInvisibleAnnotations":
                //类,方法表,字段表--为动态注解提供支持,用于标明哪些注解运行时是不可见的;
                break;
            case "RuntimeVisibleParameterAnnotations":
                //方法表--作用对象为方法参数
                break;
            case "RuntimeInvisibleParameterAnnotations":
                //方法表--作用对象为方法参数
                break;
            case "AnnotationDefault":
                //方法表--用于记录注解类元素的默认值
                break;
            case "BootstrapMethods":
                //类文件--用于保存invokedynamic指定 引用的引导方法限定符
                break;
            case "RuntimeVisibleTypeAnnotations":
                //类,方法表,属性表,Code属性--用于标明哪些类注解是运行时可见的
                break;
            case "RuntimeInvisibleTypeAnnotations":
                //类,方法表,属性表,Code属性--用于标明哪些类注解是运行时不可见的
                break;
            case "MethodParameters":
                //方法表--用于支持将方法名称编译进Class文件中,并可运行时获取;
                break;
            case "Module":
                //类--用于记录一个Module的名称,以及相关信息(requires,exports,opens,uses,provides)
                break;
            case "ModulePackages":
                //类--用于记录一个模块中所有被exports 和 opens的包
                break;
            case "ModuleMainClass":
                //类--用于指定一个模块的主类
                break;
            case "NestHost":
                //类--用于支持嵌套类(java中的内部类)的反射和访问控制的API,一个内部类通过该属性得知自己的宿主类
                break;
            case "NestMembers":
                //类--用于支持嵌套类(java中的内部类)的反射和访问控制的API,一个宿主类通过该属性得知自己有哪些内部类
                break;
        }
        return null;
    }
}
