/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.sidebar.settings.tracker;

import com.mtm.vogui.models.context.settings.tracker.TrackerSettings;
import com.mtm.vogui.models.context.settings.tracker.klt.KltSettings;
import com.mtm.vogui.models.context.settings.tracker.surf.SurfSettings;
import com.mtm.vogui.models.enums.settings.TrackerType;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.function.Supplier;

/**
 * ViewModel of the Tracker settings section (hand-rolled MVVM, twin of the Swing
 * {@code TrackerSettingsView} + {@code KltTrackerView} + {@code SurfTrackerView}). Exposes the
 * tracker type, the KLT and SURF parameter groups, and the track-display flags as JavaFX
 * properties, live-committing each into the domain {@link TrackerSettings}. Derived bindings
 * ({@link #kltVisible()}, {@link #surfVisible()}, {@link #kltDisabled()}) drive which parameter
 * group the view shows and whether KLT is greyed out (tracker type {@code Default}).
 * No Lombok (the {@code xxxProperty()} convention is hand-written).
 */
public class TrackerSettingsViewModel {

    // Re-resolved on every load(): a settings load swaps the sub-settings object (see AppContext).
    private final Supplier<TrackerSettings> settingsSupplier;
    private TrackerSettings settings;

    private final ObjectProperty<TrackerType> type = new SimpleObjectProperty<>();

    // KLT parameters
    private final IntegerProperty templateRadius = new SimpleIntegerProperty();
    private final IntegerProperty pyramidLevels = new SimpleIntegerProperty();
    private final IntegerProperty maxFeatures = new SimpleIntegerProperty();
    private final IntegerProperty radius = new SimpleIntegerProperty();
    private final DoubleProperty threshold = new SimpleDoubleProperty();

    // SURF parameters
    private final IntegerProperty maxFeaturesPerScale = new SimpleIntegerProperty();
    private final IntegerProperty extractRadius = new SimpleIntegerProperty();
    private final IntegerProperty initialSampleSize = new SimpleIntegerProperty();

    // Track display
    private final BooleanProperty showActiveTracks = new SimpleBooleanProperty();
    private final BooleanProperty showNewTracks = new SimpleBooleanProperty();

    // Only the implemented types are offered (filtered centrally on the enum, shared with Swing).
    private final ObservableList<TrackerType> trackerTypes =
            FXCollections.observableArrayList(TrackerType.enabledValues());

    private final BooleanBinding kltVisible;
    private final BooleanBinding surfVisible;
    private final BooleanBinding kltDisabled;

    public TrackerSettingsViewModel(Supplier<TrackerSettings> settingsSupplier) {
        this.settingsSupplier = settingsSupplier;
        load();

        kltVisible = Bindings.createBooleanBinding(
                () -> type.get() != null && type.get().isKlt(), type);
        surfVisible = Bindings.createBooleanBinding(
                () -> type.get() != null && type.get().isSurf(), type);
        kltDisabled = Bindings.createBooleanBinding(
                () -> TrackerType.Default == type.get(), type);

        // Live commit into the domain (fluent accessors on the tracker + its klt/surf sub-settings).
        type.addListener((_, _, value) -> settings.type(value));
        templateRadius.addListener((_, _, value) -> settings.klt().templateRadius(value.intValue()));
        pyramidLevels.addListener((_, _, value) -> settings.klt().pyramidLevels(value.intValue()));
        maxFeatures.addListener((_, _, value) -> settings.klt().maxFeatures(value.intValue()));
        radius.addListener((_, _, value) -> settings.klt().radius(value.intValue()));
        threshold.addListener((_, _, value) -> settings.klt().threshold(value.floatValue()));
        maxFeaturesPerScale.addListener((_, _, value) -> settings.surf().maxFeaturesPerScale(value.intValue()));
        extractRadius.addListener((_, _, value) -> settings.surf().extractRadius(value.intValue()));
        initialSampleSize.addListener((_, _, value) -> settings.surf().initialSampleSize(value.intValue()));
        showActiveTracks.addListener((_, _, value) -> settings.showActiveTracks(value));
        showNewTracks.addListener((_, _, value) -> settings.showNewTracks(value));
    }

    /** Re-reads every property from the domain (after a settings load/reset). */
    public void load() {
        settings = settingsSupplier.get();
        KltSettings klt = settings.klt();
        SurfSettings surf = settings.surf();
        type.set(settings.type());
        templateRadius.set(klt.templateRadius());
        pyramidLevels.set(klt.pyramidLevels());
        maxFeatures.set(klt.maxFeatures());
        radius.set(klt.radius());
        threshold.set(klt.threshold());
        maxFeaturesPerScale.set(surf.maxFeaturesPerScale());
        extractRadius.set(surf.extractRadius());
        initialSampleSize.set(surf.initialSampleSize());
        showActiveTracks.set(settings.showActiveTracks());
        showNewTracks.set(settings.showNewTracks());
    }

    public ObjectProperty<TrackerType> typeProperty() {
        return type;
    }

    public IntegerProperty templateRadiusProperty() {
        return templateRadius;
    }

    public IntegerProperty pyramidLevelsProperty() {
        return pyramidLevels;
    }

    public IntegerProperty maxFeaturesProperty() {
        return maxFeatures;
    }

    public IntegerProperty radiusProperty() {
        return radius;
    }

    public DoubleProperty thresholdProperty() {
        return threshold;
    }

    public IntegerProperty maxFeaturesPerScaleProperty() {
        return maxFeaturesPerScale;
    }

    public IntegerProperty extractRadiusProperty() {
        return extractRadius;
    }

    public IntegerProperty initialSampleSizeProperty() {
        return initialSampleSize;
    }

    public BooleanProperty showActiveTracksProperty() {
        return showActiveTracks;
    }

    public BooleanProperty showNewTracksProperty() {
        return showNewTracks;
    }

    public ObservableList<TrackerType> trackerTypes() {
        return trackerTypes;
    }

    public BooleanBinding kltVisible() {
        return kltVisible;
    }

    public BooleanBinding surfVisible() {
        return surfVisible;
    }

    public BooleanBinding kltDisabled() {
        return kltDisabled;
    }
}
