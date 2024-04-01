package com.mtm.vogui.utilities;

import boofcv.abst.sfm.d3.MonoPlaneInfinity_to_MonocularPlaneVisualOdometry;
import boofcv.abst.sfm.d3.MonocularPlaneVisualOdometry;
import boofcv.abst.tracker.PointTracker;
import boofcv.alg.sfm.d3.VisOdomMonoPlaneInfinity;
import boofcv.io.MediaManager;
import boofcv.io.image.SimpleImageSequence;
import boofcv.io.wrapper.DefaultMediaManager;
import boofcv.io.wrapper.images.LoadFileImageSequence;
import boofcv.struct.image.*;
import com.mtm.vogui.core.CoreRendering;
import com.mtm.vogui.core.integration.camera.BoofCvCamera;
import com.mtm.vogui.core.integration.camera.V4l4jCamera;
import com.mtm.vogui.models.constants.Messages;
import com.mtm.vogui.models.core.processing.ProcessingParameters;
import com.mtm.vogui.models.enums.core.ProcessingState;
import com.mtm.vogui.models.enums.settings.ImageTypeDescriptor;
import com.mtm.vogui.models.core.exceptions.CameraException;
import com.mtm.vogui.models.settings.Settings;
import io.quarkus.logging.Log;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class CoreUtils {
    private final static MediaManager MEDIA_MANAGER = DefaultMediaManager.INSTANCE;

    // Core utils

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static boolean openVideo(String videoPath,
                                    ImageTypeDescriptor imageType,
                                    ProcessingParameters params) {
        SimpleImageSequence<? extends ImageBase<?>> video = null;

        // Open video
        try {
            File videoFile = new File(videoPath);
            if (videoFile.exists() && videoFile.isFile()) {
                // Read video file
                video = getMediaManager().openVideo(videoPath, ImageUtils.getImageType(imageType));
            } else if (videoFile.exists() && videoFile.isDirectory()) {
                // Read images sequence folder
                video = new LoadFileImageSequence<>(ImageUtils.getImageType(imageType), videoPath, "");
            }
        } catch (Exception e) {
            Log.errorf(Messages.OPEN_VIDEO_ERROR, Arrays.toString(e.getStackTrace()));
        }

        // Update video in params
        params.video(video);
        if (video != null) {
            params.frameSize(new Dimension(video.getWidth(), video.getHeight()));
        }

        return video != null;
    }

    public static boolean openDeviceV4L4J(Settings settings, ProcessingParameters params) throws CameraException {
        if (!OSUtils.isUnix()) {
            // If not unix, exit
            return false;
        }

        try {
            // Creates new V4L4J device
            var camera = V4l4jCamera.from(
                    settings,
                    image -> CoreRendering.renderInputVideo(settings, image),
                    buffer -> CoreRendering.renderBufferStatus(settings, buffer)
            ).start();
            settings.state().device(camera);
            params.frameSize(camera.getFrameSize());
            return true;
        } catch (Exception e) {
            Log.errorf(Messages.OPEN_DEVICE_ERROR, Arrays.toString(e.getStackTrace()));

            // Close device
            if (settings.state().device() != null) {
                settings.state().device().stop();
                settings.state().device().clearBuffer();
            }

            return false;
        }
    }

    public static boolean openDeviceBoofCv(Settings settings, ProcessingParameters params) throws CameraException {
        try {
            // Start input device
            var camera = BoofCvCamera.from(
                    settings,
                    image -> CoreRendering.renderInputVideo(settings, image),
                    buffer -> CoreRendering.renderBufferStatus(settings, buffer)
            ).start();
            settings.state().device(camera);
            params.frameSize(camera.getFrameSize());
            return true;
        } catch (Exception e) {
            Log.errorf(Messages.OPEN_DEVICE_ERROR, Arrays.toString(e.getStackTrace()));

            // Close device
            if (settings.state().device() != null) {
                settings.state().device().stop();
                settings.state().device().clearBuffer();
            }

            return false;
        }
    }

    public static MediaManager getMediaManager() {
        return MEDIA_MANAGER;
    }

    @SuppressWarnings("unchecked")
    public static PointTracker<? extends ImageGray<?>> getTracker(@NotNull MonoPlaneInfinity_to_MonocularPlaneVisualOdometry
            <? extends ImageGray<?>> voEngine) {
        PointTracker<? extends ImageGray<?>> tracker = null;

        Field field;
        try {
            field = voEngine.getClass().getDeclaredField("alg");
            field.setAccessible(true);
            var algorithm = (VisOdomMonoPlaneInfinity<? extends ImageGray<?>>) field.get(voEngine);

            tracker = algorithm.getTracker();
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }

        return tracker;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static boolean processVisualOdometry(@NotNull MonocularPlaneVisualOdometry voEngine, ImageBase<?> leftImage) {
        return voEngine.process(leftImage);
    }

    // Thread utils

    public static void setProcessingStateSafe(@NotNull Settings settings, ProcessingState state) {
        // Ensures that the processing thread is unlocked by releasing any existing lock
        // (possibly: pause state or buffer waiting).
        settings.state().processing().set(state);
        if (settings.state().device() != null) {
            settings.state().device().awakeBufferWaiters();
        }
    }

    public static boolean isResetRequested(@NotNull Settings settings) {
        return settings.state().resetRequest().get();
    }

    public static void setResetRequested(@NotNull Settings settings, boolean resetRequested) {
        settings.state().resetRequest().set(resetRequested);
    }

    public static void setFailedEvent(@NotNull Settings settings, boolean failedEvent) {
        settings.state().failedEvent().set(failedEvent);
    }

    /**
     * Shutdown an executor service and wait for its termination.
     *
     * @param service service to shut down
     * @return {@code true} if terminated correctly, {@code false} if timeout elapsed before termination or interrupt
     * happened
     */
    public static boolean shutdownAndWait(@NotNull ExecutorService service) {
        service.shutdown();
        return awaitTermination(service);
    }

    public static boolean awaitTermination(@NotNull ExecutorService service) {
        try {
            return service.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            return false;
        }
    }
}
