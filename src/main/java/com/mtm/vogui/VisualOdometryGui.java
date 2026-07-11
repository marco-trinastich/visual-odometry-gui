/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui;

import com.mtm.vogui.gui.GuiApplication;
import com.mtm.vogui.models.context.AppContext;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.enterprise.event.Observes;
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

        @Inject
        AppContext context;

        @Override
        public int run(String... args) throws Exception {
            // Init application
            this.application.start();

            Quarkus.waitForExit();
            return 0;
        }

        void onShutdown(@Observes ShutdownEvent event) {
            // Autosave on shutdown, whatever the exit path (any frame close, Ctrl+C,
            // dev-mode stop or live reload), in the currently active format
            if (this.context.settings().autosave()) {
                this.context.saveToCurrentFormat();
            }
        }
    }
}
