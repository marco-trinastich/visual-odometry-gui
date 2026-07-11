/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.settings.core.input.device.v4l4j;

import com.mtm.vogui.models.interfaces.WithDefault;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * V4l4j camera settings
 * <p/>
 * Persisted options related to the V4l4j device. Pure data: the available
 * {@code /dev/video*} nodes are discovered at runtime by the
 * {@code core.integration.discovery} layer, never stored here.
 */
@Data
@Dependent
public class V4l4jCameraSettings implements Serializable, WithDefault<V4l4jCameraSettings> {

    private String path;
    private boolean sustainFramerate;
    private boolean timeoutImageIO;
    private boolean keepFormat;

    @Inject
    public V4l4jCameraSettings() {
        this.loadDefaults();
    }

    public V4l4jCameraSettings(@NotNull V4l4jCameraSettings v4l4j) {
        this.path = v4l4j.path != null ? v4l4j.path : "";
        this.sustainFramerate = v4l4j.sustainFramerate;
        this.timeoutImageIO = v4l4j.timeoutImageIO;
        this.keepFormat = v4l4j.keepFormat;
    }

    public void loadDefaults() {
        // An empty path means "first available device": healed at GUI level via discovery
        this.path = "";
        this.sustainFramerate(false);
        this.timeoutImageIO(false);
        this.keepFormat(false);
    }
}
