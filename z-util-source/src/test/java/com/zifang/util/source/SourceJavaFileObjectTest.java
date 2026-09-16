package com.zifang.util.source;

import com.zifang.util.source.compiler.SourceJavaFileObject;
import org.junit.Test;

import javax.tools.JavaFileObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * SourceJavaFileObject 测试
 */

/**
 * SourceJavaFileObjectTest类。
 */
public class SourceJavaFileObjectTest {

    @Test
    /**
     * testConstructorWithSource方法。
     */
    public void testConstructorWithSource() {
        String sourceCode = "public class Test {}";
        SourceJavaFileObject fileObject = new SourceJavaFileObject("Test", sourceCode);

        assertEquals(JavaFileObject.Kind.SOURCE, fileObject.getKind());
    }

    @Test(expected = UnsupportedOperationException.class)
    /**
     * testGetCharContent方法。
     */
    public void testGetCharContent() throws Exception {
        String sourceCode = "package com.example;\npublic class Hello {}\n";
        SourceJavaFileObject fileObject = new SourceJavaFileObject("Hello", sourceCode);

        // SourceJavaFileObject 没有覆盖 getCharContent，调用父类方法会抛异常
        fileObject.getCharContent(false);
    }

    @Test
    /**
     * testGetName方法。
     */
    public void testGetName() {
        SourceJavaFileObject fileObject = new SourceJavaFileObject("com.example.Test", "source code");
        assertTrue(fileObject.getName().contains("com.example.Test"));
    }
}