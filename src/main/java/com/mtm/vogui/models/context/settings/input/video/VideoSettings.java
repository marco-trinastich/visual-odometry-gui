/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.context.settings.input.video;

import com.mtm.vogui.models.context.settings.common.PathSettings;
import com.mtm.vogui.models.interfaces.WithDefault;

/**
 * Video settings
 * <p/>
 * Options related to input video.
 */
public class VideoSettings extends PathSettings implements WithDefault<VideoSettings> {

    public VideoSettings() {
        super();
    }

    public VideoSettings(VideoSettings video) {
        super(video);
    }
}
