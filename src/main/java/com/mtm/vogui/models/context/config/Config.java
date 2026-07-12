/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.context.config;

import com.mtm.vogui.models.enums.gui.UiToolkit;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "config")
public interface Config {
    @WithDefault("JavaFx")
    UiToolkit ui();

    Settings settings();

    interface Settings {
        String fileName();
    }
}
