/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.context.settings.input.device.boofcv;

import com.mtm.vogui.models.interfaces.WithDefault;
import lombok.Data;
import org.jetbrains.annotations.NotNull;


/**
 * BoofCv camera settings
 * <p/>
 * Persisted options related to the BoofCv device. Pure data: the available
 * webcams and their capabilities are discovered at runtime by the
 * {@code core.integration.discovery} layer, never stored here.
 */
@Data
public class BoofCvCameraSettings implements WithDefault<BoofCvCameraSettings> {

    private String path;

    public BoofCvCameraSettings() {
        this.loadDefaults();
    }

    public BoofCvCameraSettings(@NotNull BoofCvCameraSettings boofCv) {
        this.path = boofCv.path != null ? boofCv.path : "";
    }

    public void loadDefaults() {
        // An empty path means "first available device": healed at GUI level via discovery
        this.path = "";
    }
}
