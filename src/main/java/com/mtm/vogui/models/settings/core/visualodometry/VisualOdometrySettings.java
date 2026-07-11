/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.settings.core.visualodometry;

import com.mtm.vogui.models.enums.settings.VisualOdometryType;
import com.mtm.vogui.models.interfaces.WithDefault;
import com.mtm.vogui.models.settings.core.visualodometry.monoplaneinfinity.MonoPlaneInfinitySettings;
import com.mtm.vogui.models.settings.core.visualodometry.monoplaneoverhead.MonoPlaneOverheadSettings;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Visual Odometry settings
 * </p>
 * Options related to visual odometry algorithm type and parameters.
 */
@Data
public class VisualOdometrySettings implements Serializable, WithDefault<VisualOdometrySettings> {

    private VisualOdometryType type;
    private MonoPlaneInfinitySettings monoPlaneInfinity;
    private MonoPlaneOverheadSettings monoPlaneOverhead;

    public VisualOdometrySettings() {
        // Also the Jackson deserialization entry point
        this.monoPlaneInfinity = new MonoPlaneInfinitySettings();
        this.monoPlaneOverhead = new MonoPlaneOverheadSettings();
        this.loadDefaults();
    }

    public VisualOdometrySettings(@NotNull VisualOdometrySettings voSettings) {
        this.type = voSettings.type();
        this.monoPlaneInfinity = new MonoPlaneInfinitySettings(voSettings.monoPlaneInfinity());
        this.monoPlaneOverhead = new MonoPlaneOverheadSettings(voSettings.monoPlaneOverhead());
    }

    public void loadDefaults() {
        this.type = VisualOdometryType.MonoPlaneInfinity;
        this.monoPlaneInfinity.loadDefaults();
        this.monoPlaneOverhead.loadDefaults();
    }
}
