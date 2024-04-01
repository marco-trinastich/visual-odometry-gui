package com.mtm.vogui.models.settings.core.input.device.boofcv;

import com.github.sarxos.webcam.Webcam;
import com.mtm.vogui.models.interfaces.WithDefault;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * BoofCv camera settings
 * <p/>
 * Options related to BoofCv device.
 */
@Data
@Dependent
public class BoofCvCameraSettings implements Serializable, WithDefault<BoofCvCameraSettings> {

    private String path;

    @XStreamOmitField
    private List<Webcam> webcams;

    @Inject
    public BoofCvCameraSettings() {
        this.loadDefaults();
    }

    public BoofCvCameraSettings(@NotNull BoofCvCameraSettings boofCv) {
        this.webcams = this.reloadWebcams(boofCv.webcams);
        this.path = boofCv.path != null ? boofCv.path : this.getFirstOrEmptyPath();
    }

    public String @NotNull [] paths() {
        return this.webcams.stream().map(Webcam::getName).toArray(String[]::new);
    }

    public String getFirstOrEmptyPath() {
        return this.webcams != null && !this.webcams.isEmpty() ? this.webcams.getFirst().getName() : "";
    }

    public Webcam webcam() {
        // Returns selected webcam by path id if it exists, otherwise the first available one, otherwise null
        return Stream.ofNullable(this.webcams)
                .findFirst()
                .orElse(new ArrayList<>())
                .stream()
                .filter(Objects::nonNull)
                .filter(x -> x.getName().contains(this.path))
                .findFirst()
                .orElse(!this.webcams.isEmpty() ? this.webcams.getFirst() : null);
    }

    public void reloadWebcams() {
        this.reloadWebcams(null);
    }

    public List<Webcam> reloadWebcams(List<Webcam> webcams) {
        this.webcams = webcams != null ? webcams : Webcam.getWebcams();
        return this.webcams;
    }

    public void loadDefaults() {
        this.reloadWebcams();
        this.path = this.getFirstOrEmptyPath();
    }
}
