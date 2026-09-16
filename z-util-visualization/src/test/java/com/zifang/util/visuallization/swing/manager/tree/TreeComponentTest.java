package com.zifang.util.visuallization.swing.manager.tree;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * TreeComponent 单元测试
 * 测试树形组件类的继承行为
 */

/**
 * TreeComponentTest类。
 */
public class TreeComponentTest {

    @Test
    /**
     * testTreeComponentCreation方法。
     */
    public void testTreeComponentCreation() {
        TreeComponent tree = new TreeComponent();
        assertNotNull(tree);
    }

    @Test
    /**
     * testTreeComponentIsJTree方法。
     */
    public void testTreeComponentIsJTree() {
        TreeComponent tree = new TreeComponent();
        assertTrue(tree instanceof javax.swing.JTree);
    }

    @Test
    /**
     * testTreeComponentDefaultConstructor方法。
     */
    public void testTreeComponentDefaultConstructor() {
        TreeComponent tree = new TreeComponent();
        // JTree默认构造创建一个空模型
        assertNotNull(tree.getModel());
    }
}