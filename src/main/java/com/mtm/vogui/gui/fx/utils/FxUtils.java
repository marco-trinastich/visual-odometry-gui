/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.utils;

import com.mtm.vogui.models.constants.AppConstants;
import com.mtm.vogui.utilities.OSUtils;
import io.quarkus.logging.Log;
import jakarta.enterprise.inject.spi.CDI;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.Taskbar;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
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
     * Adds the app-wide scene stylesheet ({@code app.css}) on top of the AtlantaFX user-agent theme.
     * A scene stylesheet (not user-agent) so it survives the live light/dark theme swap.
     */
    public static void applyAppStylesheet(Scene scene) {
        scene.getStylesheets().add(appStylesheetUrl());
    }

    /**
     * Adds {@code app.css} to a parent's own stylesheets — for subtrees that live outside the main
     * scene (e.g. a {@link javafx.stage.Popup}'s content), which do not inherit the scene stylesheet.
     */
    public static void applyAppStylesheet(Parent parent) {
        parent.getStylesheets().add(appStylesheetUrl());
    }

    private static String appStylesheetUrl() {
        return Objects.requireNonNull(
                FxUtils.class.getResource("/gui/fx/app.css"), "app.css not found").toExternalForm();
    }

    /**
     * Builds an {@link FXMLLoader} for the given classpath resource with the CDI controller factory
     * wired in, so FXML controllers are resolved as beans (Dependent/Unremovable). Central place for
     * the loader setup shared by the shell and every feature that loads its own FXML.
     */
    public static FXMLLoader newFxmlLoader(String resourcePath) {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                FxUtils.class.getResource(resourcePath), () -> "FXML resource not found: " + resourcePath));
        loader.setControllerFactory(type -> CDI.current().select(type).get());
        return loader;
    }

    /**
     * Converts an AWT {@link BufferedImage} (as produced by BoofCV/the capture stack) into a JavaFX
     * {@link Image}. Safe to call off the FX Application Thread — it only allocates pixels, touching no
     * live scene — so the vo worker converts frames before the coalesced hand-off to the FX thread.
     * {@code getRGB} normalises any source {@code BufferedImage} type to ARGB, so no assumption is made
     * about the incoming raster layout.
     */
    public static Image toFxImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] argb = image.getRGB(0, 0, width, height, null, 0, width);
        WritableImage fxImage = new WritableImage(width, height);
        fxImage.getPixelWriter().setPixels(0, 0, width, height,
                PixelFormat.getIntArgbInstance(), argb, 0, width);
        return fxImage;
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

    /**
     * Ordered, non-dropping hand-off to the FX thread: every submitted value is delivered (unlike
     * {@link #coalescedFxConsumer}, which keeps only the latest), but FX jobs are coalesced — at most
     * one drain is queued at a time and it flushes the whole backlog in order. For append-style streams
     * (e.g. the tracked-points log) where losing an item is not acceptable but per-item {@code runLater}
     * flooding is. Submit from any thread; {@code fxThreadAction} runs on the FX Application Thread.
     */
    public static <T> Consumer<T> orderedFxConsumer(Consumer<T> fxThreadAction) {
        ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();
        AtomicBoolean scheduled = new AtomicBoolean(false);
        Runnable drain = new Runnable() {
            @Override
            public void run() {
                T item;
                while ((item = queue.poll()) != null) {
                    fxThreadAction.accept(item);
                }
                scheduled.set(false);
                // A value enqueued during the drain must not be stranded: re-arm if the queue refilled.
                if (!queue.isEmpty() && scheduled.compareAndSet(false, true)) {
                    Platform.runLater(this);
                }
            }
        };
        return value -> {
            queue.offer(value);
            if (scheduled.compareAndSet(false, true)) {
                Platform.runLater(drain);
            }
        };
    }
}
