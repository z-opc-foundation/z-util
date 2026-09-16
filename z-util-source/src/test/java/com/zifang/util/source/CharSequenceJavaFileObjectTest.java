package com.zifang.util.source;

import com.zifang.util.source.compiler.CharSequenceJavaFileObject;
import org.junit.Test;

import javax.tools.JavaFileObject;
import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * CharSequenceJavaFileObject 测试
 */

/**
 * CharSequenceJavaFileObjectTest类。
 */
public class CharSequenceJavaFileObjectTest {

    @Test
    /**
     * testConstructorWithSource方法。
     */
    public void testConstructorWithSource() {
        String sourceCode = "public class Test {}";
        CharSequenceJavaFileObject fileObject = new CharSequenceJavaFileObject("Test", sourceCode);

        assertEquals(JavaFileObject.Kind.SOURCE, fileObject.getKind());
        assertEquals(sourceCode, fileObject.getCharContent(false).toString());
    }

    @Test
    /**
     * testConstructorWithKind方法。
     */
    public void testConstructorWithKind() {
        CharSequenceJavaFileObject fileObject = new CharSequenceJavaFileObject("com.example.Test", JavaFileObject.Kind.CLASS);

        assertEquals(JavaFileObject.Kind.CLASS, fileObject.getKind());
        assertNull(fileObject.getCharContent(false));
    }

    @Test
    /**
     * testGetCharContent方法。
     */
    public void testGetCharContent() {
        String sourceCode = "package com.example;\npublic class Hello {}\n";
        CharSequenceJavaFileObject fileObject = new CharSequenceJavaFileObject("Hello", sourceCode);

        CharSequence result = fileObject.getCharContent(false);
        assertEquals(sourceCode, result.toString());
    }

    @Test
    /**
     * testOpenInputStream方法。
     */
    public void testOpenInputStream() throws Exception {
        CharSequenceJavaFileObject fileObject = new CharSequenceJavaFileObject("com.example.Test", JavaFileObject.Kind.CLASS);

        InputStream is = fileObject.openInputStream();
        assertNotNull(is);
        assertEquals(0, is.available()); // no byte code yet
    }

    @Test
    /**
     * testOpenOutputStream方法。
     */
    public void testOpenOutputStream() throws Exception {
        CharSequenceJavaFileObject fileObject = new CharSequenceJavaFileObject("com.example.Test", JavaFileObject.Kind.CLASS);

        byte[] data = new byte[]{(byte) 0x00, (byte) 0x01, (byte) 0x02};
        fileObject.openOutputStream().write(data);

        byte[] byteCode = fileObject.getByteCode();
        assertEquals(3, byteCode.length);
        assertEquals(0x00, byteCode[0]);
        assertEquals(0x01, byteCode[1]);
        assertEquals(0x02, byteCode[2]);
    }

    @Test
    /**
     * testGetByteCodeAfterCompile方法。
     */
    public void testGetByteCodeAfterCompile() throws Exception {
        CharSequenceJavaFileObject fileObject = new CharSequenceJavaFileObject("Test", JavaFileObject.Kind.CLASS);

        // 模拟写入字节码
        byte[] fakeByteCode = new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE};
        fileObject.openOutputStream().write(fakeByteCode);

        byte[] result = fileObject.getByteCode();
        assertEquals(4, result.length);
        assertEquals((byte) 0xCA, result[0]);
    }

    @Test
    /**
     * testGetName方法。
     */
    public void testGetName() {
        CharSequenceJavaFileObject sourceFile = new CharSequenceJavaFileObject("com.example.Test", "source");
        assertTrue(sourceFile.getName().startsWith("com.example.Test"));

        CharSequenceJavaFileObject classFile = new CharSequenceJavaFileObject("com.example.Test", JavaFileObject.Kind.CLASS);
        assertTrue(classFile.getName().startsWith("com.example.Test"));
    }
}