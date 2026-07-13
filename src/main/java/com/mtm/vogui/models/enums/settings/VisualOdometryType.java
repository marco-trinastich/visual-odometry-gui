/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.enums.settings;

import com.mtm.vogui.models.interfaces.Comparable;
import com.mtm.vogui.models.interfaces.WithValue;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum VisualOdometryType implements WithValue, Comparable {
    MonoPlaneInfinity("Monocular plane infinity", true),
    MonoPlaneOverhead("Monocular plane with synthetic overhead", true),
    StereoDepth("Stereo depth", false),
    StereoDualPnP("Stereo dual tracker PnP", false),
    StereoQuadPnP("Stereo quad tracker PnP", false),
    DepthDepthPnP("Depth PnP", false),
    Default("Default (monocular plane infinity, standard parameters)", true);

    private final String value;
    private final boolean enabled;

    VisualOdometryType(String value, boolean enabled) {
        this.value = value;
        this.enabled = enabled;
    }

    /**
     * The types actually wired to an algorithm, for the UIs to offer (both the JavaFX and Swing VO
     * combos populate from here, so the not-implemented stereo/depth types stay hidden in one place).
     */
    public static VisualOdometryType[] enabledValues() {
        return Arrays.stream(values())
                .filter(VisualOdometryType::enabled)
                .toArray(VisualOdometryType[]::new);
    }


    public boolean isMono() {
        return is(MonoPlaneInfinity) || is(MonoPlaneOverhead) || is(Default);
    }

    public boolean isStereo() {
        return is(StereoDepth) || is(StereoDualPnP) || is(StereoQuadPnP);
    }

    public boolean isDepth() {
        return is(DepthDepthPnP);
    }
}
