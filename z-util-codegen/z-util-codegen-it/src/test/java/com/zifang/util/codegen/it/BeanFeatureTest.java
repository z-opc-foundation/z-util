package com.zifang.util.codegen.it;

import com.zifang.util.codegen.it.model.Account;
import com.zifang.util.codegen.it.model.Product;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Lombok 性质特性集的集成测试：断言编译期注入出来的成员真的能用。
 */
class BeanFeatureTest {

    @Test
    void dataGeneratesAccessorsToStringEqualsAndConstructor() {
        Account account = new Account("A-1");
        account.setOwner("zifang");
        account.setBalance(42);
        account.setActive(true);

        assertEquals("A-1", account.getId());
        assertEquals("zifang", account.getOwner());
        assertEquals(42, account.getBalance());
        assertTrue(account.isActive());

        assertTrue(account.toString().contains("owner=zifang"));
        assertTrue(account.toString().contains("balance=42"));

        Account same = new Account("A-1");
        same.setOwner("zifang");
        same.setBalance(42);
        same.setActive(true);

        assertEquals(account, same);
        assertEquals(account.hashCode(), same.hashCode());

        Account other = new Account("A-2");
        assertNotEquals(account, other);
    }

    @Test
    void nonNullFieldRejectsNull() {
        Account account = new Account("A-1");
        assertThrows(NullPointerException.class, () -> new Account(null));
    }

    @Test
    void builderProducesInstance() {
        Product product = Product.builder()
                .sku("SKU-1")
                .title("键盘")
                .price(199)
                .build();

        assertEquals("SKU-1", product.getSku());
        assertEquals("键盘", product.getTitle());
        assertEquals(199, product.getPrice());
    }
}
