/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.core.integration.discovery;

import com.mtm.vogui.core.integration.bridge.V4l4jVideo;
import com.mtm.vogui.utilities.OSUtils;
import io.quarkus.logging.Log;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Arrays;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * V4L device discovery: nodes come from scanning {@code /dev/video*},
 * resolutions from the V4L4J native bridge (Linux only, fails soft elsewhere).
 */
public final class V4l4jDeviceDiscovery implements DeviceDiscovery {

    private static final V4l4jDeviceDiscovery INSTANCE = new V4l4jDeviceDiscovery();

    private static final Path DEVICES_DIRECTORY = Path.of("/dev");
    private static final Pattern DEVICE_NODE_PATTERN = Pattern.compile("/dev/video(\\d+)");

    // Common V4L device nodes, used as fallback when discovery finds none
    private static final String[] DEFAULT_DEVICE_NODES = {
            "/dev/video0",
            "/dev/video1"
    };

    private V4l4jDeviceDiscovery() {
    }

    public static V4l4jDeviceDiscovery instance() {
        return INSTANCE;
    }

    @Override
    public String[] listDevices() {
        try (Stream<Path> deviceNodes = Files.list(DEVICES_DIRECTORY)) {
            String[] discovered = deviceNodes
                    .map(Path::toString)
                    .filter(node -> DEVICE_NODE_PATTERN.matcher(node).matches())
                    .sorted(Comparator.comparingInt(V4l4jDeviceDiscovery::deviceNodeNumber))
                    .toArray(String[]::new);
            return discovered.length > 0 ? discovered : DEFAULT_DEVICE_NODES.clone();
        } catch (IOException exc) {
            return DEFAULT_DEVICE_NODES.clone();
        }
    }

    @Override
    public List<Dimension> listViewSizes(String devicePath) {
        try {
            return V4l4jVideo.listViewSizes(devicePath);
        } catch (Throwable exc) {
            // Missing natives surface as Errors at class-load time (e.g. on macOS)
            Log.warnf("V4L4J view sizes discovery unavailable: %s", exc.getMessage());
            return List.of();
        }
    }

    @Override
    public String resolveDevice(String requestedPath) {
        String requested = requestedPath == null ? "" : requestedPath.trim();
        if (!requested.isEmpty() && !OSUtils.isUnix()) {
            // Off-Linux the device list is just the static fallback: a non-empty node not
            // in it (e.g. /dev/video30 saved on a Linux box) may still be perfectly valid
            return requested;
        }
        // Device nodes are exact filesystem paths: equals, not contains
        String[] devices = this.listDevices();
        if (Arrays.stream(devices).anyMatch(requested::equals)) {
            return requested;
        }
        return devices.length > 0 ? devices[0] : requested;
    }

    private static int deviceNodeNumber(String deviceNode) {
        var matcher = DEVICE_NODE_PATTERN.matcher(deviceNode);
        return matcher.matches() ? Integer.parseInt(matcher.group(1)) : Integer.MAX_VALUE;
    }
}
