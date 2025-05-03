/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.factory;

import boofcv.abst.sfm.d3.*;
import boofcv.abst.tracker.PointTracker;
import boofcv.factory.sfm.ConfigPlanarTrackPnP;
import boofcv.factory.sfm.FactoryVisualOdometry;
import boofcv.struct.calib.MonoPlaneParameters;
import boofcv.struct.image.*;
import com.mtm.vogui.models.core.processing.tracking.Tracker;
import com.mtm.vogui.models.enums.settings.ImageTypeDescriptor;
import com.mtm.vogui.models.core.exceptions.InvalidImageFormatException;
import com.mtm.vogui.models.core.exceptions.NotImplementedException;
import com.mtm.vogui.models.settings.core.visualodometry.monoplaneinfinity.MonoPlaneInfinitySettings;
import com.mtm.vogui.models.settings.core.visualodometry.monoplaneoverhead.MonoPlaneOverheadSettings;
import com.mtm.vogui.utilities.CoreUtils;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

/**
 * Visual Odometry algorithm factory
 */
@SuppressWarnings({"unused", "unchecked", "rawtypes"})
public class VisualOdometryFactory {
    @Setter
    private Object calibration;
    private final Class<? extends ImageGray> imgType;
    private final Tracker tracker;

    private VisualOdometryFactory(Object calibration,
                                  Tracker tracker,
                                  Class<? extends ImageGray<?>> imgType,
                                  Class<? extends ImageGray<?>> depthType) {
        this.imgType = imgType;
        this.calibration = calibration;
        this.tracker = tracker;
    }

    public MonocularPlaneVisualOdometry<? extends ImageGray<?>> createMonoPlaneInfinity(@NotNull
                                                                                        MonoPlaneInfinitySettings settings) {
        ConfigPlanarTrackPnP config = settings.getConfig();
        config.tracker = this.tracker.config();

        var voEngine = FactoryVisualOdometry.monoPlaneInfinity(config, this.imgType);
        voEngine.setCalibration((MonoPlaneParameters) calibration);

        // Update tracker instance
        tracker.instance(CoreUtils.getTracker((MonoPlaneInfinity_to_MonocularPlaneVisualOdometry<? extends ImageGray<?>>)
                voEngine));

        return voEngine;
    }

    public MonocularPlaneVisualOdometry<? extends ImageGray<?>> createMonoPlaneOverhead(@NotNull
                                                                                        MonoPlaneOverheadSettings settings) {
        MonocularPlaneVisualOdometry<? extends ImageGray<?>> visualOdometry = FactoryVisualOdometry.monoPlaneOverhead(
                settings.cellSize(),
                settings.maxCellsPerPixel(),
                settings.mapHeightFraction(),
                settings.inlierGroundTol(),
                settings.ransacIterations(),
                settings.thresholdRetire(),
                settings.absoluteMinimumTracks(),
                settings.respawnTrackFraction(),
                settings.respawnCoverageFraction(),
                (PointTracker<? extends ImageGray>) tracker.instance(),
                ImageType.single(imgType)
        );
        visualOdometry.setCalibration((MonoPlaneParameters) calibration);

        return visualOdometry;
    }

    public StereoVisualOdometry<? extends ImageGray<?>> createStereoDepth() throws NotImplementedException {
        //Stereo Depth Params
        //FactoryVisualOdometry.stereoDepth(inlierPixelTol, thresholdAdd, thresholdRetire, ransacIterations,
        // refineIterations, doublePass, sparseDisparity, tracker, imageType)

        throw new NotImplementedException("createStereoDepth");
    }

    public StereoVisualOdometry<? extends ImageGray<?>> createStereoDualTrackerPnP() throws NotImplementedException {
        //Stereo Dual Tracker PnP
        //FactoryVisualOdometry.stereoDualTrackerPnP(thresholdAdd, thresholdRetire, inlierPixelTol, epipolarPixelTol,
        // ransacIterations, refineIterations, trackerLeft, trackerRight, descriptor, imageType)

        throw new NotImplementedException("createStereoDualTrackerPnP");
    }

    public StereoVisualOdometry<? extends ImageGray<?>> createStereoQuadPnP() throws NotImplementedException {
        //Stereo Quad PnP
        //FactoryVisualOdometry.stereoQuadPnP(inlierPixelTol, epipolarPixelTol, maxDistanceF2F, maxAssociationError,
        // ransacIterations, refineIterations, detector, imageType)

        throw new NotImplementedException("createStereoQuadPnP");
    }

    public DepthVisualOdometry<? extends ImageGray<?>, ? extends ImageGray<?>> createDepthDepthPnP()
            throws NotImplementedException {
        //Depth-Depth PnP
        //FactoryVisualOdometry.depthDepthPnP(inlierPixelTol, thresholdAdd, thresholdRetire, ransacIterations,
        // refineIterations, doublePass, sparseDepth, tracker, visualType, depthType)

        throw new NotImplementedException("createDepthDepthPnP");
    }

    public static @NotNull VisualOdometryFactory from(Object calibration,
                                                      Tracker tracker,
                                                      ImageTypeDescriptor imageType,
                                                      ImageTypeDescriptor depthType) throws InvalidImageFormatException {
        if (imageType == null || !ImageGray.class.isAssignableFrom(imageType.type()) ||
                (depthType != null && !ImageGray.class.isAssignableFrom(depthType.type())))
            throw new InvalidImageFormatException();

        var imageTypeClass = (Class<? extends ImageGray<?>>) imageType.type();
        var depthTypeClass = depthType != null ? (Class<? extends ImageGray<?>>) depthType.type() : null;

        return new VisualOdometryFactory(calibration, tracker, imageTypeClass, depthTypeClass);
    }
}
