/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.components.common.image;


import com.mtm.vogui.utilities.ImageUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import java.awt.image.renderable.RenderableImage;
import java.util.Vector;

public class UnscaledImage implements RenderableImage {
    private final float x;
    private final float y;
    private final int width;
    private final int height;

    @Getter
    private final BufferedImage sourceImage;

    private UnscaledImage(BufferedImage sourceImage) {
        this(0, 0, sourceImage);
    }

    private UnscaledImage(float x, float y, @NotNull BufferedImage sourceImg) {
        this.x = x;
        this.y = y;
        this.width = sourceImg.getWidth();
        this.height = sourceImg.getHeight();
        this.sourceImage = sourceImg;
    }

    @Override
    public float getMinX() {
        return x;
    }

    @Override
    public float getMinY() {
        return y;
    }

    @Override
    public float getWidth() {
        return width;
    }

    @Override
    public float getHeight() {
        return height;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public Object getProperty(String name) {
        return Image.UndefinedProperty;
    }

    @Override
    public String[] getPropertyNames() {
        return new String[0];
    }

    @Override
    public Vector<RenderableImage> getSources() {
        return null;
    }

    @Override
    public RenderedImage createDefaultRendering() {
        return this.sourceImage;
    }

    @Override
    public RenderedImage createScaledRendering(int width, int height, RenderingHints hints) {
        return this.sourceImage;
    }

    @Override
    public RenderedImage createRendering(@NotNull RenderContext context) {
        // Undo scaling transform
        // (avoid forced jdk up-scaling leading to lo-res rendering)
        AffineTransform transform = context.getTransform();
        if (transform.getScaleY() > 0 && transform.getScaleX() > 0) {
            transform.scale(1 / transform.getScaleY(), 1 / transform.getScaleX());
        }

        // Define drawing surface according to transform
        Point2D size = new Point2D.Float(this.getWidth(), this.getHeight());
        Shape shape = context.getAreaOfInterest();
        if (shape != null) {
            Rectangle2D bounds = shape.getBounds2D();
            size = new Point2D.Double(
                    bounds.getWidth(), bounds.getHeight());
        }
        transform.transform(size, size);

        // Generate transformed image
        BufferedImage image = new BufferedImage((int) Math.ceil(size.getX()), (int) Math.ceil(size.getY()),
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = (Graphics2D) image.getGraphics();
        RenderingHints hints = context.getRenderingHints();
        if (hints != null) {
            g2.setRenderingHints(hints);
        } else {
            hints = g2.getRenderingHints();
            hints.putAll(ImageUtils.getHighQualityRenderingHints());
            g2.setRenderingHints(hints);
        }
        g2.setTransform(transform);
        g2.drawImage(this.sourceImage, null, 0, 0);
        g2.dispose();

        return image;
    }

    public static @NotNull UnscaledImage from(BufferedImage sourceImage) {
        return new UnscaledImage(sourceImage);
    }
}