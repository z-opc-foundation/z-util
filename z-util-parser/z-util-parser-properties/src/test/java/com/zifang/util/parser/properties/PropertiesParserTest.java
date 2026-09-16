package com.zifang.util.parser.properties;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * PropertiesParser 测试类。
 *
 * @author zifang
 */

/**
 * PropertiesParserTest类。
 */
public class PropertiesParserTest {

    private PropertiesParser parser = new PropertiesParser();

    /**
     * 测试 1: 基本 key=value 解析
     */
    @Test
    /**
     * testBasicKeyValueParsing方法。
     */
    public void testBasicKeyValueParsing() {
        String input = "name=zifang\nage=30";
        PropertiesModel model = parser.parse(input);

        Assert.assertEquals("zifang", model.getProperty("name"));
        Assert.assertEquals("30", model.getProperty("age"));
        Assert.assertEquals(2, model.size());
    }

    /**
     * 测试 2: 含注释行（# 和 !）
     */
    @Test
    /**
     * testCommentLines方法。
     */
    public void testCommentLines() {
        String input = "# This is a comment\n" +
                "! This is also a comment\n" +
                "name=zifang\n" +
                "# Another comment\n" +
                "age=30";
        PropertiesModel model = parser.parse(input);

        Assert.assertEquals("zifang", model.getProperty("name"));
        Assert.assertEquals("30", model.getProperty("age"));
        Assert.assertEquals("This is a comment This is also a comment", model.getComment("name"));
        Assert.assertEquals("Another comment", model.getComment("age"));
    }

    /**
     * 测试 3: 含空值 key=
     */
    @Test
    /**
     * testEmptyValue方法。
     */
    public void testEmptyValue() {
        String input = "name=\nage=30";
        PropertiesModel model = parser.parse(input);

        Assert.assertEquals("", model.getProperty("name"));
        Assert.assertEquals("30", model.getProperty("age"));
    }

    /**
     * 测试 4: 含多行续行 key=line1\
     * line2
     */
    @Test
    /**
     * testMultiLineContinuation方法。
     */
    public void testMultiLineContinuation() {
        String input = "content=line1\\\nline2\\\nline3\nnext=value";
        PropertiesModel model = parser.parse(input);

        Assert.assertEquals("line1line2line3", model.getProperty("content"));
        Assert.assertEquals("value", model.getProperty("next"));
    }

    /**
     * 测试 5: Unicode 转义 \u0041\u0042 → AB
     */
    @Test
    /**
     * testUnicodeEscape方法。
     */
    public void testUnicodeEscape() {
        String input = "chars=\\u0041\\u0042\\u0043";
        PropertiesModel model = parser.parse(input);

        Assert.assertEquals("ABC", model.getProperty("chars"));
    }

    /**
     * 测试 6: 含重复 key（后者覆盖前者）
     */
    @Test
    /**
     * testDuplicateKeys方法。
     */
    public void testDuplicateKeys() {
        String input = "name=zifang1\nname=zifang2";
        PropertiesModel model = parser.parse(input);

        Assert.assertEquals("zifang2", model.getProperty("name"));
        Assert.assertEquals(1, model.size());
    }

    /**
     * 测试 7: 含空白行
     */
    @Test
    /**
     * testBlankLines方法。
     */
    public void testBlankLines() {
        String input = "name=zifang\n\n\nage=30\n";
        PropertiesModel model = parser.parse(input);

        Assert.assertEquals("zifang", model.getProperty("name"));
        Assert.assertEquals("30", model.getProperty("age"));
        Assert.assertEquals(2, model.size());
    }

    /**
     * 测试 8: 含前置空白 key
     */
    @Test
    /**
     * testLeadingWhitespaceKey方法。
     */
    public void testLeadingWhitespaceKey() {
        String input = "  name=zifang\n   age=30";
        PropertiesModel model = parser.parse(input);

        Assert.assertEquals("zifang", model.getProperty("name"));
        Assert.assertEquals("30", model.getProperty("age"));
    }

    /**
     * 测试 9: list-style key[0]=val 解析
     */
    @Test
    /**
     * testListStyleKey方法。
     */
    public void testListStyleKey() {
        String input = "items[0]=apple\nitems[1]=banana\nitems[2]=cherry";
        PropertiesModel model = parser.parse(input);

        Assert.assertEquals("apple", model.getProperty("items[0]"));
        Assert.assertEquals("banana", model.getProperty("items[1]"));
        Assert.assertEquals("cherry", model.getProperty("items[2]"));
        Assert.assertEquals(3, model.size());
    }

    /**
     * 测试 10: round-trip：解析后重新输出，内容一致
     */
    @Test
    /**
     * testRoundTrip方法。
     */
    public void testRoundTrip() {
        String input = "# Comment for name\n" +
                "name=zifang\n" +
                "# Comment for age\n" +
                "age=30\n" +
                "\n" +
                "# Comment for empty\n" +
                "empty=\n" +
                "unicode=\\u0041\\u0042\n" +
                "escaped=line1\\nline2";
        PropertiesModel model = parser.parse(input);
        String output = parser.store(model);

        PropertiesModel reparsed = parser.parse(output);

        Assert.assertEquals(model.getProperty("name"), reparsed.getProperty("name"));
        Assert.assertEquals(model.getProperty("age"), reparsed.getProperty("age"));
        Assert.assertEquals(model.getProperty("empty"), reparsed.getProperty("empty"));
        Assert.assertEquals(model.getProperty("unicode"), reparsed.getProperty("unicode"));
        Assert.assertEquals(model.getProperty("escaped"), reparsed.getProperty("escaped"));
    }

    /**
     * 测试转义字符：\t \n \r \" \\
     */
    @Test
    /**
     * testEscapeCharacters方法。
     */
    public void testEscapeCharacters() {
        String input = "tab=hello\\tworld\n" +
                "newline=line1\\nline2\n" +
                "return=line1\\rline2\n" +
                "quote=say\\\"hello\\\"\n" +
                "backslash=c:\\program\\files";
        PropertiesModel model = parser.parse(input);

        Assert.assertEquals("hello\tworld", model.getProperty("tab"));
        Assert.assertEquals("line1\nline2", model.getProperty("newline"));
        Assert.assertEquals("line1\rline2", model.getProperty("return"));
        Assert.assertEquals("say\"hello\"", model.getProperty("quote"));
        Assert.assertEquals("c:\\program\\files", model.getProperty("backslash"));
    }

    /**
     * 测试键的有序性
     */
    @Test
    /**
     * testKeyOrder方法。
     */
    public void testKeyOrder() {
        String input = "zebra=animal\napple=fruit\nbanana=yellow";
        PropertiesModel model = parser.parse(input);

        List<String> keys = model.getOrderedKeys();
        Assert.assertEquals("zebra", keys.get(0));
        Assert.assertEquals("apple", keys.get(1));
        Assert.assertEquals("banana", keys.get(2));
    }

    // ==================== 新增测试用例 ====================

    /**
     * 测试 11: parseFromReader — 从 Reader 解析（文件编码ISO-8859-1）
     */
    @Test
    /**
     * testParseFromReader方法。
     */
    public void testParseFromReader() throws Exception {
        String content = "name=zifang\nage=30";
        java.io.StringReader reader = new java.io.StringReader(content);
        PropertiesModel model = parser.parse(reader);

        Assert.assertEquals("zifang", model.getProperty("name"));
        Assert.assertEquals("30", model.getProperty("age"));
    }

    /**
     * 测试 12: parseFromFile — 从真实文件解析
     */
    @Test
    /**
     * testParseFromFile方法。
     */
    public void testParseFromFile() throws Exception {
        java.io.File tempFile = java.io.File.createTempFile("test", ".properties");
        tempFile.deleteOnExit();

        String content = "key1=value1\nkey2=value2\nkey3=value3";
        java.io.FileWriter writer = new java.io.FileWriter(tempFile);
        writer.write(content);
        writer.close();

        PropertiesModel model = parser.parse(tempFile);
        Assert.assertEquals("value1", model.getProperty("key1"));
        Assert.assertEquals("value2", model.getProperty("key2"));
        Assert.assertEquals("value3", model.getProperty("key3"));
    }

    /**
     * 测试 13: storeToWriter — 写入到 Writer 并读回
     */
    @Test
    /**
     * testStoreToWriter方法。
     */
    public void testStoreToWriter() throws Exception {
        String input = "# This is a comment\nname=test\nvalue=123";
        PropertiesModel model = parser.parse(input);

        java.io.StringWriter stringWriter = new java.io.StringWriter();
        parser.storeToWriter(model, stringWriter);

        String output = stringWriter.toString();
        Assert.assertTrue(output.contains("name=test"));
        Assert.assertTrue(output.contains("value=123"));
        Assert.assertTrue(output.contains("#This is a comment"));
    }

    /**
     * 测试 14: key包含特殊字符 — key中有空格/等号时的处理
     */
    @Test
    /**
     * testKeyWithSpecialChars方法。
     */
    public void testKeyWithSpecialChars() {
        // Key中的空格和等号应该被包含在key中（等号作为分隔符，空格作为key的一部分）
        String input = "key with space=value1\nanother key=value2";
        PropertiesModel model = parser.parse(input);

        // key with space会被解析，space在=之前，所以key是"key with space"
        Assert.assertEquals("value1", model.getProperty("key with space"));
        Assert.assertEquals("value2", model.getProperty("another key"));
    }

    /**
     * 测试 15: value含中文 — 中文值的解析与round-trip
     */
    @Test
    /**
     * testChineseValue方法。
     */
    public void testChineseValue() {
        String input = "greeting=你好世界\nmessage=欢迎使用";
        PropertiesModel model = parser.parse(input);

        Assert.assertEquals("你好世界", model.getProperty("greeting"));
        Assert.assertEquals("欢迎使用", model.getProperty("message"));

        // Round-trip test
        String output = parser.store(model);
        PropertiesModel reparsed = parser.parse(output);
        Assert.assertEquals("你好世界", reparsed.getProperty("greeting"));
        Assert.assertEquals("欢迎使用", reparsed.getProperty("message"));
    }

    /**
     * 测试 16: value含引号 — "和'在value中的处理
     */
    @Test
    /**
     * testValueWithQuotes方法。
     */
    public void testValueWithQuotes() {
        String input = "quote1=他说：\"你好\"\nquote2=她说：'你好'\nboth=双\"单'混合";
        PropertiesModel model = parser.parse(input);

        Assert.assertEquals("他说：\"你好\"", model.getProperty("quote1"));
        Assert.assertEquals("她说：'你好'", model.getProperty("quote2"));
        Assert.assertEquals("双\"单'混合", model.getProperty("both"));
    }

    /**
     * 测试 17: 大量key性能 — 创建100+键值对，验证正确性和性能量级
     */
    @Test
    /**
     * testLargeNumberOfKeys方法。
     */
    public void testLargeNumberOfKeys() {
        StringBuilder sb = new StringBuilder();
        int keyCount = 150;

        for (int i = 0; i < keyCount; i++) {
            sb.append("key").append(i).append("=value").append(i).append("\n");
        }

        long startTime = System.currentTimeMillis();
        PropertiesModel model = parser.parse(sb.toString());
        long parseTime = System.currentTimeMillis() - startTime;

        Assert.assertEquals(keyCount, model.size());

        // Verify all keys are present
        for (int i = 0; i < keyCount; i++) {
            Assert.assertEquals("value" + i, model.getProperty("key" + i));
        }

        // Performance should be reasonable (< 1 second for 150 keys)
        Assert.assertTrue("Parse time should be less than 1000ms, was: " + parseTime, parseTime < 1000);
    }

    /**
     * 测试 18: getPropertyOrDefault — 取不存在的key时返回默认值
     */
    @Test
    /**
     * testGetPropertyOrDefault方法。
     */
    public void testGetPropertyOrDefault() {
        String input = "existing=value";
        PropertiesModel model = parser.parse(input);

        Assert.assertEquals("value", model.getPropertyOrDefault("existing", "default"));
        Assert.assertEquals("default", model.getPropertyOrDefault("nonexistent", "default"));
        Assert.assertNull(model.getPropertyOrDefault("nonexistent", null));
    }

    /**
     * 测试 19: containsKey — 存在性检查
     */
    @Test
    /**
     * testContainsKey方法。
     */
    public void testContainsKey() {
        String input = "key1=value1\nkey2=value2";
        PropertiesModel model = parser.parse(input);

        Assert.assertTrue(model.containsKey("key1"));
        Assert.assertTrue(model.containsKey("key2"));
        Assert.assertFalse(model.containsKey("key3"));
        Assert.assertFalse(model.containsKey(""));
    }

    /**
     * 测试 20: removeProperty — 删除某个key
     */
    @Test
    /**
     * testRemoveProperty方法。
     */
    public void testRemoveProperty() {
        String input = "key1=value1\nkey2=value2\nkey3=value3";
        PropertiesModel model = parser.parse(input);

        Assert.assertEquals(3, model.size());

        String removed = model.removeProperty("key2");
        Assert.assertEquals("value2", removed);
        Assert.assertEquals(2, model.size());
        Assert.assertFalse(model.containsKey("key2"));
        Assert.assertTrue(model.containsKey("key1"));
        Assert.assertTrue(model.containsKey("key3"));

        // Removing non-existent key should return null
        Assert.assertNull(model.removeProperty("nonexistent"));
    }

    /**
     * 测试 21: getAllKeys — 获取所有key列表
     */
    @Test
    /**
     * testGetAllKeys方法。
     */
    public void testGetAllKeys() {
        String input = "zebra=animal\napple=fruit\nbanana=yellow";
        PropertiesModel model = parser.parse(input);

        List<String> allKeys = model.getAllKeys();
        Assert.assertEquals(3, allKeys.size());
        Assert.assertEquals("zebra", allKeys.get(0));
        Assert.assertEquals("apple", allKeys.get(1));
        Assert.assertEquals("banana", allKeys.get(2));
    }

    /**
     * 测试 22: comment含井号 — 注释本身包含#字符的处理
     */
    @Test
    /**
     * testCommentWithHash方法。
     */
    public void testCommentWithHash() {
        String input = "# This comment has a # hash inside\nname=test\n# Another # comment # for value\nvalue=123";
        PropertiesModel model = parser.parse(input);

        Assert.assertEquals("test", model.getProperty("name"));
        Assert.assertEquals("123", model.getProperty("value"));
        // Comments are stored as-is, # is part of the comment
        Assert.assertNotNull(model.getComment("name"));
    }

    /**
     * 测试 23: 续行跨多行 — 超过2行的多行续行
     */
    @Test
    /**
     * testMultiLineContinuationExtended方法。
     */
    public void testMultiLineContinuationExtended() {
        String input = "longtext=line1\\\nline2\\\nline3\\\nline4\\\nline5\nnext=value";
        PropertiesModel model = parser.parse(input);

        Assert.assertEquals("line1line2line3line4line5", model.getProperty("longtext"));
        Assert.assertEquals("value", model.getProperty("next"));
    }

    /**
     * 测试 24: 反斜杠转义 — value中包含多个连续反斜杠
     */
    @Test
    /**
     * testMultipleBackslashes方法。
     */
    public void testMultipleBackslashes() {
        String input = "single=c:\\\\path\ndouble=before\\\\\\\\after\npath=c:\\\\u005Cusers\\\\name";
        PropertiesModel model = parser.parse(input);

        // single backslash should remain as one
        Assert.assertEquals("c:\\path", model.getProperty("single"));
        // double backslash should become one
        Assert.assertEquals("before\\\\after", model.getProperty("double"));
        // path style
        Assert.assertEquals("c:\\u005Cusers\\name", model.getProperty("path"));
    }
}
