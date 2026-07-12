/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.enums.settings;

import com.mtm.vogui.models.interfaces.Comparable;
import com.mtm.vogui.models.enums.gui.ChartAxis;
import com.mtm.vogui.models.interfaces.WithValue;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public enum ChartType implements WithValue, Comparable {
    YSeconds("second", "Y/s"),
    YFrames("frame", "Y/f");

    private final String value;
    private final String id;

    ChartType(String value, String id) {
        this.value = value;
        this.id = id;
    }

    public @Nullable ChartAxis xAxis() {
        switch (this) {
            case YFrames -> {
                return ChartAxis.Frame;
            }
            case YSeconds -> {
                return ChartAxis.Seconds;
            }
            default -> {
                return null;
            }
        }
    }

    public ChartAxis yAxis() {
        return ChartAxis.Y;
    }
}
