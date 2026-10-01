package com.zifang.util.devops.nexus;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * NexusComponentManager 测试
 * <p>
 * search() 方法为 static，依赖外部 nexus.cfuture.cc，仅验证方法调用不抛异常。
 * delete() 方法涉及写操作，不在单元测试范围。
 */

/**
 * NexusComponentManagerTest类。
 */
public class NexusComponentManagerTest {

    @Test
    /**
     * testStaticSearchReturnsList方法。
     */
    public void testStaticSearchReturnsList() {
        // 验证方法签名存在、可调用、返回非null List
        java.util.List<Component> result = NexusComponentManager.search("maven-releases");
        assertNotNull(result);
        // 不断言内容，因为取决于 nexus 服务状态
    }

    @Test
    /**
     * testFindByGavReturnsNullOrComponent方法。
     */
    public void testFindByGavReturnsNullOrComponent() {
        NexusComponentManager manager = new NexusComponentManager();
        // 用不存在的 GAV 验证返回 null 而非抛异常
        Component result = manager.findByGav("com.nonexistent", "artifact-does-not-exist", "1.0.0");
        assertNull(result);
    }

    @Test
    /**
     * testCheckExistGavReturnsBoolean方法。
     */
    public void testCheckExistGavReturnsBoolean() {
        NexusComponentManager manager = new NexusComponentManager();
        // 验证返回值是 Boolean 类型
        Boolean exists = manager.checkExistGav("com.nonexistent", "artifact-does-not-exist", "1.0.0");
        assertNotNull(exists);
        assertFalse(exists);
    }

    @Test
    /**
     * testRepositoryConstant方法。
     */
    public void testRepositoryConstant() {
        assertEquals("maven-releases", NexusComponentManager.respository);
    }
}
