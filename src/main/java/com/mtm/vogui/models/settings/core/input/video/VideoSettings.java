/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.settings.core.input.video;

import com.mtm.vogui.models.constants.SettingsConstants;
import com.mtm.vogui.models.interfaces.WithDefault;
import com.mtm.vogui.models.settings.core.common.PathSettings;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import java.io.Serializable;

/**
 * Video settings
 * <p/>
 * Options related to input video.
 */
@Dependent
public class VideoSettings extends PathSettings implements Serializable, WithDefault<VideoSettings> {

    @Inject
    public VideoSettings() {
        super();
    }

    public VideoSettings(VideoSettings video) {
        super(video);
    }

    @Override
    protected String[] defaultPaths() {
        // Default video list (example vo videos)
        return SettingsConstants.DEFAULT_VIDEO_PATHS;
    }
}
