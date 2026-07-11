/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.core.integration.camera;

import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.core.integration.BufferStatus;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/**
 * Factory contract shared by every {@link BufferedCamera} implementation
 * (their static {@code from} methods), so the device-open flow can be
 * written once for all camera types.
 */
@FunctionalInterface
public interface CameraFactory {

    BufferedCamera create(AppContext context,
                          Consumer<BufferedImage> guiRenderer,
                          Consumer<BufferStatus> bufferRenderer);
}
