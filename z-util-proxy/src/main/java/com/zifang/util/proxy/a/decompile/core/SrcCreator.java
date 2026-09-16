package com.zifang.util.proxy.a.decompile.core;

import com.zifang.util.proxy.a.model.ClassFile;
import com.zifang.util.proxy.a.model.attribute.AbstractAttribute;
import com.zifang.util.proxy.a.model.attribute.Code;
import com.zifang.util.proxy.a.model.attribute.LineNumberTable;
import com.zifang.util.proxy.a.model.attribute.LocalVariableTable;
import com.zifang.util.proxy.a.model.constantpool.*;
import com.zifang.util.proxy.a.model.field.FieldInfo;
import com.zifang.util.proxy.a.model.method.MethodInfo;
import com.zifang.util.proxy.a.model.readtype.U1;

import java.util.*;
import java.util.Map.Entry;

/**
 * 源码生成器
 * <p>
 * 将 ClassFile 反编译为 Java 源代码。
 */
public class SrcCreator {

    private static final String INDENT = "\t\t";

    /**
     * 根据 ClassFile 对象，生成 Java 源代码
     *
     * @param classFile ClassFile 对象
     * @return Java 源代码字符串
     */
    public static String createJavaFileSrc(ClassFile classFile) {
        StringBuilder sb = new StringBuilder();

        // 获取常量池
        List<AbstractConstantPool> poolList = classFile.poolInfo.getPoolList();

        // 获取类名
        String thisClassQualified = classFile.getClassName();
        if (thisClassQualified == null) {
            thisClassQualified = "UnknownClass";
        }
        String thisClassSimple = thisClassQualified.substring(thisClassQualified.lastIndexOf("/") + 1);

        // 包声明
        String packageName = thisClassQualified.substring(0, thisClassQualified.lastIndexOf("/")).replace("/", ".");
        sb.append("package ").append(packageName).append(";\n\n");

        // 收集导入
        Set<String> imports = collectImports(classFile);
        for (String imp : imports) {
            sb.append("import ").append(imp).append(";\n");
        }
        if (!imports.isEmpty()) {
            sb.append("\n");
        }

        // 类定义开始
        String accessFlag = AccessFlagConvertor.classAccessFlagConvertor(
                String.format("%04X", classFile.getAccessFlag().value));
        sb.append(accessFlag).append("class ").append(thisClassSimple).append("{\n\n");

        // 字段
        for (int i = 0; i < classFile.fieldInfo.getFieldCount(); i++) {
            FieldInfo fieldInfo = classFile.fieldInfo.getField(i);
            String fieldSrc = generateFieldSrc(fieldInfo, classFile);
            if (fieldSrc != null) {
                sb.append(fieldSrc);
            }
        }

        // 方法
        for (int i = 0; i < classFile.methodInfo.getMethodCount(); i++) {
            MethodInfo methodInfo = classFile.methodInfo.getMethod(i);
            String methodSrc = generateMethodSrc(methodInfo, classFile, thisClassSimple);
            if (methodSrc != null) {
                sb.append(methodSrc);
            }
        }

        sb.append("}\n");
        return sb.toString();
    }

    /**
     * 收集需要的导入
     */
    private static Set<String> collectImports(ClassFile classFile) {
        Set<String> imports = new TreeSet<>();
        // TODO: 分析字段类型，添加需要的导入
        return imports;
    }

    /**
     * 生成字段源代码
     */
    private static String generateFieldSrc(FieldInfo fieldInfo, ClassFile classFile) {
        List<AbstractConstantPool> poolList = classFile.poolInfo.getPoolList();

        // 访问标志
        String accessFlag = AccessFlagConvertor.fieldAccessFlagConvertor(
                String.format("%04X", fieldInfo.getAccessFlags().value));

        // 字段名
        int nameIndex = fieldInfo.getNameIndex().value - 1;
        String fieldName = getUtf8String(poolList, nameIndex);

        // 描述符
        int descIndex = fieldInfo.getDescriptorIndex().value - 1;
        String descriptor = getUtf8String(poolList, descIndex);

        // 字段类型
        String fieldType = ParamsConvertor.paramsConvertorFieldType(descriptor);

        return "\t" + accessFlag + fieldType + " " + fieldName + ";\n";
    }

    /**
     * 生成方法源代码
     */
    private static String generateMethodSrc(MethodInfo methodInfo, ClassFile classFile, String thisClassSimple) {
        List<AbstractConstantPool> poolList = classFile.poolInfo.getPoolList();

        // 方法名
        int nameIndex = methodInfo.getNameIndex().value - 1;
        String methodName = getUtf8String(poolList, nameIndex);

        // 描述符
        int descIndex = methodInfo.getDescriptorIndex().value - 1;
        String descriptor = getUtf8String(poolList, descIndex);

        // 访问标志
        String accessFlag = AccessFlagConvertor.methodAccessFlagConvertor(
                String.format("%04X", methodInfo.getAccessFlags().value));

        // 构造方法处理
        if ("<init>".equals(methodName)) {
            methodName = thisClassSimple;
        }

        // 方法签名
        String methodSignature = jointMethodReturnNameParams(methodName, descriptor);

        // 异常
        String exceptions = throwExceptionsJudge(methodInfo, classFile);

        // 判断是否是抽象或 native 方法
        short accessValue = methodInfo.getAccessFlags().value;
        boolean isAbstract = (accessValue & 0x0400) != 0;
        boolean isNative = (accessValue & 0x0100) != 0;
        boolean isStatic = (accessValue & 0x0008) != 0;

        // 静态初始化方法
        if ("<clinit>".equals(methodName)) {
            return generateStaticInitializer(methodInfo, classFile, thisClassSimple);
        }

        if (isAbstract || isNative) {
            return "\t" + accessFlag + methodSignature + exceptions + ";\n\n";
        }

        // 获取 Code 属性
        Code codeAttr = findCodeAttribute(methodInfo);

        StringBuilder methodBody = new StringBuilder();
        methodBody.append("\t").append(accessFlag).append(methodSignature)
                .append(exceptions).append("{\n");

        if (codeAttr != null) {
            // 收集局部变量表
            Map<Integer, String> localVars = new LinkedHashMap<>();
            if (!isStatic) {
                localVars.put(0, "this");
            }
            collectLocalVariables(codeAttr, classFile, localVars);

            // 生成方法体
            String body = decompileMethodBody(codeAttr, classFile, thisClassSimple, localVars);
            methodBody.append(body);
        }

        methodBody.append("\t}\n\n");
        return methodBody.toString();
    }

    /**
     * 生成静态初始化块
     */
    private static String generateStaticInitializer(MethodInfo methodInfo, ClassFile classFile, String thisClassSimple) {
        Code codeAttr = findCodeAttribute(methodInfo);
        if (codeAttr == null) {
            return "";
        }

        Map<Integer, String> localVars = new LinkedHashMap<>();
        String body = decompileMethodBody(codeAttr, classFile, thisClassSimple, localVars);

        // 清理 return; 语句
        body = body.replace("\t\treturn;\n", "");
        // body 现在是 "\t\ti = 0;\n" 格式
        if (body.trim().isEmpty()) {
            return "";  // 空初始化块不生成
        }

        // 将 body 的缩进从 \t\t 改为 \t（static 块内一层缩进）
        body = body.replace("\t\t", "\t");

        StringBuilder sb = new StringBuilder();
        sb.append("\tstatic {\n");
        sb.append(body);
        sb.append("\t}\n\n");
        return sb.toString();
    }

    /**
     * 提取方法参数名
     */
    private static List<String> extractMethodParams(String descriptor, Map<Integer, String> localVars) {
        List<String> params = new ArrayList<>();
        int parenOpen = descriptor.indexOf('(');
        int parenClose = descriptor.indexOf(')');
        if (parenOpen == -1 || parenClose == -1) {
            return params;
        }

        String paramTypes = descriptor.substring(parenOpen + 1, parenClose);
        int slot = 1; // 0 is 'this' for non-static
        int i = 0;
        while (i < paramTypes.length()) {
            char c = paramTypes.charAt(i);
            String type;
            int width;
            switch (c) {
                case 'B':
                case 'C':
                case 'F':
                case 'I':
                case 'S':
                case 'Z':
                    type = "int";
                    width = 1;
                    break;
                case 'J':
                    type = "long";
                    width = 2;
                    break;
                case 'D':
                    type = "double";
                    width = 2;
                    break;
                case 'L':
                    int semi = paramTypes.indexOf(';', i);
                    String classType = paramTypes.substring(i + 1, semi);
                    type = classType.substring(classType.lastIndexOf('/') + 1);
                    width = 1;
                    i = semi;
                    break;
                case '[':
                    StringBuilder sb = new StringBuilder();
                    int j = i;
                    while (j < paramTypes.length() && paramTypes.charAt(j) == '[') {
                        sb.append("[]");
                        j++;
                    }
                    char elemType = paramTypes.charAt(j);
                    String elemTypeStr = null;
                    switch (elemType) {
                        case 'B':
                            elemTypeStr = "byte";
                            break;
                        case 'C':
                            elemTypeStr = "char";
                            break;
                        case 'F':
                            elemTypeStr = "float";
                            break;
                        case 'I':
                            elemTypeStr = "int";
                            break;
                        case 'J':
                            elemTypeStr = "long";
                            break;
                        case 'S':
                            elemTypeStr = "short";
                            break;
                        case 'Z':
                            elemTypeStr = "boolean";
                            break;
                        case 'L':
                            int end = paramTypes.indexOf(';', j);
                            elemTypeStr = paramTypes.substring(j + 1, end);
                            elemTypeStr = elemTypeStr.substring(elemTypeStr.lastIndexOf('/') + 1);
                            break;
                    }
                    type = elemTypeStr + sb.toString();
                    width = 1;
                    i = j;
                    break;
                default:
                    type = "Object";
                    width = 1;
            }
            localVars.put(slot, "arg" + slot);
            params.add(type + " arg" + slot);
            slot += width;
            i++;
        }
        return params;
    }

    /**
     * 生成方法体
     */
    private static String decompileMethodBody(Code codeAttr, ClassFile classFile,
                                              String thisClassName, Map<Integer, String> localVars) {
        List<AbstractConstantPool> poolList = classFile.poolInfo.getPoolList();
        List<DecompilerInstruction> instructions = decodeInstructions(codeAttr.getCode(), poolList);

        // 设置行号
        Map<Integer, Integer> pcToLine = new HashMap<>();
        if (codeAttr.getAttributes() != null) {
            for (AbstractAttribute attr : codeAttr.getAttributes()) {
                if (attr instanceof LineNumberTable) {
                    LineNumberTable lnt = (LineNumberTable) attr;
                    for (LineNumberTable.LineNumberInfo lni : lnt.getLineNumberTable()) {
                        pcToLine.put((int) lni.getStartPc().value, (int) lni.getLineNumber().value);
                    }
                }
            }
        }

        // 按 PC 顺序设置行号，没有行号的指令继承前一条指令的行号
        int lastLine = 0;
        for (DecompilerInstruction inst : instructions) {
            Integer line = pcToLine.get(inst.pc);
            if (line != null && line > 0) {
                lastLine = line;
            }
            inst.lineNumber = lastLine;
        }

        // 按行号分组
        Map<Integer, List<DecompilerInstruction>> instructionsByLine = new LinkedHashMap<>();
        for (DecompilerInstruction inst : instructions) {
            int line = inst.lineNumber > 0 ? inst.lineNumber : 0;
            instructionsByLine.computeIfAbsent(line, k -> new ArrayList<>()).add(inst);
        }

        StringBuilder sb = new StringBuilder();

        for (Entry<Integer, List<DecompilerInstruction>> entry : instructionsByLine.entrySet()) {
            List<DecompilerInstruction> lineInstrs = entry.getValue();
            if (lineInstrs.isEmpty()) continue;

            String javaStmt = generateJavaStatement(lineInstrs, poolList, thisClassName, localVars);
            if (javaStmt != null && !javaStmt.isEmpty()) {
                sb.append(INDENT).append(javaStmt).append("\n");
            }
        }

        if (sb.length() == 0) {
            sb.append(INDENT).append("// (empty method)\n");
        }

        return sb.toString();
    }

    /**
     * 从字节码指令生成一行 Java 语句
     */
    private static String generateJavaStatement(List<DecompilerInstruction> instructions,
                                                List<AbstractConstantPool> poolList,
                                                String thisClassName,
                                                Map<Integer, String> localVars) {
        if (instructions.isEmpty()) {
            return "";
        }

        // 分离 return 指令和其他指令
        List<DecompilerInstruction> nonReturnInstrs = new ArrayList<>();
        boolean hasReturn = false;
        for (DecompilerInstruction inst : instructions) {
            if ("return".equals(inst.opcode) || "ireturn".equals(inst.opcode) || "areturn".equals(inst.opcode)) {
                hasReturn = true;
            } else {
                nonReturnInstrs.add(inst);
            }
        }

        StringBuilder result = new StringBuilder();

        // 先处理非 return 指令
        if (!nonReturnInstrs.isEmpty()) {
            // 第一步：过滤掉 invokespecial <init>（构造方法调用）
            List<DecompilerInstruction> filtered = new ArrayList<>();
            for (DecompilerInstruction inst : nonReturnInstrs) {
                if ("invokespecial".equals(inst.opcode) && inst.operand != null) {
                    // 检查是否是 <init> 方法
                    String methodName = resolveMethodName((Integer) inst.operand, poolList);
                    if ("<init>".equals(methodName)) {
                        continue; // 跳过构造方法调用
                    }
                }
                filtered.add(inst);
            }
            nonReturnInstrs = filtered;

            // 检查静态字段赋值: iconst_1 putstatic #13
            String staticFieldAssign = matchStaticFieldAssignment(nonReturnInstrs, poolList);
            if (staticFieldAssign != null) {
                result.append(staticFieldAssign);
            } else {
                // 检查实例字段赋值: aload_0 iconst_1 putfield X
                String fieldAssign = matchFieldAssignment(nonReturnInstrs, poolList);
                if (fieldAssign != null) {
                    result.append(fieldAssign);
                } else {
                    // 检查局部变量赋值: iconst_1 istore_1
                    String varAssign = matchVariableAssignment(nonReturnInstrs, localVars);
                    if (varAssign != null) {
                        result.append(varAssign);
                    } else {
                        // 检查方法调用
                        String methodCall = matchMethodCall(nonReturnInstrs, poolList);
                        if (methodCall != null) {
                            result.append(methodCall);
                        } else {
                            // 默认：生成注释
                            StringBuilder sb = new StringBuilder();
                            for (DecompilerInstruction inst : nonReturnInstrs) {
                                // 跳过无操作数的 aload_0
                                if ("aload_0".equals(inst.opcode) && inst.operand == null) {
                                    continue;
                                }
                                if (sb.length() > 0) sb.append(" ");
                                sb.append(inst.opcode);
                                if (inst.operand != null) {
                                    sb.append(" ").append(inst.operand);
                                }
                            }
                            if (sb.length() > 0) {
                                result.append("// ").append(sb.toString());
                            }
                        }
                    }
                }
            }
        }

        // 如果有 return，追加 return 语句
        if (hasReturn) {
            if (result.length() > 0) {
                result.append("\n").append(INDENT);
            }
            result.append("return;");
        }

        return result.toString();
    }

    /**
     * 匹配静态字段赋值模式: <value> putstatic #<index>
     */
    private static String matchStaticFieldAssignment(List<DecompilerInstruction> instructions,
                                                     List<AbstractConstantPool> poolList) {
        if (instructions.isEmpty()) return null;

        for (int i = 0; i < instructions.size(); i++) {
            DecompilerInstruction inst = instructions.get(i);
            if ("putstatic".equals(inst.opcode) && inst.operand != null) {
                // 获取字段名
                String fieldName = resolveFieldName((Integer) inst.operand, poolList);
                if (fieldName == null) {
                    fieldName = "field" + inst.operand;
                }

                // 获取赋的值（指令序列中 putstatic 之前的部分）
                String value = inferValue(instructions.subList(0, i), poolList);
                if (value == null) {
                    value = "0";
                }

                return fieldName + " = " + value + ";";
            }
        }
        return null;
    }

    /**
     * 匹配实例字段赋值模式: aload_0 <value> putfield <field_index>
     */
    private static String matchFieldAssignment(List<DecompilerInstruction> instructions,
                                               List<AbstractConstantPool> poolList) {
        if (instructions.size() < 3) return null;

        for (int i = 0; i < instructions.size(); i++) {
            DecompilerInstruction inst = instructions.get(i);
            if ("putfield".equals(inst.opcode) && inst.operand != null) {
                if (i < 2) continue;

                // 获取字段名
                String fieldName = resolveFieldName((Integer) inst.operand, poolList);
                if (fieldName == null) {
                    fieldName = "field" + inst.operand;
                }

                // 获取赋的值
                String value = inferValue(instructions.subList(0, i), poolList);
                if (value == null) {
                    value = "/* value */";
                }

                return "this." + fieldName + " = " + value + ";";
            }
        }
        return null;
    }

    /**
     * 匹配局部变量赋值模式: <const> istore_<n> 或 <const> istore <n>
     */
    private static String matchVariableAssignment(List<DecompilerInstruction> instructions,
                                                  Map<Integer, String> localVars) {
        if (instructions.isEmpty()) return null;

        for (int i = 0; i < instructions.size(); i++) {
            DecompilerInstruction inst = instructions.get(i);
            if (inst.opcode.startsWith("istore")) {
                Integer varIndex = null;
                if (inst.opcode.equals("istore")) {
                    varIndex = (Integer) inst.operand;
                } else {
                    varIndex = Integer.parseInt(inst.opcode.substring("istore_".length()));
                }

                if (varIndex != null) {
                    String varName = localVars.getOrDefault(varIndex, "var" + varIndex);
                    String value = inferValue(instructions.subList(0, i), null);
                    if (value == null) {
                        value = "0";
                    }
                    return varName + " = " + value + ";";
                }
            }
        }
        return null;
    }

    /**
     * 匹配方法调用模式
     */
    private static String matchMethodCall(List<DecompilerInstruction> instructions,
                                          List<AbstractConstantPool> poolList) {
        for (DecompilerInstruction inst : instructions) {
            if (("invokespecial".equals(inst.opcode) || "invokestatic".equals(inst.opcode)
                    || "invokevirtual".equals(inst.opcode)) && inst.operand != null) {
                String methodName = resolveMethodName((Integer) inst.operand, poolList);
                if (methodName != null) {
                    // 跳过 Object.<init> 调用，在构造方法中自动调用
                    if ("<init>".equals(methodName) || "<clinit>".equals(methodName)) {
                        return null;
                    }
                    return methodName + "();";
                }
            }
        }
        return null;
    }

    /**
     * 从指令序列推断值
     */
    private static String inferValue(List<DecompilerInstruction> instructions,
                                     List<AbstractConstantPool> poolList) {
        if (instructions.isEmpty()) return null;

        List<String> stack = new ArrayList<>();
        for (DecompilerInstruction inst : instructions) {
            switch (inst.opcode) {
                // int 常量
                case "iconst_m1":
                    stack.add("-1");
                    break;
                case "iconst_0":
                    stack.add("0");
                    break;
                case "iconst_1":
                    stack.add("1");
                    break;
                case "iconst_2":
                    stack.add("2");
                    break;
                case "iconst_3":
                    stack.add("3");
                    break;
                case "iconst_4":
                    stack.add("4");
                    break;
                case "iconst_5":
                    stack.add("5");
                    break;
                case "bipush":
                case "sipush":
                    if (inst.operand != null) {
                        stack.add(String.valueOf(inst.operand));
                    }
                    break;
                // long 常量
                case "lconst_0":
                    stack.add("0L");
                    break;
                case "lconst_1":
                    stack.add("1L");
                    break;
                // float 常量
                case "fconst_0":
                    stack.add("0.0f");
                    break;
                case "fconst_1":
                    stack.add("1.0f");
                    break;
                case "fconst_2":
                    stack.add("2.0f");
                    break;
                // double 常量
                case "dconst_0":
                    stack.add("0.0");
                    break;
                case "dconst_1":
                    stack.add("1.0");
                    break;
                // 加载常量 (ldc) - 从常量池加载
                case "ldc":
                case "ldc_w":
                case "ldc2_w":
                    if (poolList != null && inst.operand != null) {
                        String constVal = resolveConstantValue((Integer) inst.operand, poolList);
                        if (constVal != null) {
                            stack.add(constVal);
                        }
                    }
                    break;
                // this 引用
                case "aload_0":
                    if ("this".equals(localVarsGet(instructions, 0)) || isThisReference(instructions)) {
                        stack.add("this");
                    } else {
                        stack.add("/* this */");
                    }
                    break;
                case "aload":
                case "aload_1":
                case "aload_2":
                case "aload_3":
                    stack.add("/* local var */");
                    break;
                default:
                    if (!inst.opcode.startsWith("istore") && !inst.opcode.startsWith("astore")
                            && !inst.opcode.startsWith("putstatic") && !inst.opcode.startsWith("putfield")) {
                        // stack.add("/* " + inst.opcode + " */");
                    }
                    break;
            }
        }

        if (!stack.isEmpty()) {
            return stack.get(stack.size() - 1);
        }
        return null;
    }

    /**
     * 判断是否是 this 引用
     */
    private static boolean isThisReference(List<DecompilerInstruction> instructions) {
        for (DecompilerInstruction inst : instructions) {
            if ("aload_0".equals(inst.opcode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取局部变量名
     */
    private static String localVarsGet(List<DecompilerInstruction> instructions, int slot) {
        // 简化实现，实际应该从 localVars Map 中获取
        return null;
    }

    /**
     * 解析常量池中的常量值
     */
    private static String resolveConstantValue(int constantPoolIndex, List<AbstractConstantPool> poolList) {
        if (constantPoolIndex <= 0 || constantPoolIndex > poolList.size()) {
            return null;
        }
        AbstractConstantPool pool = poolList.get(constantPoolIndex - 1);
        if (pool instanceof ConstantIntegerInfo) {
            return String.valueOf(((ConstantIntegerInfo) pool).getBytes().getValue());
        } else if (pool instanceof ConstantLongInfo) {
            return ((ConstantLongInfo) pool).getBytes().getValue() + "L";
        } else if (pool instanceof ConstantFloatInfo) {
            return ((ConstantFloatInfo) pool).getBytes().getValue() + "f";
        } else if (pool instanceof ConstantDoubleInfo) {
            return String.valueOf(((ConstantDoubleInfo) pool).getBytes().getValue());
        } else if (pool instanceof ConstantClassInfo) {
            int stringIndex = ((ConstantClassInfo) pool).getStringIndex().value - 1;
            if (stringIndex >= 0 && stringIndex < poolList.size()) {
                AbstractConstantPool utf8Pool = poolList.get(stringIndex);
                if (utf8Pool instanceof Utf8Info) {
                    return "\"" + ((Utf8Info) utf8Pool).getValue() + "\"";
                }
            }
        }
        return null;
    }

    /**
     * 解析字段名
     */
    private static String resolveFieldName(int constantPoolIndex, List<AbstractConstantPool> poolList) {
        if (constantPoolIndex <= 0 || constantPoolIndex > poolList.size()) {
            return null;
        }
        AbstractConstantPool pool = poolList.get(constantPoolIndex - 1);
        if (!(pool instanceof FieldRefInfo)) {
            return null;
        }
        FieldRefInfo fieldRef = (FieldRefInfo) pool;
        int nameAndTypeIndex = fieldRef.getNameIndex().value - 1;
        if (nameAndTypeIndex < 0 || nameAndTypeIndex >= poolList.size()) {
            return null;
        }
        AbstractConstantPool ntPool = poolList.get(nameAndTypeIndex);
        if (!(ntPool instanceof ConstantNameAndTypeInfo)) {
            return null;
        }
        int nameIndex = ((ConstantNameAndTypeInfo) ntPool).getNameIndex().value - 1;
        if (nameIndex < 0 || nameIndex >= poolList.size()) {
            return null;
        }
        AbstractConstantPool namePool = poolList.get(nameIndex);
        if (!(namePool instanceof Utf8Info)) {
            return null;
        }
        return ((Utf8Info) namePool).getValue();
    }

    /**
     * 解析方法名
     */
    private static String resolveMethodName(int constantPoolIndex, List<AbstractConstantPool> poolList) {
        if (constantPoolIndex <= 0 || constantPoolIndex > poolList.size()) {
            return null;
        }
        AbstractConstantPool pool = poolList.get(constantPoolIndex - 1);
        if (!(pool instanceof MethodRefInfo)) {
            return null;
        }
        MethodRefInfo methodRef = (MethodRefInfo) pool;
        int nameAndTypeIndex = methodRef.getNameIndex().value - 1;
        if (nameAndTypeIndex < 0 || nameAndTypeIndex >= poolList.size()) {
            return null;
        }
        AbstractConstantPool ntPool = poolList.get(nameAndTypeIndex);
        if (!(ntPool instanceof ConstantNameAndTypeInfo)) {
            return null;
        }
        int nameIndex = ((ConstantNameAndTypeInfo) ntPool).getNameIndex().value - 1;
        if (nameIndex < 0 || nameIndex >= poolList.size()) {
            return null;
        }
        AbstractConstantPool namePool = poolList.get(nameIndex);
        if (!(namePool instanceof Utf8Info)) {
            return null;
        }
        return ((Utf8Info) namePool).getValue();
    }

    /**
     * 解码字节码指令
     */
    private static List<DecompilerInstruction> decodeInstructions(List<U1> codeBytes,
                                                                  List<AbstractConstantPool> poolList) {
        List<DecompilerInstruction> instructions = new ArrayList<>();

        int pc = 0;
        while (pc < codeBytes.size()) {
            int opcode = codeBytes.get(pc).value & 0xFF;
            String hexCode = String.format("%02x", opcode);
            String opcodeName = CodeConvertor.codeConvertor(hexCode);

            if (opcodeName == null) {
                opcodeName = "unknown_" + hexCode;
            }

            DecompilerInstruction inst = new DecompilerInstruction();
            inst.pc = pc;
            inst.opcode = opcodeName;
            inst.lineNumber = 0;

            pc++;

            // 处理操作数
            int operandBytes = OperandBytesJudge.operandBytesCount(opcodeName);
            if (operandBytes > 0 && pc + operandBytes <= codeBytes.size()) {
                StringBuilder operand = new StringBuilder();
                for (int j = 0; j < operandBytes; j++) {
                    operand.append(String.format("%02x", codeBytes.get(pc).value));
                    pc++;
                }
                try {
                    inst.operand = Integer.parseInt(operand.toString(), 16);
                } catch (NumberFormatException e) {
                    inst.operand = operand.toString();
                }
            }

            instructions.add(inst);
        }

        return instructions;
    }

    /**
     * 收集局部变量信息
     */
    private static void collectLocalVariables(Code codeAttr, ClassFile classFile,
                                              Map<Integer, String> localVars) {
        if (codeAttr.getAttributes() == null) {
            return;
        }

        List<AbstractConstantPool> poolList = classFile.poolInfo.getPoolList();

        for (AbstractAttribute attr : codeAttr.getAttributes()) {
            if (!(attr instanceof LocalVariableTable)) {
                continue;
            }

            LocalVariableTable lvt = (LocalVariableTable) attr;
            for (LocalVariableTable.LocalVariableInfo lvi : lvt.getLocalVariableTable()) {
                int slotIndex = lvi.getIndex().value;
                int nameIdx = lvi.getNameIndex().value - 1;
                String name = getUtf8String(poolList, nameIdx);
                if (name != null && !name.isEmpty()) {
                    localVars.put(slotIndex, name);
                }
            }
        }
    }

    /**
     * 获取常量池中的 UTF8 字符串
     */
    private static String getUtf8String(List<AbstractConstantPool> poolList, int index) {
        if (index < 0 || index >= poolList.size()) {
            return "";
        }
        AbstractConstantPool pool = poolList.get(index);
        if (pool instanceof Utf8Info) {
            return ((Utf8Info) pool).getValue();
        }
        return "";
    }

    /**
     * 查找 Code 属性
     */
    private static Code findCodeAttribute(MethodInfo methodInfo) {
        if (methodInfo.getAttributes() == null) {
            return null;
        }
        for (AbstractAttribute attr : methodInfo.getAttributes()) {
            if (attr instanceof Code) {
                return (Code) attr;
            }
        }
        return null;
    }

    /**
     * 判断异常
     */
    private static String throwExceptionsJudge(MethodInfo methodInfo, ClassFile classFile) {
        // TODO: 实现异常处理
        return "";
    }

    /**
     * 拼接方法返回类型、方法名和参数
     */
    private static String jointMethodReturnNameParams(String methodName, String descriptor) {
        int parenOpen = descriptor.indexOf('(');
        int parenClose = descriptor.indexOf(')');
        if (parenOpen == -1 || parenClose == -1) {
            return methodName + "()";
        }

        String params = descriptor.substring(parenOpen + 1, parenClose);
        String returnType = descriptor.substring(parenClose + 1);

        // 转换参数类型
        String convertedParams = ParamsConvertor.paramsConvertorMethodParams(params);

        // 转换返回类型
        String convertedReturn = ParamsConvertor.paramsConvertorFieldType(returnType);

        return convertedReturn + " " + methodName + "(" + convertedParams + ")";
    }

    /**
     * 指令内部类
     */
    private static class DecompilerInstruction {
        int pc;
        String opcode;
        Object operand;
        int lineNumber;
    }
}
