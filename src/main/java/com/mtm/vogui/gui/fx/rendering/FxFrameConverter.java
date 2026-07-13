/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.rendering;

import javafx.scene.image.Image;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.nio.IntBuffer;

/**
 * FX-thread frame converter: turns an AWT {@link BufferedImage} into a live JavaFX {@link Image},
 * reusing a single {@link PixelBuffer}-backed {@link WritableImage} across frames (zero per-frame
 * allocation). This is the Swing-parity half of the render-cost fix: the vo worker hands the raw
 * {@code BufferedImage} across the thread boundary and the conversion happens <em>here</em>, on the FX
 * Application Thread, mirroring how Swing's AWT paints the reference directly.
 * <p>
 * The pixel unpack has type-aware fast paths for the two formats the capture/video stacks actually
 * produce — {@code TYPE_3BYTE_BGR} (file video, OpenCv) and {@code TYPE_INT_BGR} (BoofCv webcam) — read
 * straight off the backing raster array (no per-pixel {@code ColorModel} dispatch, unlike
 * {@code getRGB}), with a {@code getRGB} fallback for any other layout. Since unpack and upload both run
 * on the FX thread, the reused buffer needs no pool or double-buffering.
 * <p>
 * Not thread-safe by design: call {@link #convert} only on the FX Application Thread.
 */
public final class FxFrameConverter {

    private PixelBuffer<IntBuffer> pixelBuffer;
    private WritableImage image;
    private int[] argb;
    private int width;
    private int height;

    /**
     * Unpacks {@code src} into the reused PixelBuffer and returns the live Image. The returned Image is
     * stable across calls: {@link PixelBuffer#updateBuffer} refreshes the pixels in place, so a bound
     * {@code ImageView} repaints itself without the property value changing. The buffers are
     * (re)allocated only when the frame dimensions change.
     */
    public Image convert(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        if (image == null || w != width || h != height) {
            width = w;
            height = h;
            argb = new int[w * h];
            pixelBuffer = new PixelBuffer<>(w, h, IntBuffer.wrap(argb), PixelFormat.getIntArgbPreInstance());
            image = new WritableImage(pixelBuffer);
        }
        unpackArgb(src, argb, w, h);
        pixelBuffer.updateBuffer(pb -> null); // null dirty region = whole image
        return image;
    }

    // Opaque frames (alpha forced to 0xFF), so pre-multiplied ARGB equals straight ARGB.
    private static void unpackArgb(BufferedImage src, int[] dst, int w, int h) {
        int type = src.getType();
        var buffer = src.getRaster().getDataBuffer();
        if (type == BufferedImage.TYPE_INT_BGR && buffer instanceof DataBufferInt intBuf
                && intBuf.getData().length == w * h) {
            int[] s = intBuf.getData(); // 0x00BBGGRR per pixel
            for (int i = 0; i < dst.length; i++) {
                int bgr = s[i];
                dst[i] = 0xFF000000 | ((bgr & 0xFF) << 16) | (bgr & 0x0000FF00) | ((bgr >>> 16) & 0xFF);
            }
        } else if (type == BufferedImage.TYPE_3BYTE_BGR && buffer instanceof DataBufferByte byteBuf
                && byteBuf.getData().length == w * h * 3) {
            byte[] s = byteBuf.getData(); // B, G, R per pixel
            for (int i = 0, j = 0; i < dst.length; i++, j += 3) {
                dst[i] = 0xFF000000
                        | ((s[j + 2] & 0xFF) << 16) // R
                        | ((s[j + 1] & 0xFF) << 8)  // G
                        | (s[j] & 0xFF);            // B
            }
        } else {
            // Safety net: any other type / non-contiguous raster goes through the slow per-pixel path.
            src.getRGB(0, 0, w, h, dst, 0, w);
        }
    }
}
