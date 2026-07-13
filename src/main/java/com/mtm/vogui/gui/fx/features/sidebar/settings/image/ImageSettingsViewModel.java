/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.sidebar.settings.image;

import com.mtm.vogui.models.context.settings.image.ImageSettings;
import com.mtm.vogui.models.enums.settings.ImageTypeDescriptor;
import com.mtm.vogui.models.enums.settings.resolution.ResizeResolution;
import com.mtm.vogui.models.interfaces.Resolution;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.function.Supplier;

/**
 * ViewModel of the Image settings section (JabRef-style hand-rolled MVVM). Exposes the section as
 * JavaFX properties the view binds to, and commits every change straight back into the domain
 * {@link ImageSettings} — matching the Swing behaviour where widgets mutate settings live and only
 * "Save Settings" persists to disk. {@link #load()} re-syncs the properties from the domain after a
 * file load/reset. Lombok is deliberately absent: the {@code xxxProperty()} convention bindings rely
 * on is hand-written.
 */
public class ImageSettingsViewModel {

    // Re-resolved on every load(): a settings load swaps the sub-settings object (see AppContext).
    private final Supplier<ImageSettings> settingsSupplier;
    private ImageSettings settings;

    private final ObjectProperty<ImageTypeDescriptor> descriptor = new SimpleObjectProperty<>();
    private final BooleanProperty resize = new SimpleBooleanProperty();
    private final ObjectProperty<Resolution> resolution = new SimpleObjectProperty<>();
    private final BooleanProperty internalImagePreview = new SimpleBooleanProperty();
    private final BooleanProperty frameSkipEnabled = new SimpleBooleanProperty();
    private final IntegerProperty frameSkipValue = new SimpleIntegerProperty();

    private final ObservableList<ImageTypeDescriptor> imageTypes =
            FXCollections.observableArrayList(ImageTypeDescriptor.values());
    private final ObservableList<Resolution> resizeResolutions =
            FXCollections.observableArrayList(ResizeResolution.values());

    public ImageSettingsViewModel(Supplier<ImageSettings> settingsSupplier) {
        this.settingsSupplier = settingsSupplier;
        load();

        // Live commit into the domain (fluent accessors, not bean setters).
        descriptor.addListener((_, _, value) -> settings.descriptor(value));
        resize.addListener((_, _, value) -> settings.resize(value));
        resolution.addListener((_, _, value) -> {
            if (value != null) {
                settings.resolution(value);
            }
        });
        internalImagePreview.addListener((_, _, value) -> settings.internalImagePreview(value));
        frameSkipEnabled.addListener((_, _, value) -> settings.frameSkipEnabled(value));
        frameSkipValue.addListener((_, _, value) -> settings.frameSkipValue(value.intValue()));
    }

    /** Re-reads every property from the domain (after a settings load/reset). */
    public void load() {
        settings = settingsSupplier.get();
        descriptor.set(settings.descriptor());
        resize.set(settings.resize());
        resolution.set(settings.resolution());
        internalImagePreview.set(settings.internalImagePreview());
        frameSkipEnabled.set(settings.frameSkipEnabled());
        frameSkipValue.set(settings.frameSkipValue());
    }

    public ObjectProperty<ImageTypeDescriptor> descriptorProperty() {
        return descriptor;
    }

    public BooleanProperty resizeProperty() {
        return resize;
    }

    public ObjectProperty<Resolution> resolutionProperty() {
        return resolution;
    }

    public BooleanProperty internalImagePreviewProperty() {
        return internalImagePreview;
    }

    public BooleanProperty frameSkipEnabledProperty() {
        return frameSkipEnabled;
    }

    public IntegerProperty frameSkipValueProperty() {
        return frameSkipValue;
    }

    public ObservableList<ImageTypeDescriptor> imageTypes() {
        return imageTypes;
    }

    public ObservableList<Resolution> resizeResolutions() {
        return resizeResolutions;
    }
}
