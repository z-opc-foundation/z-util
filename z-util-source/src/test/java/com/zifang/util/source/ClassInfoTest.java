package com.zifang.util.source;

import com.zifang.util.source.generator.info.ClassInfo;
import com.zifang.util.source.generator.info.FieldInfo;
import com.zifang.util.source.generator.info.MethodInfo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * ClassInfo 模型测试
 */

/**
 * ClassInfoTest类。
 */
public class ClassInfoTest {

    @Test
    /**
     * testBuilder方法。
     */
    public void testBuilder() {
        ClassInfo classInfo = ClassInfo.build(
                false,
                java.lang.reflect.Modifier.PUBLIC,
                "com.example",
                new ArrayList<>(),
                "User",
                null,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );

        assertEquals("com.example", classInfo.getPackageName());
        assertEquals("User", classInfo.getSimpleClassName());
        assertFalse(classInfo.getInterfaceType());
        assertTrue(java.lang.reflect.Modifier.isPublic(classInfo.getModifiers()));
    }

    @Test
    /**
     * testAppendField方法。
     */
    public void testAppendField() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setSimpleClassName("Test");

        FieldInfo field = new FieldInfo("String", "name");
        classInfo.appendField(field);

        assertEquals(1, classInfo.getFields().size());
        assertEquals("name", classInfo.getFields().get(0).getValue());
    }

    @Test
    /**
     * testAppendFields方法。
     */
    public void testAppendFields() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setSimpleClassName("Test");

        List<FieldInfo> fields = new ArrayList<>();
        fields.add(new FieldInfo("String", "name"));
        fields.add(new FieldInfo("int", "age"));
        classInfo.appendFields(fields);

        assertEquals(2, classInfo.getFields().size());
    }

    @Test
    /**
     * testAppendMethod方法。
     */
    public void testAppendMethod() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setSimpleClassName("Test");

        MethodInfo method = new MethodInfo();
        method.setMethodName("getName");
        method.setReturnType("String");
        classInfo.appendMethod(method);

        assertEquals(1, classInfo.getMethods().size());
        assertEquals("getName", classInfo.getMethods().get(0).getMethodName());
    }

    @Test
    /**
     * testGetName方法。
     */
    public void testGetName() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setPackageName("com.example");
        classInfo.setSimpleClassName("User");

        assertEquals("com.example.User", classInfo.getName());
    }

    @Test
    /**
     * testSuperClass方法。
     */
    public void testSuperClass() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setSimpleClassName("Dog");

        ClassInfo animal = new ClassInfo();
        animal.setSimpleClassName("Animal");
        classInfo.setSuperClass(animal);

        assertNotNull(classInfo.getSuperClass());
        assertEquals("Animal", classInfo.getSuperClass().getSimpleClassName());
    }

    @Test
    /**
     * testInterfaces方法。
     */
    public void testInterfaces() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setSimpleClassName("Dog");

        List<ClassInfo> interfaces = new ArrayList<>();
        ClassInfo comparable = new ClassInfo();
        comparable.setSimpleClassName("Comparable");
        interfaces.add(comparable);
        classInfo.setInterfaces(interfaces);

        assertEquals(1, classInfo.getInterfaces().size());
        assertEquals("Comparable", classInfo.getInterfaces().get(0).getSimpleClassName());
    }

    @Test(expected = RuntimeException.class)
    /**
     * testAppendFieldNullCheck方法。
     */
    public void testAppendFieldNullCheck() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setSimpleClassName("Test");
        classInfo.setFields(null);

        classInfo.appendField(new FieldInfo("String", "name"));
    }

    @Test(expected = RuntimeException.class)
    /**
     * testAppendMethodNullCheck方法。
     */
    public void testAppendMethodNullCheck() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setSimpleClassName("Test");
        classInfo.setMethods(null);

        MethodInfo method = new MethodInfo();
        classInfo.appendMethod(method);
    }

    @Test
    /**
     * testInnerClasses方法。
     */
    public void testInnerClasses() {
        ClassInfo outer = new ClassInfo();
        outer.setSimpleClassName("Outer");

        ClassInfo inner = new ClassInfo();
        inner.setSimpleClassName("Inner");
        List<ClassInfo> innerClasses = new ArrayList<>();
        innerClasses.add(inner);
        outer.setInnerClasses(innerClasses);

        assertEquals(1, outer.getInnerClasses().size());
        assertEquals("Inner", outer.getInnerClasses().get(0).getSimpleClassName());
    }

    @Test
    /**
     * testImports方法。
     */
    public void testImports() {
        ClassInfo classInfo = new ClassInfo();
        List<String> imports = new ArrayList<>();
        imports.add("java.util.List");
        imports.add("java.util.Map");
        classInfo.setImports(imports);

        assertEquals(2, classInfo.getImports().size());
        assertTrue(classInfo.getImports().contains("java.util.List"));
    }

    @Test
    /**
     * testAnnotations方法。
     */
    public void testAnnotations() {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setSimpleClassName("Test");

        com.zifang.util.source.generator.info.AnnotationInfo annot =
                new com.zifang.util.source.generator.info.AnnotationInfo("Deprecated");
        List<com.zifang.util.source.generator.info.AnnotationInfo> annotations = new ArrayList<>();
        annotations.add(annot);
        classInfo.setAnnotations(annotations);

        assertEquals(1, classInfo.getAnnotations().size());
        assertEquals("Deprecated", classInfo.getAnnotations().get(0).getType());
    }
}