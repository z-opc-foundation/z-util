package com.zifang.util.codegen.it;

import com.zifang.util.codegen.it.model.QueryUser_;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @ZMetaModel：生成 {Class}_ 字段常量类，动态拼字段时用常量代替裸字符串。
 */
class MetaModelFeatureTest {

    @Test
    void fieldConstantsReflectFieldNames() {
        assertEquals("userName", QueryUser_.USER_NAME);
        assertEquals("age", QueryUser_.AGE);
        assertEquals("urlValue", QueryUser_.URL_VALUE);
        assertEquals("com.zifang.util.codegen.it.model.QueryUser", QueryUser_.ENTITY);
    }

    @Test
    void allCoversEveryFieldInOrder() {
        assertArrayEquals(new String[]{"userName", "age", "urlValue"}, QueryUser_.ALL);
    }
}
