package io.zifu.z.serialize.formats.json;

import io.zifu.z.serialize.annotation.FieldType;
import io.zifu.z.serialize.annotation.ZField;
import io.zifu.z.serialize.annotation.ZMessage;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class JsonTranscoderTest {

    @ZMessage(id = 300, name = "test.Product")
    public static class Product {
        @ZField(id = 1, type = FieldType.VARINT) public long id;
        @ZField(id = 2, type = FieldType.LENGTH_DELIMITED) public String name;
        @ZField(id = 3, type = FieldType.VARINT) public List<String> tags;
        @ZField(id = 4, type = FieldType.LENGTH_DELIMITED) public Map<String, Integer> attrs;
        public Product() { tags = new ArrayList<>(); attrs = new LinkedHashMap<>(); }
    }

    @Test
    public void testJsonRoundTrip() throws IOException {
        Product p = new Product();
        p.id = 42L;
        p.name = "Widget";
        p.tags.add("featured");
        p.tags.add("sale");
        p.attrs.put("color", 1);
        p.attrs.put("size", 42);

        String json = JsonTranscoder.toJson(p);

        // Write to file for debugging (maven will print path on failure)
        try (FileWriter fw = new FileWriter("/tmp/zserialize-json-debug.txt")) {
            fw.write(json);
        }

        assertTrue("_id not found", json.contains("_id"));
        assertTrue("_type not found", json.contains("_type"));
        assertTrue("field 1 not found", json.contains("\"1\""));
        assertTrue("field 2 not found", json.contains("\"2\""));

        // Round trip
        Product p2 = JsonTranscoder.fromJson(json, Product.class);
        assertEquals(p.id, p2.id);
        assertEquals(p.name, p2.name);
        assertEquals(p.tags, p2.tags);
        assertEquals(p.attrs, p2.attrs);
    }
}
