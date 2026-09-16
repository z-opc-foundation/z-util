package com.zifang.util.media.graph.image;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * GifBuilderTest类。
 */
public class GifBuilderTest {

    @Test
    /**
     * testConstruction方法。
     */
    public void testConstruction() {
        GifBuilder builder = new GifBuilder();
        assertNotNull(builder);
    }

    @Test
    /**
     * testSize方法。
     */
    public void testSize() {
        GifBuilder builder = new GifBuilder();
        GifBuilder result = builder.size(400, 300);
        assertSame(builder, result);
    }

    @Test
    /**
     * testDelay方法。
     */
    public void testDelay() {
        GifBuilder builder = new GifBuilder();
        GifBuilder result = builder.delay(100);
        assertSame(builder, result);
    }

    @Test
    /**
     * testRepeat方法。
     */
    public void testRepeat() {
        GifBuilder builder = new GifBuilder();
        GifBuilder result = builder.repeat(0);
        assertSame(builder, result);
    }

    @Test
    /**
     * testQuality方法。
     */
    public void testQuality() {
        GifBuilder builder = new GifBuilder();
        GifBuilder result = builder.quality(10);
        assertSame(builder, result);
    }

    @Test
    /**
     * testAddFrame方法。
     */
    public void testAddFrame() {
        GifBuilder builder = new GifBuilder();
        BufferedImage frame = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        GifBuilder result = builder.addFrame(frame);
        assertSame(builder, result);
    }

    @Test
    /**
     * testBuild方法。
     */
    public void testBuild() throws IOException {
        GifBuilder builder = new GifBuilder();
        builder.size(100, 100);
        builder.addFrame(new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB));
        GifBuilder result = builder.build();
        assertSame(builder, result);
    }

    @Test(expected = IllegalStateException.class)
    /**
     * testBuildWithoutFrames方法。
     */
    public void testBuildWithoutFrames() throws IOException {
        GifBuilder builder = new GifBuilder();
        builder.size(100, 100);
        builder.build(); // Should throw exception
    }

    @Test
    /**
     * testToBytes方法。
     */
    public void testToBytes() throws IOException {
        GifBuilder builder = new GifBuilder();
        builder.size(50, 50);
        builder.addFrame(new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB));
        builder.build();
        byte[] bytes = builder.toBytes();
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    /**
     * testWriteWithoutFrames方法。
     */
    public void testWriteWithoutFrames() throws IOException {
        GifBuilder builder = new GifBuilder();
        // Should throw exception
        try {
            builder.write(new ByteArrayOutputStream());
        } catch (IllegalStateException e) {
            // Expected
        }
    }
}
