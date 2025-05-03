/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.core.processing.tracking;

import com.mtm.vogui.models.enums.settings.ChartType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TrackedPoint {
    int chartId;
    int frame;
    Double time;
    String formattedTime;
    Double x;
    Double y;
    Double z;
    BigDecimal inliersPercent;
    ChartType chartType;
    boolean startPoint;
    boolean endPoint;
}
