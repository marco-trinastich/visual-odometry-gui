/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.enums.settings;

import com.mtm.vogui.models.interfaces.Comparable;
import com.mtm.vogui.models.interfaces.WithValue;
import lombok.Getter;

@Getter
public enum VisualOdometryType implements WithValue, Comparable {
    MonoPlaneInfinity("Monocular plane infinity"),
    MonoPlaneOverhead("Monocular plane with synthetic overhead"),
    StereoDepth("Stereo depth (not implemented)"),
    StereoDualPnP("Stereo dual tracker PnP (not implemented)"),
    StereoQuadPnP("Stereo quad tracker PnP (not implemented)"),
    DepthDepthPnP("Depth PnP (not implemented)"),
    Default("Default (monocular plane infinity, standard parameters)");

    private final String value;

    VisualOdometryType(String value) {
        this.value = value;
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
