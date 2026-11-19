package com.zifang.util.codegen.it;

import com.zifang.util.codegen.it.model.AccountValue;
import com.zifang.util.codegen.it.model.ImmutableAccountValue;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @ZImmutable：编译期生成 Immutable{Xxx} 不可变实现，可经抽象类型引用多态使用。
 */
class ImmutableFeatureTest {

    @Test
    void generatedImplIsUsableThroughAbstractType() {
        AccountValue account = ImmutableAccountValue.of("a-1", 100, true);
        assertEquals("a-1", account.id());
        assertEquals(100, account.balance());
        assertTrue(account.active());
        assertTrue(account instanceof ImmutableAccountValue);
        assertEquals("ImmutableAccountValue{id=a-1, balance=100, active=true}", account.toString());
    }

    @Test
    void valueSemantics() {
        AccountValue a = ImmutableAccountValue.of("a-1", 100, true);
        AccountValue b = ImmutableAccountValue.of("a-1", 100, true);
        AccountValue c = ImmutableAccountValue.of("a-2", 100, true);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        Set<AccountValue> set = new HashSet<>();
        set.add(a);
        set.add(b);
        assertEquals(1, set.size());
    }
}
