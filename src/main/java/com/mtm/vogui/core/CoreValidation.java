/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.core;

import com.mtm.vogui.models.core.processing.ProcessingParameters;
import com.mtm.vogui.models.enums.settings.ChartType;
import com.mtm.vogui.models.enums.settings.SourceType;
import com.mtm.vogui.models.settings.Settings;
import com.mtm.vogui.models.settings.core.tracker.TrackerSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CoreValidation {

    static boolean validateSettings(@NotNull Settings settings, @NotNull ProcessingParameters params) {
        // Input settings
        var calibrationPath = params.frozenSettings().core().input().calibration().path();
        var sourceType = params.frozenSettings().core().input().source();
        var videoPath = params.frozenSettings().core().input().video().path();
        var devicePath = params.frozenSettings().core().input().device().path().id();
        var deviceWidth = params.frozenSettings().core().input().device().targetWidth();
        int deviceHeight = params.frozenSettings().core().input().device().targetHeight();

        // Image settings
        var imageDescriptor = params.frozenSettings().core().image().descriptor();
        var imageResizeWidth = params.frozenSettings().core().image().resizeWidth();
        var imageResizeHeight = params.frozenSettings().core().image().resizeHeight();

        // Tracker settings
        var trackerType = params.frozenSettings().core().tracker().getTrackerType();
        var kltTrackerPyramidLevels = params.frozenSettings().core().tracker().getKltTracker_pyramidLevels();

        // Visual odometry settings
        var visualOdometryType = params.frozenSettings().core().visualOdometry().type();

        // Chart settings
        var chartType = params.frozenSettings().core().chart().type();
        var chartXZScale = params.frozenSettings().core().chart().scaleXZ();
        var chartYScale = params.frozenSettings().core().chart().scaleY();


        //Extracts mainFrame from GuiComponents (needed as JOptionPane Parent window):
        JFrame mainFrame = (JFrame) settings.state().guiComponents().get("mainFrame");


        //Calibration Path Check
        if (calibrationPath == null || calibrationPath.isEmpty()) {
            JOptionPane.showConfirmDialog(mainFrame, "Calibration path is empty!", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        //Input Source Check
        if (sourceType == null) {
            JOptionPane.showConfirmDialog(mainFrame, "Select an Input Source!", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        switch (sourceType) {
            case Video:    //If Input Source is Video Input:
                //Video Path Check
                if (videoPath == null || videoPath.isEmpty()) {
                    JOptionPane.showConfirmDialog(mainFrame, "Video path is empty!", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                break;
            case Device:    //If Input Source is Device Input:
                //Device Path Check
                if (devicePath == null || devicePath.isEmpty()) {
                    JOptionPane.showConfirmDialog(mainFrame, "Device path is empty!", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                //Device Width Check
                if (deviceWidth <= 0) {
                    JOptionPane.showConfirmDialog(mainFrame, "Device acquisition width is less than or equal to zero!\nUse only positive values", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                //Device Height Check
                if (deviceHeight <= 0) {
                    JOptionPane.showConfirmDialog(mainFrame, "Device acquisition height is less than or equal to zero!\nUse only positive values", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                break;
            default:    //If Input Source is unknown:
                JOptionPane.showConfirmDialog(mainFrame, "Wrong Input Source selected!", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
                return false;
        }

        // Image descriptor Check
        if (imageDescriptor == null || imageDescriptor.type() == null || imageDescriptor.bands() < 1) {
            JOptionPane.showConfirmDialog(mainFrame, "Select a correct Image Type!", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        //Processed Image Resize Width Check
        if (imageResizeWidth <= 0) {
            JOptionPane.showConfirmDialog(mainFrame, "Processed image resize width is less than or equal to zero!\nUse only positive values", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        //Processed Image Resize Height Check
        if (imageResizeHeight <= 0) {
            JOptionPane.showConfirmDialog(mainFrame, "Processed image resize height is less than or equal to zero!\nUse only positive values", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        //Tracker Type Check
        if (trackerType == null || trackerType.isEmpty()) {
            JOptionPane.showConfirmDialog(mainFrame, "Select a correct Tracker Type!", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        //KLT Tracker pyramidScaling Check (KLT Trackers only)
        if ((trackerType.equals(TrackerSettings.KLT) || trackerType.equals(TrackerSettings.KLT2) || trackerType.equals(TrackerSettings.DEFAULT_TRACKER))
                && kltTrackerPyramidLevels == 0) {

            int choice = JOptionPane.showConfirmDialog(
                    mainFrame,
                    "KLT Tracker Pyramid Levels is empty!\n" +
                            "Use default value 4 [1,2,4,8]?",
                    "Error",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.ERROR_MESSAGE
            );
            switch (choice) {
                case JOptionPane.OK_OPTION:
                    ((JTextField) settings.state().guiComponents().get("txtKltTracker_pyramidLevels"))
                            .setText("4");
                    //Changes original parameter (to persist the modification)
                    settings.core().tracker().setKltTracker_pyramidLevels(4);
                    //Changes stored parameter (to continue current elaboration)
                    params.frozenSettings().core().tracker().setKltTracker_pyramidLevels(4);
                    break;
                case JOptionPane.CANCEL_OPTION:
                    return false;
            }
        }

        //Visual Odometry Check
        if (visualOdometryType == null) {
            JOptionPane.showConfirmDialog(mainFrame, "Select a correct Visual Odometry type!", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        //Chart Type Check
        if (chartType == null || (!ChartType.YFrames.is(chartType) && !ChartType.YSeconds.equals(chartType))) {
            JOptionPane.showConfirmDialog(mainFrame, "Select a correct Chart Type!", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        //XZ Chart Scale Check
        if (chartXZScale == 0) {
            JOptionPane.showConfirmDialog(mainFrame, "Insert an XZ Chart scaling factor different from zero!", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        //Y Chart Scale Check
        if (chartYScale == 0) {
            JOptionPane.showConfirmDialog(mainFrame, "Insert an Y Chart scaling factor different from zero!", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
            return false;
        }

        //If all checks passed, returns true
        return true;
    }
}
