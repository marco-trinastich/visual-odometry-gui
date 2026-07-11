/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.core.integration.camera;

import com.mtm.vogui.core.integration.bridge.OpenCvVideo;
import com.mtm.vogui.core.integration.discovery.OpenCvDeviceDiscovery;
import com.mtm.vogui.core.integration.shared.VideoController;
import com.mtm.vogui.models.constants.AppConstants;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.core.concurrency.NamedThreadFactory;
import com.mtm.vogui.models.core.integration.BufferStatus;
import com.mtm.vogui.models.core.exceptions.CameraException;
import com.mtm.vogui.models.core.exceptions.CameraStartException;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class OpenCvCamera extends BufferedCamera {

    private final OpenCvVideo device;
    private final VideoController controller;
    private Exception captureException;
    private String devicePath;

    private OpenCvCamera(AppContext context, Consumer<BufferedImage> guiRenderer,
                         Consumer<BufferStatus> bufferRenderer) {
        super(context, guiRenderer, bufferRenderer);
        // Create new OpenCv (JavaCV) device
        this.device = new OpenCvVideo();
        this.controller = VideoController.from(this::fillBufferAndRender);
    }

    @Override
    public OpenCvCamera start() throws CameraStartException {
        if (!this.running.get()) {
            this.captureException = null;

            // Clear buffer
            this.clearBuffer();

            // Settings
            var input = this.context.settings().input();

            // Resolve the requested index against discovery (unknown index -> first device)
            var discovery = OpenCvDeviceDiscovery.instance();
            this.devicePath = discovery.resolveDevice(input.device().path().id().trim());
            // OpenCV advertises no resolution list: the target goes to the driver as-is and
            // the size actually granted is read back from the first frame (safety net)
            Dimension targetSize = discovery.nearestViewSize(this.devicePath,
                    new Dimension(input.device().targetWidth(), input.device().targetHeight()));
            // Start device
            if (this.device.start(
                    Integer.parseInt(this.devicePath),
                    targetSize.width,
                    targetSize.height,
                    this.controller)
            ) {
                // Wait for running state (this is guaranteed to happen)
                this.controller.waitUntilStarted();
                Executors.newSingleThreadExecutor(NamedThreadFactory.from(AppConstants.OPENCV_CAMERA_THREAD))
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
            // Chain the capture-time failure: this rethrow is just the messenger
            throw new CameraException(this.captureException);
        }
    }

    /**
     * Capture index actually opened (discovery may have resolved an unknown
     * requested path to the first available device).
     */
    @Override
    public String getDevicePath() {
        return this.devicePath;
    }

    @Override
    public Dimension getFrameSize() {
        if (this.controller != null) {
            return this.controller.frameSize();
        }
        return null;
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

    private void checkDeviceError() {
        // Wait for asynchronous device stop (due to regular stop or exceptions)
        this.controller.waitUntilStopped();

        if (this.device.hasException()) {
            this.captureException = this.device.getException();
        }

        // It is absolutely necessary to always unlock any pending thread before exiting (running and buffer lock)
        this.captureStopped();
    }

    public static @NotNull OpenCvCamera from(AppContext context, Consumer<BufferedImage> guiRenderer,
                                             Consumer<BufferStatus> bufferRenderer) {
        return new OpenCvCamera(context, guiRenderer, bufferRenderer);
    }
}
