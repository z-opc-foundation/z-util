package com.zifang.util.source;

import com.zifang.util.source.generator.info.AnnotationInfo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * AnnotationInfo 模型测试
 */

/**
 * AnnotationInfoTest类。
 */
public class AnnotationInfoTest {

    @Test
    /**
     * testDefaultConstructor方法。
     */
    public void testDefaultConstructor() {
        AnnotationInfo info = new AnnotationInfo();
        assertNull(info.getType());
        assertTrue(info.getMembers().isEmpty());
    }

    @Test
    /**
     * testTypeConstructor方法。
     */
    public void testTypeConstructor() {
        AnnotationInfo info = new AnnotationInfo("Deprecated");
        assertEquals("Deprecated", info.getType());
        assertTrue(info.getMembers().isEmpty());
    }

    @Test
    /**
     * testAddMember方法。
     */
    public void testAddMember() {
        AnnotationInfo info = new AnnotationInfo();
        info.setType("MyAnnotation");
        info.addMember("value", "\"default\"");

        assertEquals(1, info.getMembers().size());
        assertEquals("value", info.getMembers().get(0).getName());
        assertEquals("\"default\"", info.getMembers().get(0).getValue());
    }

    @Test
    /**
     * testToStringSimple方法。
     */
    public void testToStringSimple() {
        AnnotationInfo info = new AnnotationInfo("Deprecated");
        assertEquals("@Deprecated", info.toString());
    }

    @Test
    /**
     * testToStringWithMember方法。
     */
    public void testToStringWithMember() {
        AnnotationInfo info = new AnnotationInfo("MyAnnotation");
        info.addMember("value", "\"test\"");
        assertEquals("@MyAnnotation(value = \"test\")", info.toString());
    }

    @Test
    /**
     * testToStringWithMultipleMembers方法。
     */
    public void testToStringWithMultipleMembers() {
        AnnotationInfo info = new AnnotationInfo("MyAnnotation");
        info.addMember("name", "\"test\"");
        info.addMember("count", "10");
        String result = info.toString();
        assertTrue(result.contains("@MyAnnotation"));
        assertTrue(result.contains("name = \"test\""));
        assertTrue(result.contains("count = 10"));
    }

    @Test
    /**
     * testToStringWithSingleMemberAnnotation方法。
     */
    public void testToStringWithSingleMemberAnnotation() {
        AnnotationInfo info = new AnnotationInfo("MyAnnotation");
        info.addMember("", "\"singleValue\"");
        assertEquals("@MyAnnotation(\"singleValue\")", info.toString());
    }

    @Test
    /**
     * testAnnotationMember方法。
     */
    public void testAnnotationMember() {
        AnnotationInfo.AnnotationMember member = new AnnotationInfo.AnnotationMember();
        member.setName("value");
        member.setValue("\"test\"");
        assertEquals("value", member.getName());
        assertEquals("\"test\"", member.getValue());
    }

    @Test
    /**
     * testAnnotationMemberConstructor方法。
     */
    public void testAnnotationMemberConstructor() {
        AnnotationInfo.AnnotationMember member = new AnnotationInfo.AnnotationMember("key", "123");
        assertEquals("key", member.getName());
        assertEquals("123", member.getValue());
    }

    @Test
    /**
     * testSetType方法。
     */
    public void testSetType() {
        AnnotationInfo info = new AnnotationInfo();
        info.setType("Override");
        assertEquals("Override", info.getType());
    }

    @Test
    /**
     * testSetMembers方法。
     */
    public void testSetMembers() {
        AnnotationInfo info = new AnnotationInfo();
        List<AnnotationInfo.AnnotationMember> members = new ArrayList<>();
        members.add(new AnnotationInfo.AnnotationMember("a", "1"));
        members.add(new AnnotationInfo.AnnotationMember("b", "2"));
        info.setMembers(members);

        assertEquals(2, info.getMembers().size());
    }
}