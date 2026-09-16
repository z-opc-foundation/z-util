package com.zifang.util.parser.proto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * ProtoG4Parser 烟雾测试：验证 G4 DSL 路径能正确产出 ProtoDocument。
 */
public class ProtoG4ParserTest {

    private final ProtoG4Parser parser = new ProtoG4Parser();

    @Test
    public void testSyntaxAndPackage() {
        ProtoDocument doc = parser.parse("syntax = \"proto3\";\npackage example;\n");
        assertNotNull(doc);
        assertEquals("proto3", doc.getSyntax());
        assertEquals("example", doc.getPackageName());
    }

    @Test
    public void testMessageWithFields() {
        ProtoDocument doc = parser.parse(
                "message User {\n" +
                        "  int32 id = 1;\n" +
                        "  string name = 2;\n" +
                        "}"
        );
        assertEquals(1, doc.getMessages().size());
        assertEquals("User", doc.getMessages().get(0).getName());
        assertEquals(2, doc.getMessages().get(0).getFields().size());
    }

    @Test
    public void testService() {
        ProtoDocument doc = parser.parse(
                "service S {\n" +
                        "  rpc Get(Request) returns (Response);\n" +
                        "}"
        );
        assertEquals(1, doc.getServices().size());
        assertEquals("S", doc.getServices().get(0).getName());
        assertEquals("Get", doc.getServices().get(0).getRpcs().get(0).getName());
    }

    @Test
    public void testImport() {
        ProtoDocument doc = parser.parse("import \"foo.proto\";\n");
        assertEquals(1, doc.getImports().size());
        assertEquals("foo.proto", doc.getImports().get(0));
    }
}
