package com.zifang.util.parser.proto;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * ProtoParser 测试类。
 *
 * @author zifang
 */

/**
 * ProtoParserTest类。
 */
public class ProtoParserTest {

    private ProtoParser parser = new ProtoParser();

    // ==================== 1. 基本 message ====================

    @Test
    /**
     * testBasicMessage方法。
     */
    public void testBasicMessage() {
        String input = "syntax = \"proto3\";\n" +
                "message Person {}\n";

        ProtoDocument doc = parser.parse(input);

        Assert.assertEquals("proto3", doc.getSyntax());
        Assert.assertEquals(1, doc.getMessages().size());
        Assert.assertEquals("Person", doc.getMessages().get(0).getName());
    }

    // ==================== 2. 多字段 ====================

    @Test
    /**
     * testMultipleFields方法。
     */
    public void testMultipleFields() {
        String input = "syntax = \"proto3\";\n" +
                "message Person {\n" +
                "  string name = 1;\n" +
                "  int32 age = 2;\n" +
                "  bool active = 3;\n" +
                "}\n";

        ProtoDocument doc = parser.parse(input);
        ProtoMessage person = doc.getMessages().get(0);
        List<ProtoField> fields = person.getFields();

        Assert.assertEquals(3, fields.size());
        Assert.assertEquals("string", fields.get(0).getType());
        Assert.assertEquals("name", fields.get(0).getName());
        Assert.assertEquals(1, fields.get(0).getTag());
        Assert.assertEquals("int32", fields.get(1).getType());
        Assert.assertEquals("age", fields.get(1).getName());
        Assert.assertEquals(2, fields.get(1).getTag());
        Assert.assertEquals("bool", fields.get(2).getType());
        Assert.assertEquals("active", fields.get(2).getName());
        Assert.assertEquals(3, fields.get(2).getTag());
    }

    // ==================== 3. repeated/string/int32 ====================

    @Test
    /**
     * testRepeatedStringInt32方法。
     */
    public void testRepeatedStringInt32() {
        String input = "syntax = \"proto3\";\n" +
                "message Person {\n" +
                "  repeated string emails = 1;\n" +
                "  repeated int32 scores = 2;\n" +
                "}\n";

        ProtoDocument doc = parser.parse(input);
        ProtoMessage person = doc.getMessages().get(0);
        List<ProtoField> fields = person.getFields();

        Assert.assertEquals(2, fields.size());

        ProtoField emails = fields.get(0);
        Assert.assertTrue(emails.isRepeated());
        Assert.assertEquals("string", emails.getType());
        Assert.assertEquals("emails", emails.getName());
        Assert.assertEquals(1, emails.getTag());

        ProtoField scores = fields.get(1);
        Assert.assertTrue(scores.isRepeated());
        Assert.assertEquals("int32", scores.getType());
        Assert.assertEquals("scores", scores.getName());
        Assert.assertEquals(2, scores.getTag());
    }

    // ==================== 4. Enum ====================

    @Test
    /**
     * testEnum方法。
     */
    public void testEnum() {
        String input = "syntax = \"proto3\";\n" +
                "message Status {\n" +
                "  enum State {\n" +
                "    UNKNOWN = 0;\n" +
                "    ACTIVE = 1;\n" +
                "    INACTIVE = 2;\n" +
                "  }\n" +
                "  State state = 1;\n" +
                "}\n";

        ProtoDocument doc = parser.parse(input);
        ProtoMessage status = doc.getMessages().get(0);
        List<ProtoEnum> enums = status.getEnums();

        Assert.assertEquals(1, enums.size());
        ProtoEnum state = enums.get(0);
        Assert.assertEquals("State", state.getName());
        Assert.assertEquals(3, state.getValues().size());
        Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(state.getValue("UNKNOWN")));
        Assert.assertEquals(Integer.valueOf(1), Integer.valueOf(state.getValue("ACTIVE")));
        Assert.assertEquals(Integer.valueOf(2), Integer.valueOf(state.getValue("INACTIVE")));
    }

    // ==================== 5. Service + RPC ====================

    @Test
    /**
     * testServiceRpc方法。
     */
    public void testServiceRpc() {
        String input = "syntax = \"proto3\";\n" +
                "message Request {}\n" +
                "message Response {}\n" +
                "service UserService {\n" +
                "  rpc GetUser(Request) returns (Response);\n" +
                "  rpc DeleteUser(Request) returns (Response);\n" +
                "}\n";

        ProtoDocument doc = parser.parse(input);
        Assert.assertEquals(1, doc.getServices().size());

        ProtoService service = doc.getServices().get(0);
        Assert.assertEquals("UserService", service.getName());
        Assert.assertEquals(2, service.getRpcs().size());

        ProtoRpc rpc1 = service.getRpcs().get(0);
        Assert.assertEquals("GetUser", rpc1.getName());
        Assert.assertEquals("Request", rpc1.getInputType());
        Assert.assertEquals("Response", rpc1.getOutputType());

        ProtoRpc rpc2 = service.getRpcs().get(1);
        Assert.assertEquals("DeleteUser", rpc2.getName());
        Assert.assertEquals("Request", rpc2.getInputType());
        Assert.assertEquals("Response", rpc2.getOutputType());
    }

    // ==================== 6. 嵌套 message ====================

    @Test
    /**
     * testNestedMessage方法。
     */
    public void testNestedMessage() {
        String input = "syntax = \"proto3\";\n" +
                "message Outer {\n" +
                "  message Inner {\n" +
                "    string name = 1;\n" +
                "  }\n" +
                "  Inner inner = 1;\n" +
                "}\n";

        ProtoDocument doc = parser.parse(input);
        ProtoMessage outer = doc.getMessages().get(0);
        Assert.assertEquals("Outer", outer.getName());

        List<ProtoMessage> nested = outer.getMessages();
        Assert.assertEquals(1, nested.size());
        Assert.assertEquals("Inner", nested.get(0).getName());
        Assert.assertEquals(1, nested.get(0).getFields().size());
        Assert.assertEquals("name", nested.get(0).getFields().get(0).getName());

        Assert.assertEquals(1, outer.getFields().size());
        Assert.assertEquals("inner", outer.getFields().get(0).getName());
    }

    // ==================== 7. package + import ====================

    @Test
    /**
     * testPackageImport方法。
     */
    public void testPackageImport() {
        String input = "syntax = \"proto3\";\n" +
                "package com.example;\n" +
                "import \"google/protobuf/timestamp.proto\";\n" +
                "import \"user.proto\";\n" +
                "message Person {}\n";

        ProtoDocument doc = parser.parse(input);

        Assert.assertEquals("proto3", doc.getSyntax());
        Assert.assertEquals("com.example", doc.getPackageName());
        Assert.assertEquals(2, doc.getImports().size());
        Assert.assertEquals("google/protobuf/timestamp.proto", doc.getImports().get(0));
        Assert.assertEquals("user.proto", doc.getImports().get(1));
        Assert.assertEquals(1, doc.getMessages().size());
    }

    // ==================== 8. // 和 /* */ 注释 ====================

    @Test
    /**
     * testSingleLineComment方法。
     */
    public void testSingleLineComment() {
        String input = "syntax = \"proto3\";\n" +
                "// This is a single line comment\n" +
                "message Person {\n" +
                "  string name = 1; // field comment\n" +
                "}\n";

        ProtoDocument doc = parser.parse(input);
        Assert.assertEquals("proto3", doc.getSyntax());
        Assert.assertEquals(1, doc.getMessages().size());
        Assert.assertEquals("Person", doc.getMessages().get(0).getName());
    }

    @Test
    /**
     * testMultiLineComment方法。
     */
    public void testMultiLineComment() {
        String input = "syntax = \"proto3\";\n" +
                "/* This is a\n" +
                "   multi-line comment */\n" +
                "message Person {\n" +
                "  /* Inner comment */\n" +
                "  string name = 1;\n" +
                "}\n";

        ProtoDocument doc = parser.parse(input);
        Assert.assertEquals("proto3", doc.getSyntax());
        Assert.assertEquals(1, doc.getMessages().size());
        Assert.assertEquals("Person", doc.getMessages().get(0).getName());
        Assert.assertEquals(1, doc.getMessages().get(0).getFields().size());
    }

    // ==================== 9. syntax = "proto3" ====================

    @Test
    /**
     * testSyntax方法。
     */
    public void testSyntax() {
        String input = "syntax = \"proto3\";\n" +
                "message Person {}\n";

        ProtoDocument doc = parser.parse(input);
        Assert.assertEquals("proto3", doc.getSyntax());
    }

    // ==================== 10. 完整 proto round-trip ====================

    @Test
    /**
     * testRoundTrip方法。
     */
    public void testRoundTrip() {
        String input = "syntax = \"proto3\";\n" +
                "package com.example;\n" +
                "import \"google/protobuf/timestamp.proto\";\n" +
                "\n" +
                "message Person {\n" +
                "  string name = 1;\n" +
                "  int32 age = 2;\n" +
                "  repeated string emails = 3;\n" +
                "  bool active = 4;\n" +
                "}\n" +
                "\n" +
                "message Request {\n" +
                "  string id = 1;\n" +
                "}\n" +
                "\n" +
                "message Response {\n" +
                "  string result = 1;\n" +
                "}\n" +
                "\n" +
                "service UserService {\n" +
                "  rpc GetUser(Request) returns (Response);\n" +
                "  rpc DeleteUser(Request) returns (Response);\n" +
                "}\n";

        ProtoDocument doc = parser.parse(input);

        // Verify parsed data
        Assert.assertEquals("proto3", doc.getSyntax());
        Assert.assertEquals("com.example", doc.getPackageName());
        Assert.assertEquals(1, doc.getImports().size());
        Assert.assertEquals(3, doc.getMessages().size());
        Assert.assertEquals(1, doc.getServices().size());

        ProtoMessage person = doc.getMessages().get(0);
        Assert.assertEquals("Person", person.getName());
        Assert.assertEquals(4, person.getFields().size());

        // Round-trip: serialize back to proto string and re-parse
        String serialized = parser.toProto(doc);
        ProtoDocument reparsed = parser.parse(serialized);

        Assert.assertEquals(doc.getSyntax(), reparsed.getSyntax());
        Assert.assertEquals(doc.getPackageName(), reparsed.getPackageName());
        Assert.assertEquals(doc.getImports().size(), reparsed.getImports().size());
        Assert.assertEquals(doc.getMessages().size(), reparsed.getMessages().size());
        Assert.assertEquals(doc.getServices().size(), reparsed.getServices().size());

        // Verify message fields
        ProtoMessage personReparsed = reparsed.getMessages().get(0);
        Assert.assertEquals(person.getName(), personReparsed.getName());
        Assert.assertEquals(person.getFields().size(), personReparsed.getFields().size());

        for (int i = 0; i < person.getFields().size(); i++) {
            ProtoField orig = person.getFields().get(i);
            ProtoField re = personReparsed.getFields().get(i);
            Assert.assertEquals(orig.getType(), re.getType());
            Assert.assertEquals(orig.getName(), re.getName());
            Assert.assertEquals(orig.getTag(), re.getTag());
            Assert.assertEquals(orig.isRepeated(), re.isRepeated());
        }
    }

    // ==================== Additional Tests ====================

    @Test
    /**
     * testComplexEnum方法。
     */
    public void testComplexEnum() {
        String input = "syntax = \"proto3\";\n" +
                "enum Color {\n" +
                "  UNKNOWN = 0;\n" +
                "  RED = 1;\n" +
                "  GREEN = 2;\n" +
                "  BLUE = 3;\n" +
                "}\n";

        ProtoDocument doc = parser.parse(input);
        Assert.assertEquals(1, doc.getMessages().size());

        ProtoMessage wrapper = doc.getMessages().get(0);
        Assert.assertEquals(1, wrapper.getEnums().size());
        ProtoEnum color = wrapper.getEnums().get(0);
        Assert.assertEquals("Color", color.getName());
        Assert.assertEquals(4, color.getValues().size());
    }

    @Test
    /**
     * testMessageWithEnumInside方法。
     */
    public void testMessageWithEnumInside() {
        String input = "syntax = \"proto3\";\n" +
                "message Status {\n" +
                "  enum State {\n" +
                "    PENDING = 0;\n" +
                "    APPROVED = 1;\n" +
                "    REJECTED = 2;\n" +
                "  }\n" +
                "  State state = 1;\n" +
                "}\n";

        ProtoDocument doc = parser.parse(input);
        ProtoMessage status = doc.getMessages().get(0);

        Assert.assertEquals(1, status.getEnums().size());
        Assert.assertEquals("State", status.getEnums().get(0).getName());
        Assert.assertEquals(3, status.getEnums().get(0).getValues().size());
        Assert.assertEquals(1, status.getFields().size());
    }
}
