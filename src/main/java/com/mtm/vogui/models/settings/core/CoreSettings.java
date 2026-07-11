/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.settings.core;

import com.mtm.vogui.models.interfaces.WithDefault;
import com.mtm.vogui.models.settings.core.chart.ChartSettings;
import com.mtm.vogui.models.settings.core.image.ImageSettings;
import com.mtm.vogui.models.settings.core.input.InputSettings;
import com.mtm.vogui.models.settings.core.tracker.TrackerSettings;
import com.mtm.vogui.models.settings.core.visualodometry.VisualOdometrySettings;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

@Data
public class CoreSettings implements Serializable, WithDefault<CoreSettings> {
    private boolean autosave;
    private InputSettings input;
    private ImageSettings image;
    private TrackerSettings tracker;
    private VisualOdometrySettings visualOdometry;
    private ChartSettings chart;

    public CoreSettings() {
        // Also the Jackson deserialization entry point: builds the default tree so
        // fields absent from the persisted file keep their default values
        this.autosave = true;
        this.input = new InputSettings();
        this.image = new ImageSettings();
        this.tracker = new TrackerSettings();
        this.visualOdometry = new VisualOdometrySettings();
        this.chart = new ChartSettings();
    }

    public CoreSettings(@NotNull CoreSettings core) {
        this.autosave(core.autosave);
        this.input(new InputSettings(core.input));
        this.image(new ImageSettings(core.image));
        this.tracker(new TrackerSettings(core.tracker));
        this.visualOdometry(new VisualOdometrySettings(core.visualOdometry));
        this.chart(new ChartSettings(core.chart));
    }

    public void loadDefaults(){
        this.autosave(true);
        this.input().loadDefaults();
        this.image().loadDefaults();
        this.tracker().loadDefaults();
        this.visualOdometry().loadDefaults();
        this.chart().loadDefaults();
    }
}
