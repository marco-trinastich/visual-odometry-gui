/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.sidebar.settings.visualodometry;

import com.mtm.vogui.models.context.settings.visualodometry.VisualOdometrySettings;
import com.mtm.vogui.models.context.settings.visualodometry.monoplaneinfinity.MonoPlaneInfinitySettings;
import com.mtm.vogui.models.context.settings.visualodometry.monoplaneoverhead.MonoPlaneOverheadSettings;
import com.mtm.vogui.models.enums.settings.VisualOdometryType;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.function.Supplier;

/**
 * ViewModel of the Visual Odometry settings section (hand-rolled MVVM, twin of the Swing
 * {@code VoSettingsView} + {@code MonoPlaneInfinityView} + {@code MonoPlaneOverheadView}). Exposes
 * the VO type plus the two mono-plane parameter variants as JavaFX properties, live-committing each
 * into the domain {@link VisualOdometrySettings}. Derived bindings ({@link #monoInfinityVisible()},
 * {@link #monoOverheadVisible()}, {@link #fallbackVisible()}, {@link #monoInfinityDisabled()}) drive
 * which variant the view shows and whether the infinity group is greyed out (VO type {@code Default}).
 * The remaining not-implemented stereo/depth types show the fallback placeholder.
 * No Lombok (the {@code xxxProperty()} convention is hand-written).
 */
public class VoSettingsViewModel {

    // Re-resolved on every load(): a settings load swaps the sub-settings object (see AppContext).
    private final Supplier<VisualOdometrySettings> settingsSupplier;
    private VisualOdometrySettings settings;

    private final ObjectProperty<VisualOdometryType> type = new SimpleObjectProperty<>();

    // Mono-plane-infinity parameters
    private final IntegerProperty infThresholdAdd = new SimpleIntegerProperty();
    private final IntegerProperty infThresholdRetire = new SimpleIntegerProperty();
    private final DoubleProperty infInlierPixelTol = new SimpleDoubleProperty();
    private final IntegerProperty infRansacIterations = new SimpleIntegerProperty();

    // Mono-plane-overhead parameters
    private final DoubleProperty ovhCellSize = new SimpleDoubleProperty();
    private final DoubleProperty ovhMaxCellsPerPixel = new SimpleDoubleProperty();
    private final DoubleProperty ovhMapHeightFraction = new SimpleDoubleProperty();
    private final DoubleProperty ovhInlierGroundTol = new SimpleDoubleProperty();
    private final IntegerProperty ovhRansacIterations = new SimpleIntegerProperty();
    private final IntegerProperty ovhThresholdRetire = new SimpleIntegerProperty();
    private final IntegerProperty ovhAbsoluteMinimumTracks = new SimpleIntegerProperty();
    private final DoubleProperty ovhRespawnTrackFraction = new SimpleDoubleProperty();
    private final DoubleProperty ovhRespawnCoverageFraction = new SimpleDoubleProperty();

    // Only the implemented types are offered (filtered centrally on the enum, shared with Swing).
    private final ObservableList<VisualOdometryType> visualOdometryTypes =
            FXCollections.observableArrayList(VisualOdometryType.enabledValues());

    private final BooleanBinding monoInfinityVisible;
    private final BooleanBinding monoOverheadVisible;
    private final BooleanBinding fallbackVisible;
    private final BooleanBinding monoInfinityDisabled;

    public VoSettingsViewModel(Supplier<VisualOdometrySettings> settingsSupplier) {
        this.settingsSupplier = settingsSupplier;
        load();

        // MonoPlaneInfinity params also back the Default type (standard parameters, greyed out).
        monoInfinityVisible = Bindings.createBooleanBinding(
                () -> type.get() == VisualOdometryType.MonoPlaneInfinity
                        || type.get() == VisualOdometryType.Default, type);
        monoOverheadVisible = Bindings.createBooleanBinding(
                () -> type.get() == VisualOdometryType.MonoPlaneOverhead, type);
        // Everything else (the not-implemented stereo/depth types) falls back to the placeholder.
        fallbackVisible = Bindings.createBooleanBinding(
                () -> type.get() != null && !type.get().isMono(), type);
        monoInfinityDisabled = Bindings.createBooleanBinding(
                () -> VisualOdometryType.Default == type.get(), type);

        // Live commit into the domain (fluent accessors on the two mono-plane sub-settings).
        type.addListener((_, _, value) -> settings.type(value));
        infThresholdAdd.addListener((_, _, value) -> settings.monoPlaneInfinity().thresholdAdd(value.intValue()));
        infThresholdRetire.addListener((_, _, value) -> settings.monoPlaneInfinity().thresholdRetire(value.intValue()));
        infInlierPixelTol.addListener((_, _, value) -> settings.monoPlaneInfinity().inlierPixelTol(value.doubleValue()));
        infRansacIterations.addListener((_, _, value) -> settings.monoPlaneInfinity().ransacIterations(value.intValue()));
        ovhCellSize.addListener((_, _, value) -> settings.monoPlaneOverhead().cellSize(value.doubleValue()));
        ovhMaxCellsPerPixel.addListener((_, _, value) -> settings.monoPlaneOverhead().maxCellsPerPixel(value.doubleValue()));
        ovhMapHeightFraction.addListener((_, _, value) -> settings.monoPlaneOverhead().mapHeightFraction(value.doubleValue()));
        ovhInlierGroundTol.addListener((_, _, value) -> settings.monoPlaneOverhead().inlierGroundTol(value.doubleValue()));
        ovhRansacIterations.addListener((_, _, value) -> settings.monoPlaneOverhead().ransacIterations(value.intValue()));
        ovhThresholdRetire.addListener((_, _, value) -> settings.monoPlaneOverhead().thresholdRetire(value.intValue()));
        ovhAbsoluteMinimumTracks.addListener((_, _, value) -> settings.monoPlaneOverhead().absoluteMinimumTracks(value.intValue()));
        ovhRespawnTrackFraction.addListener((_, _, value) -> settings.monoPlaneOverhead().respawnTrackFraction(value.doubleValue()));
        ovhRespawnCoverageFraction.addListener((_, _, value) -> settings.monoPlaneOverhead().respawnCoverageFraction(value.doubleValue()));
    }

    /** Re-reads every property from the domain (after a settings load/reset). */
    public void load() {
        settings = settingsSupplier.get();
        MonoPlaneInfinitySettings inf = settings.monoPlaneInfinity();
        MonoPlaneOverheadSettings ovh = settings.monoPlaneOverhead();
        type.set(settings.type());
        infThresholdAdd.set(inf.thresholdAdd());
        infThresholdRetire.set(inf.thresholdRetire());
        infInlierPixelTol.set(inf.inlierPixelTol());
        infRansacIterations.set(inf.ransacIterations());
        ovhCellSize.set(ovh.cellSize());
        ovhMaxCellsPerPixel.set(ovh.maxCellsPerPixel());
        ovhMapHeightFraction.set(ovh.mapHeightFraction());
        ovhInlierGroundTol.set(ovh.inlierGroundTol());
        ovhRansacIterations.set(ovh.ransacIterations());
        ovhThresholdRetire.set(ovh.thresholdRetire());
        ovhAbsoluteMinimumTracks.set(ovh.absoluteMinimumTracks());
        ovhRespawnTrackFraction.set(ovh.respawnTrackFraction());
        ovhRespawnCoverageFraction.set(ovh.respawnCoverageFraction());
    }

    public ObjectProperty<VisualOdometryType> typeProperty() {
        return type;
    }

    public IntegerProperty infThresholdAddProperty() {
        return infThresholdAdd;
    }

    public IntegerProperty infThresholdRetireProperty() {
        return infThresholdRetire;
    }

    public DoubleProperty infInlierPixelTolProperty() {
        return infInlierPixelTol;
    }

    public IntegerProperty infRansacIterationsProperty() {
        return infRansacIterations;
    }

    public DoubleProperty ovhCellSizeProperty() {
        return ovhCellSize;
    }

    public DoubleProperty ovhMaxCellsPerPixelProperty() {
        return ovhMaxCellsPerPixel;
    }

    public DoubleProperty ovhMapHeightFractionProperty() {
        return ovhMapHeightFraction;
    }

    public DoubleProperty ovhInlierGroundTolProperty() {
        return ovhInlierGroundTol;
    }

    public IntegerProperty ovhRansacIterationsProperty() {
        return ovhRansacIterations;
    }

    public IntegerProperty ovhThresholdRetireProperty() {
        return ovhThresholdRetire;
    }

    public IntegerProperty ovhAbsoluteMinimumTracksProperty() {
        return ovhAbsoluteMinimumTracks;
    }

    public DoubleProperty ovhRespawnTrackFractionProperty() {
        return ovhRespawnTrackFraction;
    }

    public DoubleProperty ovhRespawnCoverageFractionProperty() {
        return ovhRespawnCoverageFraction;
    }

    public ObservableList<VisualOdometryType> visualOdometryTypes() {
        return visualOdometryTypes;
    }

    public BooleanBinding monoInfinityVisible() {
        return monoInfinityVisible;
    }

    public BooleanBinding monoOverheadVisible() {
        return monoOverheadVisible;
    }

    public BooleanBinding fallbackVisible() {
        return fallbackVisible;
    }

    public BooleanBinding monoInfinityDisabled() {
        return monoInfinityDisabled;
    }
}
