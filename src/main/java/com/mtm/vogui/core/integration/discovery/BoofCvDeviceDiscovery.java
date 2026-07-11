/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.core.integration.discovery;

import com.github.sarxos.webcam.Webcam;
import com.mtm.vogui.core.integration.bridge.BoofCvVideo;
import io.quarkus.logging.Log;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Sarxos-based webcam discovery (BoofCv capture path).
 */
public final class BoofCvDeviceDiscovery implements DeviceDiscovery {

    private static final BoofCvDeviceDiscovery INSTANCE = new BoofCvDeviceDiscovery();

    private List<Webcam> webcams;

    private BoofCvDeviceDiscovery() {
    }

    public static BoofCvDeviceDiscovery instance() {
        return INSTANCE;
    }

    @Override
    public String[] listDevices() {
        return this.availableWebcams().stream().map(Webcam::getName).toArray(String[]::new);
    }

    @Override
    public List<Dimension> listViewSizes(String devicePath) {
        try {
            Webcam webcam = this.webcam(devicePath);
            return webcam != null ? Arrays.asList(webcam.getViewSizes()) : List.of();
        } catch (Throwable exc) {
            Log.warnf("BoofCv view sizes discovery unavailable: %s", exc.getMessage());
            return List.of();
        }
    }

    @Override
    public synchronized void reload() {
        try {
            // Going through the bridge also guarantees the native driver is active
            this.webcams = BoofCvVideo.webcams();
        } catch (Throwable exc) {
            Log.warnf("BoofCv webcam discovery unavailable: %s", exc.getMessage());
            this.webcams = List.of();
        }
    }

    @Override
    public String resolveDevice(String requestedPath) {
        String requested = requestedPath == null ? "" : requestedPath.trim();
        List<Webcam> available = this.availableWebcams();
        // Webcam identifiers are display names: a persisted partial name matches by contains
        return available.stream()
                .filter(Objects::nonNull)
                .map(Webcam::getName)
                .filter(name -> name.contains(requested))
                .findFirst()
                .orElse(!available.isEmpty() ? available.getFirst().getName() : requested);
    }

    /**
     * The webcam the given name resolves to ({@link #resolveDevice}), otherwise null
     */
    public Webcam webcam(String devicePath) {
        String resolved = this.resolveDevice(devicePath);
        return this.availableWebcams().stream()
                .filter(Objects::nonNull)
                .filter(webcam -> webcam.getName().equals(resolved))
                .findFirst()
                .orElse(null);
    }

    private synchronized List<Webcam> availableWebcams() {
        if (this.webcams == null) {
            this.reload();
        }
        return this.webcams;
    }
}
