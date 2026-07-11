/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.enums.gui;

/**
 * Identifies which recent-paths history a successfully used path belongs to,
 * so the GUI can refresh the matching selector without the core knowing any widget.
 */
public enum RecentPathTarget {
    Calibration,
    VideoSource
}
