package com.zifang.util.doc.pdf;

import com.zifang.util.doc.DocBlock;
import com.zifang.util.doc.DocDocument;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 图片解码：Flate 位图解成 PNG，带 SMask 时补 alpha 通道。 */
class PdfImageTest {

    private final PdfDocReader reader = new PdfDocReader();

    private static byte[] rgb(int width, int height) {
        byte[] data = new byte[width * height * 3];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = (y * width + x) * 3;
                data[i] = (byte) (x * 16);
                data[i + 1] = (byte) (y * 16);
                data[i + 2] = (byte) 128;
            }
        }
        return data;
    }

    private static byte[] alpha(int width, int height) {
        byte[] data = new byte[width * height];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 256);
        }
        return data;
    }

    private DocBlock.Image onlyImage(byte[] pdf) throws IOException {
        DocDocument doc = reader.read(pdf);
        DocBlock.Image image = null;
        for (DocBlock block : doc.getBlocks()) {
            if (block instanceof DocBlock.Image img) {
                assertEquals(null, image, "只应有一张图片");
                image = img;
            }
        }
        assertNotNull(image, "应当抽到图片");
        return image;
    }

    @Test
    void flateRgbImageBecomesPng() throws Exception {
        int width = 8;
        int height = 4;
        DocBlock.Image image = onlyImage(TestPdfs.withImage(width, height, rgb(width, height), null));
        assertEquals("image/png", image.mimeType());
        BufferedImage bi = ImageIO.read(new ByteArrayInputStream(image.data()));
        assertNotNull(bi, "产出的字节应当是合法 PNG");
        assertEquals(width, bi.getWidth());
        assertEquals(height, bi.getHeight());
        int pixel = bi.getRGB(3, 2);
        assertEquals(48, (pixel >> 16) & 0xFF, "红色通道");
        assertEquals(32, (pixel >> 8) & 0xFF, "绿色通道");
        assertEquals(128, pixel & 0xFF, "蓝色通道");
    }

    @Test
    void softMaskBecomesAlphaChannel() throws Exception {
        int width = 6;
        int height = 3;
        byte[] mask = alpha(width, height);
        DocBlock.Image image = onlyImage(TestPdfs.withImage(width, height, rgb(width, height), mask));
        BufferedImage bi = ImageIO.read(new ByteArrayInputStream(image.data()));
        assertNotNull(bi);
        assertEquals(width, bi.getWidth());
        int index = 2 * width + 1;
        assertEquals(mask[index] & 0xFF, (bi.getRGB(1, 2) >> 24) & 0xFF, "alpha 通道取自 SMask");
    }
}
