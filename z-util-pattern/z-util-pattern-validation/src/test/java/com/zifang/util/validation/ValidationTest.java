package com.zifang.util.validation;

import com.zifang.util.validation.annotation.*;
import com.zifang.util.validation.core.ValidateResult;
import com.zifang.util.validation.core.ValidationEngine;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 校验框架测试
 */

/**
 * ValidationTest类。
 */
public class ValidationTest {

    // ==================== 测试模型类 ====================

    @Test
    /**
     * testNotNullValid方法。
     */
    public void testNotNullValid() {
        User user = new User("zifang", "123456", "test@example.com", 25, "13800138000");
        ValidateResult result = ValidationEngine.validate(user);
        assertFalse("NotNull校验通过时不应有错误", result.hasErrors());
    }

    @Test
    /**
     * testNotNullInvalid方法。
     */
    public void testNotNullInvalid() {
        User user = new User(null, "123456", "test@example.com", 25, "13800138000");
        ValidateResult result = ValidationEngine.validate(user);
        assertTrue("username为null应有错误", result.hasErrors());
        assertNotNull(result.getErrors().get("username"));
    }

    // ==================== NotNull 测试 ====================

    @Test
    /**
     * testLengthValid方法。
     */
    public void testLengthValid() {
        User user = new User("zifang", "12345678", "test@example.com", 25, "13800138000");
        ValidateResult result = ValidationEngine.validate(user);
        assertFalse("Length校验通过时不应有错误", result.hasErrors());
    }

    @Test
    /**
     * testLengthTooShort方法。
     */
    public void testLengthTooShort() {
        User user = new User("zifang", "123", "test@example.com", 25, "13800138000");
        ValidateResult result = ValidationEngine.validate(user);
        assertTrue("密码过短应有错误", result.hasErrors());
    }

    // ==================== Length 测试 ====================

    @Test
    /**
     * testLengthTooLong方法。
     */
    public void testLengthTooLong() {
        User user = new User("zifang", "123456789012345678901", "test@example.com", 25, "13800138000");
        ValidateResult result = ValidationEngine.validate(user);
        assertTrue("密码过长应有错误", result.hasErrors());
    }

    @Test
    /**
     * testEmailValid方法。
     */
    public void testEmailValid() {
        User user = new User("zifang", "123456", "test@example.com", 25, "13800138000");
        ValidateResult result = ValidationEngine.validate(user);
        assertFalse("Email校验通过时不应有错误", result.hasErrors());
    }

    @Test
    /**
     * testEmailInvalid方法。
     */
    public void testEmailInvalid() {
        User user = new User("zifang", "123456", "invalid-email", 25, "13800138000");
        ValidateResult result = ValidationEngine.validate(user);
        assertTrue("邮箱格式错误应有错误", result.hasErrors());
    }

    // ==================== Email 测试 ====================

    @Test
    /**
     * testRangeValid方法。
     */
    public void testRangeValid() {
        User user = new User("zifang", "123456", "test@example.com", 25, "13800138000");
        ValidateResult result = ValidationEngine.validate(user);
        assertFalse("Range校验通过时不应有错误", result.hasErrors());
    }

    @Test
    /**
     * testRangeTooLow方法。
     */
    public void testRangeTooLow() {
        Product product = new Product("Test", -1.0);
        ValidateResult result = ValidationEngine.validate(product);
        assertTrue("价格为负应有错误", result.hasErrors());
    }

    // ==================== Range 测试 ====================

    @Test
    /**
     * testPatternValid方法。
     */
    public void testPatternValid() {
        User user = new User("zifang", "123456", "test@example.com", 25, "13800138000");
        ValidateResult result = ValidationEngine.validate(user);
        assertFalse("手机号格式正确时不应有错误", result.hasErrors());
    }

    @Test
    /**
     * testPatternInvalid方法。
     */
    public void testPatternInvalid() {
        User user = new User("zifang", "123456", "test@example.com", 25, "12345");
        ValidateResult result = ValidationEngine.validate(user);
        assertTrue("手机号格式错误应有错误", result.hasErrors());
    }

    // ==================== Pattern 测试 ====================

    @Test
    /**
     * testMultipleErrors方法。
     */
    public void testMultipleErrors() {
        User user = new User(null, "123", "invalid-email", 200, "12345");
        ValidateResult result = ValidationEngine.validate(user);
        assertTrue("多个字段错误时应有错误", result.hasErrors());
        // 5个错误: username(null), password(太短), email(格式错误), age(超出范围), phone(格式错误)
        assertEquals(5, result.getErrors().size());
    }

    @Test
    /**
     * testAllValid方法。
     */
    public void testAllValid() {
        User user = new User("zifang", "123456", "test@example.com", 25, "13800138000");
        ValidateResult result = ValidationEngine.validate(user);
        assertFalse("所有字段有效时不应有错误", result.hasErrors());
    }

    // ==================== 多字段综合测试 ====================

    @Test(expected = com.zifang.util.validation.core.ValidationException.class)
    /**
     * testValidateAndThrow方法。
     */
    public void testValidateAndThrow() {
        User user = new User(null, "123", "invalid-email", 25, "12345");
        ValidationEngine.validateAndThrow(user);
    }

    @Test
    /**
     * testValidateResultToString方法。
     */
    public void testValidateResultToString() {
        User user = new User(null, "123", "invalid-email", 25, "12345");
        ValidateResult result = ValidationEngine.validate(user);
        String str = result.toString();
        assertTrue("toString应包含valid", str.contains("valid"));
    }

    // ==================== 异常测试 ====================

    @Test
    /**
     * testValidateResultFirstError方法。
     */
    public void testValidateResultFirstError() {
        User user = new User(null, "123", "invalid-email", 25, "12345");
        ValidateResult result = ValidationEngine.validate(user);
        assertTrue(result.getFirstError().isPresent());
    }

    // ==================== ValidateResult 测试 ====================

    static class User {
        @NotNull(message = "用户名不能为空")
        String username;

        @NotNull(message = "密码不能为空")
        @Length(min = 6, max = 20, message = "密码长度必须在6-20之间")
        String password;

        @Email(message = "邮箱格式不正确")
        String email;

        @Range(min = 0, max = 150, message = "年龄必须在0-150之间")
        int age;

        @Pattern(regex = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String phone;

        /**
         * User方法。
         * * @param username String类型参数
         *
         * @param password String类型参数
         * @param email    String类型参数
         * @param age      int类型参数
         * @param phone    String类型参数
         */
        public User(String username, String password, String email, int age, String phone) {
            this.username = username;
            this.password = password;
            this.email = email;
            this.age = age;
            this.phone = phone;
        }
    }

    static class Product {
        @NotNull(message = "产品名称不能为空")
        @Length(min = 2, max = 100, message = "产品名称长度必须在2-100之间")
        String name;

        @NotNull(message = "价格不能为空")
        @Range(min = 0.01, max = 999999.99, message = "价格必须在合理范围内")
        double price;

        /**
         * Product方法。
         * * @param name String类型参数
         *
         * @param price double类型参数
         */
        public Product(String name, double price) {
            this.name = name;
            this.price = price;
        }
    }
}