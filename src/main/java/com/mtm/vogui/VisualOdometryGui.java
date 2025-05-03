/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui;

import com.mtm.vogui.gui.GuiApplication;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;

/**
 * Visual Odometry GUI
 * (Tracking and Mapping System based on Visual Odometry)
 */
@QuarkusMain
public class VisualOdometryGui {

    public static void main(String... args) {
        Quarkus.run(App.class, args);
    }

    public static class App implements QuarkusApplication {
        @Inject
        GuiApplication application;

        @Override
        public int run(String... args) throws Exception {
            // Init application
            this.application.start();

            Quarkus.waitForExit();
            return 0;
        }
    }
}
