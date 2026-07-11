/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.context.settings.common;

import com.mtm.vogui.models.constants.AppConstants;
import com.mtm.vogui.utilities.CommonUtils;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Path settings
 * <p/>
 * Generic path based settings. {@code paths} is a rolling most-recently-used
 * history: entries are recorded on browse confirmation and on successful use
 * (never on typing), newest first, capped at {@link #MAX_RECENT_PATHS}.
 */
@Data
public abstract class PathSettings {

    public static final int MAX_RECENT_PATHS = 25;

    private String path;
    private String[] paths;

    public PathSettings() {
        this.loadDefaults();
    }

    public PathSettings(@NotNull PathSettings pathSettings) {
        this.path = pathSettings.path != null ? pathSettings.path : AppConstants.EMPTY_STRING;
        this.paths = pathSettings.paths != null ? pathSettings.paths.clone() : new String[]{};
    }

    public void loadDefaults() {
        // No built-in defaults: paths are user-provided and persisted in the settings file
        this.paths = new String[]{};
        this.path = CommonUtils.getStringArrayFirst(this.paths);
    }

    /**
     * Records a path in the most-recently-used history: moved to front when already
     * present, inserted at front otherwise, keeping at most {@link #MAX_RECENT_PATHS}
     * entries.
     */
    public void pushRecentPath(String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        String recentPath = value.trim();
        var recent = new ArrayList<>(Arrays.asList(this.paths));
        recent.remove(recentPath);
        recent.addFirst(recentPath);
        if (recent.size() > MAX_RECENT_PATHS) {
            recent.subList(MAX_RECENT_PATHS, recent.size()).clear();
        }
        this.paths = recent.toArray(String[]::new);
    }

    /**
     * Removes a path from the most-recently-used history (the current {@code path}
     * selection is not affected).
     */
    public void removeRecentPath(String value) {
        if (value == null) {
            return;
        }
        this.paths = Arrays.stream(this.paths)
                .filter(recentPath -> !recentPath.equals(value.trim()))
                .toArray(String[]::new);
    }
}
