/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.core.rendering;

import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.context.settings.common.PathSettings;
import com.mtm.vogui.models.core.integration.BufferStatus;
import com.mtm.vogui.models.core.processing.ProcessingParameters;
import com.mtm.vogui.models.core.processing.ProcessingStatus;
import com.mtm.vogui.models.core.processing.fps.FpsStatus;
import com.mtm.vogui.models.enums.gui.AppStatus;
import com.mtm.vogui.models.enums.gui.RecentPathTarget;
import com.mtm.vogui.models.enums.settings.DevicePath;
import com.mtm.vogui.models.interfaces.Resolution;

import java.awt.image.BufferedImage;

/**
 * Presentation boundary of the processing core.
 * <p>
 * Everything the core wants shown to the user goes through this interface: the core never
 * touches widgets, toolkit classes or the {@code guiComponents} map directly, so the GUI
 * implementation ({@code gui.rendering.SwingRenderSink}) stays replaceable as a whole.
 * Implementations are responsible for marshalling to their UI thread; methods are called
 * from the vo worker thread.
 */
public interface RenderSink {

    // Dialogs (block the calling worker thread until the user answers)

    void notifyError(String message);

    /**
     * Shows an OK/Cancel error dialog.
     *
     * @return {@code true} only if the user confirmed with OK
     */
    boolean confirmOrCancel(String message);

    // App status

    void renderAppStatus(AppStatus appStatus);

    default void renderAppStatus(AppContext context) {
        renderAppStatus(context, null);
    }

    default void renderAppStatus(AppContext context, Throwable ex) {
        renderAppStatus(AppStatus.from(context.state().processing().get(), ex));
    }

    // Processing lifecycle

    void renderStartPoint(ProcessingParameters params);

    void renderEndPoint(ProcessingParameters params);

    void renderClearAllPoints();

    void resizeAndRepositionVideoFrames(ProcessingParameters params);

    // Per-frame rendering

    void renderVO(ProcessingStatus status, ProcessingParameters params, boolean voResult);

    void renderInputVideo(BufferedImage image);

    void renderCurrentFps(FpsStatus fpsStatus, ProcessingStatus status, ProcessingParameters params);

    void renderBufferStatus(BufferStatus bufferStatus);

    // Settings healed by the core at capture/validation time, reflected back into the GUI

    void deviceResolutionChanged(Resolution resolution);

    void devicePathChanged(DevicePath devicePath);

    void recentPathUsed(RecentPathTarget target, PathSettings pathSettings, String usedPath);

    void kltPyramidLevelsChanged(int pyramidLevels);
}
