package com.zifang.util.media.graph.image.GIF;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * EncoderTest类。
 */
public class EncoderTest {

    @Test
    /**
     * testEncoderExists方法。
     */
    public void testEncoderExists() {
        // Encoder class has package-private constructor with args
        // Just verify the class is accessible
        assertNotNull(Encoder.class);
    }
}
