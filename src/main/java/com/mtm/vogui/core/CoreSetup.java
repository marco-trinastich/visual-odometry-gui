/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.core;

import boofcv.abst.sfm.d3.VisualOdometry;
import boofcv.io.calibration.CalibrationIO;
import com.mtm.vogui.core.integration.camera.BoofCvCamera;
import com.mtm.vogui.core.integration.camera.OpenCvCamera;
import com.mtm.vogui.core.integration.camera.V4l4jCamera;
import com.mtm.vogui.factory.TrackerFactory;
import com.mtm.vogui.factory.VisualOdometryFactory;
import com.mtm.vogui.models.core.processing.ProcessingParameters;
import com.mtm.vogui.models.core.processing.tracking.Tracker;
import com.mtm.vogui.models.core.exceptions.CameraException;
import com.mtm.vogui.models.core.exceptions.InvalidImageFormatException;
import com.mtm.vogui.models.enums.core.CalibrationLoadResult;
import com.mtm.vogui.models.enums.settings.TrackerType;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.context.settings.visualodometry.monoplaneinfinity.MonoPlaneInfinitySettings;
import com.mtm.vogui.models.constants.Messages;
import com.mtm.vogui.utilities.CoreUtils;
import com.mtm.vogui.utilities.LogUtils;
import com.mtm.vogui.utilities.OSUtils;
import georegression.struct.se.Se3_F64;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

public class CoreSetup {

    /**
     * Open Calibration
     * </>
     * Tries to open a mono, stereo or depth calibration
     */
    static CalibrationLoadResult openCalibration(@NotNull ProcessingParameters params) {
        String calibrationPath = params.frozenContext().settings().input().calibration().path();

        // Read the whole calibration file (containing camera description)
        String content;
        try (Reader reader = CoreUtils.getMediaManager().openFile(calibrationPath)) {
            if (reader == null) {
                return CalibrationLoadResult.NotFound;
            }
            var writer = new StringWriter();
            reader.transferTo(writer);
            content = writer.toString();
        } catch (Exception _) {
            return CalibrationLoadResult.NotFound;
        }

        // Legacy calibrations (BoofCV <= 0.17) were XStream-serialized XML files,
        // no longer readable by CalibrationIO (YAML only)
        if (content.stripLeading().startsWith("<")) {
            return CalibrationLoadResult.LegacyXmlFormat;
        }

        // Load YAML calibration
        Object calibration = null;
        try {
            calibration = params.calibration(CalibrationIO.load(new StringReader(content)));
        } catch (Exception _) {
        }

        return calibration != null ? CalibrationLoadResult.Ok : CalibrationLoadResult.Invalid;
    }

    /**
     * Open Video
     * </p>
     * Tries to open a video file
     */
    static boolean openVideo(@NotNull ProcessingParameters params) {
        var videoPath = params.frozenContext().settings().input().video().path();
        var descriptor = params.frozenContext().settings().image().descriptor();

        // Get video
        return CoreUtils.openVideo(videoPath, descriptor, params);
    }

    /**
     * Open Device
     * </p>
     * Tries to open a video stream from a video input device
     */
    static boolean openDevice(@NotNull AppContext context, @NotNull ProcessingParameters params) {
        var deviceType = params.frozenContext().settings().input().device().type();

        try {
            switch (deviceType) {
                case BoofCv -> {
                    return CoreUtils.openDevice(context, params, BoofCvCamera::from);
                }
                case OpenCv -> {
                    return CoreUtils.openDevice(context, params, OpenCvCamera::from);
                }
                case V4L4J -> {
                    // V4L4J is Linux-only: don't even try elsewhere
                    return OSUtils.isUnix() && CoreUtils.openDevice(context, params, V4l4jCamera::from);
                }
                default -> {
                    return false;
                }
            }
        } catch (CameraException _) {
            // potential error in camera open catch block
            return false;
        }
    }

    /**
     * Setup Tracker
     * </p>
     * Tries to create and set up a tracker
     */
    static boolean setupTracker(@NotNull AppContext context, @NotNull ProcessingParameters params) {
        JFrame mainFrame = (JFrame) context.state().guiComponents().get("mainFrame");

        var imageType = params.frozenContext().settings().image().descriptor();
        var trackerType = params.frozenContext().settings().tracker().type();
        TrackerFactory trackerFactory;
        try {
            trackerFactory = TrackerFactory.from(imageType);
        } catch (InvalidImageFormatException _) {
            return false;
        }

        switch (trackerType) {
            //If the KLT or KLT-Modern Tracker has been selected:
            case Klt:
            case Klt2:
                // Klt
                var kltTrackerTemplateRadius = params.frozenContext().settings().tracker().klt().templateRadius();
                var kltTrackerPyramidLevels = params.frozenContext().settings().tracker().klt().pyramidLevels();
                var kltTrackerMaxFeatures = params.frozenContext().settings().tracker().klt().maxFeatures();
                var kltTrackerRadius = params.frozenContext().settings().tracker().klt().radius();
                var kltTrackerThreshold = params.frozenContext().settings().tracker().klt().threshold();

                //If the extracted pyramid levels is 0
                if (kltTrackerPyramidLevels == 0) {
                    //Shows a message to tell that pyramidLevels is not valid, and asks
                    //to change for default value:
                    int choice = JOptionPane.showConfirmDialog(
                            mainFrame,
                            "KLT Tracker Pyramid Levels is not valid!\n" +
                                    "Use default value 4 [1,2,4,8]?",
                            "Error",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.ERROR_MESSAGE
                    );
                    switch (choice) {
                        case JOptionPane.OK_OPTION:
                            //If accepted
                            //Reset KLT Tracker pyramidLevels TextField content to default value
                            ((JTextField) params.frozenContext().state().guiComponents().get("txtKltTracker_pyramidLevels"))
                                    .setText("4");
                            //Changes original Parameters (to persist the modification)
                            context.settings().tracker().klt().pyramidLevels(4);
                            //Changes stored Parameter (to continue current elaboration)
                            params.frozenContext().settings().tracker().klt().pyramidLevels(4);

                            //Sets the local pyramidScaling value to default value
                            kltTrackerPyramidLevels = params.frozenContext().settings().tracker().klt().pyramidLevels();
                            break;
                        case JOptionPane.CANCEL_OPTION:
                        default:
                            //If canceled
                            //Exits and returns false (error in Tracker settings)
                            return false;
                    }
                }

                try {
                    //If the pyramidLevels is valid
                    //Depending on whether KLT or KLT2 generates a working tracker:
                    if (TrackerType.Klt.is(trackerType)) {
                        params.tracker(
                                trackerFactory.createKLT(kltTrackerTemplateRadius, kltTrackerPyramidLevels,
                                        kltTrackerMaxFeatures, kltTrackerRadius,
                                        kltTrackerThreshold));
                    } else {
                        params.tracker(trackerFactory.createKLTModern());
//						trackerGenerator.createKLT_TwoPass(kltTracker_templateRadius, extracted_pyramidScaling,
//																   kltTracker_maxFeatures, kltTracker_radius,
//																   kltTracker_threshold));
                    }
                } catch (Exception exc) {
                    LogUtils.errorf(exc, Messages.TRACKER_SETUP_ERROR, exc);
                    return false;
                }
                break;
            case Surf:
                // Surf
                var surfTrackerMaxFeaturesPerScale = params.frozenContext().settings().tracker()
                        .surf().maxFeaturesPerScale();
                var surfTrackerExtractRadius = params.frozenContext().settings().tracker().surf().extractRadius();
                var surfTrackerInitialSampleSize = params.frozenContext().settings().tracker().surf().initialSampleSize();

                //If the SURF Tracker has been selected:
                try {
                    //Tries to generate the Tracker
                    params.tracker(
                            trackerFactory.createSURF(surfTrackerMaxFeaturesPerScale, surfTrackerExtractRadius,
                                    surfTrackerInitialSampleSize));
                } catch (Exception exc) {
                    LogUtils.errorf(exc, Messages.TRACKER_SETUP_ERROR, exc);
                    return false;
                }
                break;
            case Surf2:
                // TODO: implement other trackers
//                //If the SURF-2Pass Tracker has been selected:
//                try {
//                    //Tries to generate the Tracker
//                    parameters.getRuntime().processingParameters().setTracker(
//                            trackerGenerator.createSURF_TwoPass(surfTracker_maxFeaturesPerScale, surfTracker_extractRadius,
//                                    surfTracker_initialSampleSize));
//                } catch (Exception e) {
//                    //If the Tracker generation fails
//                    return false;
//                }
                break;
            case Default:
                //If the Default Tracker (KLT with default parameters) has been selected:
                try {
                    // Tries to generate the Tracker
                    params.tracker(trackerFactory.createDefault());
                } catch (Exception exc) {
                    LogUtils.errorf(exc, Messages.TRACKER_SETUP_ERROR, exc);
                    //If the Tracker generation fails
                    //Returns false
                    return false;
                }
                break;
            default:
                //If an invalid Tracker has been selected:
                //Returns false
                return false;
        }

        //If the Tracker has been successfully created returns true
        return true;
    }

    /**
     * Setup Visual Odometry
     * </p>
     * Attempts creation and setup of a new Visual Odometry
     */
    static boolean setupVisualOdometry(@NotNull ProcessingParameters params) {
        var voSettings = params.frozenContext().settings().visualOdometry();
        var voType = params.frozenContext().settings().visualOdometry().type();
        var imageType = params.frozenContext().settings().image().descriptor();

        Tracker tracker = params.tracker();
        Object calibration = params.calibration();

        // Visual odometry factory
        VisualOdometryFactory voFactory;
        try {
            voFactory = VisualOdometryFactory.from(calibration, tracker, imageType, null);
        } catch (InvalidImageFormatException _) {
            return false;
        }

        VisualOdometry<Se3_F64> voEngine = null;
        try {
            switch (voType) {
                case MonoPlaneInfinity ->
                        voEngine = voFactory.createMonoPlaneInfinity(voSettings.monoPlaneInfinity());
                case MonoPlaneOverhead ->
                        voEngine = voFactory.createMonoPlaneOverhead(voSettings.monoPlaneOverhead());
                case Default ->
                        voEngine = voFactory.createMonoPlaneInfinity(new MonoPlaneInfinitySettings());
                default -> { /* StereoDepth, StereoDualPnP, StereoQuadPnP, DepthDepthPnP: not implemented, voEngine stays null */ }
            }
        } catch (Exception _) {
        }

        if (voEngine == null)
            return false;

        params.visualOdometry(voEngine);
        return true;
    }
}
