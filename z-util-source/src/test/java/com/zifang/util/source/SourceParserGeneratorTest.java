package com.zifang.util.source;

import com.zifang.util.source.generator.JavaSourceGenerator;
import com.zifang.util.source.generator.info.ClassInfo;
import com.zifang.util.source.parser.SourceCodeParser;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 源码解析与生成测试
 * <p>
 * 验证流程：解析 Java 源码 → ClassInfo → 生成源码 → 比较
 */

/**
 * SourceParserGeneratorTest类。
 */
public class SourceParserGeneratorTest {

    /**
     * 测试1: 解析 → 生成 → 再解析 → 比较 ClassInfo
     */
    @Test
    /**
     * testFullRoundTrip方法。
     */
    public void testFullRoundTrip() throws Exception {
        String originalSource =
                "package com.test;\n" +
                        "\n" +
                        "public class User {\n" +
                        "    private String name;\n" +
                        "    private int age;\n" +
                        "\n" +
                        "    public User() {\n" +
                        "    }\n" +
                        "\n" +
                        "    public String getName() {\n" +
                        "        return this.name;\n" +
                        "    }\n" +
                        "\n" +
                        "    public void setName(String name) {\n" +
                        "        this.name = name;\n" +
                        "    }\n" +
                        "}\n";

        SourceCodeParser parser = new SourceCodeParser();
        ClassInfo classInfo1 = parser.parseSource(originalSource);

        JavaSourceGenerator generator = new JavaSourceGenerator();
        String generatedSource = generator.generate(classInfo1);

        ClassInfo classInfo2 = parser.parseSource(generatedSource);

        assertEquals(classInfo1.getPackageName(), classInfo2.getPackageName());
        assertEquals(classInfo1.getSimpleClassName(), classInfo2.getSimpleClassName());
        assertEquals(classInfo1.getModifiers(), classInfo2.getModifiers());
        assertEquals(classInfo1.getFields().size(), classInfo2.getFields().size());
        assertEquals(classInfo1.getMethods().size(), classInfo2.getMethods().size());
    }

    /**
     * 测试2: 复杂类 - 带父类、接口、多个方法
     */
    @Test
    /**
     * testComplexClass方法。
     */
    public void testComplexClass() {
        String source =
                "package com.example;\n" +
                        "\n" +
                        "public abstract class Animal extends Object implements Runnable {\n" +
                        "    protected String name;\n" +
                        "    public static final int MAX_AGE = 100;\n" +
                        "\n" +
                        "    public abstract void speak();\n" +
                        "\n" +
                        "    public void run() {\n" +
                        "        System.out.println(\"Running\");\n" +
                        "    }\n" +
                        "}\n";

        SourceCodeParser parser = new SourceCodeParser();
        ClassInfo classInfo = parser.parseSource(source);

        assertEquals("com.example", classInfo.getPackageName());
        assertEquals("Animal", classInfo.getSimpleClassName());
        assertTrue(java.lang.reflect.Modifier.isAbstract(classInfo.getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isPublic(classInfo.getModifiers()));

        assertNotNull(classInfo.getSuperClass());
        assertEquals("Object", classInfo.getSuperClass().getSimpleClassName());

        assertEquals(1, classInfo.getInterfaces().size());
        assertEquals("Runnable", classInfo.getInterfaces().get(0).getSimpleClassName());

        assertEquals(2, classInfo.getFields().size());
        assertEquals(2, classInfo.getMethods().size());
    }

    /**
     * 测试3: 枚举解析
     */
    @Test
    /**
     * testEnumParsing方法。
     */
    public void testEnumParsing() {
        String source =
                "package com.example;\n" +
                        "\n" +
                        "public enum Season {\n" +
                        "    SPRING(\"春天\"),\n" +
                        "    SUMMER(\"夏天\"),\n" +
                        "    AUTUMN(\"秋天\"),\n" +
                        "    WINTER(\"冬天\");\n" +
                        "\n" +
                        "    private String chineseName;\n" +
                        "\n" +
                        "    private Season(String chineseName) {\n" +
                        "        this.chineseName = chineseName;\n" +
                        "    }\n" +
                        "\n" +
                        "    public String getChineseName() {\n" +
                        "        return this.chineseName;\n" +
                        "    }\n" +
                        "}\n";

        SourceCodeParser parser = new SourceCodeParser();
        ClassInfo classInfo = parser.parseSource(source);

        assertEquals("com.example", classInfo.getPackageName());
        assertEquals("Season", classInfo.getSimpleClassName());
        assertFalse(classInfo.getInterfaceType());
        assertEquals(5, classInfo.getFields().size());
        // 构造函数不是方法，所以只有 1 个方法 (getChineseName)
        assertEquals(1, classInfo.getMethods().size());

        System.out.println("=== 枚举解析结果 ===");
        System.out.println("包名: " + classInfo.getPackageName());
        System.out.println("类名: " + classInfo.getSimpleClassName());
        System.out.println("字段数: " + classInfo.getFields().size());
        System.out.println("方法数: " + classInfo.getMethods().size());
    }

    /**
     * 测试4: 注解解析
     */
    @Test
    /**
     * testAnnotationParsing方法。
     */
    public void testAnnotationParsing() {
        String source =
                "package com.example;\n" +
                        "\n" +
                        "public @interface MyAnnotation {\n" +
                        "    String value() default \"\";\n" +
                        "    int count() default 0;\n" +
                        "}\n";

        SourceCodeParser parser = new SourceCodeParser();
        ClassInfo classInfo = parser.parseSource(source);

        assertEquals("com.example", classInfo.getPackageName());
        assertEquals("MyAnnotation", classInfo.getSimpleClassName());
        assertFalse(classInfo.getInterfaceType());

        assertEquals(2, classInfo.getMethods().size());

        System.out.println("=== 注解解析结果 ===");
        System.out.println("包名: " + classInfo.getPackageName());
        System.out.println("类名: " + classInfo.getSimpleClassName());
        System.out.println("方法数: " + classInfo.getMethods().size());
        classInfo.getMethods().forEach(m -> System.out.println("  - " + m.getMethodName() + "(): " + m.getReturnType()));
    }

    /**
     * 测试5: 带注解的类解析
     */
    @Test
    /**
     * testClassWithAnnotations方法。
     */
    public void testClassWithAnnotations() {
        String source =
                "package com.example;\n" +
                        "\n" +
                        "@Deprecated\n" +
                        "public class Person {\n" +
                        "    @Deprecated\n" +
                        "    private String name;\n" +
                        "\n" +
                        "    @Deprecated\n" +
                        "    public Person(@Deprecated String name) {\n" +
                        "        this.name = name;\n" +
                        "    }\n" +
                        "\n" +
                        "    @Override\n" +
                        "    public String toString() {\n" +
                        "        return \"Person{name='\" + name + '\\'' + '}';\n" +
                        "    }\n" +
                        "}\n";

        SourceCodeParser parser = new SourceCodeParser();
        ClassInfo classInfo = parser.parseSource(source);

        assertEquals("Person", classInfo.getSimpleClassName());
        assertEquals(1, classInfo.getAnnotations().size());
        assertEquals("Deprecated", classInfo.getAnnotations().get(0).getType());

        assertEquals(1, classInfo.getFields().size());
        assertEquals(1, classInfo.getFields().get(0).getAnnotations().size());

        // 构造函数不是方法，所以只有 1 个方法 (toString)
        assertEquals(1, classInfo.getMethods().size());
        assertEquals(1, classInfo.getMethods().get(0).getAnnotations().size());

        System.out.println("=== 带注解的类解析结果 ===");
        System.out.println("类注解: " + classInfo.getAnnotations());
        System.out.println("字段注解: " + classInfo.getFields().get(0).getAnnotations());
        System.out.println("方法注解: " + classInfo.getMethods().get(0).getAnnotations());
    }

    /**
     * 测试6: 内部类解析
     */
    @Test
    /**
     * testInnerClassParsing方法。
     */
    public void testInnerClassParsing() {
        String source =
                "package com.example;\n" +
                        "\n" +
                        "public class Outer {\n" +
                        "    private String outerField;\n" +
                        "\n" +
                        "    public class Inner {\n" +
                        "        private String innerField;\n" +
                        "\n" +
                        "        public String getInnerField() {\n" +
                        "            return innerField;\n" +
                        "        }\n" +
                        "    }\n" +
                        "\n" +
                        "    public Outer() {\n" +
                        "    }\n" +
                        "\n" +
                        "    public String getOuterField() {\n" +
                        "        return outerField;\n" +
                        "    }\n" +
                        "}\n";

        SourceCodeParser parser = new SourceCodeParser();
        ClassInfo classInfo = parser.parseSource(source);

        assertEquals("Outer", classInfo.getSimpleClassName());
        assertEquals(1, classInfo.getInnerClasses().size());
        assertEquals("Inner", classInfo.getInnerClasses().get(0).getSimpleClassName());
        assertEquals(1, classInfo.getInnerClasses().get(0).getFields().size());
        assertEquals(1, classInfo.getInnerClasses().get(0).getMethods().size());

        System.out.println("=== 内部类解析结果 ===");
        System.out.println("外部类: " + classInfo.getSimpleClassName());
        System.out.println("内部类数: " + classInfo.getInnerClasses().size());
        System.out.println("内部类: " + classInfo.getInnerClasses().get(0).getSimpleClassName());
        System.out.println("内部类字段: " + classInfo.getInnerClasses().get(0).getFields().size());
        System.out.println("内部类方法: " + classInfo.getInnerClasses().get(0).getMethods().size());
    }

    /**
     * 测试7: 完整循环 - 枚举
     */
    @Test
    /**
     * testEnumRoundTrip方法。
     */
    public void testEnumRoundTrip() {
        String source =
                "package com.example;\n" +
                        "\n" +
                        "public enum Color {\n" +
                        "    RED, GREEN, BLUE;\n" +
                        "\n" +
                        "    private int value;\n" +
                        "\n" +
                        "    private Color() {\n" +
                        "        this.value = ordinal();\n" +
                        "    }\n" +
                        "\n" +
                        "    public int getValue() {\n" +
                        "        return this.value;\n" +
                        "    }\n" +
                        "}\n";

        SourceCodeParser parser = new SourceCodeParser();
        ClassInfo classInfo1 = parser.parseSource(source);

        JavaSourceGenerator generator = new JavaSourceGenerator();
        String generatedSource = generator.generate(classInfo1);

        System.out.println("=== 枚举生成结果 ===");
        System.out.println(generatedSource);

        ClassInfo classInfo2 = parser.parseSource(generatedSource);
        assertEquals(classInfo1.getSimpleClassName(), classInfo2.getSimpleClassName());
        assertEquals(classInfo1.getFields().size(), classInfo2.getFields().size());
        assertEquals(classInfo1.getMethods().size(), classInfo2.getMethods().size());
    }

    /**
     * 测试8: 完整循环 - 带注解的类
     */
    @Test
    /**
     * testAnnotatedClassRoundTrip方法。
     */
    public void testAnnotatedClassRoundTrip() {
        String source =
                "package com.example;\n" +
                        "\n" +
                        "@Deprecated\n" +
                        "public class AnnotatedClass {\n" +
                        "    @SuppressWarnings(\"unchecked\")\n" +
                        "    private String data;\n" +
                        "\n" +
                        "    @Override\n" +
                        "    public int hashCode() {\n" +
                        "        return 0;\n" +
                        "    }\n" +
                        "}\n";

        SourceCodeParser parser = new SourceCodeParser();
        ClassInfo classInfo1 = parser.parseSource(source);

        JavaSourceGenerator generator = new JavaSourceGenerator();
        String generatedSource = generator.generate(classInfo1);

        System.out.println("=== 注解类生成结果 ===");
        System.out.println(generatedSource);

        ClassInfo classInfo2 = parser.parseSource(generatedSource);
        assertEquals(classInfo1.getSimpleClassName(), classInfo2.getSimpleClassName());
        assertEquals(classInfo1.getAnnotations().size(), classInfo2.getAnnotations().size());
    }
}
