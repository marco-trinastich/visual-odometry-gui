/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.core;

import boofcv.abst.sfm.AccessPointTracks3D;
import boofcv.abst.sfm.d3.MonoPlaneInfinity_to_MonocularPlaneVisualOdometry;
import boofcv.abst.sfm.d3.MonocularPlaneVisualOdometry;
import boofcv.abst.sfm.d3.VisualOdometry;
import boofcv.abst.tracker.PointTrack;
import boofcv.abst.tracker.PointTracker;
import boofcv.struct.image.ImageBase;
import com.mtm.vogui.models.constants.Messages;
import com.mtm.vogui.models.core.processing.ProcessingParameters;
import com.mtm.vogui.models.core.processing.ProcessingStatus;
import com.mtm.vogui.models.core.processing.fps.FpsCounter;
import com.mtm.vogui.models.core.processing.frames.InnerFrame;
import com.mtm.vogui.models.core.processing.frames.ProcessedFrame;
import com.mtm.vogui.models.core.processing.tracking.TrackingStatus;
import com.mtm.vogui.models.enums.core.ProcessingState;
import com.mtm.vogui.models.enums.gui.AppStatus;
import com.mtm.vogui.models.enums.settings.SourceType;
import com.mtm.vogui.models.core.exceptions.BufferTimeoutException;
import com.mtm.vogui.models.core.exceptions.CameraException;
import com.mtm.vogui.models.core.exceptions.UnknownSourceException;
import com.mtm.vogui.models.core.exceptions.VoProcessingException;
import com.mtm.vogui.models.settings.Settings;
import com.mtm.vogui.utilities.CommonUtils;
import com.mtm.vogui.utilities.CoreUtils;
import com.mtm.vogui.utilities.ImageUtils;
import georegression.struct.point.Point2D_F64;
import georegression.struct.se.Se3_F64;
import io.quarkus.logging.Log;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

public class CoreProcessing {

    // State check

    public static boolean shouldContinue(@NotNull Settings settings, FpsCounter counter) {
        boolean shouldContinue = true;
        switch (settings.state().processing().get()) {
            case Paused -> handlePauseVO(settings, counter);
            case Stopped, Cleared -> shouldContinue = false;
        }

        return shouldContinue;
    }

    private static void handlePauseVO(@NotNull Settings settings, @NotNull FpsCounter counter) {
        // Suspend processing thread until user action
        CoreRendering.renderAppStatus(settings);
        counter.pause();
        settings.state().processing().waitUntilNot(ProcessingState.Paused);
        counter.resume();
    }

    public static void handleResetVO(@NotNull Settings settings, @NotNull VisualOdometry<Se3_F64> vo) {
        if (CoreUtils.isResetRequested(settings)) {
            // Reset vo context
            vo.reset();
            CoreUtils.setResetRequested(settings, false);
            CoreUtils.setFailedEvent(settings, false);
            CoreRendering.renderAppStatus(settings, AppStatus.VoReset);
        }
    }

    public static void closeSource(@NotNull Settings settings, @NotNull ProcessingParameters params)
            throws CameraException {
        // Close source
        var sourceType = params.frozenSettings().core().input().source();
        switch (sourceType) {
            case Video -> params.video().close();
            case Device -> {
                settings.state().device().stop();
                settings.state().device().clearBuffer();
            }
        }
    }


    // Completion check

    public static boolean isProcessCompleted(@NotNull Settings settings, @NotNull ProcessingParameters params)
            throws UnknownSourceException {
        var sourceType = params.frozenSettings().core().input().source();

        var isProcessCompleted = false;
        switch (sourceType) {
            case Video -> isProcessCompleted = isVideoEnded(params);
            case Device -> isProcessCompleted = isDeviceStopped(settings);
            default -> throw new UnknownSourceException(sourceType.value());
        }

        if (isProcessCompleted) {
            CoreUtils.setProcessingStateSafe(settings, ProcessingState.Completed);
        }

        return isProcessCompleted;
    }

    private static boolean isVideoEnded(@NotNull ProcessingParameters params) {
        // video ended
        return !params.video().hasNext();
    }

    private static boolean isDeviceStopped(@NotNull Settings settings) {
        // capture stopped and empty buffer
        return !settings.state().device().hasNext();
    }

    // Image acquisition

    public static @Nullable ProcessedFrame getProcessedFrame(Settings settings,
                                                             ProcessingParameters params,
                                                             FpsCounter counter) throws BufferTimeoutException {
        var inputFrames = getInputFrames(settings, params, counter);
        // Frame skip
        if (inputFrames == null)
            return null;

        var voFrames = getVoFrames(settings, params, inputFrames);

        return ProcessedFrame.builder()
                .input(InnerFrame.from(inputFrames))
                .vo(InnerFrame.from(voFrames))
                .build();
    }

    private static @Nullable Pair<BufferedImage, ImageBase<?>> getInputFrames(@NotNull Settings settings,
                                                                              @NotNull ProcessingParameters params,
                                                                              @NotNull FpsCounter counter)
            throws BufferTimeoutException {
        // Input settings
        var sourceType = params.frozenSettings().core().input().source();
        var device = settings.state().device();

        // Image settings
        var imageType = params.frozenSettings().core().image().descriptor().type();

        // Count currently received frame
        counter.addFrame();

        // Frame skip settings
        var frameSkipEnabled = settings.core().image().frameSkipEnabled();
        int frameSkipValue = settings.core().image().frameSkipValue();
        var isFrameSkip = (frameSkipEnabled && frameSkipValue > 0) &&
                (counter.totalFrames() % frameSkipValue) != 0;

        // Read input frames (BufferedImage for gui, ImageBase for vo engine)
        BufferedImage inputImage = null;
        ImageBase<?> inputLeftImage = null;

        switch (sourceType) {
            case Video -> {
                inputLeftImage = params.video().next();
                if (isFrameSkip)
                    return null;
                inputImage = params.video().getGuiImage();
            }
            case Device -> {
                // Wait for buffer replenishment or process interrupt, whichever comes first
                if (device.waitBuffer()) {
                    // Interrupted
                    return null;
                }
                inputImage = device.nextImage();
                if (isFrameSkip)
                    return null;
                inputLeftImage = ImageUtils.getBoofCvFromBuffered(inputImage, imageType);
            }
        }

        return Pair.with(inputImage, inputLeftImage);
    }

    private static @NotNull Pair<BufferedImage, ImageBase<?>> getVoFrames(@NotNull Settings settings,
                                                                          @NotNull ProcessingParameters params,
                                                                          @NotNull Pair<BufferedImage, ImageBase<?>> inputFrames) {
        // Image settings
        var imageType = params.frozenSettings().core().image().descriptor().type();
        var isResize = params.frozenSettings().core().image().resize();
        var isInternalImagePreview = settings.core().image().internalImagePreview();
        var isInputPreview = settings.core().input().inputPreview();
        int resizeWidth = params.frozenSettings().core().image().resizeWidth();
        int resizeHeight = params.frozenSettings().core().image().resizeHeight();

        BufferedImage inputImage = inputFrames.getValue0();
        ImageBase<?> inputLeftImage = inputFrames.getValue1();

        // Create vo frames (BufferedImage for gui, ImageBase for vo engine)
        BufferedImage voImage;
        ImageBase<?> voLeftImage;

        if (!isResize) {
            // Boofcv image processed as-is
            voLeftImage = inputLeftImage;

            if (isInternalImagePreview) {
                // Deep copy of boofcv image
                voImage = ImageUtils.getBufferedFromBoofCv(voLeftImage);
            } else if (isInputPreview) {
                // Deep copy of buffered image (to keep separate input/output images)
                voImage = ImageUtils.deepCopyBufferedImage(inputImage);
            } else {
                // No copy (faster) if no input preview is enabled
                voImage = inputImage;
            }
        } else {
            // Resize vo images
            voImage = ImageUtils.resizeBufferedImageJdk(inputImage, resizeWidth, resizeHeight);
            voLeftImage = ImageUtils.getBoofCvFromBuffered(voImage, imageType);

            if (isInternalImagePreview) {
                // Deep copy of resized boofcv image
                voImage = ImageUtils.getBufferedFromBoofCv(voLeftImage);
            }
        }

        return Pair.with(voImage, voLeftImage);
    }


// Processing

    public static boolean processVO(MonocularPlaneVisualOdometry<? extends ImageBase<?>> monoVo, @NotNull Settings settings,
                                    ProcessingParameters params, ProcessingStatus status, FpsCounter counter)
            throws VoProcessingException {
        var infoPanel = settings.state().guiController().infoPanel();

        // Execute vo processing
        boolean result;
        try {
            result = CoreUtils.processVisualOdometry(monoVo, status.frame().vo().left());
        } catch (Exception ex) {
            // Exception estimating ego motion
            infoPanel.setAppStatus(AppStatus.VoException);
            Log.errorf(Messages.VO_EXCEPTION, ex.getMessage(), Arrays.toString(ex.getStackTrace()));
            throw new VoProcessingException();
        }

        if (!result) {
            // Failed estimating ego motion
            infoPanel.setAppStatus(AppStatus.VoFailed);
            if (!settings.state().failedEvent().get()) {
                Log.warn(Messages.VO_FAILED);
                CoreUtils.setFailedEvent(settings, true);
            }

            // Update fps/time status
            status.fps(counter.getStatus());

            return false;
        } else {
            // Extract real world information from current frame (translation/rotation)
            Se3_F64 leftToWorld = monoVo.getCameraToWorld();
            status.translation(leftToWorld.getTranslation());
            status.rotation(leftToWorld.getRotation());

            // Update tracking status (inlier/new/total tracks)
            status.tracking(getTrackingStatus(monoVo, params.tracker().instance()));

            // Update fps/time status
            status.fps(counter.addProcessedFrame());

            return true;
        }
    }

    private static TrackingStatus getTrackingStatus(VisualOdometry<?> alg, PointTracker<?> tracker) {
        TrackingStatus trackingStatus = TrackingStatus.builder()
                .trackInliers(new ArrayList<>())
                .trackNew(new ArrayList<>())
                .totalTracks(0)
                .inliersPercent(BigDecimal.valueOf(0))
                .build();

        if (!(alg instanceof AccessPointTracks3D access))
            return trackingStatus;

        ArrayList<Point2D_F64> trackInliers = new ArrayList<>();
        ArrayList<Point2D_F64> trackNew = new ArrayList<>();
        int totalTracks = access.getTotalTracks();
        for (int i = 0; i < totalTracks; i++) {
            Point2D_F64 p = new Point2D_F64();
            access.getTrackPixel(i, p);
            if (access.isTrackInlier(i))
                trackInliers.add(p);
            if (access.isTrackNew(i))
                // Still not implemented by BoofCv (no new tracks will be found)
                trackNew.add(p);
        }

        if (alg instanceof MonoPlaneInfinity_to_MonocularPlaneVisualOdometry<?>) {
            // New tracks, only valid for monoPlaneInfinity
            for (PointTrack p : tracker.getNewTracks(null)) {
                trackNew.add(p.pixel);
            }
        }

        // Calculate inliers / total tracks %
        BigDecimal inliersPercent = BigDecimal.valueOf(0);
        try {
            inliersPercent = CommonUtils.roundBigDecimal(100.0d * trackInliers.size() / totalTracks, 2);
        } catch (Exception ignored) {
        }

        trackingStatus.trackInliers(trackInliers);
        trackingStatus.trackNew(trackNew);
        trackingStatus.totalTracks(totalTracks);
        trackingStatus.inliersPercent(inliersPercent);

        return trackingStatus;
    }
}
