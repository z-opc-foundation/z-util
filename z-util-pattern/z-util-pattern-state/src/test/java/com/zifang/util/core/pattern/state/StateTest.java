package com.zifang.util.core.pattern.state;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 烟雾: State 的字段读写必须可逆 (getCode/getName 等于 ctor 传入值).
 * 这是状态机状态值的最小契约 —— 状态切换逻辑全靠这两个字段 identity.
 */
class StateTest {

    @Test
    @DisplayName("无参构造 + getCode/getName 必须可读")
    void noArgCtorAndGetters() {
        State s = new State();
        assertEquals(null, s.getCode());
        assertEquals(null, s.getName());
    }

    @Test
    @DisplayName("code/name 二元构造必须原样透传")
    void codeNameCtorPreserves() {
        State s = new State("INIT", "initial");
        assertEquals("INIT", s.getCode());
        assertEquals("initial", s.getName());
    }
}