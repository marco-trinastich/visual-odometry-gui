/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.settings.core.image;

import java.io.Serializable;

import com.mtm.vogui.models.enums.settings.ImageTypeDescriptor;
import com.mtm.vogui.models.enums.settings.resolution.CustomResolution;
import com.mtm.vogui.models.enums.settings.resolution.ResizeResolution;
import com.mtm.vogui.models.interfaces.Resolution;
import com.mtm.vogui.models.interfaces.WithDefault;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

/**
 * Image settings
 * <p/>
 * Options for the images provided to the algorithm.
 */
@Data
@Dependent
public class ImageSettings implements Serializable, WithDefault<ImageSettings> {

    private ImageTypeDescriptor descriptor;
    private boolean resize;
    private int resizeWidth;
    private int resizeHeight;
    private boolean frameSkipEnabled;
    private int frameSkipValue;
    private boolean internalImagePreview;

    @Inject
    public ImageSettings() {
        this.loadDefaults();
    }

    public ImageSettings(@NotNull ImageSettings image) {
        this.descriptor = image.descriptor;
        this.resize = image.resize;
        this.resizeWidth = image.resizeWidth;
        this.resizeHeight = image.resizeHeight;
        this.frameSkipEnabled = image.frameSkipEnabled;
        this.frameSkipValue = image.frameSkipValue;
        this.internalImagePreview = image.internalImagePreview;
    }

    public Resolution resolution() {
        ResizeResolution resizeResolution =
                ResizeResolution.findByResolution(this.resizeWidth, this.resizeHeight);
        return resizeResolution != null ?
                resizeResolution :
                CustomResolution.from(this.resizeWidth, this.resizeHeight);
    }

    public void resolution(@NotNull Resolution resolution) {
        this.resizeWidth = resolution.width();
        this.resizeHeight = resolution.height();
    }

    public void loadDefaults() {
        this.descriptor = ImageTypeDescriptor.GrayF32;
        this.resize = false;
        this.resolution(ResizeResolution.R_400);
        this.frameSkipEnabled = false;
        this.frameSkipValue = 1;
        this.internalImagePreview = false;
    }
}
