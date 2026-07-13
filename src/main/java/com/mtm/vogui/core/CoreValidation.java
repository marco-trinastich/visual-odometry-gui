/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.core;

import com.mtm.vogui.core.rendering.RenderSink;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.core.processing.ProcessingParameters;
import com.mtm.vogui.models.enums.settings.ChartType;

import org.jetbrains.annotations.NotNull;

public class CoreValidation {
    private final static int KLT_PYRAMID_LEVELS_DEFAULT = 4;

    static boolean validateSettings(@NotNull AppContext context, @NotNull ProcessingParameters params,
                                    @NotNull RenderSink sink) {
        // Input settings
        var calibrationPath = params.frozenContext().settings().input().calibration().path();
        var sourceType = params.frozenContext().settings().input().source();
        var videoPath = params.frozenContext().settings().input().video().path();
        var devicePath = params.frozenContext().settings().input().device().path().id();
        var deviceWidth = params.frozenContext().settings().input().device().targetWidth();
        int deviceHeight = params.frozenContext().settings().input().device().targetHeight();

        // Image settings
        var imageDescriptor = params.frozenContext().settings().image().descriptor();
        var imageResizeWidth = params.frozenContext().settings().image().resizeWidth();
        var imageResizeHeight = params.frozenContext().settings().image().resizeHeight();

        // Tracker settings
        var trackerType = params.frozenContext().settings().tracker().type();
        var kltTrackerPyramidLevels = params.frozenContext().settings().tracker().klt().pyramidLevels();

        // Visual odometry settings
        var visualOdometryType = params.frozenContext().settings().visualOdometry().type();

        // Chart settings
        var chartType = params.frozenContext().settings().chart().type();
        var chartAutoXZScale = params.frozenContext().settings().chart().autoScaleXZ();
        var chartXZScale = params.frozenContext().settings().chart().scaleXZ();
        var chartAutoYScale = params.frozenContext().settings().chart().autoScaleY();
        var chartYScale = params.frozenContext().settings().chart().scaleY();


        //Calibration Path Check
        if (calibrationPath == null || calibrationPath.isEmpty()) {
            sink.notifyError("Calibration path is empty!");
            return false;
        }

        //Input Source Check
        if (sourceType == null) {
            sink.notifyError("Select an Input Source!");
            return false;
        }

        switch (sourceType) {
            case Video:    //If Input Source is Video Input:
                //Video Path Check
                if (videoPath == null || videoPath.isEmpty()) {
                    sink.notifyError("Video path is empty!");
                    return false;
                }
                break;
            case Device:    //If Input Source is Device Input:
                //Device Path Check
                if (devicePath == null || devicePath.isEmpty()) {
                    sink.notifyError("Device path is empty!");
                    return false;
                }

                //Device Width Check
                if (deviceWidth <= 0) {
                    sink.notifyError("Device acquisition width is less than or equal to zero!\nUse only positive values");
                    return false;
                }

                //Device Height Check
                if (deviceHeight <= 0) {
                    sink.notifyError("Device acquisition height is less than or equal to zero!\nUse only positive values");
                    return false;
                }
                break;
            default:    //If Input Source is unknown:
                sink.notifyError("Wrong Input Source selected!");
                return false;
        }

        // Image descriptor Check
        if (imageDescriptor == null || imageDescriptor.type() == null || imageDescriptor.bands() < 1) {
            sink.notifyError("Select a correct Image Type!");
            return false;
        }

        //Processed Image Resize Width Check
        if (imageResizeWidth <= 0) {
            sink.notifyError("Processed image resize width is less than or equal to zero!\nUse only positive values");
            return false;
        }

        //Processed Image Resize Height Check
        if (imageResizeHeight <= 0) {
            sink.notifyError("Processed image resize height is less than or equal to zero!\nUse only positive values");
            return false;
        }

        //Tracker Type Check
        if (trackerType == null) {
            sink.notifyError("Select a correct Tracker Type!");
            return false;
        }

        //KLT Tracker pyramidScaling Check (KLT Trackers only)
        if (trackerType.isKlt() && kltTrackerPyramidLevels == 0 &&
                !healKltPyramidLevels(context, params, sink,
                        "KLT Tracker Pyramid Levels is empty!\nUse default value 4 [1,2,4,8]?")) {
            return false;
        }

        //Visual Odometry Check
        if (visualOdometryType == null) {
            sink.notifyError("Select a correct Visual Odometry type!");
            return false;
        }

        //Chart Type Check
        if (chartType == null || (!ChartType.YFrames.is(chartType) && !ChartType.YSeconds.equals(chartType))) {
            sink.notifyError("Select a correct Chart Type!");
            return false;
        }

        //XZ Chart Scale Check (skipped when the axis auto-ranges: the scale is then ignored)
        if (!chartAutoXZScale && chartXZScale == 0) {
            sink.notifyError("Insert an XZ Chart scaling factor different from zero!");
            return false;
        }

        //Y Chart Scale Check (skipped when the axis auto-ranges: the scale is then ignored)
        if (!chartAutoYScale && chartYScale == 0) {
            sink.notifyError("Insert an Y Chart scaling factor different from zero!");
            return false;
        }

        //If all checks passed, returns true
        return true;
    }

    /**
     * Asks the user to fall back to the default KLT pyramid levels; on confirmation heals both
     * the original settings (persisted) and the frozen ones (current elaboration), then lets
     * the GUI reflect the healed value through the sink.
     *
     * @return {@code false} if the user declined (the elaboration must not proceed)
     */
    static boolean healKltPyramidLevels(@NotNull AppContext context, @NotNull ProcessingParameters params,
                                        @NotNull RenderSink sink, String message) {
        if (!sink.confirmOrCancel(message)) {
            return false;
        }

        //Changes original parameter (to persist the modification)
        context.settings().tracker().klt().pyramidLevels(KLT_PYRAMID_LEVELS_DEFAULT);
        //Changes stored parameter (to continue current elaboration)
        params.frozenContext().settings().tracker().klt().pyramidLevels(KLT_PYRAMID_LEVELS_DEFAULT);
        sink.kltPyramidLevelsChanged(KLT_PYRAMID_LEVELS_DEFAULT);
        return true;
    }
}
