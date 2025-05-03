/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.core.processing.frames;

import boofcv.struct.image.ImageBase;
import lombok.Builder;
import lombok.Data;
import org.javatuples.Pair;

import java.awt.image.BufferedImage;

@Data
@Builder
public class InnerFrame {
    private BufferedImage buffered;
    private ImageBase<?> left;

    public static InnerFrame from(Pair<BufferedImage, ImageBase<?>> frame) {
        return InnerFrame.builder()
                .buffered(frame.getValue0())
                .left(frame.getValue1())
                .build();
    }
}