/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.core;

import boofcv.abst.sfm.d3.DepthVisualOdometry;
import boofcv.abst.sfm.d3.MonocularPlaneVisualOdometry;
import boofcv.abst.sfm.d3.StereoVisualOdometry;
import boofcv.struct.image.*;
import com.mtm.vogui.models.constants.Messages;
import com.mtm.vogui.models.core.processing.*;
import com.mtm.vogui.models.core.processing.fps.FpsCounter;
import com.mtm.vogui.models.core.processing.frames.ProcessedFrame;
import com.mtm.vogui.models.enums.core.ProcessingState;
import com.mtm.vogui.models.core.exceptions.BufferTimeoutException;
import com.mtm.vogui.models.core.exceptions.VoProcessingException;
import com.mtm.vogui.models.settings.Settings;
import com.mtm.vogui.models.core.processing.tracking.PointFactory;
import com.mtm.vogui.models.enums.gui.AppStatus;
import com.mtm.vogui.gui.components.info.InfoScrollPane;

import javax.swing.*;

import com.mtm.vogui.utilities.CoreUtils;
import com.mtm.vogui.utilities.OSUtils;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;


/**
 * Visual Odometry processing core
 */
@ApplicationScoped
public class Core {

    private final Settings settings;
    private final ProcessingParameters params;

    @Inject
    public Core(Settings settings, ProcessingParameters params) {
        this.settings = settings;
        this.params = params;
    }

    /**
     * Start processing
     */
    public void start() {
        // Run VO
        this.finalize(this.run());
    }

    private ProcessingResult run() {
        ProcessingState result;
        Throwable exception = null;
        try {
            if (this.setup() && this.process(this.settings, this.params)) {
                // Successful processing
                result = ProcessingState.StandBy;
            } else {
                // Checked errors occurred during processing
                result = ProcessingState.Error;
            }
        } catch (Throwable ex) {
            // Unexpected errors occurred during processing. Errors too (e.g. LinkageError from
            // missing natives): escaping here would skip finalize() and leave locks/state hanging.
            // Unexpected means a bug somewhere: always log the full stack trace
            Log.error(Messages.VO_UNEXPECTED_ERROR, ex);
            result = ProcessingState.Error;
            exception = ex;
        }

        return ProcessingResult.builder()
                .state(result)
                .exception(exception)
                .build();
    }

    private boolean setup() {
        this.params.reset();

        // Settings deep copy
        // - frozen settings -> not impacted by GUI interactions (used for validation and processing)
        // - original settings -> impacted by GUI interactions (used for preview, frame skip and buffer)
        this.params.frozenSettings(settings.deepClone());

        // GUI components
        InfoScrollPane infoPanel = settings.state().guiController().infoPanel();
        infoPanel.setAppStatus(AppStatus.Init);
        JFrame mainFrame = (JFrame) settings.state().guiComponents().get("mainFrame");

        // Check settings
        boolean valid = CoreValidation.validateSettings(this.settings, this.params);
        if (!valid) {
            infoPanel.setAppStatus(AppStatus.InvalidSettings);
            return false;
        }
        infoPanel.setAppStatus(AppStatus.ValidSettings);

        // Open calibration
        var calibrationResult = CoreSetup.openCalibration(this.params);
        if (!calibrationResult.isOk()) {
            String message = switch (calibrationResult) {
                case NotFound -> "Calibration file doesn't exist or can't be opened!\nCheck the calibration path.";
                case LegacyXmlFormat -> "Calibration file uses the legacy BoofCV XML format,\nwhich is no longer supported.\nConvert it to the current YAML format (*.yaml).";
                default -> "Calibration file isn't a valid YAML camera calibration!";
            };
            JOptionPane.showConfirmDialog(mainFrame, message, "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
            infoPanel.setAppStatus(AppStatus.InvalidCalibration);
            return false;
        }
        infoPanel.setAppStatus(AppStatus.ValidCalibration);
        // Successful open commits the used path to the recent-paths history
        CoreRendering.renderRecentPath(this.settings, this.settings.core().input().calibration(),
                this.params.frozenSettings().core().input().calibration().path(), "txtCalibration");

        // Open input source
        switch (this.params.frozenSettings().core().input().source()) {
            case Video:
                if (!CoreSetup.openVideo(this.params)) {
                    // Open video file
                    JOptionPane.showConfirmDialog(mainFrame, "Video file isn't valid or the specified file doesn't exist!", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
                    infoPanel.setAppStatus(AppStatus.InvalidVideo);
                    return false;
                }
                infoPanel.setAppStatus(AppStatus.ValidVideo);
                // Successful open commits the used path to the recent-paths history
                CoreRendering.renderRecentPath(this.settings, this.settings.core().input().video(),
                        this.params.frozenSettings().core().input().video().path(), "txtVideoSource");
                break;
            case Device:
                // Open input device
                if (!CoreSetup.openDevice(this.settings, this.params)) {
                    if (!OSUtils.isUnix() && this.params.frozenSettings().core().input().device().path().id().indexOf("V4L4J") >= 0) {
                        JOptionPane.showConfirmDialog(
                                mainFrame,
                                "V4L4J Device Driver runs only under Linux!\nYour current os is: "
                                        + (OSUtils.isWindows() ?
                                        "Windows"
                                        : (OSUtils.isMac() ?
                                        "Mac"
                                        : "Unknown"))
                                , "Error"
                                , JOptionPane.PLAIN_MESSAGE
                                , JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showConfirmDialog(mainFrame, "Device path/type isn't valid, or doesn't exist\nor doesn't support selected adjustments!", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
                    }
                    infoPanel.setAppStatus(AppStatus.InvalidDevice);
                    return false;
                }
                infoPanel.setAppStatus(AppStatus.ValidDevice);
                break;
            default:
                infoPanel.setAppStatus(AppStatus.UnknownDevice);
                return false;
        }

        // Setup tracker
        if (!CoreSetup.setupTracker(this.settings, this.params)) {
            JOptionPane.showConfirmDialog(mainFrame, "Error setting up the Tracker!\nCheck out Tracker settings", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
            infoPanel.setAppStatus(AppStatus.InvalidTracker);
            return false;
        }
        infoPanel.setAppStatus(AppStatus.ValidTracker);

        // Setup visual odometry
        if (!CoreSetup.setupVisualOdometry(this.params)) {
            JOptionPane.showConfirmDialog(mainFrame, "Error setting up the Visual Odometry!\nCheck out Visual Odometry settings, or if the selected\n Visual Odometry type is not implemented.", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
            infoPanel.setAppStatus(AppStatus.InvalidVo);
            return false;
        }
        infoPanel.setAppStatus(AppStatus.ValidVo);

        return true;
    }

    /**
     * Process
     * </p>
     * Start vo processing based on the selected vo type
     */
    private boolean process(@NotNull Settings settings, @NotNull ProcessingParameters params) {
        var voEngine = params.visualOdometry();
        var voType = params.frozenSettings().core().visualOdometry().type();

        var result = false;
        if (voType.isMono()) {
            result = processMonoVO((MonocularPlaneVisualOdometry<?>) voEngine, settings, params);
        } else if (voType.isStereo()) {
            result = processStereoVO((StereoVisualOdometry<?>) voEngine, settings, params);
        } else if (voType.isDepth()) {
            result = processDepthVO((DepthVisualOdometry<?, ?>) voEngine, settings, params);
        }

        if (!result) {
            JFrame mainFrame = (JFrame) this.settings.state().guiComponents().get("mainFrame");
            JOptionPane.showConfirmDialog(mainFrame, "An error has occurred during the Visual Odometry elaboration!\nCheck out your Visual Odometry/Tracker Settings.\nOtherwise your input video may be invalid or not estimable, or has an inadequate calibration.", "Error", JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
        }

        return result;
    }

    private void finalize(@NotNull ProcessingResult result) {
        // It is absolutely necessary to always unlock any pending thread before exiting (vo processing/gui locks)
        CoreUtils.setProcessingStateSafe(settings, result.state());

        if (result.exception() != null) {
            // Show exception message
            CoreRendering.renderAppStatus(settings, result.exception());
        }

        if (result.state().is(ProcessingState.Error)) {
            // Ensure resources cleanup
            // (any exception is swallowed: cleanup must never propagate and kill the vo task)
            try {
                CoreProcessing.closeSource(this.settings, this.params);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Process Monocular Visual Odometry
     * </p>
     * <p>
     * Retrieves images, executes monocular visual odometry with specified settings and renders results.
     */
    @SneakyThrows
    private static boolean processMonoVO(MonocularPlaneVisualOdometry<? extends ImageBase<?>> monoVo,
                                         @NotNull Settings settings,
                                         @NotNull ProcessingParameters params) {
        Exception processingException = null;

        ProcessingStatus status = ProcessingStatus.build();
        params.pointFactory(PointFactory.from(settings, params));

        CoreRendering.renderStartPoint(settings, params);
        CoreRendering.resizeAndRepositionVideoFrames(settings, params);

        // Start fps counter thread
        try (FpsCounter counter = FpsCounter.with(fpsStatus -> CoreRendering.renderCurrentFps(settings, fpsStatus,
                status, params)).start()) {

            // Set running state
            CoreUtils.setProcessingStateSafe(settings, ProcessingState.Running);

            // Start core vo processing cycle
            while (CoreProcessing.shouldContinue(settings, counter) &&
                    !CoreProcessing.isProcessCompleted(settings, params)) {
                CoreRendering.renderAppStatus(settings);
                ProcessedFrame frame = CoreProcessing.getProcessedFrame(settings, params, counter);
                if (frame != null) {
                    // Frame not skipped
                    status.frame(frame);
                    CoreProcessing.handleResetVO(settings, monoVo);
                    var voResult = CoreProcessing.processVO(monoVo, settings, params, status, counter);
                    CoreRendering.renderVO(settings, status, params, voResult);
                }
            }
        } catch (BufferTimeoutException | VoProcessingException ex) {
            processingException = ex;
        }

        // End processing
        if (settings.state().processing().is(ProcessingState.Cleared)) {
            CoreRendering.renderClearAllPoints(settings);
        } else {
            CoreRendering.renderEndPoint(settings, params);
        }
        CoreProcessing.closeSource(settings, params);
        CoreRendering.renderAppStatus(settings, processingException);

        return true;
    }

    /**
     * Process stereo Visual Odometry
     * Not implemented
     */
    private static boolean processStereoVO(StereoVisualOdometry<? extends ImageBase<?>> stereoVo,
                                           @NotNull Settings settings,
                                           @NotNull ProcessingParameters params) {
        return false;
    }

    /**
     * Process depth Visual Odometry
     * Not implemented
     */
    private static boolean processDepthVO(DepthVisualOdometry<? extends ImageBase<?>, ? extends ImageGray<?>> depthVo,
                                          @NotNull Settings settings,
                                          @NotNull ProcessingParameters params) {
        return false;
    }
}
