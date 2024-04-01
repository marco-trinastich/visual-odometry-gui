package com.mtm.vogui.models.enums.settings.resolution;

import com.mtm.vogui.models.constants.AppConstants;
import com.mtm.vogui.models.interfaces.Comparable;
import com.mtm.vogui.models.interfaces.Resolution;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public enum ResizeResolution implements Resolution, Comparable {
    R_100(100, 100, null),
    QQVGA(160, 120, "QQVGA"),
    R_200(200, 200, null),
    R_300(300, 300, null),
    QVGA(320, 240, "QVGA"),
    R_400(400, 400, null),
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

    ResizeResolution(int width, int height, String type) {
        this.width = width;
        this.height = height;
        this.type = type;
    }

    @Override
    public String value() {
        return String.format(AppConstants.RESOLUTION_PATTERN, this.width, this.height);
    }

    public static @Nullable ResizeResolution findByResolution(int width, int height) {
        for (ResizeResolution deviceResolution : values()) {
            if (deviceResolution.width == width && deviceResolution.height == height) {
                return deviceResolution;
            }
        }
        return null;
    }
}
