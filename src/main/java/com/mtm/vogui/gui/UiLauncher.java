/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui;

/**
 * Uniform lifecycle of a UI toolkit, so the entry point boots any UI the same way:
 * launch blocking the main thread, terminate on Quarkus-initiated shutdown.
 */
public interface UiLauncher {

    /**
     * Starts the UI and blocks the calling (main) thread until the application exits;
     * returning triggers the normal Quarkus shutdown.
     */
    void launchAndWait(String... args);

    /**
     * Terminates the UI toolkit on a Quarkus-initiated shutdown (Ctrl+C/SIGTERM),
     * so {@link #launchAndWait} unblocks.
     */
    void terminate();
}
