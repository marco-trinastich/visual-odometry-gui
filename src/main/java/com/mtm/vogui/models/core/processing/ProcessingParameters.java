/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.core.processing;

import java.awt.*;

import boofcv.abst.sfm.d3.VisualOdometry;
import boofcv.io.image.SimpleImageSequence;
import boofcv.struct.image.ImageBase;

import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.core.processing.tracking.PointFactory;
import com.mtm.vogui.models.core.processing.tracking.Tracker;

import georegression.struct.se.Se3_F64;
import jakarta.inject.Singleton;
import lombok.Data;

/**
 * Processing parameters
 */
@Singleton
@Data
public class ProcessingParameters {

    private Object calibration;
    private SimpleImageSequence<? extends ImageBase<?>> video;
    private Dimension frameSize;
    private Tracker tracker;
    private VisualOdometry<Se3_F64> visualOdometry;
    private PointFactory pointFactory;
    private AppContext frozenContext;

    public ProcessingParameters() {
        this.reset();
    }

    public void reset() {
        this.calibration = null;
        this.video = null;
        this.frameSize = null;
        this.tracker = null;
        this.visualOdometry = null;
        this.frozenContext = null;
        this.pointFactory = null;
    }
}
