/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.utilities;

/**
 * ANSI escape-sequence helpers for colouring terminal/log output. Colours use the 256-colour
 * palette ({@code 38;5;N}); terminals that don't honour ANSI simply ignore the sequences.
 */
public final class AnsiUtils {

    private static final String RESET = "[0m";
    private static final String BOLD = "[1m";
    private static final String COLOUR_256 = "[38;5;%dm";

    // 256-colour palette codes
    public static final int BLUE = 39;
    public static final int ORANGE = 208;

    private AnsiUtils() {
    }

    /** Wraps {@code text} in bold + the given 256-colour code, resetting styling afterwards. */
    public static String boldColoured(String text, int colour256) {
        return BOLD + COLOUR_256.formatted(colour256) + text + RESET;
    }
}
