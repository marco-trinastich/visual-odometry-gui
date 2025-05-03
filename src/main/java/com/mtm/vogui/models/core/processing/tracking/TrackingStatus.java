/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.core.processing.tracking;

import georegression.struct.point.Point2D_F64;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class TrackingStatus {
    //Counts inliers (matched tracks) in current Visual Odometry
    private List<Point2D_F64> trackInliers;
    //Counts new tracks in current Visual Odometry
    private List<Point2D_F64> trackNew;
    //Counts total tracks in current Visual Odometry
    private int totalTracks;
    //inliers/total tracks %
    private BigDecimal inliersPercent;

    public TrackingStatus deepClone(){
        return TrackingStatus.builder()
                .trackInliers(trackInliers)
                .trackNew(trackNew)
                .totalTracks(totalTracks)
                .inliersPercent(inliersPercent)
                .build();
    }
}
