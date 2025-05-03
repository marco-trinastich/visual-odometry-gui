/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.settings.core.common;

import com.mtm.vogui.models.constants.AppConstants;
import com.mtm.vogui.utilities.CommonUtils;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Path settings
 * <p/>
 * Generic path based settings.
 */
@Data
public abstract class PathSettings implements Serializable {

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
        this.paths = this.defaultPaths();
        this.path = CommonUtils.getStringArrayFirst(this.paths);
    }

    protected abstract String[] defaultPaths();
}
