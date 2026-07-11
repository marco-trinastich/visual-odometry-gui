/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.context.settings.input.device.opencv;

import com.mtm.vogui.models.interfaces.WithDefault;
import lombok.Data;
import org.jetbrains.annotations.NotNull;


/**
 * OpenCv camera settings
 * <p/>
 * Persisted options related to the OpenCv (JavaCV) device. Pure data: device
 * identifiers are numeric capture indices, resolved at runtime by the
 * {@code core.integration.discovery} layer, never stored here.
 */
@Data
public class OpenCvCameraSettings implements WithDefault<OpenCvCameraSettings> {

    private String path;

    public OpenCvCameraSettings() {
        this.loadDefaults();
    }

    public OpenCvCameraSettings(@NotNull OpenCvCameraSettings openCv) {
        this.path = openCv.path != null ? openCv.path : "";
    }

    public void loadDefaults() {
        // An empty path means "first available device": healed at GUI level via discovery
        this.path = "";
    }
}
