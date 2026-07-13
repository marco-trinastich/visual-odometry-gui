/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.state;

import com.mtm.vogui.models.enums.gui.AppStatus;
import com.mtm.vogui.models.enums.gui.RecentPathTarget;
import com.mtm.vogui.models.enums.settings.DevicePath;
import com.mtm.vogui.models.enums.settings.SourceType;
import com.mtm.vogui.models.interfaces.Resolution;
import io.quarkus.arc.Unremovable;
import jakarta.inject.Singleton;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;

/**
 * Observable state of the JavaFX GUI, written by {@code fx.rendering.FxRenderSink} (fed by the
 * core through {@code RenderSink}) and bound by the views. Views never talk to the core directly.
 * <p>
 * JavaFX property pattern is hand-written on purpose: Lombok (especially the project's fluent
 * accessors) does not produce the {@code xxxProperty()} convention bindings rely on.
 * All writes must happen on the FX Application Thread.
 * <p>
 * {@code @Unremovable}: also resolved programmatically ({@code CDI.current()} in
 * {@code FxRenderSink}), which Arc cannot see at build time.
 */
@Singleton
@Unremovable
public class GuiState {

    private final ObjectProperty<AppStatus> appStatus = new SimpleObjectProperty<>(AppStatus.Ready);

    /** Latest input-preview frame ({@code null} when preview is off or no run is active). */
    private final ObjectProperty<Image> inputFrame = new SimpleObjectProperty<>();

    /** Latest vo output frame with tracked features drawn ({@code null} when no run is active). */
    private final ObjectProperty<Image> outputFrame = new SimpleObjectProperty<>();

    /**
     * KLT pyramid levels healed by the core at capture time (the requested value clamped to what the
     * frame size supports); {@code 0} until the core negotiates one. The tracker settings section
     * reflects it back into its field.
     */
    private final IntegerProperty kltPyramidLevels = new SimpleIntegerProperty();

    /**
     * Device resolution/path healed by the core at capture time (the requested value mapped to what the
     * device actually advertises). The input settings section reflects each back into its combo.
     */
    private final ObjectProperty<Resolution> healedDeviceResolution = new SimpleObjectProperty<>();
    private final ObjectProperty<DevicePath> healedDevicePath = new SimpleObjectProperty<>();

    /**
     * The last recent-path successfully used by the core (target + path), so the input section can
     * refresh the matching recent-path combo. A record rather than a bare string: the reflection needs
     * to know which history (calibration vs video) the path belongs to.
     */
    private final ObjectProperty<RecentPathUsed> recentPathUsed = new SimpleObjectProperty<>();

    /** A recent path the core used, tagged with the history it belongs to (see {@link #recentPathUsed}). */
    public record RecentPathUsed(RecentPathTarget target, String usedPath) {
    }

    /**
     * The currently selected input source, mirrored from the Input settings section so features outside
     * it (the toolbar's Device-only timed button) can react without reaching into that section.
     */
    private final ObjectProperty<SourceType> inputSource = new SimpleObjectProperty<>();

    public ObjectProperty<AppStatus> appStatusProperty() {
        return appStatus;
    }

    public ObjectProperty<Image> inputFrameProperty() {
        return inputFrame;
    }

    public ObjectProperty<Image> outputFrameProperty() {
        return outputFrame;
    }

    public IntegerProperty kltPyramidLevelsProperty() {
        return kltPyramidLevels;
    }

    public ObjectProperty<Resolution> healedDeviceResolutionProperty() {
        return healedDeviceResolution;
    }

    public ObjectProperty<DevicePath> healedDevicePathProperty() {
        return healedDevicePath;
    }

    public ObjectProperty<RecentPathUsed> recentPathUsedProperty() {
        return recentPathUsed;
    }

    public ObjectProperty<SourceType> inputSourceProperty() {
        return inputSource;
    }
}
