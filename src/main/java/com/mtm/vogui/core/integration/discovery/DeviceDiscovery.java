/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.core.integration.discovery;

import com.mtm.vogui.models.enums.settings.DeviceType;
import com.mtm.vogui.utilities.CommonUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.Dimension;
import java.util.Comparator;
import java.util.List;

/**
 * Runtime discovery of capture devices and their capabilities.
 * <p/>
 * Settings stay pure persisted data: everything that must be asked to the
 * hardware (device list, advertised resolutions) goes through this layer.
 * Implementations must fail soft (empty results) when the driver or its
 * natives are unavailable on the current platform.
 */
public interface DeviceDiscovery {

    /** Identifiers of the available devices (webcam names, /dev nodes, ...) */
    String[] listDevices();

    /** Resolutions advertised by a device; empty when it cannot be queried */
    List<Dimension> listViewSizes(String devicePath);

    /**
     * The device identifier to actually open for the requested one: the request itself
     * when it matches an available device (or cannot be verified on this platform),
     * otherwise the first available one. Single source of truth for the
     * requested-to-actual mapping: the GUI heal at startup and the cameras at capture
     * time must both go through here, so they can never disagree.
     */
    String resolveDevice(String requestedPath);

    /**
     * The advertised resolution closest to the target, or the target itself when the
     * device cannot be queried (driver-level capture adjustment remains the safety net).
     */
    default Dimension nearestViewSize(String devicePath, Dimension target) {
        return this.listViewSizes(devicePath).stream()
                .min(Comparator.comparingLong(size ->
                        CommonUtils.getResolutionDistance(size.width, size.height, target.width, target.height)))
                .orElse(target);
    }

    /** Invalidates any cached device list; next access re-discovers */
    default void reload() {
    }

    default String firstDeviceOrEmpty() {
        String[] devices = this.listDevices();
        return devices.length > 0 ? devices[0] : "";
    }

    static DeviceDiscovery forType(@NotNull DeviceType type) {
        return switch (type) {
            case BoofCv -> BoofCvDeviceDiscovery.instance();
            case V4L4J -> V4l4jDeviceDiscovery.instance();
        };
    }
}
