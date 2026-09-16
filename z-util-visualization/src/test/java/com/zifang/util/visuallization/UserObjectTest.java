package com.zifang.util.visuallization;

import com.zifang.util.visuallization.swing.manager.subpanels.UserObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户对象和相关类的单元测试
 */
class UserObjectTest {

    // ==================== UserObject 测试 ====================

    @Test
    void testUserObjectDefaultConstructor() {
        UserObject user = new UserObject();
        assertNull(user.getId());
        assertNull(user.getDisplayName());
    }

    @Test
    void testUserObjectSetters() {
        UserObject user = new UserObject();
        user.setId(100);
        user.setDisplayName("TestUser");
        assertEquals(Integer.valueOf(100), user.getId());
        assertEquals("TestUser", user.getDisplayName());
    }

    @Test
    void testUserObjectToString() {
        UserObject user = new UserObject();
        user.setDisplayName("DisplayName");
        assertEquals("DisplayName", user.toString());
    }

    @Test
    void testUserObjectEquals() {
        UserObject user1 = new UserObject();
        user1.setId(1);
        user1.setDisplayName("User1");
        UserObject user2 = new UserObject();
        user2.setId(1);
        user2.setDisplayName("User1");
        assertEquals(user1, user2);
    }

    @Test
    void testUserObjectEqualsDifferent() {
        UserObject user1 = new UserObject();
        user1.setId(1);
        user1.setDisplayName("User1");
        UserObject user2 = new UserObject();
        user2.setId(2);
        user2.setDisplayName("User2");
        assertNotEquals(user1, user2);
    }

    @Test
    void testUserObjectEqualsNull() {
        UserObject user = new UserObject();
        assertNotEquals(user, null);
    }

    @Test
    void testUserObjectEqualsSame() {
        UserObject user = new UserObject();
        assertEquals(user, user);
    }

    @Test
    void testUserObjectEqualsDifferentClass() {
        UserObject user = new UserObject();
        assertNotEquals(user, "string");
    }

    @Test
    void testUserObjectHashCode() {
        UserObject user1 = new UserObject();
        user1.setId(1);
        user1.setDisplayName("User");
        UserObject user2 = new UserObject();
        user2.setId(1);
        user2.setDisplayName("User");
        assertEquals(user1.hashCode(), user2.hashCode());
    }
}