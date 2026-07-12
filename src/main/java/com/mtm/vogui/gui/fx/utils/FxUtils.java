/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.utils;

import com.mtm.vogui.models.constants.AppConstants;
import com.mtm.vogui.utilities.OSUtils;
import io.quarkus.logging.Log;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.Taskbar;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Filter;
import java.util.logging.Logger;

/**
 * FX helpers: FX Application Thread hand-off for worker threads, plus app-icon / dock wiring.
 */
public final class FxUtils {

    private static final String MACOS_APP_NAME_PROPERTY = "apple.awt.application.name";

    /**
     * JavaFX 20+ logs this at WARN whenever its classes come from the classpath (unnamed module)
     * instead of the module path. Unavoidable under the Quarkus boot (Quarkus owns the classpath),
     * and harmless — the toolkit runs fine. Filtered out by {@link #suppressUnnamedModuleWarning()}.
     */
    private static final String UNNAMED_MODULE_WARNING =
            "Unsupported JavaFX configuration: classes were loaded from";

    private FxUtils() {
    }

    /**
     * Drops only that one message on the {@code javafx} logger, chaining any pre-existing filter so
     * every other JavaFX log still passes. The message goes through the JBoss LogManager (Quarkus
     * console format), so a filter on the logger node stops it regardless of handler wiring — which
     * the equivalent {@code quarkus.log.*} filter config did not reliably do under {@code quarkus:dev}.
     * Call before {@code Application.launch}.
     */
    public static void suppressUnnamedModuleWarning() {
        Logger javafxLogger = Logger.getLogger("javafx");
        Filter existing = javafxLogger.getFilter();
        javafxLogger.setFilter(record ->
                (existing == null || existing.isLoggable(record))
                        && (record.getMessage() == null
                            || !record.getMessage().startsWith(UNNAMED_MODULE_WARNING)));
    }

    /**
     * Loads the app icon as a JavaFX image at its native resolution, or {@code null} if the
     * resource is missing. Callers size it at display time (e.g. via {@code ImageView.setFitWidth}).
     */
    public static Image appImage() {
        try (InputStream stream = FxUtils.class.getResourceAsStream(AppConstants.APP_ICON)) {
            return stream == null ? null : new Image(stream);
        } catch (IOException e) {
            Log.warnf(e, "Could not load app icon %s", AppConstants.APP_ICON);
            return null;
        }
    }

    /**
     * Wires the app icon to the given stage. The JavaFX stage icon covers the window and the
     * Windows/Linux taskbar; on macOS the dock ignores stage icons, so we mirror the Swing trick
     * and set it via AWT {@link Taskbar} (both toolkits share the same dock in-process).
     * Best-effort: a missing resource or an unsupported platform must never block startup.
     */
    public static void applyAppIcon(Stage stage) {
        Image icon = appImage();
        if (icon != null) {
            stage.getIcons().add(icon);
        }
        if (OSUtils.isMac()) {
            // Dock/menu app name (set before AWT initialises, i.e. before the Taskbar call below)
            System.setProperty(MACOS_APP_NAME_PROPERTY, AppConstants.APP_TITLE);
            setMacDockIcon();
        }
    }

    private static void setMacDockIcon() {
        try (InputStream stream = FxUtils.class.getResourceAsStream(AppConstants.APP_ICON)) {
            if (stream == null || !Taskbar.isTaskbarSupported()) {
                return;
            }
            Taskbar taskbar = Taskbar.getTaskbar();
            if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                taskbar.setIconImage(ImageIO.read(stream));
            }
        } catch (IOException | UnsupportedOperationException | SecurityException e) {
            Log.warnf(e, "Could not set macOS dock icon");
        }
    }

    /**
     * Runs the action on the FX thread and blocks the calling worker thread until it completes.
     * Needed for modal dialogs whose answer the worker must wait for.
     */
    public static void runOnFxThreadAndWait(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Runs the supplier on the FX thread, blocking the calling worker thread for the result.
     */
    public static <T> T callOnFxThreadAndWait(Supplier<T> action) {
        AtomicReference<T> result = new AtomicReference<>();
        runOnFxThreadAndWait(() -> result.set(action.get()));
        return result.get();
    }

    /**
     * Coalesced hand-off to the FX thread: high-frequency producers overwrite the pending value
     * and at most one FX job is queued at a time, so only the latest value is ever delivered
     * (same mechanism {@code javafx.concurrent.Task} uses internally; avoids runLater flooding).
     */
    public static <T> Consumer<T> coalescedFxConsumer(Consumer<T> fxThreadAction) {
        AtomicReference<T> pending = new AtomicReference<>();
        return value -> {
            if (pending.getAndSet(value) == null) {
                Platform.runLater(() -> fxThreadAction.accept(pending.getAndSet(null)));
            }
        };
    }
}
