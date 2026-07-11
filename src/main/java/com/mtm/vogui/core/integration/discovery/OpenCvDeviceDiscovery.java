/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.core.integration.discovery;

import java.awt.Dimension;
import java.util.List;
import java.util.regex.Pattern;

/**
 * OpenCv (JavaCV) device discovery: OpenCV identifies capture devices by numeric
 * index and offers no enumeration API, so the device list is a static set of
 * common indices and {@code resolveDevice} keeps any explicitly requested index
 * beyond it. An index with no device behind it fails loud and clean at capture
 * start ("out device of bound"). Resolutions cannot be queried without opening
 * the device: callers fall back to the static standard list, and the size
 * actually granted by the driver is read back from the first captured frame.
 * <p/>
 * Deliberately pure: piggybacking on the sibling discoveries (native webcam
 * enumeration, {@code /dev/video*} scan) to map counts/names onto indices was
 * considered and rejected — nothing guarantees their ordering matches the OpenCV
 * backend's, and showing name X while opening device Y would fail silently.
 */
public final class OpenCvDeviceDiscovery implements DeviceDiscovery {

    private static final OpenCvDeviceDiscovery INSTANCE = new OpenCvDeviceDiscovery();

    private static final Pattern DEVICE_INDEX_PATTERN = Pattern.compile("\\d+");

    // Common capture indices, used as the whole list since OpenCV cannot enumerate
    private static final String[] DEFAULT_DEVICE_INDICES = {"0", "1", "2"};

    private OpenCvDeviceDiscovery() {
    }

    public static OpenCvDeviceDiscovery instance() {
        return INSTANCE;
    }

    @Override
    public String[] listDevices() {
        return DEFAULT_DEVICE_INDICES.clone();
    }

    @Override
    public List<Dimension> listViewSizes(String devicePath) {
        return List.of();
    }

    @Override
    public String resolveDevice(String requestedPath) {
        String requested = requestedPath == null ? "" : requestedPath.trim();
        // Any numeric index is potentially valid (e.g. a fourth camera), even beyond
        // the static list; anything else falls back to the first index
        if (DEVICE_INDEX_PATTERN.matcher(requested).matches()) {
            return requested;
        }
        return DEFAULT_DEVICE_INDICES[0];
    }
}
