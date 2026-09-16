package com.zifang.util.media.graph.image;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * GifEncoderTest类。
 */
public class GifEncoderTest {

    @Test
    /**
     * testGifEncoderExists方法。
     */
    public void testGifEncoderExists() {
        // GifEncoder class is in GIF subpackage
        assertNotNull(com.zifang.util.media.graph.image.GIF.GifEncoder.class);
    }
}
