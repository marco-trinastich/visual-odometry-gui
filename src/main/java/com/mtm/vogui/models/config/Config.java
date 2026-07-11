/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "config")
public interface Config {
    Settings settings();

    interface Settings {
        String fileName();
    }
}
