package com.mtm.vogui.core.integration.camera;

import com.mtm.vogui.core.integration.shared.VideoController;
import com.mtm.vogui.core.integration.bridge.V4l4jVideo;
import com.mtm.vogui.models.constants.AppConstants;
import com.mtm.vogui.models.core.concurrency.NamedThreadFactory;
import com.mtm.vogui.models.core.integration.BufferStatus;
import com.mtm.vogui.models.core.exceptions.CameraException;
import com.mtm.vogui.models.core.exceptions.CameraStartException;
import com.mtm.vogui.models.settings.Settings;
import com.mtm.vogui.utilities.ImageUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class V4l4jCamera extends BufferedCamera {

    private final V4l4jVideo device;
    private final VideoController controller;
    private Exception captureException;

    private V4l4jCamera(Settings settings, Consumer<BufferedImage> guiRenderer, Consumer<BufferStatus> bufferRenderer) {
        super(settings, guiRenderer, bufferRenderer);
        // Create new V4L4J device
        this.device = new V4l4jVideo();
        this.controller = VideoController.from(this::fillBufferAndRender);
    }

    @Override
    public BufferedCamera start() throws CameraStartException {
        if (!this.running.get()) {
            this.captureException = null;

            // Clear buffer
            this.clearBuffer();

            // Settings
            var input = this.settings.core().input();
            var image = this.settings.core().image();

            // Activate specific V4L4J parameters (device controls)
            this.device.setControlsActive(
                    input.device().v4l4j().sustainFramerate(),
                    input.device().v4l4j().timeoutImageIO(),
                    input.device().v4l4j().keepFormat()
            );
            // Start device
            if (this.device.start(
                    input.device().path().id().trim(),
                    input.device().targetWidth(),
                    input.device().targetHeight(),
                    ImageUtils.getParametrizedImageType(image.descriptor()),
                    this.controller)
            ) {
                // Wait for running state (this is guaranteed to happen)
                this.controller.waitUntilStarted();
                Executors.newSingleThreadExecutor(NamedThreadFactory.from(AppConstants.V4L4J_CAMERA_THREAD))
                        .submit(this::checkDeviceError);

                // Notify running state
                this.running.set(true);
            } else {
                throw new CameraStartException();
            }
        }

        return this;
    }

    @Override
    public void stop() throws CameraException {
        if (this.running.get()) {
            // Stop device (standard) and wait until stopped (this is guaranteed to happen)
            this.controller.stop();
            this.controller.waitUntilStopped();
        }

        // It is absolutely necessary to always unlock any pending thread before exiting (running and buffer lock)
        this.captureStopped();

        if (this.captureException != null) {
            throw new CameraException();
        }
    }

    @Override
    public double getCurrentFPS() {
        if (this.controller != null) {
            return this.controller.getCurrentFPS();
        }
        return 0;
    }

    @Override
    public double getAverageFPS() {
        if (this.controller != null) {
            return this.controller.getAverageFPS();
        }
        return 0;
    }

    @Override
    public Dimension getFrameSize() {
        if (this.controller != null) {
            return this.controller.frameSize();
        }
        return null;
    }

    private void checkDeviceError() {
        // Wait for asynchronous device stop (due to regular stop or exceptions)
        this.controller.waitUntilStopped();

        if (this.device.hasException()) {
            this.captureException = this.device.getException();
        }

        // It is absolutely necessary to always unlock any pending thread before exiting (running and buffer lock)
        this.captureStopped();
    }

    public static @NotNull V4l4jCamera from(Settings settings, Consumer<BufferedImage> guiRenderer,
                                            Consumer<BufferStatus> bufferRenderer) {
        return new V4l4jCamera(settings, guiRenderer, bufferRenderer);
    }
}
