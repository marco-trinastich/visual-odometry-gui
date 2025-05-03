/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.settings.core.input.device;

import com.mtm.vogui.models.enums.settings.*;
import com.mtm.vogui.models.enums.settings.resolution.DeviceResolution;
import com.mtm.vogui.models.enums.settings.resolution.CustomResolution;
import com.mtm.vogui.models.interfaces.Resolution;
import com.mtm.vogui.models.interfaces.WithDefault;
import com.mtm.vogui.models.settings.core.input.device.boofcv.BoofCvCameraSettings;
import com.mtm.vogui.models.settings.core.input.device.v4l4j.V4l4jCameraSettings;
import com.mtm.vogui.utilities.CommonUtils;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Device settings
 * <p/>
 * Options related to input device.
 */
@Data
@Dependent
public class DeviceSettings implements Serializable, WithDefault<DeviceSettings> {

    // Device
    private DeviceType type;
    private int targetWidth;
    private int targetHeight;
    private BoofCvCameraSettings boofCv;
    private V4l4jCameraSettings v4l4j;

    @Inject
    public DeviceSettings(BoofCvCameraSettings boofCv, V4l4jCameraSettings v4l4j) {
        this.boofCv = boofCv;
        this.v4l4j = v4l4j;

        this.loadDefaults();
    }

    public DeviceSettings(@NotNull DeviceSettings device) {
        this.type = device.type != null ? device.type : defaultDeviceType();
        this.targetWidth = device.targetWidth >= 0 ? device.targetWidth : defaultDeviceResolution().width();
        this.targetHeight = device.targetHeight >= 0 ? device.targetHeight : defaultDeviceResolution().height();
        this.boofCv = new BoofCvCameraSettings(device.boofCv);
        this.v4l4j = new V4l4jCameraSettings(device.v4l4j);
    }

    public Resolution resolution() {
        DeviceResolution deviceResolution =
                DeviceResolution.findByResolution(this.targetWidth, this.targetHeight);
        return deviceResolution != null ?
                deviceResolution :
                CustomResolution.from(this.targetWidth, this.targetHeight);
    }

    public void resolution(@NotNull Resolution resolution) {
        this.targetWidth = resolution.width();
        this.targetHeight = resolution.height();
    }

    public DevicePath path() {
        return CommonUtils.getDevicePathDescriptor(switch (this.type) {
            case BoofCv -> this.boofCv.path();
            case V4L4J -> this.v4l4j.path();
        });
    }

    public void path(@NotNull DevicePath path) {
        switch (this.type) {
            case BoofCv -> this.boofCv.path(path.id());
            case V4L4J -> this.v4l4j.path(path.id());
        }
    }

    public DevicePath[] paths() {
        return CommonUtils.getDevicePathDescriptors(switch (this.type) {
            case BoofCv -> this.boofCv.paths();
            case V4L4J -> this.v4l4j.paths();
        });
    }

    public void reloadPaths() {
        if (DeviceType.BoofCv.is(this.type)) {
            this.boofCv.reloadWebcams();
        }
    }

    public void loadDefaults() {
        this.type = defaultDeviceType();
        this.resolution(defaultDeviceResolution());
        this.boofCv.loadDefaults();
        this.v4l4j.loadDefaults();
    }

    private static DeviceType defaultDeviceType() {
        return DeviceType.BoofCv;
    }

    private static DeviceResolution defaultDeviceResolution() {
        return DeviceResolution.QVGA;
    }
}
