/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.settings.core.input.device.boofcv;

import com.github.sarxos.webcam.Webcam;
import com.mtm.vogui.models.interfaces.WithDefault;
import com.mtm.vogui.utilities.WebcamDriverUtils;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * BoofCv camera settings
 * <p/>
 * Options related to BoofCv device.
 */
@Data
@Dependent
public class BoofCvCameraSettings implements Serializable, WithDefault<BoofCvCameraSettings> {

    static {
        // This class is the single webcam discovery entry point, so the driver is guaranteed
        // to be active before any sarxos Webcam API call
        WebcamDriverUtils.useNativeDriver();
    }

    private String path;

    // Runtime-discovered, never persisted (neither xml nor dat)
    @XStreamOmitField
    private transient List<Webcam> webcams;

    @Inject
    public BoofCvCameraSettings() {
        this.loadDefaults();
    }

    public BoofCvCameraSettings(@NotNull BoofCvCameraSettings boofCv) {
        this.webcams = this.reloadWebcams(boofCv.webcams);
        this.path = boofCv.path != null && !boofCv.path.isEmpty() ? boofCv.path : this.getFirstOrEmptyPath();
    }

    public String @NotNull [] paths() {
        return this.availableWebcams().stream().map(Webcam::getName).toArray(String[]::new);
    }

    public String getFirstOrEmptyPath() {
        return !this.availableWebcams().isEmpty() ? this.availableWebcams().getFirst().getName() : "";
    }

    public Webcam webcam() {
        // Returns selected webcam by path id if it exists, otherwise the first available one, otherwise null
        return this.availableWebcams().stream()
                .filter(Objects::nonNull)
                .filter(x -> x.getName().contains(this.path))
                .findFirst()
                .orElse(!this.availableWebcams().isEmpty() ? this.availableWebcams().getFirst() : null);
    }

    private List<Webcam> availableWebcams() {
        // The field is transient/omitted, so it is null on deserialized instances: discover lazily
        return this.webcams != null ? this.webcams : this.reloadWebcams(null);
    }

    public void reloadWebcams() {
        this.reloadWebcams(null);
    }

    public List<Webcam> reloadWebcams(List<Webcam> webcams) {
        if (webcams != null) {
            this.webcams = webcams;
            return this.webcams;
        }
        try {
            this.webcams = Webcam.getWebcams();
        } catch (Throwable exc) {
            System.err.println("BoofCv webcam discovery unavailable: " + exc.getMessage());
            this.webcams = new ArrayList<>();
        }

        // An empty path may have been persisted while discovery was unavailable: heal it
        if (this.path == null || this.path.isEmpty()) {
            this.path = this.getFirstOrEmptyPath();
        }
        return this.webcams;
    }

    public void loadDefaults() {
        this.reloadWebcams();
        this.path = this.getFirstOrEmptyPath();
    }
}
