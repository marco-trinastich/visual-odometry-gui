/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.constants;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class AppConstants {
    // Time patterns
    public final static String SECONDS_PATTERN = "%ss";
    public final static String MINUTES_PATTERN = "%sm %ss";

    // Decimal patterns
    public static final DecimalFormatSymbols US_LOCALE = new DecimalFormatSymbols(Locale.US);
    public static final String EXPONENTIAL_PATTERN = "0.##E0";
    public static final DecimalFormat EXPONENTIAL_FORMAT = new DecimalFormat(EXPONENTIAL_PATTERN, US_LOCALE);
    public static final String POSITIVE_EXP = "E";
    public static final String POSITIVE_EXP_ALT = " * 10^";
    public static final String NEGATIVE_EXP = "E-";
    public static final String NEGATIVE_EXP_ALT = " * 10^-";
    public static final String EMPTY_DECIMAL = ".0";

    // Generic
    public static final String DOT_SEPARATED_PATTERN = "%s.%s";
    public static final String RESOLUTION_SEPARATOR = "x";
    public static final String RESOLUTION_PATTERN = "%s" + RESOLUTION_SEPARATOR + "%s";
    public static final String RESOLUTION_PATTERN_GUI = "%s" + RESOLUTION_SEPARATOR + "%s (%s)";
    public static final String EMPTY_STRING = "";

    // Threads
    public static final String BOOFCV_CAMERA_THREAD = "BoofCv Camera Thread";
    public static final String V4L4J_CAMERA_THREAD = "V4l4j Camera Thread";
    public static final String BUFFER_MONITOR_THREAD = "Buffer Monitor Thread";
    public static final String FPS_COUNTER_THREAD = "Fps Counter Thread";
    public static final String VO_EXECUTOR_THREAD = "VO Executor Thread";
    public static final String VO_TOOLBAR_THREAD = "VO Toolbar Thread";
    public static final String VO_TIMED_STOP_THREAD = "VO Timed Stop Thread";
    public static final String VO_TIMED_STOP_COUNTDOWN_THREAD = "VO Timed Stop Countdown Thread";
}
