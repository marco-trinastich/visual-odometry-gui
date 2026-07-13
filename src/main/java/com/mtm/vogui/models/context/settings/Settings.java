/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.context.settings;

import com.mtm.vogui.models.context.settings.chart.ChartSettings;
import com.mtm.vogui.models.context.settings.image.ImageSettings;
import com.mtm.vogui.models.context.settings.input.InputSettings;
import com.mtm.vogui.models.context.settings.tracker.TrackerSettings;
import com.mtm.vogui.models.context.settings.visualodometry.VisualOdometrySettings;
import com.mtm.vogui.models.enums.gui.ThemeMode;
import com.mtm.vogui.models.interfaces.WithDefault;

import lombok.Data;
import org.jetbrains.annotations.NotNull;


@Data
public class Settings implements WithDefault<Settings> {
    private boolean autosave;
    private ThemeMode theme;
    private InputSettings input;
    private ImageSettings image;
    private TrackerSettings tracker;
    private VisualOdometrySettings visualOdometry;
    private ChartSettings chart;

    public Settings() {
        // Also the Jackson deserialization entry point: builds the default tree so
        // fields absent from the persisted file keep their default values
        this.autosave = true;
        this.theme = ThemeMode.AUTO;
        this.input = new InputSettings();
        this.image = new ImageSettings();
        this.tracker = new TrackerSettings();
        this.visualOdometry = new VisualOdometrySettings();
        this.chart = new ChartSettings();
    }

    public Settings(@NotNull Settings core) {
        this.autosave(core.autosave);
        this.theme(core.theme);
        this.input(new InputSettings(core.input));
        this.image(new ImageSettings(core.image));
        this.tracker(new TrackerSettings(core.tracker));
        this.visualOdometry(new VisualOdometrySettings(core.visualOdometry));
        this.chart(new ChartSettings(core.chart));
    }

    public void loadDefaults(){
        this.autosave(true);
        this.theme(ThemeMode.AUTO);
        this.input().loadDefaults();
        this.image().loadDefaults();
        this.tracker().loadDefaults();
        this.visualOdometry().loadDefaults();
        this.chart().loadDefaults();
    }
}
