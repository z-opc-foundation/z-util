package com.zifang.util.core.pattern.register;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 烟雾: Register 的核心契约 = put/get/contains + 拒 null key.
 * key=null 抛 IllegalArgumentException 是关键安全网: null key 在 put 时静默成功,
 * 下游 get/contains 会 NPE, 难定位. 钉死这条.
 */
class RegisterTest {

    @Test
    @DisplayName("register/get/contains 三连必须贯通")
    void putGetContains() {
        Register<String, Integer> r = new Register<String, Integer>();
        r.register("k", 42);
        assertTrue(r.contains("k"));
        assertEquals(42, r.get("k"));
    }

    @Test
    @DisplayName("register(null key) 必须立刻抛 IllegalArgumentException, 不能静默成功")
    void nullKeyRejectedImmediately() {
        Register<String, Integer> r = new Register<String, Integer>();
        assertThrows(IllegalArgumentException.class, () -> r.register(null, 1),
                "null key 静默成功会导致下游 NPE, 必须立刻抛");
    }

    @Test
    @DisplayName("未注册的 key get 必须返 null, 不能抛")
    void missingGetReturnsNull() {
        Register<String, Integer> r = new Register<String, Integer>();
        assertEquals(null, r.contains("missing"));
        assertFalse(r.contains("missing"));
        assertEquals(null, r.get("missing"));
    }
}