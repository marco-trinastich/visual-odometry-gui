/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.settings.input;

import com.mtm.vogui.core.integration.discovery.DeviceDiscovery;
import com.mtm.vogui.models.context.settings.input.InputSettings;
import com.mtm.vogui.models.enums.settings.DevicePath;
import com.mtm.vogui.models.enums.settings.DeviceType;
import com.mtm.vogui.models.enums.settings.SourceType;
import com.mtm.vogui.models.enums.settings.resolution.CustomResolution;
import com.mtm.vogui.models.enums.settings.resolution.DeviceResolution;
import com.mtm.vogui.models.interfaces.Resolution;
import com.mtm.vogui.utilities.CommonUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

/**
 * ViewModel of the Input settings section (hand-rolled MVVM, twin of the settings-bearing part of the
 * Swing {@code InputSettingsView}). Exposes the persisted input choices — calibration/video paths,
 * the Video/Device source, the device driver type, the V4L4J device adjustments and the two preview
 * flags — plus the discovery-backed device path/resolution — as JavaFX properties, live-committing
 * each into the domain {@link InputSettings}. Device lists and advertised resolutions are queried
 * through {@link DeviceDiscovery} (never persisted); a saved device path is healed to what will
 * actually open, and a saved resolution to the nearest advertised one, mirroring the Swing view. The
 * {@code reflect*} methods apply values the core heals at capture time (published on the FX
 * {@code GuiState} by {@code FxRenderSink}).
 * <p>
 * Scope note: only the toolbar coupling ({@code setTimedEnabled} on the Device source) is still
 * deferred — it has no FX target yet.
 * The {@link #videoSourceSelected()} / {@link #deviceSourceSelected()} bindings drive which controls
 * are enabled. No Lombok (the {@code xxxProperty()} convention is hand-written).
 */
public class InputSettingsViewModel {

    // Re-resolved from the context on every load(): a settings load/reset swaps the sub-settings object
    // (AppContext.assignFrom replaces it), so a captured reference would go stale after a load.
    private final Supplier<InputSettings> settingsSupplier;
    private InputSettings settings;

    private final StringProperty calibrationPath = new SimpleStringProperty();
    private final ObservableList<String> calibrationPaths = FXCollections.observableArrayList();

    private final ObjectProperty<SourceType> source = new SimpleObjectProperty<>();

    private final StringProperty videoPath = new SimpleStringProperty();
    private final ObservableList<String> videoPaths = FXCollections.observableArrayList();

    private final ObjectProperty<DeviceType> deviceType = new SimpleObjectProperty<>();
    private final ObservableList<DeviceType> deviceTypes =
            FXCollections.observableArrayList(DeviceType.values());

    // Discovery-backed device selection (runtime, never persisted through these lists)
    private final ObjectProperty<DevicePath> devicePath = new SimpleObjectProperty<>();
    private final ObservableList<DevicePath> devicePaths = FXCollections.observableArrayList();
    private final ObjectProperty<Resolution> deviceResolution = new SimpleObjectProperty<>();
    private final ObservableList<Resolution> deviceResolutions = FXCollections.observableArrayList();

    // V4L4J device adjustments
    private final BooleanProperty sustainFramerate = new SimpleBooleanProperty();
    private final BooleanProperty timeoutImageIO = new SimpleBooleanProperty();
    private final BooleanProperty keepFormat = new SimpleBooleanProperty();

    // Preview flags
    private final BooleanProperty fullResolutionPreview = new SimpleBooleanProperty();
    private final BooleanProperty inputPreview = new SimpleBooleanProperty();

    private final BooleanBinding videoSourceSelected;
    private final BooleanBinding deviceSourceSelected;
    private final BooleanBinding v4l4jAdjustmentsVisible;

    public InputSettingsViewModel(Supplier<InputSettings> settingsSupplier) {
        this.settingsSupplier = settingsSupplier;
        load();

        videoSourceSelected = Bindings.createBooleanBinding(
                () -> SourceType.Video == source.get(), source);
        deviceSourceSelected = Bindings.createBooleanBinding(
                () -> SourceType.Device == source.get(), source);
        // Progressive disclosure: the adjustments are V4L4J-specific parameters, shown only when that
        // driver is actually the chosen device source (not merely disabled for the other drivers).
        v4l4jAdjustmentsVisible = Bindings.createBooleanBinding(
                () -> SourceType.Device == source.get() && DeviceType.V4L4J == deviceType.get(),
                source, deviceType);

        // Live commit into the domain (fluent accessors). Paths commit on typing/selection like the
        // rest; the recent-path history is only touched on browse/successful-use (a later sub-slice).
        calibrationPath.addListener((_, _, value) -> settings.calibration().path(value));
        source.addListener((_, _, value) -> settings.source(value));
        videoPath.addListener((_, _, value) -> settings.video().path(value));
        deviceType.addListener((_, _, value) -> {
            settings.device().type(value);
            // A driver swap advertises its own devices/resolutions: re-discover and repopulate.
            DeviceDiscovery.forType(value).reload();
            refreshDeviceCombos();
        });
        devicePath.addListener((_, _, value) -> {
            if (value != null) {
                settings.device().path(value);
                // A user-picked device advertises its own resolution set; skip during programmatic
                // repopulation (refreshDeviceCombos runs the query once itself).
                if (!populatingDevices) {
                    refreshDeviceResolutions();
                }
            }
        });
        deviceResolution.addListener((_, _, value) -> {
            if (value != null) {
                settings.device().resolution(value);
            }
        });
        sustainFramerate.addListener((_, _, value) -> settings.device().v4l4j().sustainFramerate(value));
        timeoutImageIO.addListener((_, _, value) -> settings.device().v4l4j().timeoutImageIO(value));
        keepFormat.addListener((_, _, value) -> settings.device().v4l4j().keepFormat(value));
        fullResolutionPreview.addListener((_, _, value) -> settings.fullResolutionPreview(value));
        inputPreview.addListener((_, _, value) -> settings.inputPreview(value));
    }

    // Recent-path history mutations (browse confirmation records a path; the drop-down "✕" removes one).
    // The current selection commits through the path properties like any other edit.

    public void pushCalibrationPath(String path) {
        settings.calibration().pushRecentPath(path);
        calibrationPaths.setAll(settings.calibration().paths());
        calibrationPath.set(path);
    }

    public void removeCalibrationPath(String path) {
        settings.calibration().removeRecentPath(path);
        calibrationPaths.setAll(settings.calibration().paths());
    }

    public void pushVideoPath(String path) {
        settings.video().pushRecentPath(path);
        videoPaths.setAll(settings.video().paths());
        videoPath.set(path);
    }

    public void removeVideoPath(String path) {
        settings.video().removeRecentPath(path);
        videoPaths.setAll(settings.video().paths());
    }

    // Core-healed values reflected back (published on GuiState by FxRenderSink at capture time). These
    // never re-query the hardware — the core already negotiated the values, so they are just applied.

    public void reflectDevicePath(DevicePath path) {
        if (path == null) {
            return;
        }
        if (!devicePaths.contains(path)) {
            devicePaths.add(path);
        }
        populatingDevices = true;
        devicePath.set(path);
        populatingDevices = false;
    }

    public void reflectDeviceResolution(Resolution resolution) {
        if (resolution == null) {
            return;
        }
        if (!deviceResolutions.contains(resolution)) {
            deviceResolutions.add(resolution);
        }
        deviceResolution.set(resolution);
    }

    public void reflectRecentCalibration(String usedPath) {
        calibrationPaths.setAll(settings.calibration().paths());
        calibrationPath.set(usedPath);
    }

    public void reflectRecentVideo(String usedPath) {
        videoPaths.setAll(settings.video().paths());
        videoPath.set(usedPath);
    }

    /** Re-reads every property from the domain (after a settings load/reset). */
    public void load() {
        // Re-resolve the current sub-settings: a load swaps this object under us.
        settings = settingsSupplier.get();
        calibrationPaths.setAll(settings.calibration().paths());
        calibrationPath.set(settings.calibration().path());
        source.set(settings.source());
        videoPaths.setAll(settings.video().paths());
        videoPath.set(settings.video().path());
        deviceType.set(settings.device().type());
        sustainFramerate.set(settings.device().v4l4j().sustainFramerate());
        timeoutImageIO.set(settings.device().v4l4j().timeoutImageIO());
        keepFormat.set(settings.device().v4l4j().keepFormat());
        fullResolutionPreview.set(settings.fullResolutionPreview());
        inputPreview.set(settings.inputPreview());
        // Populate the device path/resolution combos from discovery (heals the saved path + snaps the
        // saved resolution to the nearest advertised one).
        refreshDeviceCombos();
    }

    // Device discovery: runtime queries through DeviceDiscovery (device list, advertised resolutions),
    // never persisted through these lists. Mirrors the Swing InputSettingsView helpers.

    private boolean populatingDevices;

    private DeviceDiscovery discovery() {
        return DeviceDiscovery.forType(settings.device().type());
    }

    private void refreshDeviceCombos() {
        healDevicePath();
        // Programmatic repopulation: suppress the path->resolution re-query cascade, run it once below.
        populatingDevices = true;
        devicePaths.setAll(Arrays.asList(CommonUtils.getDevicePathDescriptors(discovery().listDevices())));
        devicePath.set(settings.device().path());
        populatingDevices = false;
        refreshDeviceResolutions();
    }

    /**
     * Heals the persisted device path to what will actually open, via the same {@code resolveDevice}
     * mapping the cameras use at capture time (startup and play can never disagree).
     */
    private void healDevicePath() {
        var device = settings.device();
        String saved = device.path().id().trim();
        String resolved = DeviceDiscovery.forType(device.type()).resolveDevice(saved);
        if (!resolved.isEmpty() && !resolved.equals(saved)) {
            device.path(CommonUtils.getDevicePathDescriptor(resolved));
        }
    }

    /**
     * Repopulates the resolution list from the device-advertised sizes, snapping the saved value to the
     * nearest advertised one. When the device cannot be queried the static standard list is kept and the
     * saved value preserved (capture-time adjustment remains the safety net).
     */
    private void refreshDeviceResolutions() {
        var device = settings.device();
        List<Dimension> sizes = discovery().listViewSizes(device.path().id().trim());
        if (sizes.isEmpty()) {
            deviceResolutions.setAll(DeviceResolution.values());
            deviceResolution.set(device.resolution());
            return;
        }
        List<Resolution> available = sizes.stream()
                .distinct()
                .sorted(Comparator.comparingLong(size -> (long) size.width * size.height))
                .map(size -> {
                    Resolution standard = DeviceResolution.findByResolution(size.width, size.height);
                    return standard != null ? standard : CustomResolution.from(size.width, size.height);
                })
                .toList();
        Resolution saved = device.resolution();
        Resolution nearest = available.stream()
                .min(Comparator.comparingLong(item ->
                        CommonUtils.getResolutionDistance(item.width(), item.height(), saved.width(), saved.height())))
                .orElse(available.getFirst());
        deviceResolutions.setAll(available);
        deviceResolution.set(nearest);
        device.resolution(nearest);
    }

    public StringProperty calibrationPathProperty() {
        return calibrationPath;
    }

    public ObservableList<String> calibrationPaths() {
        return calibrationPaths;
    }

    public ObjectProperty<SourceType> sourceProperty() {
        return source;
    }

    public StringProperty videoPathProperty() {
        return videoPath;
    }

    public ObservableList<String> videoPaths() {
        return videoPaths;
    }

    public ObjectProperty<DeviceType> deviceTypeProperty() {
        return deviceType;
    }

    public ObservableList<DeviceType> deviceTypes() {
        return deviceTypes;
    }

    public ObjectProperty<DevicePath> devicePathProperty() {
        return devicePath;
    }

    public ObservableList<DevicePath> devicePaths() {
        return devicePaths;
    }

    public ObjectProperty<Resolution> deviceResolutionProperty() {
        return deviceResolution;
    }

    public ObservableList<Resolution> deviceResolutions() {
        return deviceResolutions;
    }

    public BooleanProperty sustainFramerateProperty() {
        return sustainFramerate;
    }

    public BooleanProperty timeoutImageIOProperty() {
        return timeoutImageIO;
    }

    public BooleanProperty keepFormatProperty() {
        return keepFormat;
    }

    public BooleanProperty fullResolutionPreviewProperty() {
        return fullResolutionPreview;
    }

    public BooleanProperty inputPreviewProperty() {
        return inputPreview;
    }

    public BooleanBinding videoSourceSelected() {
        return videoSourceSelected;
    }

    public BooleanBinding deviceSourceSelected() {
        return deviceSourceSelected;
    }

    public BooleanBinding v4l4jAdjustmentsVisible() {
        return v4l4jAdjustmentsVisible;
    }
}
