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
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.core.processing.*;
import com.mtm.vogui.models.core.processing.fps.FpsCounter;
import com.mtm.vogui.models.core.processing.frames.ProcessedFrame;
import com.mtm.vogui.models.enums.core.ProcessingState;
import com.mtm.vogui.models.core.exceptions.BufferTimeoutException;
import com.mtm.vogui.models.core.exceptions.VoProcessingException;
import com.mtm.vogui.models.core.processing.tracking.PointFactory;
import com.mtm.vogui.models.enums.gui.AppStatus;
import com.mtm.vogui.models.enums.gui.RecentPathTarget;
import com.mtm.vogui.core.rendering.RenderSink;
import com.mtm.vogui.core.rendering.SettingsSync;

import com.mtm.vogui.utilities.CoreUtils;
import com.mtm.vogui.utilities.OSUtils;
import io.quarkus.arc.Unremovable;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;


/**
 * Visual Odometry processing core
 * <p>
 * {@code @Unremovable}: resolved programmatically by the UI launchers ({@code CDI.current()}),
 * so Arc sees no injection point and would otherwise drop the bean at build time.
 */
@ApplicationScoped
@Unremovable
public class Core {

    private final AppContext context;
    private final ProcessingParameters params;
    private final RenderSink sink;

    @Inject
    public Core(AppContext context, ProcessingParameters params, RenderSink sink) {
        this.context = context;
        this.params = params;
        this.sink = sink;
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
            if (this.setup() && this.process(this.context, this.params)) {
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

        // AppContext deep copy
        // - frozen settings -> not impacted by GUI interactions (used for validation and processing)
        // - original settings -> impacted by GUI interactions (used for preview, frame skip and buffer)
        this.params.frozenContext(context.deepClone());

        this.sink.renderAppStatus(AppStatus.Init);

        // Check settings
        boolean valid = CoreValidation.validateSettings(this.context, this.params, this.sink);
        if (!valid) {
            this.sink.renderAppStatus(AppStatus.InvalidSettings);
            return false;
        }
        this.sink.renderAppStatus(AppStatus.ValidSettings);

        // Open calibration
        var calibrationResult = CoreSetup.openCalibration(this.params);
        if (!calibrationResult.isOk()) {
            String message = switch (calibrationResult) {
                case NotFound -> "Calibration file doesn't exist or can't be opened!\nCheck the calibration path.";
                case LegacyXmlFormat -> "Calibration file uses the legacy BoofCV XML format,\nwhich is no longer supported.\nConvert it to the current YAML format (*.yaml).";
                default -> "Calibration file isn't a valid YAML camera calibration!";
            };
            this.sink.notifyError(message);
            this.sink.renderAppStatus(AppStatus.InvalidCalibration);
            return false;
        }
        this.sink.renderAppStatus(AppStatus.ValidCalibration);
        // Successful open commits the used path to the recent-paths history
        SettingsSync.commitRecentPath(this.sink, RecentPathTarget.Calibration,
                this.context.settings().input().calibration(),
                this.params.frozenContext().settings().input().calibration().path());

        // Open input source
        switch (this.params.frozenContext().settings().input().source()) {
            case Video:
                if (!CoreSetup.openVideo(this.params)) {
                    // Open video file
                    this.sink.notifyError("Video file isn't valid or the specified file doesn't exist!");
                    this.sink.renderAppStatus(AppStatus.InvalidVideo);
                    return false;
                }
                this.sink.renderAppStatus(AppStatus.ValidVideo);
                // Successful open commits the used path to the recent-paths history
                SettingsSync.commitRecentPath(this.sink, RecentPathTarget.VideoSource,
                        this.context.settings().input().video(),
                        this.params.frozenContext().settings().input().video().path());
                break;
            case Device:
                // Open input device
                if (!CoreSetup.openDevice(this.context, this.params, this.sink)) {
                    if (!OSUtils.isUnix() && this.params.frozenContext().settings().input().device().path().id().indexOf("V4L4J") >= 0) {
                        this.sink.notifyError("V4L4J Device Driver runs only under Linux!\nYour current os is: "
                                + (OSUtils.isWindows() ?
                                "Windows"
                                : (OSUtils.isMac() ?
                                "Mac"
                                : "Unknown")));
                    } else {
                        this.sink.notifyError("Device path/type isn't valid, or doesn't exist\nor doesn't support selected adjustments!");
                    }
                    this.sink.renderAppStatus(AppStatus.InvalidDevice);
                    return false;
                }
                this.sink.renderAppStatus(AppStatus.ValidDevice);
                break;
            default:
                this.sink.renderAppStatus(AppStatus.UnknownDevice);
                return false;
        }

        // Setup tracker
        if (!CoreSetup.setupTracker(this.context, this.params, this.sink)) {
            this.sink.notifyError("Error setting up the Tracker!\nCheck out Tracker settings");
            this.sink.renderAppStatus(AppStatus.InvalidTracker);
            return false;
        }
        this.sink.renderAppStatus(AppStatus.ValidTracker);

        // Setup visual odometry
        if (!CoreSetup.setupVisualOdometry(this.params)) {
            this.sink.notifyError("Error setting up the Visual Odometry!\nCheck out Visual Odometry settings, or if the selected\n Visual Odometry type is not implemented.");
            this.sink.renderAppStatus(AppStatus.InvalidVo);
            return false;
        }
        this.sink.renderAppStatus(AppStatus.ValidVo);

        return true;
    }

    /**
     * Process
     * </p>
     * Start vo processing based on the selected vo type
     */
    private boolean process(@NotNull AppContext context, @NotNull ProcessingParameters params) {
        var voEngine = params.visualOdometry();
        var voType = params.frozenContext().settings().visualOdometry().type();

        var result = false;
        if (voType.isMono()) {
            result = processMonoVO((MonocularPlaneVisualOdometry<?>) voEngine, context, params, this.sink);
        } else if (voType.isStereo()) {
            result = processStereoVO((StereoVisualOdometry<?>) voEngine, context, params);
        } else if (voType.isDepth()) {
            result = processDepthVO((DepthVisualOdometry<?, ?>) voEngine, context, params);
        }

        if (!result) {
            this.sink.notifyError("An error has occurred during the Visual Odometry elaboration!\nCheck out your Visual Odometry/Tracker Settings.\nOtherwise your input video may be invalid or not estimable, or has an inadequate calibration.");
        }

        return result;
    }

    private void finalize(@NotNull ProcessingResult result) {
        // It is absolutely necessary to always unlock any pending thread before exiting (vo processing/gui locks)
        CoreUtils.setProcessingStateSafe(context, result.state());

        if (result.exception() != null) {
            // Show exception message
            this.sink.renderAppStatus(context, result.exception());
        }

        if (result.state().is(ProcessingState.Error)) {
            // Ensure resources cleanup
            // (any exception is swallowed: cleanup must never propagate and kill the vo task)
            try {
                CoreProcessing.closeSource(this.context, this.params);
            } catch (Exception _) {
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
                                         @NotNull AppContext context,
                                         @NotNull ProcessingParameters params,
                                         @NotNull RenderSink sink) {
        Exception processingException = null;

        ProcessingStatus status = ProcessingStatus.build();
        params.pointFactory(PointFactory.from(sink.chartsCount(),
                params.frozenContext().settings().chart().type()));

        sink.renderStartPoint(params);
        sink.resizeAndRepositionVideoFrames(params);

        // Start fps counter thread
        try (FpsCounter counter = FpsCounter.with(fpsStatus -> sink.renderCurrentFps(fpsStatus,
                status, params)).start()) {

            // Set running state
            CoreUtils.setProcessingStateSafe(context, ProcessingState.Running);

            // Start core vo processing cycle
            while (CoreProcessing.shouldContinue(context, sink, counter) &&
                    !CoreProcessing.isProcessCompleted(context, params)) {
                sink.renderAppStatus(context);
                ProcessedFrame frame = CoreProcessing.getProcessedFrame(context, params, counter);
                if (frame != null) {
                    // Frame not skipped
                    status.frame(frame);
                    CoreProcessing.handleResetVO(context, sink, monoVo);
                    var voResult = CoreProcessing.processVO(monoVo, context, sink, params, status, counter);
                    sink.renderVO(status, params, voResult);
                }
            }
        } catch (BufferTimeoutException | VoProcessingException ex) {
            processingException = ex;
        }

        // End processing
        if (context.state().processing().is(ProcessingState.Cleared)) {
            sink.renderClearAllPoints();
        } else {
            sink.renderEndPoint(params);
        }
        CoreProcessing.closeSource(context, params);
        sink.renderAppStatus(context, processingException);

        return true;
    }

    /**
     * Process stereo Visual Odometry
     * Not implemented
     */
    private static boolean processStereoVO(StereoVisualOdometry<? extends ImageBase<?>> stereoVo,
                                           @NotNull AppContext context,
                                           @NotNull ProcessingParameters params) {
        return false;
    }

    /**
     * Process depth Visual Odometry
     * Not implemented
     */
    private static boolean processDepthVO(DepthVisualOdometry<? extends ImageBase<?>, ? extends ImageGray<?>> depthVo,
                                          @NotNull AppContext context,
                                          @NotNull ProcessingParameters params) {
        return false;
    }
}
