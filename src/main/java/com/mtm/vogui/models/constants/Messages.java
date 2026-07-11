/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.constants;

public class Messages {

    // Visual Odometry
    public final static String VO_FAILED = "Warning: Visual Odometry estimation failed. Skipping frames, reset " +
            "context to restore.";
    public final static String VO_EXCEPTION = "Error: Visual Odometry processing failed: %s";
    public final static String VO_UNEXPECTED_ERROR = "Error: unexpected error during Visual Odometry processing";

    // Devices
    public final static String UNKNOWN_SOURCE_EXCEPTION = "Error: Unknown source %s";
    public final static String BUFFER_TIMEOUT_EXCEPTION = "Error: Buffer timed out. Aborting.";
    public final static String BUFFER_EMPTY_POLL = "Warning: device buffer empty after waitBuffer while running. " +
            "Skipping frame.";
    public final static String OPEN_VIDEO_ERROR = "Error opening video: %s";
    public final static String OPEN_DEVICE_ERROR = "Error opening camera device: %s";
    public final static String DEVICE_SETUP_ERROR = "Error setting up capture: %s";
    public final static String TRACKER_SETUP_ERROR = "Error setting up tracker: %s";
    public final static String DEVICE_INIT_ERROR = "Error starting capture: %s";
    public final static String DEVICE_V4L4J_MISSING_CONTROLS = "Specified controls not found";
    public final static String DEVICE_V4L4J_ERROR = "Error during V4L4J capture: %s.\n Cause: %s";
    public final static String DEVICE_V4L4J_CLOSE_ERROR = "Error closing V4L4J device.\n Cause: %s";
    public final static String DEVICE_WARMUP_LOG = "Device warmup: discarded %d black frames in %d ms before the " +
            "first real frame";
    public final static String DEVICE_BOOFCV_CAPTURE_ERROR = "Error during BoofCv capture.\n Cause: %s";
    public final static String DEVICE_BOOFCV_CLOSE_ERROR = "Error closing BoofCv device.\n Cause: %s";

    // Gui
    public final static String DISPLAY_VALUE_NOT_EDITABLE_EXCEPTION = "Error: Cannot set a display value combo box " +
            "as editable.";

    // Generic
    public final static String NOT_IMPLEMENTED_EXCEPTION = "Error: Method %s not implemented";
    public final static String GENERATOR_EXCEPTION = "Error: Cannot generate output value. Caller: %s (details: %s)";
    public final static String CONSUMER_EXCEPTION = "Error: Cannot consume input value. Caller: %s (details: %s)";

    // Logging
    public final static String LOAD_SETTINGS_EXCEPTION = "Error loading settings from: %s (%s)";
    public final static String SAVE_SETTINGS_EXCEPTION = "Error saving settings to: %s (%s)";
    public final static String V4L4J_DEVICE_FPS_LOG = """
            \s
            Fps status:
            \s
            Average FPS: %s
            Current FPS: %s
            Total processed: %s
            Total seconds: %s
            """;
    public final static String BUFFER_MONITOR_LOG = """
            \s
            Buffer Status:
            \s
            Initial heap size: %s
            Max heap size: %s
            Current buffer size: %s (image size: %s)

            Max buffer size: %s ==> Max buffer items: %s
            """;
}
