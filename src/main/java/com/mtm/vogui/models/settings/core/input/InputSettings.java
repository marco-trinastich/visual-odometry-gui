package com.mtm.vogui.models.settings.core.input;

import com.mtm.vogui.models.enums.settings.SourceType;
import com.mtm.vogui.models.interfaces.WithDefault;
import com.mtm.vogui.models.settings.core.input.calibration.CalibrationSettings;
import com.mtm.vogui.models.settings.core.input.device.DeviceSettings;
import com.mtm.vogui.models.settings.core.input.video.VideoSettings;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Input settings
 * <p/>
 * Options related to the input of the algorithm.
 */

@Data
@Dependent
public class InputSettings implements Serializable, WithDefault<InputSettings> {

    private CalibrationSettings calibration;
    private SourceType source;
    private VideoSettings video;
    private DeviceSettings device;
    private boolean inputPreview;
    private boolean fullResolutionPreview;

    @Inject
    public InputSettings(CalibrationSettings calibration, VideoSettings video, DeviceSettings device) {
        this.calibration = calibration;
        this.video = video;
        this.device = device;

        this.loadDefaults();
    }

    public InputSettings(@NotNull InputSettings input) {
        this.calibration = new CalibrationSettings(input.calibration);
        this.source = input.source;
        this.video = new VideoSettings(input.video);
        this.device = new DeviceSettings(input.device);
        this.inputPreview = input.inputPreview;
        this.fullResolutionPreview = input.fullResolutionPreview;
    }

    public void loadDefaults() {
        // Set default values
        this.calibration.loadDefaults();
        this.source = SourceType.Video;
        this.video.loadDefaults();
        this.device.loadDefaults();
        this.inputPreview = true;
        this.fullResolutionPreview = false;
    }

    public DeviceSettings getDevice() {
        return device;
    }
}
