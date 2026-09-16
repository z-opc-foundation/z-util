package io.zifu.z.serialize.core;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 测试字段索引的编码/解码和范围查询。
 */
public class FieldIndexTest {

    @Test
    public void testEncodeDecode() throws IOException {
        FieldIndex index = FieldIndex.builder()
                .add(1, 0, 5)
                .add(2, 5, 10)
                .add(3, 15, 8)
                .build();

        byte[] encoded = index.encode();
        assertNotNull(encoded);
        assertTrue(encoded.length > 0);

        FieldIndex decoded = FieldIndex.decode(encoded);
        assertEquals(3, decoded.size());

        FieldIndex.Entry e1 = decoded.findByFieldId(1);
        assertNotNull(e1);
        assertEquals(0, e1.offset);
        assertEquals(5, e1.length);
        assertEquals(5, e1.end());

        FieldIndex.Entry e2 = decoded.findByFieldId(2);
        assertNotNull(e2);
        assertEquals(5, e2.offset);
        assertEquals(10, e2.length);

        FieldIndex.Entry e3 = decoded.findByFieldId(3);
        assertNotNull(e3);
        assertEquals(15, e3.offset);
        assertEquals(8, e3.length);
    }

    @Test
    public void testRangeQuery() throws IOException {
        FieldIndex index = FieldIndex.builder()
                .add(1, 0, 5)
                .add(2, 5, 10)
                .add(3, 15, 8)
                .add(4, 23, 6)
                .add(5, 29, 4)
                .build();

        byte[] encoded = index.encode();
        FieldIndex decoded = FieldIndex.decode(encoded);

        // 查询 fieldId 2-4
        List<FieldIndex.Range> ranges = decoded.findRanges(2, 4);
        assertEquals(3, ranges.size());
        assertEquals(2, ranges.get(0).fieldId());
        assertEquals(3, ranges.get(1).fieldId());
        assertEquals(4, ranges.get(2).fieldId());

        // 查询 fieldId 1-1
        List<FieldIndex.Range> single = decoded.findRanges(1, 1);
        assertEquals(1, single.size());

        // 查询不存在的范围
        List<FieldIndex.Range> empty = decoded.findRanges(10, 20);
        assertEquals(0, empty.size());
    }

    @Test
    public void testFindByIdNotFound() throws IOException {
        FieldIndex index = FieldIndex.builder()
                .add(1, 0, 5)
                .build();

        byte[] encoded = index.encode();
        FieldIndex decoded = FieldIndex.decode(encoded);

        assertNull(decoded.findByFieldId(99));
    }

    @Test
    public void testEmptyIndex() throws IOException {
        FieldIndex index = FieldIndex.builder().build();
        byte[] encoded = index.encode();
        FieldIndex decoded = FieldIndex.decode(encoded);
        assertEquals(0, decoded.size());
    }
}
