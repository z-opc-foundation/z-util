package com.zifang.util.source;

import com.zifang.util.source.generator.JavaSourceGenerator;
import com.zifang.util.source.generator.info.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * JavaSourceGenerator 测试
 */

/**
 * JavaSourceGeneratorTest类。
 */
public class JavaSourceGeneratorTest {

    private JavaSourceGenerator generator = new JavaSourceGenerator();

    @Test
    /**
     * testGenerateSimpleClass方法。
     */
    public void testGenerateSimpleClass() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setSimpleClassName("User");
        classInfo.setPackageName("com.example");
        classInfo.setModifiers(java.lang.reflect.Modifier.PUBLIC);
        classInfo.setInterfaceType(false);  // 修复 NPE

        String result = generator.generate(classInfo);

        assertTrue(result.contains("package com.example"));
        assertTrue(result.contains("public class User"));
    }

    @Test
    /**
     * testGenerateClassWithFields方法。
     */
    public void testGenerateClassWithFields() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setSimpleClassName("User");
        classInfo.setPackageName("com.example");
        classInfo.setModifiers(java.lang.reflect.Modifier.PUBLIC);
        classInfo.setInterfaceType(false);

        FieldInfo field = new FieldInfo("String", "name");
        List<FieldInfo> fields = new ArrayList<>();
        fields.add(field);
        classInfo.setFields(fields);

        String result = generator.generate(classInfo);

        assertTrue(result.contains("String name"));
        assertTrue(result.contains("class User"));
    }

    @Test
    /**
     * testGenerateClassWithMethods方法。
     */
    public void testGenerateClassWithMethods() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setSimpleClassName("User");
        classInfo.setPackageName("com.example");
        classInfo.setModifiers(java.lang.reflect.Modifier.PUBLIC);
        classInfo.setInterfaceType(false);

        MethodInfo method = new MethodInfo();
        method.setMethodName("getName");
        method.setReturnType("String");
        method.setModifier(java.lang.reflect.Modifier.PUBLIC);
        List<String> statements = new ArrayList<>();
        statements.add("return this.name;");
        method.setStatements(statements);

        List<MethodInfo> methods = new ArrayList<>();
        methods.add(method);
        classInfo.setMethods(methods);

        String result = generator.generate(classInfo);

        assertTrue(result.contains("String getName()"));
        assertTrue(result.contains("return this.name;"));
    }

    @Test
    /**
     * testGenerateClassWithSuperClass方法。
     */
    public void testGenerateClassWithSuperClass() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setSimpleClassName("Dog");
        classInfo.setPackageName("com.example");
        classInfo.setModifiers(java.lang.reflect.Modifier.PUBLIC);
        classInfo.setInterfaceType(false);

        ClassInfo animal = new ClassInfo();
        animal.setSimpleClassName("Animal");
        classInfo.setSuperClass(animal);

        String result = generator.generate(classInfo);

        assertTrue(result.contains("extends Animal"));
    }

    @Test
    /**
     * testGenerateClassWithInterfaces方法。
     */
    public void testGenerateClassWithInterfaces() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setSimpleClassName("Dog");
        classInfo.setPackageName("com.example");
        classInfo.setModifiers(java.lang.reflect.Modifier.PUBLIC);
        classInfo.setInterfaceType(false);

        List<ClassInfo> interfaces = new ArrayList<>();
        ClassInfo comparable = new ClassInfo();
        comparable.setSimpleClassName("Comparable");
        interfaces.add(comparable);
        classInfo.setInterfaces(interfaces);

        String result = generator.generate(classInfo);

        assertTrue(result.contains("implements Comparable"));
    }

    @Test
    /**
     * testGenerateInterface方法。
     */
    public void testGenerateInterface() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setSimpleClassName("MyInterface");
        classInfo.setPackageName("com.example");
        classInfo.setInterfaceType(true);
        classInfo.setModifiers(java.lang.reflect.Modifier.PUBLIC);

        String result = generator.generate(classInfo);

        // 接口生成时修饰符为 "public "，类型输出为 "interface MyInterface"
        assertTrue(result.contains("public"));
        assertTrue(result.contains("interface MyInterface"));
    }

    @Test
    /**
     * testGenerateClassWithAnnotations方法。
     */
    public void testGenerateClassWithAnnotations() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setSimpleClassName("User");
        classInfo.setPackageName("com.example");
        classInfo.setModifiers(java.lang.reflect.Modifier.PUBLIC);
        classInfo.setInterfaceType(false);

        AnnotationInfo annot = new AnnotationInfo("Deprecated");
        List<AnnotationInfo> annotations = new ArrayList<>();
        annotations.add(annot);
        classInfo.setAnnotations(annotations);

        String result = generator.generate(classInfo);

        assertTrue(result.contains("@Deprecated"));
    }

    @Test
    /**
     * testGenerateFieldWithInitializer方法。
     */
    public void testGenerateFieldWithInitializer() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setSimpleClassName("Constants");
        classInfo.setPackageName("com.example");
        classInfo.setModifiers(java.lang.reflect.Modifier.PUBLIC);
        classInfo.setInterfaceType(false);

        FieldInfo field = new FieldInfo("int", "MAX_SIZE", new int[]{}, "100");
        List<FieldInfo> fields = new ArrayList<>();
        fields.add(field);
        classInfo.setFields(fields);

        String result = generator.generate(classInfo);

        assertTrue(result.contains("= 100"));
    }

    @Test
    /**
     * testGenerateMethodWithParameters方法。
     */
    public void testGenerateMethodWithParameters() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setSimpleClassName("Calculator");
        classInfo.setPackageName("com.example");
        classInfo.setModifiers(java.lang.reflect.Modifier.PUBLIC);
        classInfo.setInterfaceType(false);

        MethodInfo method = new MethodInfo();
        method.setMethodName("add");
        method.setReturnType("int");
        method.setModifier(java.lang.reflect.Modifier.PUBLIC);

        List<MethodParameterPair> params = new ArrayList<>();
        MethodParameterPair p1 = new MethodParameterPair();
        p1.setParamType("int");
        p1.setParamName("a");
        params.add(p1);
        MethodParameterPair p2 = new MethodParameterPair();
        p2.setParamType("int");
        p2.setParamName("b");
        params.add(p2);
        method.setMethodParameterPairs(params);

        List<String> statements = new ArrayList<>();
        statements.add("return a + b;");
        method.setStatements(statements);

        List<MethodInfo> methods = new ArrayList<>();
        methods.add(method);
        classInfo.setMethods(methods);

        String result = generator.generate(classInfo);

        assertTrue(result.contains("int add(int a, int b)"));
        assertTrue(result.contains("return a + b;"));
    }

    @Test
    /**
     * testGenerateMethodWithAnnotations方法。
     */
    public void testGenerateMethodWithAnnotations() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setSimpleClassName("User");
        classInfo.setPackageName("com.example");
        classInfo.setModifiers(java.lang.reflect.Modifier.PUBLIC);
        classInfo.setInterfaceType(false);

        MethodInfo method = new MethodInfo();
        method.setMethodName("getName");
        method.setReturnType("String");
        method.setModifier(java.lang.reflect.Modifier.PUBLIC);

        AnnotationInfo override = new AnnotationInfo("Override");
        method.getAnnotations().add(override);

        List<String> statements = new ArrayList<>();
        statements.add("return name;");
        method.setStatements(statements);

        List<MethodInfo> methods = new ArrayList<>();
        methods.add(method);
        classInfo.setMethods(methods);

        String result = generator.generate(classInfo);

        assertTrue(result.contains("@Override"));
    }

    @Test
    /**
     * testGenerateFieldWithAnnotations方法。
     */
    public void testGenerateFieldWithAnnotations() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setSimpleClassName("User");
        classInfo.setPackageName("com.example");
        classInfo.setModifiers(java.lang.reflect.Modifier.PUBLIC);
        classInfo.setInterfaceType(false);

        FieldInfo field = new FieldInfo("String", "name");
        AnnotationInfo nullable = new AnnotationInfo("Nullable");
        field.getAnnotations().add(nullable);
        List<FieldInfo> fields = new ArrayList<>();
        fields.add(field);
        classInfo.setFields(fields);

        String result = generator.generate(classInfo);

        assertTrue(result.contains("@Nullable"));
        assertTrue(result.contains("String name"));
    }

    @Test
    /**
     * testGenerateAbstractClass方法。
     */
    public void testGenerateAbstractClass() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setSimpleClassName("AbstractDao");
        classInfo.setPackageName("com.example");
        classInfo.setModifiers(java.lang.reflect.Modifier.ABSTRACT | java.lang.reflect.Modifier.PUBLIC);
        classInfo.setInterfaceType(false);

        String result = generator.generate(classInfo);

        assertTrue(result.contains("public abstract class AbstractDao"));
    }

    @Test
    /**
     * testGenerateInnerClass方法。
     */
    public void testGenerateInnerClass() {
        ClassInfo outer = new ClassInfo();
        outer.setSimpleClassName("Outer");
        outer.setPackageName("com.example");
        outer.setModifiers(java.lang.reflect.Modifier.PUBLIC);
        outer.setInterfaceType(false);

        ClassInfo inner = new ClassInfo();
        inner.setSimpleClassName("Inner");
        inner.setModifiers(java.lang.reflect.Modifier.PRIVATE);
        inner.setInterfaceType(false);
        List<ClassInfo> innerClasses = new ArrayList<>();
        innerClasses.add(inner);
        outer.setInnerClasses(innerClasses);

        String result = generator.generate(outer);

        assertTrue(result.contains("class Inner"));
        assertTrue(result.contains("private "));
    }
}