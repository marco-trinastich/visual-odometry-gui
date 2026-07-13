/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.state;

import com.mtm.vogui.models.core.integration.BufferStatus;
import com.mtm.vogui.models.core.processing.tracking.TrackedPoint;
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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

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
 * <p>
 * Round-trip invariant: inside a listener on one of these properties, always act on the listener's
 * {@code newValue} — never re-read {@code context.settings()} or another ViewModel property to
 * recover the value that just changed. A bound/mirrored property (e.g. {@link #inputSource}) can
 * reach its listeners before the separate listener that commits the choice into settings has run, so
 * a settings re-read may still see the previous value (this bit the toolbar's Device-only timed
 * button). Re-reading settings is legitimate only in the reflect pattern, where the core writes
 * settings before emitting the signal that triggers the listener.
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

    /** Latest per-frame telemetry snapshot ({@code null} until the first processed frame). */
    private final ObjectProperty<Telemetry> telemetry = new SimpleObjectProperty<>();

    /** Latest instantaneous framerates ({@code null} until the first per-second tick). */
    private final ObjectProperty<CurrentFps> currentFps = new SimpleObjectProperty<>();

    /** Latest buffer status, or {@code null} when the buffer section should be hidden. */
    private final ObjectProperty<BufferStatus> buffer = new SimpleObjectProperty<>();

    /** The run's tracked-points log (append-only during a run, cleared on Clear); bound by the list view. */
    private final ObservableList<TrackedPoint> trackedPoints = FXCollections.observableArrayList();

    /**
     * Trajectory event stream. The sink emits {@link TrajectoryEvent}s (per-frame plot points + segment
     * lifecycle); the charts feature registers the consumer that applies them to its LineCharts. A single
     * registered handler rather than an observable list, so events are not retained — the chart holds the
     * state and is the only consumer. Both the setter and {@link #emitTrajectory} run on the FX thread.
     */
    private Consumer<TrajectoryEvent> trajectoryHandler = _ -> {
    };

    /**
     * Open-segment count mirror, read from the vo worker thread by {@code FxRenderSink.chartsCount()} to
     * tag each run's points (the core queries it synchronously before a run). Seeded to 1 (one open
     * segment, like the Swing chart), advanced by the charts feature when it actually closes a segment or
     * clears — an {@link AtomicInteger} so the cross-thread read needs no FX hop.
     */
    private final AtomicInteger segmentCount = new AtomicInteger(1);

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

    public ObjectProperty<Telemetry> telemetryProperty() {
        return telemetry;
    }

    public ObjectProperty<CurrentFps> currentFpsProperty() {
        return currentFps;
    }

    public ObjectProperty<BufferStatus> bufferProperty() {
        return buffer;
    }

    public ObservableList<TrackedPoint> trackedPoints() {
        return trackedPoints;
    }

    /** Registers the charts feature's consumer of the trajectory event stream (FX thread). */
    public void setTrajectoryHandler(Consumer<TrajectoryEvent> handler) {
        this.trajectoryHandler = handler;
    }

    /** Delivers one trajectory event to the registered handler (call on the FX thread). */
    public void emitTrajectory(TrajectoryEvent event) {
        this.trajectoryHandler.accept(event);
    }

    /** Open-segment count mirror (see {@link #segmentCount}); read cross-thread, advanced by the charts feature. */
    public AtomicInteger segmentCount() {
        return segmentCount;
    }
}
