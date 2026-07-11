/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.context.settings.tracker;

import com.mtm.vogui.models.context.settings.tracker.klt.KltSettings;
import com.mtm.vogui.models.context.settings.tracker.surf.SurfSettings;
import com.mtm.vogui.models.enums.settings.TrackerType;
import com.mtm.vogui.models.interfaces.WithDefault;

import lombok.Data;
import org.jetbrains.annotations.NotNull;


/**
 * Tracker settings
 * <p/>
 * Options related to tracker type and options, with per-algorithm sub-settings.
 */
@Data
public class TrackerSettings implements WithDefault<TrackerSettings> {

    private TrackerType type;
    private KltSettings klt;
    private SurfSettings surf;
    private boolean showActiveTracks;
    private boolean showNewTracks;

    public TrackerSettings() {
        // Also the Jackson deserialization entry point
        this.klt = new KltSettings();
        this.surf = new SurfSettings();

        this.loadDefaults();
    }

    public TrackerSettings(@NotNull TrackerSettings tracker) {
        this.type = tracker.type;
        this.klt = new KltSettings(tracker.klt);
        this.surf = new SurfSettings(tracker.surf);
        this.showActiveTracks = tracker.showActiveTracks;
        this.showNewTracks = tracker.showNewTracks;
    }

    public void loadDefaults() {
        this.type = TrackerType.Klt;
        this.klt.loadDefaults();
        this.surf.loadDefaults();
        this.showActiveTracks = true;
        this.showNewTracks = false;
    }
}
