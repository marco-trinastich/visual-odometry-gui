package com.mtm.vogui.models.enums.settings.resolution;

import com.mtm.vogui.models.constants.AppConstants;
import com.mtm.vogui.models.interfaces.Comparable;
import com.mtm.vogui.models.interfaces.Resolution;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public enum DeviceResolution implements Resolution, Comparable {
    QQVGA(160, 120, "QQVGA"),
    QVGA(320, 240, "QVGA"),
    nHD(640, 360, "nHD/360p"),
    VGA(640, 480, "VGA"),
    SVGA(800, 600, "SVGA"),
    HD(1280, 720, "HD/720p"),
    HDp(1600, 800, "HD+/800p"),
    FHD(1920, 1080, "FHD/1080p"),
    UHD(3840, 2160, "UHD/4k");

    private final int width;
    private final int height;
    private final String type;

    DeviceResolution(int width, int height, String type) {
        this.width = width;
        this.height = height;
        this.type = type;
    }

    @Override
    public String value() {
        return String.format(AppConstants.RESOLUTION_PATTERN_GUI, this.width, this.height, this.type);
    }

    public static @Nullable DeviceResolution findByResolution(int width, int height) {
        for (DeviceResolution deviceResolution : values()) {
            if (deviceResolution.width == width && deviceResolution.height == height) {
                return deviceResolution;
            }
        }
        return null;
    }
}
