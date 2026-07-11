/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui;

import com.mtm.vogui.gui.RenderSinkProducer;
import com.mtm.vogui.gui.fx.FxApplication;
import com.mtm.vogui.gui.swing.GuiApplication;
import com.mtm.vogui.models.context.AppContext;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import javafx.application.Application;
import javafx.application.Platform;
import org.eclipse.microprofile.config.inject.ConfigProperty;

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
        // Lazy: the Swing application must not even be instantiated when the JavaFX UI is active
        @Inject
        Instance<GuiApplication> swingApplication;

        @Inject
        AppContext context;

        @ConfigProperty(name = RenderSinkProducer.UI_PROPERTY, defaultValue = "javafx")
        String ui;

        @Override
        public int run(String... args) throws Exception {
            if (RenderSinkProducer.UI_SWING.equalsIgnoreCase(this.ui)) {
                // Legacy Swing UI (parity reference during the JavaFX migration)
                this.swingApplication.get().start();
                Quarkus.waitForExit();
            } else {
                // Blocks the main thread until Platform.exit()/last window closed;
                // returning then shuts Quarkus down normally (CDI is up before launch)
                Application.launch(FxApplication.class, args);
            }
            return 0;
        }

        void onShutdown(@Observes ShutdownEvent event) {
            // Quarkus-initiated exits (Ctrl+C/SIGTERM) must also terminate the FX toolkit,
            // or Application.launch() would keep the main thread blocked
            if (!RenderSinkProducer.UI_SWING.equalsIgnoreCase(this.ui)) {
                Platform.exit();
            }

            // Autosave on shutdown, whatever the exit path (any window close, Ctrl+C,
            // dev-mode stop or live reload), in the currently active format
            if (this.context.settings().autosave()) {
                this.context.saveToCurrentFormat();
            }
        }
    }
}
