package com.zifang.util.devops.nexus;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Component 类测试
 */

/**
 * ComponentTest类。
 */
public class ComponentTest {

    @Test
    /**
     * testDefaultConstructor方法。
     */
    public void testDefaultConstructor() {
        Component component = new Component();
        assertNotNull(component);
        assertNull(component.getId());
        assertNull(component.getRepository());
        assertNull(component.getGroup());
        assertNull(component.getName());
        assertNull(component.getVersion());
        assertNull(component.getFormat());
        assertNotNull(component.getAssets());
        assertTrue(component.getAssets().isEmpty());
    }

    @Test
    /**
     * testIdSetterAndGetter方法。
     */
    public void testIdSetterAndGetter() {
        Component component = new Component();
        component.setId("comp-123");
        assertEquals("comp-123", component.getId());
    }

    @Test
    /**
     * testRepositorySetterAndGetter方法。
     */
    public void testRepositorySetterAndGetter() {
        Component component = new Component();
        component.setRepository("maven-releases");
        assertEquals("maven-releases", component.getRepository());
    }

    @Test
    /**
     * testGroupSetterAndGetter方法。
     */
    public void testGroupSetterAndGetter() {
        Component component = new Component();
        component.setGroup("com.example");
        assertEquals("com.example", component.getGroup());
    }

    @Test
    /**
     * testNameSetterAndGetter方法。
     */
    public void testNameSetterAndGetter() {
        Component component = new Component();
        component.setName("my-library");
        assertEquals("my-library", component.getName());
    }

    @Test
    /**
     * testVersionSetterAndGetter方法。
     */
    public void testVersionSetterAndGetter() {
        Component component = new Component();
        component.setVersion("1.0.0");
        assertEquals("1.0.0", component.getVersion());
    }

    @Test
    /**
     * testFormatSetterAndGetter方法。
     */
    public void testFormatSetterAndGetter() {
        Component component = new Component();
        component.setFormat("maven");
        assertEquals("maven", component.getFormat());
    }

    @Test
    /**
     * testAssetsSetterAndGetter方法。
     */
    public void testAssetsSetterAndGetter() {
        Component component = new Component();

        Asset asset1 = new Asset();
        asset1.setId("asset-1");
        Asset asset2 = new Asset();
        asset2.setId("asset-2");

        List<Asset> assets = Arrays.asList(asset1, asset2);
        component.setAssets(assets);

        assertNotNull(component.getAssets());
        assertEquals(2, component.getAssets().size());
        assertEquals("asset-1", component.getAssets().get(0).getId());
    }

    @Test
    /**
     * testCompareTo方法。
     */
    public void testCompareTo() {
        Component comp1 = new Component();
        comp1.setVersion("1.0.0");

        Component comp2 = new Component();
        comp2.setVersion("2.0.0");

        Component comp3 = new Component();
        comp3.setVersion("1.0.0");

        assertTrue(comp1.compareTo(comp2) < 0);
        assertTrue(comp2.compareTo(comp1) > 0);
        assertEquals(0, comp1.compareTo(comp3));
    }

    @Test
    /**
     * testEquals方法。
     */
    public void testEquals() {
        Component comp1 = new Component();
        comp1.setId("id1");
        comp1.setRepository("repo");
        comp1.setGroup("group");
        comp1.setName("name");
        comp1.setVersion("1.0");
        comp1.setFormat("maven");

        Component comp2 = new Component();
        comp2.setId("id1");
        comp2.setRepository("repo");
        comp2.setGroup("group");
        comp2.setName("name");
        comp2.setVersion("1.0");
        comp2.setFormat("maven");

        assertEquals(comp1, comp2);
    }

    @Test
    /**
     * testEqualsWithDifferentId方法。
     */
    public void testEqualsWithDifferentId() {
        Component comp1 = new Component();
        comp1.setId("id1");

        Component comp2 = new Component();
        comp2.setId("id2");

        assertNotEquals(comp1, comp2);
    }

    @Test
    /**
     * testEqualsWithSameObject方法。
     */
    public void testEqualsWithSameObject() {
        Component component = new Component();
        assertEquals(component, component);
    }

    @Test
    /**
     * testEqualsWithNull方法。
     */
    public void testEqualsWithNull() {
        Component component = new Component();
        assertNotEquals(component, null);
    }

    @Test
    /**
     * testEqualsWithDifferentClass方法。
     */
    public void testEqualsWithDifferentClass() {
        Component component = new Component();
        assertNotEquals(component, "not a component");
    }

    @Test
    /**
     * testHashCode方法。
     */
    public void testHashCode() {
        Component comp1 = new Component();
        comp1.setId("id1");
        comp1.setRepository("repo");

        Component comp2 = new Component();
        comp2.setId("id1");
        comp2.setRepository("repo");

        assertEquals(comp1.hashCode(), comp2.hashCode());
    }

    @Test
    /**
     * testHashCodeConsistency方法。
     */
    public void testHashCodeConsistency() {
        Component component = new Component();
        component.setId("id1");

        int hashCode1 = component.hashCode();
        int hashCode2 = component.hashCode();

        assertEquals(hashCode1, hashCode2);
    }

    @Test
    /**
     * testToString方法。
     */
    public void testToString() {
        Component component = new Component();
        component.setId("test-id");
        component.setName("test-artifact");
        component.setVersion("1.0");

        String str = component.toString();

        assertNotNull(str);
        assertTrue(str.contains("Component"));
        assertTrue(str.contains("test-id"));
        assertTrue(str.contains("test-artifact"));
    }

    @Test
    /**
     * testCompleteComponent方法。
     */
    public void testCompleteComponent() {
        Component component = new Component();
        component.setId("comp-full");
        component.setRepository("maven-releases");
        component.setGroup("com.example");
        component.setName("full-library");
        component.setVersion("2.5.1");
        component.setFormat("maven");

        Asset asset1 = new Asset();
        asset1.setId("asset-1");
        asset1.setPath("com/example/full-library-2.5.1.jar");
        component.setAssets(Arrays.asList(asset1));

        // Verify
        assertEquals("comp-full", component.getId());
        assertEquals("maven-releases", component.getRepository());
        assertEquals("com.example", component.getGroup());
        assertEquals("full-library", component.getName());
        assertEquals("2.5.1", component.getVersion());
        assertEquals("maven", component.getFormat());
        assertEquals(1, component.getAssets().size());
    }
}
