/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.settings.core.input.device.v4l4j;

import com.mtm.vogui.models.interfaces.WithDefault;
import com.mtm.vogui.models.settings.core.common.PathSettings;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

/**
 * V4l4j camera settings
 * <p/>
 * Options related to V4l4j device.
 */
@Data
@Dependent
@EqualsAndHashCode(callSuper = true)
public class V4l4jCameraSettings extends PathSettings implements WithDefault<V4l4jCameraSettings> {

    private boolean sustainFramerate;
    private boolean timeoutImageIO;
    private boolean keepFormat;

    @Inject
    public V4l4jCameraSettings() {
        super();
        this.loadInternalDefaults();
    }

    public V4l4jCameraSettings(@NotNull V4l4jCameraSettings v4l4j) {
        super(v4l4j);
        this.sustainFramerate = v4l4j.sustainFramerate;
        this.timeoutImageIO = v4l4j.timeoutImageIO;
        this.keepFormat = v4l4j.keepFormat;
    }

    public void loadDefaults() {
        super.loadDefaults();
        this.loadInternalDefaults();
    }

    private void loadInternalDefaults() {
        this.sustainFramerate(false);
        this.timeoutImageIO(false);
        this.keepFormat(false);
    }

    @Override
    protected String[] defaultPaths() {
        // Default path list (ideally the common ones)
        return new String[]{
                "/dev/video0",
                "/dev/video1"
        };
    }
}
