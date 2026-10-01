package com.zifang.util.office.core;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;

/**
 * RoundTripAssert 自测：用一对写入/读回 lambda 验证最少闭环。
 */
public class RoundTripAssertTest {

    @Test
    public void assertEquals_passesWhenRoundTripEquals() throws Exception {
        String expected = "hello";
        RoundTripAssert.assertEquals(expected,
                out -> out.write(expected.getBytes("UTF-8")),
                in -> new String(readAll(in), "UTF-8"));
    }

    @Test(expected = AssertionError.class)
    public void assertEquals_throwsWhenRoundTripDiffers() throws Exception {
        RoundTripAssert.assertEquals("expected",
                out -> out.write("actual".getBytes()),
                in -> new String(readAll(in)));
    }

    @Test
    public void assertContains_passes() throws Exception {
        RoundTripAssert.assertContains("substring",
                out -> out.write("this is a substring inside".getBytes()),
                in -> new String(readAll(in)));
    }

    @Test(expected = AssertionError.class)
    public void assertContains_throwsWhenMissing() throws Exception {
        RoundTripAssert.assertContains("missing",
                out -> out.write("present".getBytes()),
                in -> new String(readAll(in)));
    }

    private static byte[] readAll(InputStream in) throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] chunk = new byte[1024];
        int n;
        while ((n = in.read(chunk)) > 0) {
            buf.write(chunk, 0, n);
        }
        return buf.toByteArray();
    }
}
